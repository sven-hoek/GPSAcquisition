#ifndef HOST_AXI_ACCESS_H
#define HOST_AXI_ACCESS_H

// Includes for data types used in this header and its source file
#include <stdbool.h>	// bool
#include <inttypes.h>	// uint32_t
#include <stddef.h>		// size_t

typedef struct
{
	// This is a 32 bit view of the parameter data.
	// As parameters will most likely be 64 bits in size, the indices
	// of the array pointed to have to be scaled by the actual size of a parameter!
	// The parameter size is known implicitly and is therefore not part of this struct.
	const uint32_t* parameters;

	// States that a given parameter index is "dirty" and needs to be send to the CGRA.
	// Is ignored (i.e. all data is send) if this is a null ptr!
	const bool* dirty_flags;

	// The amount of actual parameters, representing the size of the dirty_flags array.
	// Does not need to be scaled with the actual parameter size. This is done internally.
	// If you have 8 parameters behind the given pointer (no matter their size) you have to
	// specify 8 as the value of this member!
	size_t parameter_count;
} ParameterArrayRef;

// Data which has to be provided to the CGRA setup functions. 
// For each CGRA instance one of these structs has to be maintained in a consistent manner.
// It is assumed that all values of this struct are the most recent ones when passed to a procedure.
// This is important, as some values may be send to the CGRA without the procedure being transparent 
// about it. 
typedef struct
{
	// Log data base address
	uint32_t log_lower_addr;

	// Log data upper address boundary.
	// (wrap around to lower address if next address increment is greater than this address)
	uint32_t log_upper_addr; 

	// The increment of the log address after every CGRA cycle
	uint32_t log_inc_addr; 

	// OCM data base address
	uint32_t ocm_lower_addr; 

	// See log_upper_addr
	uint32_t ocm_upper_addr;

	// The increment of the ocm address after every CGRA cycle
	uint32_t ocm_inc_addr;

	// The clock cycle count of one complete CGRA cycle (sensor data, execute, Log/OCM send)
	uint32_t cgra_cycle_clock_count;

	// The amount of cycles before 
	// * parameters are written, 
	// * the CGRA port MULTI_RATE_WAS_TRIGGERED_O is asserted (when sensor writes are done)
	//
	// This is the value used to specify how many CGRA execution runs equal one 
	// Host execution run. The value used (by the CGRA) by default is 1.
	// 
	// This value 1 biased (not 0 biased, as its inside the CGRA)
	// Conversion to CGRA internals happens automatically.
	uint32_t multirate_counter_reset_val;

	// Determines how many host results are expected before the CGRA continues with its execution
	uint32_t expected_host_results;

	// Controls the length of AXI bursts issued during init
	unsigned max_axi_transaction_length;

} CgraSetupData;

typedef enum
{
	// The CGRA will do calculations for control cycles on its own
	// -> will write parameters whenever possible and without waiting
	CgraRun, 				

	// The CGRA will run based on the given host results 
	// -> waits on the given amount of host results before starting
	CgraSupport, 			

	// The CGRA will run with a different rate than the host 
	// -> will synchronise host results depending on the set multi rate counter value
	// -> will not wait for any host results (but writes the available ones when possible)
	CgraMultirate, 

} CgraRunType;

// The procedure for setting up the CGRA
// performs actions in this order:
//		-> Send all single transfer configuration values
// 		-> Send all contexts needed for execution and logging
//		-> Send entries of the ID Context (IDC, mapping from AXI address to PE/RF offset)
// 		-> Send static parameters (which will never change during run time)
//
// Every call to the AXI transaction routine (currently named cgra_config_axi_transaction) 
// is guaranteed to be of a length smaller than the AXI burst length.
//
// Does not include a CGRA state change and will not send any data which hast to be 
// provided by the user of this procedure. This includes:
//		* run time parameters (see further notes below...)
// 		* constants for the CGRA internal look up table
// 		* more?
// 
// That said: an initial set of run time parameters is transfered automatically
// when calling thins procedure. The user does not have any influence on these
// values and may override them afterwards by calling cgra_set_runtime_parameters(...).
// The initial values which are send automatically are generated during scheduling
// and should reflect the values of the original dsc file.
void cgra_setup(const CgraSetupData* init);

// Change the CGRA state to be running with the given parameters.
// These parameters will be send to the CGRA before changing the state.
//
// Argument "expected_host_results" should be 0 for run type CgraMultirate
// Argument "multirate_counter_reset_val" should be 0 for run types CgraSupport and CgraRun
// Failing to comply with this will cause an assertion to trigger.
void cgra_change_state_run(const CgraSetupData* data, CgraRunType type);

// Let the CGRA pause its actions.
// The CGRA will not provide any interrupts any more and does not run a schedule.
//
// Will not reset any runtime parameters or Log/OCM addresses.
void cgra_change_state_pause();

// Let the CGRA stop its actions.
// Will reset runtime parameters and Log/OCM addresses.
// The CGRA will not provide any interrupts any more and does not run a schedule.
//
// The runtime_parameters argument should refer to the runtime parameters which
// should be used for resetting the CGRA internal state.
// All addresses are reset based on the given CgraSetupData pointer.
void cgra_change_state_stop(const CgraSetupData* rst_info, ParameterArrayRef runtime_parameters);

// Use this procedure to change the cycle parameters of the CGRA.
// As we don't know the parameter size statically, this procedure takes only a pointer
// to const uint32_t. Please make sure that it points to data which is a multiple 
// of 4 bytes in size.
void cgra_set_cycle_parameters(const CgraSetupData* data, const uint32_t* integration_step_size);

// Set the amount of cycles before 
// * parameters are written, 
// * the CGRA port MULTI_RATE_WAS_TRIGGERED_O is asserted (when sensor writes are done)
//
// This is the procedure used to specify how many CGRA execution runs equal one 
// Host execution run. The value used (by the CGRA) by default is 1.
// 
// Argument "reset_value" is 1 biased (not 0 biased as its inside the CGRA)
// Conversion to CGRA internals happens automatically
void cgra_set_multi_rate_counter_reset_value(const CgraSetupData* data);

// The procedure to disable sending of log data.
// Implementation is not of high priority.
// (Currently not implemented!)
void cgra_disable_log(void);

// Re-enables the logging feature
// (Currently not implemented!)
void cgra_enable_log(void);

// Send the constant look up table data to the CGRA
// (Currently not implemented!)
void cgra_set_const_lut_data(const uint32_t* constants, uint32_t constant_count, unsigned max_tran_len);

// Send run time parameters.
// This procedure will honor the dirty flag array of the ParameterArrayRef
// struct, sending only what is dirty.
void cgra_set_runtime_parameters(ParameterArrayRef parameters, unsigned max_tran_len);

// Send host results (the inputs for the CGRA which where "calculated" by the host)
// This procedure will honor the dirty flag array of the ParameterArrayRef
// struct, sending only what is dirty.
void cgra_set_host_results(ParameterArrayRef parameters, unsigned max_tran_len);

#endif // HOST_AXI_ACCESS_H
