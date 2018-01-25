//`include "constbuf.v" // yes, we need access to the definition of the Memory module

`include "axiinterface.vh"
`include "ultrasynth.vh"
`include "cgra.vh"
`include "parameterbuffer.vh"

/* === ComUnit Assertions START === */
module tb_SVA_ComUnit #
(
parameter integer IDC_ADDR_WIDTH = -1,
parameter integer IDC_WIDTH = -1
)
(
input wire CGRA_CLK_I,
input wire RST_N_I,
input wire EN_I,
input wire [8-1:0] transfer_cntr,
input wire [8-1:0] burst_len,
input wire [8-1:0] incoming_len,
input wire [2-1:0] burst_type,
input wire [2-1:0] incoming_type,
input wire [`ADDR_CONTROL_WIDTH-1:0] burst_control,
input wire [`ADDR_CONTROL_WIDTH-1:0] incoming_control,
input wire [`ADDR_OFFSET_WIDTH-1:0] incoming_offset,
input wire burst_underway,
input wire hold_transactions,
input wire allowed_to_proceed,
input wire enable_write_ctrl,
input wire PARAMETER_BUFFER_FULL_I,
input wire PARAMETER_CLEANUP_I,
input wire S_AXI_WREADY,
input wire S_AXI_WVALID,
input wire S_AXI_AWVALID,
input wire S_AXI_AWREADY,
input wire m_axi_awready,
input wire m_axi_wready
);

/* --- Incoming Transactions, start of the transaction handling --- */
property p_incomingAddr;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		S_AXI_AWVALID && S_AXI_AWREADY && EN_I |=> burst_underway;
endproperty

a_incomingAddr: assert property (p_incomingAddr)
else $error("\"burst_underway\" should be HIGH but is LOW");

property p_wreadyCheck;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		burst_underway && allowed_to_proceed && EN_I |=> S_AXI_WREADY;
endproperty

a_wreadyCheck: assert property (p_wreadyCheck)
else $error("expected the assertion of \"S_AXI_WREADY\"");

property p_controlWriteEnableCheck;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		S_AXI_WVALID && S_AXI_WREADY && EN_I |-> enable_write_ctrl;
endproperty

a_controlWriteEnableCheck: assert property (p_controlWriteEnableCheck)
else $error("expected the assertion of \"enable_write_ctrl\"");

/* --- AXI hold conditions --- */
property p_holdOnFullParameterBuffer;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		!(incoming_control[`GENERAL_TARGET_SELECTION_HIGH:`GENERAL_TARGET_SELECTION_LOW] == `GENERAL_TARGET_PARAMETER && 
			PARAMETER_BUFFER_FULL_I && allowed_to_proceed);
endproperty

a_holdOnFullParameterBuffer: assert property (p_holdOnFullParameterBuffer)
else $error("\"allowed_to_proceed\" should be LOW when trying to write to a full parameter buffer");

property p_holdOnParameterWriteAndIncomingContext;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		!(incoming_control[`GENERAL_TARGET_SELECTION_HIGH:`GENERAL_TARGET_SELECTION_LOW] == `GENERAL_TARGET_PE && 
			PARAMETER_CLEANUP_I && allowed_to_proceed);
endproperty

a_holdOnParameterWriteAndIncomingContext: assert property (p_holdOnParameterWriteAndIncomingContext)
else $error("\"allowed_to_proceed\" should be LOW when trying to write a PE context during parameter writes");

/* --- Conditions which have to hold during a transaction --- */
property p_incomingIsBurst;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		(	incoming_control == burst_control &&
			incoming_len == burst_len &&
			incoming_type == burst_type )
		|| transfer_cntr == 0;
endproperty

a_incomingIsBurst: assert property (p_incomingIsBurst)
else $error("\"incoming_*\" and \"burst_*\" should be equal during a burst");

property p_awreadLow;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		(burst_underway || (S_AXI_AWREADY && S_AXI_AWVALID)) && EN_I
		|=> ~S_AXI_AWREADY;
endproperty

a_awreadLow: assert property (p_awreadLow)
else $error("\"S_AXI_AWREADY\" should be LOW if it was asserted together with awvalid or if a burst is running");

property p_wreadLow;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		~allowed_to_proceed && EN_I
		|=> ~S_AXI_WREADY;
endproperty

a_wreadLow: assert property (p_wreadLow)
else $error("\"S_AXI_WREADY\" should be LOW if the previous cycle did not drive a HIGH \"allowed_to_proceed\"");

property p_noWreadyWithoutEN;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I)
		~EN_I
		|-> ~m_axi_wready;
endproperty

a_noWreadyWithoutEN: assert property (p_noWreadyWithoutEN)
else $error("\"m_axi_wready\" should be LOW if \"EN_I\" is deasserted");

property p_noAwreadyWithoutEN;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I)
		~EN_I
		|-> ~m_axi_awready;
endproperty

a_noAwreadyWithoutEN: assert property (p_noAwreadyWithoutEN)
else $error("\"m_axi_awready\" should be LOW if \"EN_I\" is deasserted");

endmodule
/* === ComUnit Assertions END === */

/* === CGRA Assertions START === */
module tb_SVA_CGRA #
(
parameter integer CONTEXT_ADDR_WIDTH = -1,
parameter integer CONTEXT_SIZE = -1
)
(
input wire CGRA_CLK_I,
input wire RST_I,
input wire EN_I,
input wire start_exec,
input wire hybrid,
input wire ccu_load_en_sync,
input wire [CONTEXT_ADDR_WIDTH-1:0] start_addr,
input wire [CONTEXT_ADDR_WIDTH-1:0] w_ccnt,
input wire [`STATE_WIDTH-1:0] state,
input wire parameter_buffer_empty,
input wire parameter_buffer_full,
input wire parameter_update_allowed_sync,
input wire parameter_context_preped,
input wire enable_write_ctrl,
input wire valid_context,
input wire is_peLog_context,
input wire context_wr_en,
input wire ccu_context_wr_en,
input wire [`CBOX_EVAL_BLOCK_COUNT:0] cbox_context_wr_en,
input wire idc_wr_en,
input wire parameter_buffer_wr_en,
input wire syncUnit_state_change,
input wire glog_context_wr_en,
input wire sensor_context_wr_en,
input wire actor_context_wr_en
);

wire RST_N_I;
assign RST_N_I = ~RST_I;

/* --- Start Execution --- */
sequence start_sequence;
	start_exec && !(~parameter_buffer_empty && hybrid) && ~ccu_load_en_sync && EN_I;
endsequence

property p_setAddr;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		start_sequence |=> (start_addr == w_ccnt);
endproperty

a_setAddr: assert property (p_setAddr)
else $error("\"w_ccnt\" not set to the right address");

property p_startStateChange;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		start_sequence |=> (state == `START);
endproperty

a_startStateChange: assert property (p_startStateChange)
else $error("\"start_exec\" was HIGH but \"state\" did not change to START");

/* --- State when ccnt == max(ccnt) --- */
property p_counterMaxState;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		w_ccnt == CONTEXT_SIZE-1 && ~start_exec && EN_I |=> (state == `IDLE || state == `UPDATE_PARAMETER);
endproperty

a_counterMaxState: assert property (p_counterMaxState)
else $error("\"w_ccnt\" was MAX but \"state\" is not IDLE or UPDATE_PARAMETER");

/* --- Parameter writing --- */
property p_parameterWriteTime;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		(state == `IDLE || state == `UPDATE_PARAMETER) && ~parameter_buffer_empty && !(start_exec && !(~parameter_buffer_empty && hybrid)) && EN_I
		|=> parameter_update_allowed_sync;
endproperty

a_parameterWriteTime: assert property (p_parameterWriteTime)
else $error("conditions for writing a parameter were met but \"parameter_update_allowed_sync\" is LOW");

property p_parameterAllowedPreped;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		parameter_update_allowed_sync && EN_I |=> parameter_context_preped;
endproperty

a_parameterAllowedPreped: assert property (p_parameterAllowedPreped)
else $error("\"parameter_update_allowed_sync\" was HIGH but \"parameter_context_preped\" is LOW");

property p_parameterNotAllowed;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		(parameter_buffer_empty || state == `EXECUTE || state == `START) && EN_I
		|=> ~parameter_update_allowed_sync || (parameter_update_allowed_sync && state == `IDLE);
endproperty

a_parameterNotAllowed: assert property (p_parameterNotAllowed)
else $error("\"parameter_update_allowed_sync\" was HIGH but should be LOW at this point");

property p_parameterBufferFull;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		!(parameter_buffer_wr_en && parameter_buffer_full);
endproperty

a_parameterBufferFull: assert property (p_parameterBufferFull)
else $error("\"parameter_buffer_wr_en\" and \"parameter_buffer_full\" should not be asserted together");

property p_parameterDelayRunIfHybridState;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		hybrid && ~parameter_buffer_empty && state != `EXECUTE && EN_I
		|=> state == `IDLE || state == `UPDATE_PARAMETER;
endproperty

a_parameterDelayRunIfHybridState: assert property (p_parameterDelayRunIfHybridState)
else $error("Control is hybrid and parameter buffer not empty, expected states are IDLE or UPDATE_PARAMETER.");

property p_parameterDelayRunIfHybrid;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		hybrid && ~parameter_buffer_empty && state != `EXECUTE && EN_I
		|=> parameter_update_allowed_sync;
endproperty

a_parameterDelayRunIfHybrid: assert property (p_parameterDelayRunIfHybrid)
else $error("Expected \"parameter_update_allowed_sync\" to be HIGH");



endmodule
/* === CGRA Assertions END === */

/* === ParameterBuffer Assertions START === */
module tb_SVA_ParameterBuffer
(
input wire CGRA_CLK_I,
input wire RST_N_I,
input wire EN_I,
input wire NEXT_I,
input wire WRITE_EN_I,
input wire full,
input wire empty
);

property p_notFullAndWrite;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		!(WRITE_EN_I && full);
endproperty

a_notFullAndWrite: assert property (p_notFullAndWrite)
else $error("Full parameter buffer with asserted write enable is not allowed");

property p_notFullAndEmpty;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		!(full && empty);
endproperty

a_notFullAndEmpty: assert property (p_notFullAndEmpty)
else $error("Parameter Buffer may not be empty and full at the same time");

endmodule
/* === ParameterBuffer Assertions END === */

/* === SyncUnit Assertions START === */
module tb_SVA_SyncUnit #
(
parameter integer CONTEXT_ADDR_WIDTH = -1,
parameter integer CYCLE_COUNTER_WIDTH = -1,
parameter integer INCOMING_DATA_WIDTH = -1,
parameter integer RUN_COUNTER_WIDTH = -1
)
(
input wire CGRA_CLK_I,
input wire RST_N_I,
input wire EN_I,
input wire RUN_STARTED_I,
input wire SENSOR_WRITES_COMPLETE_I,
input wire run,
input wire sync_in,
input wire trigger_run,
input wire was_triggered,
input wire [CYCLE_COUNTER_WIDTH-1:0] cycle_counter,
input wire [CYCLE_COUNTER_WIDTH-1:0] interval_length
);

property p_newCycle;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		cycle_counter === interval_length && EN_I
		|=> cycle_counter === 0;
endproperty

a_newCycle: assert property (p_newCycle)
else $error("Expected \"cycle_counter\" reset to zero because of a new cycle");

property p_syncInAssertion;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		cycle_counter === 0 && run && EN_I
		|=> sync_in
endproperty

a_syncInAssertion: assert property (p_syncInAssertion)
else $error("Expected the assertion of \"sync_in\"");

property p_oneSyncInCycle;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		sync_in && EN_I
		|=> ~sync_in;
endproperty

a_oneSyncInCycle: assert property (p_oneSyncInCycle)
else $error("Expected exactly one cycle of an asserted \"sync_in\"");

property p_triggerAssertion;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		run && SENSOR_WRITES_COMPLETE_I && ~RUN_STARTED_I && ~was_triggered && EN_I
		|=> trigger_run
endproperty

a_triggerAssertion: assert property (p_triggerAssertion)
else $error("Expected the assertion of \"trigger_run\"");

property p_triggerStable;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		~RUN_STARTED_I && trigger_run && EN_I 
		|=> $stable(trigger_run);
endproperty

a_triggerStable: assert property (p_triggerStable)
else $error("Expected a stable (HIGH) \"trigger_run\" until the assertion of \"RUN_STARTED_I\"");

endmodule
/* === SyncUnit Assertions END === */

/* === Log Assertions START === */
// module tb_SVA_Log #
// (
// parameter integer LOG_GLOBAL_CONTEXT_ADDR_WIDTH = -1,
// parameter integer LOG_GLOBAL_CONTEXT_SIZE = -1,
// parameter integer LOG_ID_WIDTH = -1,
// parameter integer MAX_LOG_ADDR_WIDTH = -1,
// parameter integer LOG_GLOBAL_CONTEXT_WIDTH = -1,
// parameter integer CONTEXT_SIZE = -1,
// parameter integer OCM_CONTEXT_WIDTH = -1,
// parameter integer OCM_DATA_BUFFER_WIDTH = -1,
// parameter integer OCM_DATA_BUFFER_SIZE = -1,
// parameter integer OCM_DATA_BUFFER_ADDR_WIDTH = -1
// )
// (
// input wire CGRA_CLK_I,
// input wire AXI_ACLK_I,
// input wire RST_N_I,
// input wire EN_I,
// input wire OCM_SEND_DONE_O,
// input wire start_transaction,
// input wire awvalid,
// input wire awready,
// input wire wvalid,
// input wire wready,
// input wire wlast,
// input wire [8-1:0] transfer_counter,
// input wire [8-1:0] transaction_length,
// input wire [LOG_GLOBAL_CONTEXT_ADDR_WIDTH-1:0] log_ccnt,
// input wire log_read_en,
// input wire new_transaction_set,
// input wire [OCM_DATA_BUFFER_ADDR_WIDTH-1:0] ocm_transfer_count,
// input wire c_out_awvalid,
// input wire c_out_transaction_set_done,
// input wire ocm_start_transaction,
// input wire new_ocm_transaction_set,
// input wire ocm_wvalid,
// input wire ocm_wready,
// input wire ocm_wlast,
// input wire ocm_awvalid,
// input wire ocm_awready,
// input wire ocm_finished,
// input wire ocm_wvalid_was_deasserted
// );

// /* --- Log --- */

// property p_noTransactionWhileStart;
// 	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
// 		!( start_transaction && (wvalid || awvalid) );
// endproperty

// a_noTransactionWhileStart: assert property (p_noTransactionWhileStart)
// else $error("\"start_transaction\" must not be asserted while \"wvalid || awvalid\" is true");

// property p_startAwvalid;
// 	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
// 		start_transaction && EN_I
// 		|=> awvalid;
// endproperty

// a_startAwvalid: assert property (p_startAwvalid)
// else $error("\"awvalid\" should have been HIGH at this point");

// property p_stableAwvalid;
// 	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
// 		~awready && awvalid && EN_I
// 		|=> $stable(awvalid);
// endproperty

// a_stableAwvalid: assert property (p_stableAwvalid)
// else $error("\"awvalid\" did not remain stable but should have");

// property p_startWvalid;
// 	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
// 		start_transaction && EN_I
// 		|=> ( (wvalid && new_transaction_set) || ~wvalid ##1 wvalid );
// endproperty

// a_startWvalid: assert property (p_startWvalid)
// else $error("\"wvalid\" did not follow the expected sequence while starting a transaction");

// property p_wvalidStable;
// 	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
// 		wvalid && !(wlast && wready) && EN_I
// 		|=> $stable(wvalid);
// endproperty

// a_wvalidStable: assert property (p_wvalidStable)
// else $error("\"wvalid\" did not remain stable until wlast");

// property p_logReadEn;
// 	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
// 		((wvalid && wready && ~wlast) || (awvalid && awready)) && EN_I
// 		|-> log_read_en;
// endproperty

// a_logReadEn: assert property (p_logReadEn)
// else $error("\"log_read_en\" should have been HIGH");

// property p_noWvalidWithoutEN;
// 	@(posedge CGRA_CLK_I) disable iff (~RST_N_I)
// 		wvalid && wready && ~EN_I
// 		|=> ~wvalid;
// endproperty

// a_noWvalidWithoutEN: assert property (p_noWvalidWithoutEN)
// else $error("\"wvalid\" should be low after a transfer completes with deasserted EN_I");

// property p_awvalidAlwaysLowAfterAwready;
// 	@(posedge CGRA_CLK_I) disable iff (~RST_N_I)
// 		awvalid && awready
// 		|=> ~awvalid;
// endproperty

// a_awvalidAlwaysLowAfterAwready: assert property (p_awvalidAlwaysLowAfterAwready)
// else $error("\"awvalid\" should always (meaning also while ~EN_I) be low after awready has been observed");

// /* --- OCM --- */

// property p_ocm_start_transaction_oneCycle;
// 	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
// 		ocm_start_transaction && EN_I
// 		|=> ~ocm_start_transaction;
// endproperty

// a_ocm_start_transaction_oneCycle: assert property (p_ocm_start_transaction_oneCycle)
// else $error("\"ocm_start_transaction\" should only be asserted for one cycle");

// property p_ocm_start_transaction_Assertion1;
// 	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
// 		ocm_finished && ocm_transfer_count != 0 && ~ocm_wvalid && ~new_ocm_transaction_set && ~ocm_start_transaction && ~ocm_wvalid_was_deasserted && EN_I
// 		|=> ocm_start_transaction;
// endproperty

// a_ocm_start_transaction_Assertion1: assert property (p_ocm_start_transaction_Assertion1)
// else $error("\"ocm_start_transaction\" should have been asserted (all results available but some remaining to be send)");

// property p_ocm_start_transaction_Assertion2;
// 	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
// 		ocm_transfer_count > 8'hff && ~ocm_wvalid && ~ocm_start_transaction && ~ocm_wvalid_was_deasserted && EN_I
// 		|=> ocm_start_transaction;
// endproperty

// a_ocm_start_transaction_Assertion2: assert property (p_ocm_start_transaction_Assertion2)
// else $error("\"ocm_start_transaction\" should have been asserted (more than 256 results available)");

// property p_new_ocm_transaction_set_Deassertion;
// 	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
// 		ocm_wvalid && ocm_wready && EN_I
// 		|=> ~new_ocm_transaction_set;
// endproperty

// a_new_ocm_transaction_set_Deassertion: assert property (p_new_ocm_transaction_set_Deassertion)
// else $error("\"new_ocm_transaction_set\" should have been deasserted (data was sent)");

// property p_new_ocm_transaction_set_Assertion;
// 	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
// 		ocm_transfer_count == 0 && ocm_finished && EN_I
// 		|=> new_ocm_transaction_set;
// endproperty

// a_new_ocm_transaction_set_Assertion: assert property (p_new_ocm_transaction_set_Assertion)
// else $error("\"new_ocm_transaction_set\" should have been asserted (all transfers of the previous transaction complete)");

// property p_ocm_noWvalidWithoutEN;
// 	@(posedge CGRA_CLK_I) disable iff (~RST_N_I)
// 		ocm_wvalid && ocm_wready && ~EN_I
// 		|=> ~ocm_wvalid;
// endproperty

// a_ocm_noWvalidWithoutEN: assert property (p_ocm_noWvalidWithoutEN)
// else $error("\"ocm_wvalid\" should be low after a transfer completes with deasserted EN_I");

// property p_ocm_awvalidAlwaysLowAfterAwready;
// 	@(posedge CGRA_CLK_I) disable iff (~RST_N_I)
// 		ocm_awvalid && ocm_awready
// 		|=> ~ocm_awvalid;
// endproperty

// a_ocm_awvalidAlwaysLowAfterAwready: assert property (p_ocm_awvalidAlwaysLowAfterAwready)
// else $error("\"ocm_awvalid\" should always (meaning also while ~EN_I) be low after awready has been observed");

// property p_ocm_sendDoneIfFinished;
// 	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
// 		ocm_finished |-> OCM_SEND_DONE_O;
// endproperty

// a_ocm_sendDoneIfFinished: assert property (p_ocm_sendDoneIfFinished)
// else $error("\"OCM_SEND_DONE_O\" should be asserted if ocm_finished is asserted");

// endmodule
/* === Log Assertions END === */

/* === ConstBuf Assertions START === */
/*module tb_SVA_ConstBuf
(
input wire CGRA_CLK_I,
input wire RST_N_I,
input wire READ_ENABLE_PE0_I,
input wire [{tb_SVA_ConstBuf_addr_width}-1:0] READ_ADDR_PE0_I,
input wire [{tb_SVA_ConstBuf_data_width}-1:0] DATA_PE0_O,
input ref Memory mem0
);

property p_read;
	@(posedge CGRA_CLK_I) disable iff (~RST_N_I || ~EN_I)
		logic [{tb_SVA_ConstBuf_addr_width}-1:0] addr;
		(READ_ENABLE_PE0_I, addr = READ_ADDR_PE0_I)
		|=> DATA_PE0_O === mem0.mem[addr];
endproperty

a_read: assert property (p_read)
else $error("ConstBuf Did not provide the correct value.");

endmodule*/
/* === ConstBuf Assertions END === */

