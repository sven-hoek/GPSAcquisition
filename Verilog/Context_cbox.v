/*
Author: Dennis L. Wolf
Date: 15.09.2015
Version: 1.2 (standard pbox-context for autogeneration)
Version History: 1.2 changed to BRAM style 
		 1.1 debugged and simulated
		 1.0 layout & concept
*/
`timescale 1 ns / 1 ps

`include "definitions.vh" // import definitions of parameters and types

module Context_pbox (
  CLK_I,
//  RST_N_I,
  EN_I,
  PC_I,
  DATA_I,
  WR_ADDR_I,
  WR_EN_I,
  DATA_O
  );

// PORTS:
input wire  				CLK_I;
// input wire 				RST_N_I;
 input wire				EN_I;
input wire [`CONTEXT_ADDR_WIDTH-1:0] 	PC_I;
input wire [`CONTEXT_WIDTH_PBOX-1:0]  		DATA_I;
input wire [`CONTEXT_ADDR_WIDTH-1:0] 	WR_ADDR_I;
input wire  				WR_EN_I;
output wire [`CONTEXT_WIDTH_PBOX-1:0]	DATA_O;

// memory

 (* ram_style = "block" *) reg [`CONTEXT_WIDTH_PBOX-1:0] memory [`CONTEXT_MEMORY_LENGTH-1:0];
reg [`CONTEXT_WIDTH_PBOX-1:0] out;

always@(posedge CLK_I) begin
    if(WR_EN_I)
        memory[WR_ADDR_I] <= DATA_I;
    if(EN_I)
        out <= memory[PC_I];
end // close always

assign DATA_O = out;
endmodule
  
