#include "axi.h"

#include <stdio.h>

void cgra_config_axi_transaction(const uint32_t* data, size_t data_count, uint32_t addr)
{
	printf("AXI transfer targeting addr 0x%x, length %zu\n", addr >> 2, data_count);

	if (data_count >= 5)
		printf("First data items: 0x%x, 0x%x, 0x%x, 0x%x, 0x%x, ...\n", 
			data[0], data[1], data[2], data[3], data[4]);
	else
		printf("First data item: %lu\n", data[0]);
}
