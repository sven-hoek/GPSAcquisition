#include "cgraprivatestaticdata.h"

// Offsets into the ID context of the CGRA which performs the mapping
// of incoming addresses to the respective register file and PE.
// These are offsets to the CGRA internal IDs.
#define CGRA_INTERNAL_ID_OFFSET_STATIC_PARAMETERS 0
#define CGRA_INTERNAL_ID_OFFSET_RUN_TIME_PARAMETERS 16
#define CGRA_INTERNAL_ID_OFFSET_HOST_RESULTS 32

// Defines for individual run time parameters
#define CGRA_INTERNAL_ID_OFFSET_INT_STEP_SIZE 16 
#define CGRA_INTERNAL_ID_LENGTH_INT_STEP_SIZE 1

const uint32_t cgra_static_parameters[CGRA_STATIC_PARAMETER_COUNT * CGRA_PARAMETER_SIZE] =
{
	0,
};

const CgraIdRange cgra_run_time_parameter_map[CGRA_RUN_TIME_PARAMETER_COUNT] =
{
	{ .lower_id = 1 - 1, .upper_id = 1 },
	{ .lower_id = 2 - 1, .upper_id = 2 },
	{ .lower_id = 3 - 1, .upper_id = 3 },
	{ .lower_id = 4 - 1, .upper_id = 4 },
	{ .lower_id = 5 - 1, .upper_id = 5 },
	{ .lower_id = 6 - 1, .upper_id = 6 },
	{ .lower_id = 7 - 1, .upper_id = 7 },
	{ .lower_id = 8 - 1, .upper_id = 8 },
	{ .lower_id = 9 - 1, .upper_id = 9 },
	{ .lower_id = 10 - 1, .upper_id = 10 },
	{ .lower_id = 11 - 1, .upper_id = 11 },
	{ .lower_id = 12 - 1, .upper_id = 12 },
	{ .lower_id = 13 - 1, .upper_id = 13 },
	{ .lower_id = 14 - 1, .upper_id = 14 },
	{ .lower_id = 15 - 1, .upper_id = 15 },
	{ .lower_id = 16 - 1, .upper_id = 16 },
};

const CgraIdRange cgra_integration_step_size_id_range = 
{
	.lower_id = CGRA_INTERNAL_ID_OFFSET_INT_STEP_SIZE,
	.upper_id = CGRA_INTERNAL_ID_OFFSET_INT_STEP_SIZE + CGRA_INTERNAL_ID_LENGTH_INT_STEP_SIZE,
};

const CgraIdRange cgra_static_parameter_id_range = 
{
	.lower_id = CGRA_INTERNAL_ID_OFFSET_STATIC_PARAMETERS,
	.upper_id = CGRA_INTERNAL_ID_OFFSET_STATIC_PARAMETERS + CGRA_STATIC_PARAMETER_COUNT,
};

const CgraIdRange cgra_host_result_id_range = 
{
	.lower_id = CGRA_INTERNAL_ID_OFFSET_HOST_RESULTS,
	.upper_id = CGRA_INTERNAL_ID_OFFSET_HOST_RESULTS + CGRA_HOST_RESULT_COUNT,
};
