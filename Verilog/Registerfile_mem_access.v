/*
Author: Dennis L. Wolf
Date: 15.09.2015
Version: 1.1 (standard registefile with memory access for autogeneration)
Version History: 1.1 debugged and simulated
		 1.0 layout & concept
*/

`timescale 1 ns / 1 ps

`include "definitions.vh" // import definitions of parameters and types

module Registerfile_mem_access #(
  parameter REGFILE_SIZE = 16,
  parameter REGFILE_ADDR_WIDTH = 4
)(
  CLK_I,
  RST_N_I,
  EN_I,
  RD_PORT_DIRECT_ADDR_I,
  RD_PORT_DIRECT_O,
  RD_PORT_MUX_ADDR_I,
  RD_PORT_MUX_O,
  RD_PORT_CACHE_ADDR_I,
  RD_PORT_CACHE_O,
  WR_PORT_EN_I,
  WR_PORT_ADDR_I,
  WR_PORT_DATA_I
  );


// PORTS:


  input  wire   			CLK_I;
  input  wire   			RST_N_I;
  input  wire   			EN_I;
  input  wire [REGFILE_ADDR_WIDTH-1:0]	RD_PORT_DIRECT_ADDR_I;
  output wire [`DATA_WIDTH-1:0] 	RD_PORT_DIRECT_O;
  input  wire [REGFILE_ADDR_WIDTH-1:0]	RD_PORT_MUX_ADDR_I;
  output wire [`DATA_WIDTH-1:0] 	RD_PORT_MUX_O;
  input  wire [REGFILE_ADDR_WIDTH-1:0]	RD_PORT_CACHE_ADDR_I;
  output wire [`DATA_WIDTH-1:0]		RD_PORT_CACHE_O;
  input  wire   			WR_PORT_EN_I;
  input  wire [REGFILE_ADDR_WIDTH-1:0] WR_PORT_ADDR_I;
  input  wire [`DATA_WIDTH-1:0] 	WR_PORT_DATA_I;




// Registerfile

reg signed [`DATA_WIDTH-1:0] memory [REGFILE_SIZE-1:0];

// clocekd singals

integer i;

always@(posedge CLK_I) begin


if (WR_PORT_EN_I && EN_I)
  memory[WR_PORT_ADDR_I] <=  WR_PORT_DATA_I; 


/*
 if(~RST_N_I) begin	// RESET 
   for (i = 0; i < REGFILE_SIZE; i=i+1) begin
     memory[i] <= 0;
    end
  end // END RESET
   else begin
    if(~EN_I) begin // DISABLE
     for (i = 0; i < REGFILE_SIZE; i=i+1) begin
       memory[i] <= memory[i];
      end
     end // END DISABLE
      else begin // BEHAVIOR 
  for (i = 0; i < REGFILE_SIZE; i=i+1) begin
    memory[i] <= ((i == WR_PORT_ADDR_I) && WR_PORT_EN_I) ? WR_PORT_DATA_I : memory[i];
   end
  end // END BEHAVIOR
 end // closes else after RST
*/
end // close always


//always@(*) begin
  assign RD_PORT_DIRECT_O = memory[RD_PORT_DIRECT_ADDR_I];
  assign RD_PORT_MUX_O = memory[RD_PORT_MUX_ADDR_I];
  assign RD_PORT_CACHE_O = memory[RD_PORT_CACHE_ADDR_I];
// end

endmodule
