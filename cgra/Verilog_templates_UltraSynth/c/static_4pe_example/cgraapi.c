#include "cgraapi.h"
#include "cgracontext.h"
#include "cgrastaticdata.h"
#include "cgraprivatestaticdata.h"
#include "axi.h"

#include <assert.h>

typedef enum
{
	// Item order matters, do not reorder!
	GeneralTargetPe,
	GeneralTargetParameter,
	GeneralTargetOther,
	GeneralTargetSingleReg,
} CgraGeneralTargetSelection;

typedef enum
{
	// Item order matters, do not reorder!
	StatePause,
	StateRun,
	StateStop,
	StateRunHybrid,
} CgraState;

typedef enum
{
	// Item order matters, do not reorder!
	IdCCUContext,
	IdCBoxContext,
	IdCBoxEvalContext0,
	IdIDC,
	IdSensorContext,
	IdActorContext,
	IdGlblLogContext,
	IdOCMContext,
	IdOCMOutputContext,
	IdConstBuffer,
} OtherId;

typedef enum
{
	// Item order matters, do not reorder!
	IdCgraStateChange,
	IdLogDest,
	IdLogDestBound,
	IdLogDestInc,
	IdOCMDest,
	IdOCMDestBound,
	IdOCMDestInc,
	IdIntervalLength,
	IdExpectedParameterCount,
} SingleRegId;

static inline uint32_t create_addr(CgraGeneralTargetSelection target, unsigned target_id, uint32_t offset)
{
	uint32_t addr = 0;
	addr |= target << (CGRA_AXI_OFFSET_WIDTH + CGRA_AXI_TARGET_ID_WIDTH);
	addr |= target_id << CGRA_AXI_OFFSET_WIDTH;
	addr |= offset;
	addr <<= CGRA_AXI_SYSTEM_ADDR_OFFSET;

	return addr;
}

static inline uint32_t create_state_data(CgraState state, uint32_t start_addr)
{
	uint32_t state_data = 0;
	state_data |= state << CGRA_STATE_DATA_OFFSET;
	state_data |= start_addr;
	return state_data;
}

// Returns a new address calculated from an old one.
// Changes the offset bits of an AXI address. These are the bits carying the CGRA internal offset
// of an AXI write. Due to the fact that they are located at the lower end of the address, 
// simply adding the actual change is perfectly fine.
static inline uint32_t increment_addr_offset(uint32_t old_addr, size_t transfers_per_entry, size_t transfers_done)
{
	return old_addr + (transfers_done / transfers_per_entry) * AXI_TRANSFER_SIZE;
}

// Send all data given by the data ptr and its associated size.
// This procedure guarantees that no 4k address boundaries will be cossed and
// that calls to the AXI transaction procedure are done with lengths smaller 
// the max AXI transaction length.
static void send_data_stream(const uint32_t* data, size_t data_count, size_t transfers_per_entry, 
	uint32_t start_addr, unsigned max_tran_len)
{
	assert(data_count % transfers_per_entry == 0);

	while (data_count > 0)
	{
		// The length of the next transaction.
		// It is required that the length is a multiple of transfers_per_entry!
		size_t transaction_length = data_count > max_tran_len ?
			max_tran_len - (max_tran_len % transfers_per_entry) :
			data_count - (data_count % transfers_per_entry);

		// check if we will be crossing a 4k boundary, if so: split the transaction
		const uint32_t start_mod = start_addr % AXI_TRANSACTION_BOUNDARY;

		const uint32_t next_start = increment_addr_offset(start_addr, transfers_per_entry, transaction_length);
		const uint32_t end_mod = next_start % AXI_TRANSACTION_BOUNDARY;

		if (start_mod > end_mod && end_mod != 0)
			transaction_length -= end_mod / AXI_TRANSFER_SIZE;

		// do the actual transaction
		cgra_config_axi_transaction(data, transaction_length, start_addr);

		// loop variant and next iteration preparation
		data += transaction_length;
		data_count -= transaction_length;
		start_addr = increment_addr_offset(start_addr, transfers_per_entry, transaction_length);
	}
}

