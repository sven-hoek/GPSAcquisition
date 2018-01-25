#ifndef AXI_H
#define AXI_H

#include <inttypes.h>	// unit32_t
#include <stddef.h>		// size_t

void cgra_config_axi_transaction(const uint32_t* data, size_t data_count, uint32_t addr);

#endif // AXI_H