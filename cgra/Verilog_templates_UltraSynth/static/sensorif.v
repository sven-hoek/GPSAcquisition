`include "sensorif.vh"
module SensorIF #
(
	parameter integer CGRA_CONTEXT_SIZE = -1,
	parameter integer CGRA_CONTEXT_ADDR_WIDTH = -1
)
(
input wire EN_I,
input wire CGRA_CLK_I,
input wire RST_N_I,
input wire [CGRA_CONTEXT_ADDR_WIDTH-1:0] CCNT_I,
(* dont_touch = "true" *) output wire [`SENSOR_ID_WIDTH-1:0] SENSOR_ADDR_O,
(* dont_touch = "true" *) output wire SENSOR_READ_EN_O,
input wire CONTEXT_WRITE_EN_I,
input wire [CGRA_CONTEXT_ADDR_WIDTH-1:0] CONTEXT_ADDR_I,
input wire [`SENSOR_CONTEXT_WIDTH-1:0] CONTEXT_DATA_I
);

// memory declaration
(* ram_style = "block" *) reg [`SENSOR_CONTEXT_WIDTH-1:0] context_memory [CGRA_CONTEXT_SIZE-1:0];
reg [`SENSOR_CONTEXT_WIDTH-1:0] context_out;

// output assignments
assign SENSOR_ADDR_O = context_out[`SENSOR_ID_WIDTH-1:0];
assign SENSOR_READ_EN_O = context_out[`SENSOR_CONTEXT_WIDTH-1];

// context logic
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