// Send the given continuous range of parameter IDs.
// Yes, it is absolutly required that parameter id_range points to a continuous range!
//
// Returns the amount of transfers done to send the given range
static size_t send_parameter_range(const uint32_t* parameters, const CgraIdRange* id_range, unsigned max_tran_len)
{
	const size_t parameter_count = id_range->upper_id - id_range->lower_id;
	const size_t transfer_count = parameter_count * CGRA_PARAMETER_SIZE;
	const uint32_t addr = create_addr(GeneralTargetParameter, id_range->lower_id, CGRA_IS_HYBRID);

	send_data_stream(parameters, transfer_count, CGRA_PARAMETER_SIZE, addr, max_tran_len);

	return transfer_count;
}

// Sends all run time parameters which are dirty.
static void send_run_time_parameter_stream(ParameterArrayRef parameters_array_ref, unsigned max_tran_len)
{
	const uint32_t* parameters = parameters_array_ref.parameters;
	const bool* dirty_flags = parameters_array_ref.dirty_flags;
	const size_t parameter_count = parameters_array_ref.parameter_count;

	assert(dirty_flags && "Sending run time parameters requires a dirty flag array pointer!");

	// These arrays will be filled synchronous:
	// An ID located at index idx in the ids_to_send array maps to 
	// the index (idx * CGRA_PARAMETER_SIZE) in the data array.
	uint32_t data_to_send[CGRA_RUN_TIME_PARAMETER_COUNT * CGRA_PARAMETER_SIZE];
	size_t ids_to_send[CGRA_RUN_TIME_PARAMETER_COUNT];

	size_t data_write_idx = 0;
	size_t id_write_idx = 0;

	for (size_t parameter_idx = 0; parameter_idx < parameter_count; ++parameter_idx)
	{
		const bool dirty = dirty_flags[parameter_idx];
		if (dirty)
		{
			CgraIdRange id_range = cgra_run_time_parameter_map[parameter_idx];

			// Iterate all IDs in the given range
			for (size_t id = id_range.lower_id; id < id_range.upper_id; ++id)
			{
				ids_to_send[id_write_idx++] = id;

				// Iterate the parameter data to this ID in 32 bit steps
				for (size_t j = 0; j < CGRA_PARAMETER_SIZE; ++j)
					data_to_send[data_write_idx++] = parameters[CGRA_PARAMETER_SIZE * parameter_idx + j];
			}
		}
	}

	if (0 == id_write_idx)
		return; // nothing to do

	// Range of paramter IDs which may be send in a AXI burst.
	CgraIdRange id_range = { ids_to_send[0], ids_to_send[0] + 1 };
	size_t next_data_idx_to_send = 0;

	for (size_t i = 1; i < id_write_idx; ++i)
	{
		const size_t current_id = ids_to_send[i];
		if (current_id != id_range.upper_id)
		{
			next_data_idx_to_send += send_parameter_range(&data_to_send[next_data_idx_to_send], &id_range, max_tran_len);
			id_range.lower_id = current_id;
			id_range.upper_id = current_id + 1;
		}
		else
			++id_range.upper_id;
	}

	// check for unsend parameters
	if (next_data_idx_to_send < data_write_idx)
		send_parameter_range(&data_to_send[next_data_idx_to_send], &id_range, max_tran_len);
}

static void cgra_set_cycle_clock_count(uint32_t clock_count)
{
	const uint32_t addr = create_addr(GeneralTargetSingleReg, IdIntervalLength, 0);
	cgra_config_axi_transaction(&clock_count, 1, addr);
}

static void cgra_set_hybrid_parameter_count(uint32_t param_count)
{
	const uint32_t addr = create_addr(GeneralTargetSingleReg, IdExpectedParameterCount, 0);
	cgra_config_axi_transaction(&param_count, 1, addr);
}

