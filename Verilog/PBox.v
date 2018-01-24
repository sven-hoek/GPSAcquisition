// configuration storage

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
input   wire 			    CLK_I;
input   wire				RST_N_I;
input   wire				EN_I;
input 	 wire			    STATUS_0_I;
input   wire				STATUS_1_I;
input wire[`CONTEXT_WIDTH_PBOX-1:0]   CONTEXT_FULL_I;
output  wire				Reg_O;
output	reg			        Comb_O;


// memory

reg [`PBOX_PARALLEL_BRANCHES_MAX-1:0] memory;

// mask for context

wire[`OUTH_pbox:`OUTL_pbox] w_mux_out;
assign w_mux_out = CONTEXT_FULL_I[`OUTH_pbox:`OUTL_pbox];

wire w_bypass_b;
assign w_bypass_b = CONTEXT_FULL_I[`BYPASSB_pbox];

wire w_bypass_a;
assign w_bypass_a = CONTEXT_FULL_I[`BYPASSA_pbox];

wire w_mode_b;
assign w_mode_b = CONTEXT_FULL_I[`ModeB_pbox];

wire w_mode_a;
assign w_mode_a = CONTEXT_FULL_I[`ModeA_pbox];

wire[`raddroutH_pbox-`raddroutL_pbox+1:0] w_rd_addr_out;
assign w_rd_addr_out = CONTEXT_FULL_I[`raddroutH_pbox:`raddroutL_pbox];

wire[`raddrcombH_pbox-`raddrcombL_pbox+1:0] w_rd_addr_comb;
assign w_rd_addr_comb = CONTEXT_FULL_I[`raddrcombH_pbox:`raddrcombL_pbox];

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
case(w_mux_inputs)
 0	: w_status = STATUS_0_I; 
 1	: w_status = STATUS_1_I; 
 default: w_status = 1'b0;
 endcase
end

wire w_reg_in_A,w_reg_in_B;
reg w_regcomb, w_regout;
assign Reg_O = w_regout;

wire linked_status_a;
assign linked_status_a = (w_mode_a == 1'b0) ? w_status & w_regcomb : w_status | w_regcomb;
assign w_reg_in_A = (w_bypass_a == 1'b1) ? w_status : linked_status_a;

wire linked_status_b;
assign linked_status_b = (w_mode_b == 1'b0) ? ~w_status & w_regcomb : ~w_status | w_regcomb;
assign w_reg_in_B = (w_bypass_b == 1'b1) ? ~w_status : linked_status_b;

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
    memory[i]<= ((i == w_wr_addr_a) && w_wr_en == 1'b1) ? w_reg_in_A : 
		((i == w_wr_addr_b) && w_wr_en == 1'b1) ? w_reg_in_B : memory[i];
   end
  end // END BEHAVIOR
 end // closes else after RST
end // close always

always@(*) begin
  w_regcomb = memory[w_rd_addr_comb];
  w_regout = memory[w_rd_addr_out];  


case(w_mux_out)
 0	: Comb_O = w_reg_in_A ; 
 1	: Comb_O = w_reg_in_B ; 
 2	: Comb_O = w_regout; 
 default: Comb_O = w_regout;
 endcase
end

endmodule
  
