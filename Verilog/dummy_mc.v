// configuration storage

`timescale 1 ns / 1 ps

`include "definitions.vh" // import definitions of parameters and types

module Dummy_mc (
  CLK_I,
  RST_N_I,
  EN_I,
  OP_A_I,
  OP_B_I,
  LOAD_I,
  RESULT_O
  );

// PORTS:
input   			CLK_I;
input  				RST_N_I;
input   			EN_I;
input [`DATA_WIDTH-1:0] 	OP_A_I;
input [`DATA_WIDTH-1:0]  	OP_B_I;
input 				LOAD_I;
output wire [`DATA_WIDTH-1:0]	RESULT_O;

reg [`DATA_WIDTH-1:0] tmp0,tmp1,tmp2;

always@(posedge CLK_I) begin
 if(~RST_N_I) begin	// RESET 
  tmp0 <= 0;
  tmp1 <= 0;
  tmp2 <= 0;
  end // END RESET
   else begin
    if(~EN_I) begin // DISABLE
    tmp0 <= tmp0;
    tmp1 <= tmp1;
    tmp2 <= tmp2;
     end // END DISABLE
      else begin // BEHAVIOR
	if(LOAD_I) begin
	  tmp0 <= 0;
	  tmp1 <= OP_A_I;
	  tmp2 <= OP_B_I;
	end
	 else begin
	  tmp0 <= tmp1;
	  tmp1 <= tmp2;
	  tmp2 <= tmp0;
	 end
  end // END BEHAVIOR
 end // closes else after RST
end // close always

assign RESULT_O = tmp0 + tmp1;

endmodule
  
