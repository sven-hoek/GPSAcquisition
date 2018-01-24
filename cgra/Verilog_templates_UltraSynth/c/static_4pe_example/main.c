#include <stdio.h>

#include "cgraapi.h"

#define TEST_PARAM_COUNT 8
#define TEST_CONST_LUT_COUNT 16

uint32_t parameters[TEST_PARAM_COUNT] = { 1, 2, 3, 4, 5, 6, 7, 8 };
bool dirty[TEST_PARAM_COUNT] = { true, true, false, true, true, false, true, true };

uint32_t const_lut_data[TEST_CONST_LUT_COUNT] = { 16, 15 };

CgraSetupData setup_data = { 
	.log_lower_addr = 8192,
	.log_upper_addr = 16384,
	.ocm_lower_addr = 32768,
	.ocm_upper_addr = 65536,
	.const_lut_data = const_lut_data,
	.const_lut_data_count = TEST_CONST_LUT_COUNT,
	.max_axi_transaction_length = 256,
};

// An example for using all this in conjuntion with the 
// 4 PE test composition and the schedule in context.h
uint32_t parameter = 2;
bool dirty_flag = true;

int main(void) {
	ParameterArrayRef param_ref = { parameters, dirty, TEST_PARAM_COUNT };
	setup_data.initial_run_time_parameters = param_ref;

	cgra_setup(&setup_data);

	cgra_change_state(&setup_data, CgraCommandRun);
	cgra_change_state(&setup_data, CgraCommandStop);

	getchar();

	return 0;
}