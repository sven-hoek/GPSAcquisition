/*
Author: Dennis L. Wolf
Date: parameter driven module - latest change: 8th April 2016
Version: 1.0 draft (Changed to String Template Interface)
Version History: 1.2 Code Review and Cleanup
		 1.1 debugged and tested
		 1.0 Concept

Comments: This controlunit uses an adder to reduce the amoubnt of needed contextmemory.
the main advantage is the use of an context that is clocked synchronously as
all other contexts. the d-flip flip is in prallel. therefore the contexct
is the only clocked module, wherefore the second stage of a potential pipeline
can be drawn within or after the alu. an noticable increase in frequency should be
recognizable.
*/

`include "definitions.vh" // import definitions of parameters and types

`default_nettype wire

module ContextControlUnit #(parameter CONTEXT_ADDR_WIDTH = 0, CONTEXT_MEMORY_LENGTH = 0)(

input  wire  					CLK_I,
//input  wire  					RST_N_I,
input  wire 					EN_I,
input  wire 			 		CBOX_I,
input  wire [CONTEXT_ADDR_WIDTH+1:0]           DATA_I,
input  wire 					WR_EN_I,
input  wire [CONTEXT_ADDR_WIDTH-1:0]	  	ADDR_I,
input  wire  					LOAD_EN_I,
output wire [CONTEXT_ADDR_WIDTH-1:0]  		CCNT_O);

// contextmemory
reg [CONTEXT_ADDR_WIDTH+2:0] contextmemory [CONTEXT_MEMORY_LENGTH-1:0];
reg [CONTEXT_ADDR_WIDTH+2:0] mem_out;

wire [CONTEXT_ADDR_WIDTH-1:0] alternative_ccnt;
reg [CONTEXT_ADDR_WIDTH-1:0] ccnt;
wire conditional, unconditional;
reg [CONTEXT_ADDR_WIDTH-1:0] r_plus;

assign alternative_ccnt = mem_out[CONTEXT_ADDR_WIDTH-1:0];
assign conditional = mem_out[CONTEXT_ADDR_WIDTH];
assign unconditional = mem_out[CONTEXT_ADDR_WIDTH+1];
assign relative = mem_out[CONTEXT_ADDR_WIDTH+2];

assign CCNT_O = ccnt;

// contextmemory
always@(posedge CLK_I) begin
/* if(!RST_N_I) begin
  r_plus <=  CONTEXT_MEMORY_LENGTH-2;
 end
  else begin */
   if(WR_EN_I)
    contextmemory[ADDR_I] <= DATA_I;
   if(EN_I) begin
    mem_out <= contextmemory[ccnt];
    r_plus <= ccnt;
   end 
 end
end 

always@* begin
 if(LOAD_EN_I) begin
  ccnt = ADDR_I;
 end
else begin

 if(relative) begin
  if(unconditional || (conditional && !CBOX_I)) begin
   ccnt = r_plus + alternative_ccnt;
  end
   else begin
     ccnt = r_plus + 1;
    end
 end
 else begin
  if(unconditional || (conditional && !CBOX_I)) begin
   ccnt = alternative_ccnt;
  end
    else begin
     ccnt = r_plus + 1;
    end
  end
 end
end



initial begin
// forces start condition
contextmemory[CONTEXT_MEMORY_LENGTH-1] = {2'b010,{CONTEXT_ADDR_WIDTH{1'b1}}};
//mem_out = {(CONTEXT_ADDR_WIDTH+2){1'b1}};
end


endmodule

