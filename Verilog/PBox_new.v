`timescale 1 ns / 1 ps

`include "definitions.vh" // import definitions of parameters and types


module PBox_new (
  CLK_I,
  RST_N_I,
  EN_I,
  STATUS_0_I,
  STATUS_1_I,
  CONTEXT_FULL_I,
  Reg_O,
  Comb_O
  );

// PORTS:
input   wire 				CLK_I;
input   wire				RST_N_I;
input   wire				EN_I;
input 	wire				STATUS_0_I;
input   wire				STATUS_1_I;
input   wire [`CONTEXT_WIDTH_PBOX-1:0] 	CONTEXT_FULL_I;
output  wire				Reg_O;
output	reg			        Comb_O;


// memory

reg [`PBOX_PARALLEL_BRANCHES_MAX-1:0] memory;

// mask for context

wire[`OUTH_pbox:`OUTL_pbox] w_mux_out;
assign w_mux_out = CONTEXT_FULL_I[`OUTH_pbox:`OUTL_pbox];

wire w_bypass_and_b;
assign w_bypass_and_b = CONTEXT_FULL_I[`bypassb_and_pbox];

wire w_bypass_and_a;
assign w_bypass_and_a = CONTEXT_FULL_I[`bypassa_and_pbox];

wire w_bypass_or_b;
assign w_bypass_or_b = CONTEXT_FULL_I[`bypassb_or_pbox];

wire w_bypass_or_a;
assign w_bypass_or_a = CONTEXT_FULL_I[`bypassa_or_pbox];

wire[`raddrbH_pbox-`raddrbL_pbox+1:0] w_rd_addr_b;
assign w_rd_addr_b = CONTEXT_FULL_I[`raddrbH_pbox:`raddrbL_pbox];

wire[`raddraH_pbox-`raddraL_pbox+1:0] w_rd_addr_a;
assign w_rd_addr_a = CONTEXT_FULL_I[`raddraH_pbox:`raddraL_pbox];

wire[`waddrBH_pbox-`waddrBL_pbox+1:0] w_wr_addr_b;
assign w_wr_addr_b = CONTEXT_FULL_I[`waddrBH_pbox:`waddrBL_pbox];

wire[`waddrAH_pbox-`waddrAL_pbox+1:0] w_wr_addr_a;
assign w_wr_addr_a = CONTEXT_FULL_I[`waddrAH_pbox:`waddrAL_pbox];

wire[`muxH_pbox-`muxL_pbox+1:0] w_mux_inputs;
assign w_mux_inputs = CONTEXT_FULL_I[`muxH_pbox:`muxL_pbox];

wire w_wr_en;
assign w_wr_en = CONTEXT_FULL_I[`write_enable_pbox];


reg w_status;
always@(*) begin
(* full_case *)
case(w_mux_inputs)
 0	: w_status = STATUS_0_I; 
 1	: w_status = STATUS_1_I; 
 endcase
end

wire w_reg_in_a,w_reg_in_b;
reg w_reg_a, w_reg_b;
assign Reg_O = w_reg_b;

assign w_reg_in_a = (w_bypass_or_a && w_bypass_and_a) ? w_status : (w_bypass_or_a) ? w_status && w_reg_a  : (w_bypass_and_a) ? w_status || w_reg_b : (w_status && w_reg_a) || w_reg_b;
assign w_reg_in_b = (w_bypass_or_b && w_bypass_and_b) ? !w_status : (w_bypass_or_b) ? !w_status && w_reg_b  : (w_bypass_and_b) ? !w_status || w_reg_a : (w_status && w_reg_b) || w_reg_a;
integer i;

always@(posedge CLK_I) begin
 if(!RST_N_I) begin	// RESET 
   for (i = 0; i < `PBOX_PARALLEL_BRANCHES_MAX; i=i+1) begin
     memory[i] <= 0;
    end
  end // END RESET
   else begin
    if(~EN_I) begin // DISABLE
     for (i = 0; i < `PBOX_PARALLEL_BRANCHES_MAX; i=i+1) begin
       memory[i] <= memory[i];
      end
     end // END DISABLE
      else begin // BEHAVIOR
  for (i = 0; i < `PBOX_PARALLEL_BRANCHES_MAX; i=i+1) begin
    memory[i]<= ((i == w_wr_addr_a) && w_wr_en == 1'b1) ? w_reg_in_a : 
		((i == w_wr_addr_b) && w_wr_en == 1'b1) ? w_reg_in_b : memory[i];
   end
  end // END BEHAVIOR
 end // closes else after RST
end // close always

always@(*) begin
  w_reg_a = memory[w_rd_addr_a];
  w_reg_b = memory[w_rd_addr_b];  
(* full_case *)
case(w_mux_out)
 0	: Comb_O = w_reg_in_a ; 
 1	: Comb_O = w_reg_in_b ; 
 2	: Comb_O = w_reg_a; 
 3	: Comb_O = w_reg_b; 
 endcase
end

endmodule
  
