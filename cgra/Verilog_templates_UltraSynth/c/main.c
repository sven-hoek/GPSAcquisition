#include <stdio.h>

#include "cgraapi.h"

#define TEST_PARAM_COUNT 8

uint32_t parameters[TEST_PARAM_COUNT] = { 1, 2, 3, 4, 5, 6, 7, 8 };
bool dirty[TEST_PARAM_COUNT] = { true, true, false, true, true, false, true, true };

CgraSetupData setup_data = { 
	.log_lower_addr = 8192,
	.log_upper_addr = 16384,
	.log_inc_addr = 256,
	.ocm_lower_addr = 32768,
	.ocm_upper_addr = 65536,
	.ocm_inc_addr = 256,
	.cgra_cycle_clock_count = 50000,
	.multirate_counter_reset_val = 0,
	.expected_host_results = 0,
	.max_axi_transaction_length = 256,
};

ParameterArrayRef param_ref = { parameters, dirty, TEST_PARAM_COUNT };

int main(void) {
	cgra_setup(&setup_data);
	cgra_set_run_time_parameters(param_ref, setup_data.max_axi_transaction_length);

	cgra_change_state_run(&setup_data, CgraRun);
	cgra_change_state_stop(&setup_data, param_ref);

	getchar();

	return 0;
}