static void cgra_set_ocm_low_addr(uint32_t low_addr)
{
	const uint32_t addr = create_addr(GeneralTargetSingleReg, IdOCMDest, 0);
	cgra_config_axi_transaction(&low_addr, 1, addr);
}

static void cgra_set_ocm_high_addr(uint32_t high_addr)
{
	const uint32_t addr = create_addr(GeneralTargetSingleReg, IdOCMDestBound, 0);
	cgra_config_axi_transaction(&high_addr, 1, addr);
}

static void cgra_set_ocm_addr_increment(uint32_t addr_inc)
{
	const uint32_t addr = create_addr(GeneralTargetSingleReg, IdOCMDestInc, 0);
	cgra_config_axi_transaction(&addr_inc, 1, addr);
}

static void cgra_set_log_low_addr(uint32_t low_addr)
{
	const uint32_t addr = create_addr(GeneralTargetSingleReg, IdLogDest, 0);
	cgra_config_axi_transaction(&low_addr, 1, addr);
}

static void cgra_set_log_high_addr(uint32_t high_addr)
{
	const uint32_t addr = create_addr(GeneralTargetSingleReg, IdLogDestBound, 0);
	cgra_config_axi_transaction(&high_addr, 1, addr);
}

static void cgra_set_log_addr_increment(uint32_t addr_inc)
{
	const uint32_t addr = create_addr(GeneralTargetSingleReg, IdLogDestInc, 0);
	cgra_config_axi_transaction(&addr_inc, 1, addr);
}

// Generic single transfer procedure, called from cgra_setup.
//
// Just like any other init procedure, the following procedure will call a cascade of
// procedures for all required use cases.
//
// TODO: other single value transfers
static void cgra_init_single_transfers(const CgraSetupData* setup_data)
{
	cgra_set_cycle_clock_count(CGRA_CLOCKS_PER_CYCLE);
	cgra_set_hybrid_parameter_count(CGRA_EXPECTED_HYBRID_PARAMETER_COUNT);

	cgra_set_ocm_low_addr(setup_data->ocm_lower_addr);
	cgra_set_ocm_high_addr(setup_data->ocm_upper_addr);
	cgra_set_ocm_addr_increment(cgra_scheduling_data.ocm_addr_inc);

	cgra_set_log_low_addr(setup_data->log_lower_addr);
	cgra_set_log_high_addr(setup_data->log_upper_addr);
	cgra_set_log_addr_increment(cgra_scheduling_data.log_addr_inc);
}

static void cgra_set_pe_context(uint32_t pe_id, const uint32_t* entries, size_t entry_count, 
	size_t transfers_per_entry, uint32_t context_offset, unsigned max_transaction_length)
{
	const uint32_t addr = create_addr(GeneralTargetPe, pe_id, context_offset);
	send_data_stream(entries, entry_count, transfers_per_entry, addr, max_transaction_length);
}

static void cgra_set_pe_log_context(uint32_t pe_id, const uint32_t* entries, size_t entry_count, 
	size_t transfers_per_entry, uint32_t context_offset, unsigned max_transaction_length)
{
	const uint32_t addr = create_addr(GeneralTargetPe, pe_id + CGRA_PE_LOG_ID_OFFSET, context_offset);
	send_data_stream(entries, entry_count, transfers_per_entry, addr, max_transaction_length);
}

static void cgra_set_other_context(OtherId id, const uint32_t* entries, size_t entry_count, 
	size_t transfers_per_entry, uint32_t context_offset, unsigned max_transaction_length)
{
	const uint32_t addr = create_addr(GeneralTargetOther, id, context_offset);
	send_data_stream(entries, entry_count, transfers_per_entry, addr, max_transaction_length);
}

