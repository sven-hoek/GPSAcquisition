/*
Author: Dennis L. Wolf
Date: 14.09.2015
Version: 1.1
Version History: 1.1 debugged and tested
		 1.0 concept
*/

`timescale 1 ns / 1 ps

`include "definitions.vh" // import definitions of parameters and types

module PE
  ( 
  CLK_I,
  RST_N_I,
  EN_GLOBAL_I,
//stop - module input list -stop
  AMIDAR_I,
  CACHE_DATA_I,
  CONTEXT_FULL_I,
  PBOX_I,
  DIRECT_O,
  CACHE_ADDR_O, 
  DATA_O,
  CACHE_WR_O,
  CACHE_VALID_O,
  ALU_STATUS_O);

  input wire 			 	CLK_I;
  input wire 			 	RST_N_I;
  input wire 			 	EN_GLOBAL_I;
//stop - module inputs -stop
  input wire [`DATA_WIDTH-1:0]   	AMIDAR_I;
  input wire [`DATA_WIDTH-1:0] 	 	CACHE_DATA_I; 
  input wire [`CONTEXT_WIDTH-1:0]	CONTEXT_FULL_I;
  input wire 			 	PBOX_I;
  output wire [`DATA_WIDTH-1:0] 	DIRECT_O;
  output wire [`CACHE_ADDR_WIDTH-1:0] 	CACHE_ADDR_O; 
  output wire [`DATA_WIDTH-1:0] 	DATA_O;
  output wire 				CACHE_WR_O; 
  output wire 				CACHE_VALID_O; 
  output wire 				ALU_STATUS_O;


  parameter   	INDMA = 2, 
		IN = 1, 
		ALU = 0;

  parameter REGFILE_SIZE = 512;
  parameter REGFILE_ADDR_WIDTH = 4;


// direct assignments
wire [`DATA_WIDTH-1:0] w_reg_mux;
assign DATA_O = w_alu_in_B; 

// INTERNAL SIGNALS:

wire w_enable_context;
assign w_enable_context = CONTEXT_FULL_I[`en_pe];

wire w_conditional_dma;
assign w_conditional_dma = CONTEXT_FULL_I[`cond_dma_pe];

wire w_conditional_write;
assign w_conditional_write = CONTEXT_FULL_I[`cond_wr_pe];

wire w_write_enable;
assign w_write_enable = CONTEXT_FULL_I[`wr_en_pe];

wire [`rdmuxH_pe-`rdmuxL_pe:0] w_mux_addr;
assign w_mux_addr = CONTEXT_FULL_I[`rdmuxH_pe:`rdmuxL_pe];

wire [`rddoH_pe-`rddoL_pe:0] w_directout_addr;
assign w_directout_addr = CONTEXT_FULL_I[`rddoH_pe:`rddoL_pe];

wire[`rdCacheH_pe-`rdCacheL_pe:0] w_cache_addr;
assign w_cache_addr = CONTEXT_FULL_I[`rdCacheH_pe:`rdCacheL_pe];

wire [`wrH_pe-`wrL_pe:0] w_write_addr;
assign w_write_addr = CONTEXT_FULL_I[`wrH_pe:`wrL_pe];

wire[`muxAH_pe-`muxAL_pe:0] w_muxA;
assign w_muxA = CONTEXT_FULL_I[`muxAH_pe:`muxAL_pe];

wire[`muxBH_pe-`muxBL_pe:0] w_muxB;
assign w_muxB = CONTEXT_FULL_I[`muxBH_pe:`muxBL_pe];

wire [`muxRegH_pe-`muxRegL_pe:0] w_MuxR;
assign w_MuxR = CONTEXT_FULL_I[`muxRegH_pe:`muxRegL_pe];

wire [`opH_pe-`opL_pe:0] w_opcode;
assign w_opcode = CONTEXT_FULL_I[`opH_pe:`opL_pe];




reg [`DATA_WIDTH-1:0] w_alu_in_A, w_alu_in_B; 
wire [`DATA_WIDTH-1:0] w_alu_Out;

// Both multiplexors to which connects the Data input of the registerfile
always@(*)begin
(* full_case *)
 case(w_muxA)
//stop - muxA -stop
 endcase
(* full_case *)
 case(w_muxB)
//stop - muxB -stop
 endcase
end

// enable generation
 wire w_enable_local;
 and(w_enable_local,EN_GLOBAL_I,w_enable_context);

// Multiplexor in front of registerfile
reg [`DATA_WIDTH-1:0] w_reg_in;


always@(*) begin
(* full_case *)
 case(w_MuxR)
   ALU	: w_reg_in = w_alu_Out; 
   INDMA: w_reg_in = CACHE_DATA_I; 
   IN	: w_reg_in = AMIDAR_I;
  endcase
end

wire  w_write_enable_regfile;  
assign w_write_enable_regfile = (w_conditional_write) ? (PBOX_I && w_write_enable) : w_write_enable;

// INSTANCES:

//stop - Alu init -stop
  .CLK_I        (CLK_I),
  .RST_N_I      (RST_N_I),
  .EN_I		(w_enable_local),
  .OPERAND_A    (w_alu_in_A),
  .OPERAND_B    (w_alu_in_B),
  .OPCODE_I     (w_opcode),
  .PBOX_I 	(PBOX_I),
  .CONDITIONAL_I(w_conditional_dma),
  .RESULT_O  	(w_alu_Out),
  .CACHE_VALID_O(CACHE_VALID_O),
  .CACHE_WRITE_O(CACHE_WR_O), 
  .STATUS_O 	(ALU_STATUS_O)
);


Registerfile_mem_access #(
  .REGFILE_SIZE(`REGFILE_SIZE),
  .REGFILE_ADDR_WIDTH(`REGFILE_ADDR_WIDTH)
) 
regfile(
  .CLK_I(CLK_I),
  .RST_N_I(RST_N_I),
  .EN_I(w_enable_local),
  .RD_PORT_DIRECT_ADDR_I(w_directout_addr),
  .RD_PORT_DIRECT_O(DIRECT_O),
  .RD_PORT_MUX_ADDR_I(w_mux_addr),
  .RD_PORT_MUX_O(w_reg_mux),
  .RD_PORT_CACHE_ADDR_I(w_cache_addr),
  .RD_PORT_CACHE_O(CACHE_ADDR_O), 
  .WR_PORT_EN_I(w_write_enable_regfile),
  .WR_PORT_ADDR_I(w_write_addr),
  .WR_PORT_DATA_I(w_reg_in)
  );

endmodule
