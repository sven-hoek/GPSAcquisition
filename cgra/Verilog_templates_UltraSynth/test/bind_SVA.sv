/*
 * How this file should be used:
 * Add it to the Design Units used by the modelsim simulator to instantiate all
 * assertion modules inside the modules to test.
 * This is done by using the "bind" feature of SystemVerilog. It instantiates the
 * Assertion modules (e.g. tb_SVA_ComUnit) inside the respective DUT ($root.tb.cgra.axi_interface),
 * taking local wires of the DUT as inputs (named equally).
 */

`include "tb_SVA.sv"

module bind_SVA ();

bind ComUnit : $root.tb.cgra.axi_interface tb_SVA_ComUnit 
#
(
  .IDC_ADDR_WIDTH(IDC_ADDR_WIDTH),
  .IDC_WIDTH(IDC_WIDTH)
)
bound_tb_SVA_ComUnit 
(
//.assertion_module_port(target_module_wire)
	.*
);

bind Cgra_Ultrasynth : $root.tb.cgra tb_SVA_CGRA 
#
(
	.CONTEXT_ADDR_WIDTH(CONTEXT_ADDR_WIDTH),
	.CONTEXT_SIZE(CONTEXT_SIZE)
)
bound_tb_SVA_CGRA
(
//.assertion_module_port(target_module_wire)
	.*
);

bind ParameterBuffer : $root.tb.cgra.parameterBuffer tb_SVA_ParameterBuffer
bound_tb_SVA_ParameterBuffer
(
//.assertion_module_port(target_module_wire)
	.*
);

bind SyncUnit : $root.tb.cgra.syncUnit tb_SVA_SyncUnit
#
(
  .CONTEXT_ADDR_WIDTH(CONTEXT_ADDR_WIDTH),
  .CYCLE_COUNTER_WIDTH(CYCLE_COUNTER_WIDTH),
  .INCOMING_DATA_WIDTH(INCOMING_DATA_WIDTH),
  .RUN_COUNTER_WIDTH(RUN_COUNTER_WIDTH)
)
bound_tb_SVA_ParameterBuffer
(
//.assertion_module_port(target_module_wire)
	.*
);

/*bind Log : $root.tb.cgra.log tb_SVA_Log
#
(
  .LOG_GLOBAL_CONTEXT_ADDR_WIDTH(LOG_GLOBAL_CONTEXT_ADDR_WIDTH),
  .LOG_GLOBAL_CONTEXT_SIZE(LOG_GLOBAL_CONTEXT_SIZE),
  .LOG_ID_WIDTH(LOG_ID_WIDTH),
  .MAX_LOG_ADDR_WIDTH(MAX_LOG_ADDR_WIDTH),
  .LOG_GLOBAL_CONTEXT_WIDTH(LOG_GLOBAL_CONTEXT_WIDTH),
  .CONTEXT_SIZE(CONTEXT_SIZE),
  .OCM_CONTEXT_WIDTH(OCM_CONTEXT_WIDTH),
  .OCM_DATA_BUFFER_WIDTH(OCM_DATA_BUFFER_WIDTH),
  .OCM_DATA_BUFFER_SIZE(OCM_DATA_BUFFER_SIZE),
  .OCM_DATA_BUFFER_ADDR_WIDTH(OCM_DATA_BUFFER_ADDR_WIDTH)
)
bound_tb_SVA_Log
(
//.assertion_module_port(target_module_wire)
	.*
);*/

endmodule