// Generic context transfer procedure, called from cgra_setup.
// Calls all other context transfer procedures for all CGRA entities.
static void cgra_init_contexts(const CgraSetupData* setup_data)
{
	const unsigned max_tans_length = setup_data->max_axi_transaction_length;

	cgra_set_pe_context(0, c_pe0, CGRA_CONTEXT_SIZE, CGRA_TRANSFERS_PER_ENTRY_PE0, 
		0, max_tans_length);
	cgra_set_pe_context(1, c_pe1, CGRA_CONTEXT_SIZE, CGRA_TRANSFERS_PER_ENTRY_PE1, 
		0, max_tans_length);
	cgra_set_pe_context(2, c_pe2, CGRA_CONTEXT_SIZE, CGRA_TRANSFERS_PER_ENTRY_PE2, 
		0, max_tans_length);
	cgra_set_pe_context(3, c_pe3, CGRA_CONTEXT_SIZE, CGRA_TRANSFERS_PER_ENTRY_PE3, 
		0, max_tans_length);	

	cgra_set_pe_log_context(0, c_pe0_log, CGRA_CONTEXT_SIZE, CGRA_TRANSFERS_PER_ENTRY_PE0_LOG, 
		0, max_tans_length);
	cgra_set_pe_log_context(1, c_pe1_log, CGRA_CONTEXT_SIZE, CGRA_TRANSFERS_PER_ENTRY_PE1_LOG, 
		0, max_tans_length);
	cgra_set_pe_log_context(2, c_pe2_log, CGRA_CONTEXT_SIZE, CGRA_TRANSFERS_PER_ENTRY_PE2_LOG, 
		0, max_tans_length);
	cgra_set_pe_log_context(3, c_pe3_log, CGRA_CONTEXT_SIZE, CGRA_TRANSFERS_PER_ENTRY_PE3_LOG, 
		0, max_tans_length);

	cgra_set_other_context(IdCCUContext, c_ccu, CGRA_CONTEXT_SIZE, 
		CGRA_TRANSFERS_PER_ENTRY_CCU, 0, max_tans_length);
	cgra_set_other_context(IdCBoxContext, c_cbox, CGRA_CONTEXT_SIZE, 
		CGRA_TRANSFERS_PER_ENTRY_CBOX, 0, max_tans_length); // Eval block contexts are missing but not required in this version
	cgra_set_other_context(IdOCMContext, c_ocm_in, CGRA_CONTEXT_SIZE, 
		CGRA_TRANSFERS_PER_ENTRY_OCM_IN, 0, max_tans_length);
	cgra_set_other_context(IdOCMOutputContext, c_ocm_out, CGRA_OCM_OUT_CONTEXT_SIZE,
		CGRA_TRANSFERS_PER_ENTRY_OCM_OUT, 0, max_tans_length);
	cgra_set_other_context(IdGlblLogContext, c_glog_out, CGRA_GLOG_OUT_CONTEXT_SIZE, 
		CGRA_TRANSFERS_PER_ENTRY_GLOG_OUT, 0, max_tans_length);
	cgra_set_other_context(IdActorContext, c_actor, CGRA_CONTEXT_SIZE, 
		CGRA_TRANSFERS_PER_ENTRY_ACTOR, 0, max_tans_length);
	cgra_set_other_context(IdSensorContext, c_sensor, CGRA_CONTEXT_SIZE, 
		CGRA_TRANSFERS_PER_ENTRY_SENSOR, 0, max_tans_length);
}

static void cgra_set_idc_entries(const uint32_t* entries, size_t entry_count, 
	size_t transfers_per_entry, uint32_t context_offset, unsigned max_tran_len)
{
	cgra_set_other_context(IdIDC, entries, entry_count, transfers_per_entry, context_offset, max_tran_len);
}

static void cgra_set_static_parameters(unsigned max_tran_len)
{
	send_parameter_range(cgra_static_parameters, &cgra_static_parameter_id_range, max_tran_len);
}

static void cgra_reset_parameters(const CgraSetupData* setup_data)
{
	cgra_set_run_time_parameters(setup_data->initial_run_time_parameters,
		setup_data->max_axi_transaction_length);
	cgra_set_static_parameters(setup_data->max_axi_transaction_length);
}

