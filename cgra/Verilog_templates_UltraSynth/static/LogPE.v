`include "cgra.vh"

module LogPE #
(
	parameter integer CONTEXT_SIZE = -1,
	parameter integer CONTEXT_ADDR_WIDTH = -1,
	parameter integer CONTEXT_WIDTH = -1,
	parameter integer LOG_SIZE = -1,
	parameter integer LOG_ADDR_WIDTH = -1
)
(
	(* dont_touch = "true" *) input wire EN_I,
	(* dont_touch = "true" *) input wire CGRA_CLK_I,
	(* dont_touch = "true" *) input wire RST_N_I,
	(* dont_touch = "true" *) input wire SYNC_IN_I,
	(* dont_touch = "true" *) input wire LOG_TRANSACTIONS_DONE_I,
	(* dont_touch = "true" *) input wire [CONTEXT_ADDR_WIDTH-1:0] CCNT_I,
	(* dont_touch = "true" *) input wire CONTEXT_WREN_I,
	(* dont_touch = "true" *) input wire [CONTEXT_ADDR_WIDTH-1:0] CONTEXT_ADDR_I,
	(* dont_touch = "true" *) input wire [CONTEXT_WIDTH-1:0] CONTEXT_DATA_I,
	(* dont_touch = "true" *) input wire [`DATA_WIDTH-1:0] LOG_DATA_I,
	//(* dont_touch = "true" *) input wire LOG_CLEAR_I, // ? not used atm
	(* dont_touch = "true" *) input wire [LOG_ADDR_WIDTH-1:0] LOG_READ_ADDR_I,
	(* dont_touch = "true" *) input wire LOG_READ_EN_I,
	(* dont_touch = "true" *) output wire [`DATA_WIDTH-1:0] LOG_DATA_O
);

// context declaration
(* ram_style = "block" *) reg [CONTEXT_WIDTH-1:0] logContext [CONTEXT_SIZE-1:0];

// log BRAMs
(* ram_style = "block" *) reg [`DATA_WIDTH-1:0] log1 [LOG_SIZE-1:0];
(* ram_style = "block" *) reg [`DATA_WIDTH-1:0] log2 [LOG_SIZE-1:0];

// further declarations
reg write_is_log_1;
reg read_is_log_1;
reg [`DATA_WIDTH-1:0] log_read_out;
reg [CONTEXT_WIDTH-1:0] context_out;
wire enable_log_write;
wire [CONTEXT_WIDTH-2:0] log_write_addr;

assign enable_log_write = context_out[CONTEXT_WIDTH-1];
assign log_write_addr = context_out[CONTEXT_WIDTH-2:0];

// output assignments
assign LOG_DATA_O = log_read_out;

// logic
always @(posedge CGRA_CLK_I) begin
	if (~RST_N_I) begin
		write_is_log_1 <= 1'b0; // the first sync in switches this to 1'b1
		read_is_log_1 <= 1'b1;
	end else if (EN_I) begin
		// context
		if (CONTEXT_WREN_I)
			logContext[CONTEXT_ADDR_I] <= CONTEXT_DATA_I[CONTEXT_WIDTH-1:0];

		// read and write dual buffer switching
		if (SYNC_IN_I)
			write_is_log_1 <= ~write_is_log_1;
		if (LOG_TRANSACTIONS_DONE_I)
			read_is_log_1 <= ~read_is_log_1;

		// write to the correct buffer
		if (enable_log_write && write_is_log_1)
			log1[log_write_addr] <= LOG_DATA_I;
		if (enable_log_write && ~write_is_log_1)
			log2[log_write_addr] <= LOG_DATA_I;

		// data read
		if (LOG_READ_EN_I && read_is_log_1)
			log_read_out <= log1[LOG_READ_ADDR_I];
		else if (LOG_READ_EN_I && ~read_is_log_1)
			log_read_out <= log2[LOG_READ_ADDR_I];

		// context read
		context_out <= logContext[CCNT_I];
	end
end

endmodule
