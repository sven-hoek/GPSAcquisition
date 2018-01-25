/*
Author: Dennis L. Wolf
Date: 15.09.2015
Version: 1.0 draft 
Version History: 1.0 draft
Comments: This controlunit uses an adder to reduce the amoubnt of needed memory.
the main advantage is the use of an context that is clocked synchronously as
all other contexts. the d-flip flip is in prallel. therefore the contexct
is the only clocked module, wherefore the second stage of a potential pipeline
can be drawn within or after the alu. an noticable increase in frequency should be
recognizable.
*/

`include "definitions.vh" // import definitions of parameters and types

module Controlunit (
  CLK_I,
//  RST_N_I,
  EN_I,
  CBOX_I,
  DATA_I,
  WR_EN_I,
  ADDR_I,
  LOAD_EN_I,
  PC_O
  );

// PORTS:
input  wire  								CLK_I;
//input  wire  							    RST_N_I;
input  wire 								EN_I;
input  wire 			 					CBOX_I;
input  wire [`CONTEXT_ADDR_WIDTH+1:0]            DATA_I;
input  wire 								WR_EN_I;
input  wire [`CONTEXT_ADDR_WIDTH-1:0]	  	ADDR_I;
input  wire  								LOAD_EN_I;
output wire [`CONTEXT_ADDR_WIDTH-1:0]  		PC_O;

// memory
reg [`CONTEXT_ADDR_WIDTH+1:0] memory [`CONTEXT_MEMORY_LENGTH-1:0];
reg [`CONTEXT_ADDR_WIDTH+1:0] mem_out;

wire [`CONTEXT_ADDR_WIDTH-1:0] alternative_pc;
reg [`CONTEXT_ADDR_WIDTH-1:0] pc;
wire conditional, unconditional;
reg [`CONTEXT_ADDR_WIDTH-1:0] r_plus;

assign alternative_pc = mem_out[`CONTEXT_ADDR_WIDTH-1:0];
assign conditional = mem_out[`CONTEXT_ADDR_WIDTH];
assign unconditional = mem_out[`CONTEXT_ADDR_WIDTH+1];

assign PC_O = pc;

/*
integer i;
	initial begin
		// Init Mem
		for (i = 4; i <`CONTEXT_MEMORY_LENGTH-1; i = i + 1) begin
			memory[i] <= {2'b00, {(`CONTEXT_ADDR_WIDTH){1'b1}}};
		end		
		memory[`CONTEXT_MEMORY_LENGTH-1] <= {{2'b10}, {(`CONTEXT_ADDR_WIDTH){1'b1}}};
	end
*/

// memory
always@(posedge CLK_I) begin
 if(WR_EN_I)
    memory[ADDR_I] <= DATA_I;
 if(EN_I)
 mem_out <= memory[pc];

// if(!RST_N_I)
//    r_plus = {{(`CONTEXT_ADDR_WIDTH-1){1'b1}},{1'b0}};
// else begin
 if(EN_I)
   r_plus <= pc;
// else
//   r_plus <= r_plus;
//   end
end 

always@* begin
 if(LOAD_EN_I) begin
  pc = ADDR_I;
 end
 else begin
  if((!CBOX_I && conditional) || unconditional)
    pc = alternative_pc;
  else
    pc = r_plus+1;
 end
end

endmodule





  