// Generic (initial) parameter transfer procedure, called from cgra_setup.
// These are constant parameters, NOT results from any host calculations
// during hybrid control.
static void cgra_init_parameters(const CgraSetupData* setup_data)
{
	cgra_set_idc_entries(c_idc, CGRA_IDC_SIZE, CGRA_TRANSFERS_PER_ENTRY_IDC, 
		0, setup_data->max_axi_transaction_length);	
	cgra_reset_parameters(setup_data);
}

static void cgra_set_consts(const uint32_t* constants, uint32_t constant_count, unsigned max_tran_len)
{
	uint32_t addr = create_addr(GeneralTargetOther, IdConstBuffer, 0);
	send_data_stream(constants, constant_count, CGRA_CONST_LUT_DATA_SIZE, addr, max_tran_len);
}

// Generic constant transfer procedure, called from cgra_setup
// (const look up table)
static void cgra_init_consts(const CgraSetupData* setup_data)
{
	cgra_set_consts(setup_data->const_lut_data, setup_data->const_lut_data_count, 
		setup_data->max_axi_transaction_length);
}

// Reset the CGRA
// This will not change any contexts but will make sure that the next
// state change (to running state) will behave as if we just setup the 
// CGRA with cgra_setup(...)
// This procedure is called automatically if cgra_change_state(...) is called
// with arg CgraCommandStop.
static void cgra_reset(const CgraSetupData* rst_info)
{
	cgra_init_single_transfers(rst_info); // Reset Log and OCM addresses
	cgra_reset_parameters(rst_info); // Reset all parameters (static and run time)
}

//
// =====--- Public Interface ---=====
//

void cgra_setup(const CgraSetupData* init_data)
{
	cgra_init_single_transfers(init_data);
	cgra_init_contexts(init_data);
	cgra_init_consts(init_data);
	cgra_init_parameters(init_data);
}

void cgra_change_state(const CgraSetupData* rst_info, CgraStateChangeCommand new_state)
{
	const uint32_t addr = create_addr(GeneralTargetSingleReg, IdCgraStateChange, 0);
	const size_t transaction_size = 1;
	uint32_t state_data;

	switch (new_state) {
	case CgraCommandRun:
		// we want to start -> check if hybrid or not
		state_data = CGRA_IS_HYBRID ?
			create_state_data(StateRunHybrid, 0) : create_state_data(StateRun, 0);

		cgra_config_axi_transaction(&state_data, transaction_size, addr);
		break;
	case CgraCommandPause:
		// we want to prevent further CGRA execution without resetting
		state_data = create_state_data(StatePause, 0);
		cgra_config_axi_transaction(&state_data, transaction_size, addr);
		break;
	case CgraCommandStop:
		// we want to stop the CGRA and reset it
		state_data = create_state_data(StateStop, 0);
		cgra_config_axi_transaction(&state_data, transaction_size, addr);
		cgra_reset(rst_info); // reset
		break;
	default:
		break;
	}
}

void cgra_change_cycle_parameters(uint32_t clock_count, const uint32_t* integration_step_size, unsigned max_tran_len)
{
	cgra_set_cycle_clock_count(clock_count);
	send_parameter_range(integration_step_size, &cgra_integration_step_size_id_range, max_tran_len);
}

// implement this by using a special log context, consisting of one entry
// signaling done and the rest listen to ccnt max value!
void cgra_disable_log(void)
{
	assert(false && "Not Implemented!");
}

// write back the correct context
void cgra_enable_log(void)
{
	assert(false && "Not Implemented!");
}

void cgra_set_run_time_parameters(ParameterArrayRef parameters, unsigned max_tran_len)
{
	send_run_time_parameter_stream(parameters, max_tran_len);
}

void cgra_set_host_results(ParameterArrayRef parameters, unsigned max_tran_len)
{
	send_parameter_range(parameters.parameters, &cgra_host_result_id_range, max_tran_len);
}
