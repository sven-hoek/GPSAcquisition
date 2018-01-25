#ifndef CGRA_PRIVATE_STATIC_DATA_H
#define CGRA_PRIVATE_STATIC_DATA_H

#include <inttypes.h>	// uint32_t

//
// ===---- AXI constant definitions ----===
//
#define AXI_MAX_TRANSACTION_LENGTH 256 // the max length of a transaction
#define AXI_TRANSACTION_BOUNDARY 4096 // crossing this address boundary is prohibited
#define AXI_TRANSFER_SIZE 4 // size of a transfer in bytes

//
// ===---- General constant definitions ----===
//
#define CGRA_STATE_DATA_OFFSET 8 // basically the ccnt width 
#define CGRA_CONST_LUT_DATA_SIZE 1 // size (in 32 bit steps) of any parameter send to the CGRA 
#define CGRA_PE_LOG_ID_OFFSET 4 // the ID offset to the first PE Log ID

#define CGRA_AXI_OFFSET_WIDTH 8 // AXI address modification bits (e.g. Context offset)
#define CGRA_AXI_TARGET_ID_WIDTH 4 // The ID width needed to represent IDC address width, PE and other IDs
#define CGRA_AXI_TARGET_WIDTH 2 // Width of the write operation send to the CGRA

// As we need 4 bytes alligned addresses, we will have to shift the actual CGRA
// address (basically the internal usage) two bits to the left.
#define CGRA_AXI_SYSTEM_ADDR_OFFSET 2 

//
// ===---- Scheduling constant definitions ----===
//
#define CGRA_IS_HYBRID 0 // Determines if CGRA and host are doing calculations in parallel (0 for not hybrid, 1 for hybrid)
#define CGRA_CLOCKS_PER_CYCLE 50000000 // CGRA clock cycles for running the schedule one time
#define CGRA_MAX_PARAMETER_ID_COUNT 256 // maximum amount of unique IDs used while running this schedule

// Determines the amount of host results/inputs required to reach PE RegFiles 
// during hybrid control before the CGRA will start executing.
#define CGRA_EXPECTED_HYBRID_PARAMETER_COUNT 0 

//
// ===---- Parameter related declarations and definitions ----===
//

// The size of a parameter in 32 bit increments.
// If this is 2, we have a parameter size of 64 bits.
#define CGRA_PARAMETER_SIZE 1

// Defines the amount of parameters in the respective category
#define CGRA_STATIC_PARAMETER_COUNT 16
#define CGRA_RUN_TIME_PARAMETER_COUNT 16
#define CGRA_HOST_RESULT_COUNT 16

// The actuall values of the static parameters
extern const uint32_t cgra_static_parameters[CGRA_STATIC_PARAMETER_COUNT * CGRA_PARAMETER_SIZE];

// Represents a CGRA internal ID range (for writing any parameter)
// Range is including lower but excluding upper: [lower_id, upper_id)
typedef struct
{
	size_t lower_id;
	size_t upper_id;
} CgraIdRange;

// Use a parameter ID (used during scheduling) to index this array
// and get a range of CGRA internal IDs out of it.
extern const CgraIdRange cgra_run_time_parameter_map[CGRA_RUN_TIME_PARAMETER_COUNT];

// Range of CGRA internal IDs which represent the integration step size.
extern const CgraIdRange cgra_integration_step_size_id_range;

// Range of CGRA internal IDs which represent the static parameters.
extern const CgraIdRange cgra_static_parameter_id_range;

// Range of CGRA internal IDs which represent the host results.
extern const CgraIdRange cgra_host_result_id_range;

#endif // CGRA_PRIVATE_STATIC_DATA_H