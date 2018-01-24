#ifndef HOST_AXI_ACCESS_H
#define HOST_AXI_ACCESS_H

#include <stdbool.h>
#include <inttypes.h>

// The procedure to for completely setting up the CGRA
// performs actions in this order:
//		-> Send single transfer configuration values
//			-> Cycle clock cycle count
//			-> Hybrid parameter count 
// 			-> OCM addresses (lower bound, upper bound and increment)
// 			-> Log addresses (lower bound, upper bound and increment)
//			-> any cycle counter reference values
// 		-> Send all contexts needed for execution and logging
//		-> Send entries of the ID Context (IDC)
//		-> Send initial set of parameters (not counted parameters)
// 		-> Send constants
//
// Does not include CGRA state change!
void cgra_setup();

typedef enum
{
	CgraStop, 		// The CGRA is not running, counter are reset, implies default parameter resending
	CgraPause, 		// The CGRA is not running, anything else remains untouched
	CgraRun, 		// The CGRA will do calculations for control cycles on its own
	CgraRunHybrid 	// The CGRA will calculate parts of a control cycle
} CgraState;

// Setup the CGRA depending on the provided parameters
void cgra_change_state(CgraState new_state, uint32_t context_start_address);

// Generic single transfer procedure, called from cgra_setup.
//
// Just like any other init procedure, the following procedure will call a cascade of
// procedure below it for all required use cases.
void cgra_init_single_transfers();

void cgra_set_cycle_clock_count(uint32_t clock_count);
void cgra_set_hybrid_parameter_count(uint32_t param_count);

void cgra_set_ocm_low_addr(uint32_t low_addr);
void cgra_set_ocm_high_addr(uint32_t high_addr);
void cgra_set_ocm_addr_increment(uint32_t addr_inc);

void cgra_set_log_low_addr(uint32_t low_addr);
void cgra_set_log_high_addr(uint32_t high_addr);
void cgra_set_log_addr_increment(uint32_t addr_inc);

// TODO: other single value transfers

// Generic context transfer procedure, called from cgra_setup
void cgra_init_contexts();

void cgra_set_pe_context(uint32_t pe_id, const uint32_t* entries, uint32_t entry_count, 
						 uint32_t entry_size_in_bits, uint32_t context_offset);

void cgra_set_pe_log_context(uint32_t pe_id, const uint32_t* entries, uint32_t entry_count, 
						     uint32_t entry_size_in_bits, uint32_t context_offset);

// set by generator
typedef enum
{
	CgraCBoxId,
	CgraCCUId,
	CgraSensorId,
	CgraActorId,
	CgraOcmContextId,
	CgraGlobalLogId
} OtherId;

void cgra_set_other_context(OtherId id, const uint32_t* entries, uint32_t entry_count, 
						 	uint32_t entry_size_in_bits, uint32_t context_offset);

// Generic (initial) parameter transfer procedure, called from cgra_setup
void cgra_init_parameters();

void cgra_set_idc_entries(const uint32_t* entries, uint32_t entry_count, 
						  uint32_t entry_size_in_bits, uint32_t context_offset);
void cgra_set_parameters(const uint32_t* parameters, uint32_t parameter_count,
						 uint32_t first_parameter_id, bool is_hostResult);

// Generic constant transfer procedure, called from cgra_setup
void cgra_init_consts();

void cgra_set_consts(const uint32_t* constants, uint32_t constant_count);

#endif // HOST_AXI_ACCESS_H