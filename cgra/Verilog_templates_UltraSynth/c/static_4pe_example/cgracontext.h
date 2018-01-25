#ifndef CONTEXT_H
#define CONTEXT_H

#include <inttypes.h>	// unit32_t

#define CGRA_CONTEXT_SIZE 256
#define CGRA_GLOG_OUT_CONTEXT_SIZE 256
#define CGRA_OCM_OUT_CONTEXT_SIZE 256
#define CGRA_IDC_SIZE 256

#define CGRA_TRANSFERS_PER_ENTRY_PE0 1
#define CGRA_TRANSFERS_PER_ENTRY_PE1 1
#define CGRA_TRANSFERS_PER_ENTRY_PE2 1
#define CGRA_TRANSFERS_PER_ENTRY_PE3 1

#define CGRA_TRANSFERS_PER_ENTRY_PE0_LOG 1
#define CGRA_TRANSFERS_PER_ENTRY_PE1_LOG 1
#define CGRA_TRANSFERS_PER_ENTRY_PE2_LOG 1
#define CGRA_TRANSFERS_PER_ENTRY_PE3_LOG 1

#define CGRA_TRANSFERS_PER_ENTRY_CCU 1
#define CGRA_TRANSFERS_PER_ENTRY_CBOX 1
#define CGRA_TRANSFERS_PER_ENTRY_IDC 1
#define CGRA_TRANSFERS_PER_ENTRY_OCM_IN 1
#define CGRA_TRANSFERS_PER_ENTRY_OCM_OUT 1
#define CGRA_TRANSFERS_PER_ENTRY_GLOG_OUT 1
#define CGRA_TRANSFERS_PER_ENTRY_ACTOR 1
#define CGRA_TRANSFERS_PER_ENTRY_SENSOR 1

extern const uint32_t c_pe0[CGRA_CONTEXT_SIZE];
extern const uint32_t c_pe1[CGRA_CONTEXT_SIZE];
extern const uint32_t c_pe2[CGRA_CONTEXT_SIZE];
extern const uint32_t c_pe3[CGRA_CONTEXT_SIZE];

extern const uint32_t c_pe0_log[CGRA_CONTEXT_SIZE];
extern const uint32_t c_pe1_log[CGRA_CONTEXT_SIZE];
extern const uint32_t c_pe2_log[CGRA_CONTEXT_SIZE];
extern const uint32_t c_pe3_log[CGRA_CONTEXT_SIZE];

extern const uint32_t c_ccu[CGRA_CONTEXT_SIZE];
extern const uint32_t c_cbox[CGRA_CONTEXT_SIZE];
extern const uint32_t c_cbox_eval0[CGRA_CONTEXT_SIZE]; // not used but present for completenesses sake
extern const uint32_t c_idc[CGRA_CONTEXT_SIZE];
extern const uint32_t c_ocm_in[CGRA_CONTEXT_SIZE];
extern const uint32_t c_ocm_out[CGRA_OCM_OUT_CONTEXT_SIZE];
extern const uint32_t c_glog_out[CGRA_GLOG_OUT_CONTEXT_SIZE];
extern const uint32_t c_actor[CGRA_CONTEXT_SIZE];
extern const uint32_t c_sensor[CGRA_CONTEXT_SIZE];

#endif // CONTEXT
