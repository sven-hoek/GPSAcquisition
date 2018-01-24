`include "parameterbuffer.vh"

module ParameterBuffer #
(
	parameter integer BUFFER_SIZE = 32,
	parameter integer PARAMETER_WIDTH = 32,
	parameter integer PE_ID_WIDTH = 2,
	parameter integer RF_WIDTH = 6,
	parameter integer COUNTER_WIDTH = 5
)
(
	input wire EN_I,
	input wire CGRA_CLK_I,
	input wire RST_N_I,
	// ports for setting the value of the expected hybrid parameter count
	input wire WRITE_EN_PARAMETER_COUNT_I,
	input wire [`PARAMETER_BUFFER_EXPECTED_COUNTER_WIDTH-1:0] EXPECTED_PARAMETER_COUNT_I,
	// ports for reading and writing parameters
	input wire WRITE_EN_I,
	input wire IS_HYBRID_PARAMETER_I, // the parameter written will be treated as a hybrid parameter
	input wire SYNC_IN_I, // resets the parameter counter
	input wire NEXT_I, // the reader informs the buffer that the next entry may be read
	input wire [PARAMETER_WIDTH-1:0] DATA_I, // just the parameter data
	input wire [PE_ID_WIDTH+RF_WIDTH-1:0] DESTINATION_I, // the PE ID and the RF offset, directly from the ID_Context
	output wire FULL_O,
	output wire EMPTY_O,
	output wire ALL_HYBRID_PARAMETERS_DONE_O, // asserted after parameter counter reached the given value
	output wire [PARAMETER_WIDTH-1:0] DATA_O, // just the parameter data, directly writeable to the register file, no further register stage needed
	output wire [PE_ID_WIDTH-1:0] DESTINATION_PE_O, // the PE ID
	output wire [RF_WIDTH-1:0] DESTINATION_RF_OFFSET_O // the RF offset
);

// declarations
reg 	[1+PE_ID_WIDTH+RF_WIDTH+PARAMETER_WIDTH-1:0] buffer [BUFFER_SIZE-1:0]; // +1 for determining if this is a hybrid parameter
reg 	[COUNTER_WIDTH-1:0] writeCntr; // track the first empty buffer position
wire 	[COUNTER_WIDTH-1:0] writeCntrPlus1; // track the first empty buffer position
reg 	[COUNTER_WIDTH-1:0] savedReadCntr; // track the first unprocessed buffer position
wire 	[COUNTER_WIDTH-1:0] readCntr; // track the first unprocessed buffer position
reg 	[PARAMETER_WIDTH-1:0] holdCycle_0;
reg 	[PARAMETER_WIDTH-1:0] holdCycle_1;
reg 	[PARAMETER_WIDTH-1:0] holdCycle_2;
reg 	[PARAMETER_WIDTH-1:0] dataOut;
reg 	[PE_ID_WIDTH-1:0] pe_id_out;
reg 	[RF_WIDTH-1:0] rf_offset;
reg 	[RF_WIDTH-1:0] rf_offset_out;
reg 	is_hybrid_parameter;
wire 	full;
wire 	empty;

// hybrid counter
reg 	[`PARAMETER_BUFFER_EXPECTED_COUNTER_WIDTH-1:0] expected_hybrid_count;
reg 	[`PARAMETER_BUFFER_EXPECTED_COUNTER_WIDTH-1:0] hybrid_count;

// output assignments
assign DATA_O = dataOut;
assign DESTINATION_PE_O = pe_id_out;
assign DESTINATION_RF_OFFSET_O = rf_offset_out;
assign EMPTY_O = empty;
assign FULL_O = full;
assign ALL_HYBRID_PARAMETERS_DONE_O = expected_hybrid_count == hybrid_count;

// logic
assign empty = writeCntr == readCntr ? 1'b1 : 1'b0;
assign readCntr = NEXT_I ? savedReadCntr + 1 : savedReadCntr;
assign writeCntrPlus1 = writeCntr + 1;
assign full = writeCntrPlus1 == readCntr ? 1'b1 : 1'b0;

always @(posedge CGRA_CLK_I) begin
	if (RST_N_I == 1'b0) begin
		writeCntr <= 0;
		savedReadCntr <= 0;
		is_hybrid_parameter <= 0;
		pe_id_out <= 0;
		rf_offset <= 0;
		rf_offset_out <= 0;
		holdCycle_0 <= 0;
		holdCycle_1 <= 0;
		holdCycle_2 <= 0;
		dataOut <= 0;
	end
	else if (EN_I) begin
		if (WRITE_EN_I && ~full) begin
			writeCntr <= writeCntr + 1;
			buffer[writeCntr][PARAMETER_WIDTH-1:0] <= DATA_I;
			buffer[writeCntr][PE_ID_WIDTH+RF_WIDTH+PARAMETER_WIDTH-1:PARAMETER_WIDTH] <= DESTINATION_I;
			buffer[writeCntr][PE_ID_WIDTH+RF_WIDTH+PARAMETER_WIDTH] <= IS_HYBRID_PARAMETER_I;
		end

		savedReadCntr <= readCntr;

		// hybrid parameter switch
		is_hybrid_parameter <= buffer[readCntr][PE_ID_WIDTH+RF_WIDTH+PARAMETER_WIDTH];
		// id
		pe_id_out <= buffer[readCntr][PE_ID_WIDTH+RF_WIDTH+PARAMETER_WIDTH-1:PARAMETER_WIDTH+RF_WIDTH];
		// offset
		rf_offset <= buffer[readCntr][RF_WIDTH+PARAMETER_WIDTH-1:PARAMETER_WIDTH];
		rf_offset_out <= rf_offset;
		// data
		holdCycle_0 <= buffer[readCntr][PARAMETER_WIDTH-1:0];
		holdCycle_1 <= holdCycle_0;
		holdCycle_2 <= holdCycle_1;
		dataOut <= holdCycle_2;
	end
end

always @(posedge CGRA_CLK_I) begin
	if (RST_N_I == 1'b0) begin
		hybrid_count <= 0;
		expected_hybrid_count <= 0;
	end
	else if (EN_I) begin
		// We zero the counter when a new run is started and 
		// we also reset it when a new expected amount is written!
		if (SYNC_IN_I || WRITE_EN_PARAMETER_COUNT_I)
			hybrid_count <= 0;
		else if (is_hybrid_parameter && NEXT_I)
			hybrid_count <= hybrid_count + 1;

		if (WRITE_EN_PARAMETER_COUNT_I)
			expected_hybrid_count <= EXPECTED_PARAMETER_COUNT_I;
	end
end

endmodule
