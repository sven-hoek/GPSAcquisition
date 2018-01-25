`include "actorif.vh"
`include "cgra.vh"
module ActorIF #
(
	parameter integer CGRA_CONTEXT_ADDR_WIDTH = -1,
	parameter integer CGRA_CONTEXT_SIZE = -1
)
(
input wire EN_I,
input wire CGRA_CLK_I,
input wire RST_N_I,
input wire [CGRA_CONTEXT_ADDR_WIDTH-1:0] CCNT_I,
output wire [`ACTOR_ID_WIDTH-1:0] ACTOR_WRITE_ADDR_O, // address to write to an actor
output wire [`PE_ID_WIDTH-1:0] ACTOR_SOURCE_PE_ID_O, // ID of the PE to take the data of this write from
output wire ACTOR_WRITE_ENABLE_O, // write enable an actor
output wire SYNC_OUT_O,
input wire CONTEXT_WRITE_EN_I,
input wire [CGRA_CONTEXT_ADDR_WIDTH-1:0] CONTEXT_ADDR_I,
input wire [`ACTOR_CONTEXT_WIDTH-1:0] CONTEXT_DATA_I
);

// memory declarations
(* ram_style = "block" *) reg [`ACTOR_CONTEXT_WIDTH-1:0] context_memory [CGRA_CONTEXT_SIZE-1:0];
reg [`ACTOR_CONTEXT_WIDTH-1:0] context_out;

// output assignments
assign ACTOR_WRITE_ADDR_O = context_out[`ACTOR_ID_WIDTH-1:0];
assign ACTOR_SOURCE_PE_ID_O = context_out[`ACTOR_CONTEXT_WIDTH-3:`ACTOR_ID_WIDTH];
assign ACTOR_WRITE_ENABLE_O = context_out[`ACTOR_CONTEXT_WIDTH-2];
assign SYNC_OUT_O = context_out[`ACTOR_CONTEXT_WIDTH-1];

// context logic and data input handling
always @(posedge CGRA_CLK_I) begin
	if (~RST_N_I) begin
		context_out <= 0;
	end else if (EN_I) begin
		if (CONTEXT_WRITE_EN_I) begin
			context_memory[CONTEXT_ADDR_I] <= CONTEXT_DATA_I;
		end

		context_out <= context_memory[CCNT_I];
	end
end

endmodule