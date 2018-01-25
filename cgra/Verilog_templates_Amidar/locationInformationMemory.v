module locationInformationMemory #(
	parameter ADDR_WIDTH = 9,
	parameter RF_ADDR_WIDTH = 9,
	parameter NR_OF_PES = 16,
	parameter VIA_WIDTH = 5,
	parameter MUX_WIDTH = 4,
	parameter MEM_WIDTH = 25,
	parameter INIT_PATH = "NO_PATH",
	parameter USE_INIT_PATH = 0
)(
	input wire clk_i,
	input wire [ADDR_WIDTH-1:0] addr_i,
	output wire [NR_OF_PES-1:0] pe_selection_o,
	output wire [RF_ADDR_WIDTH-1:0] registerfile_addr_o,
	output wire [VIA_WIDTH-1:0] live_out_selection_o,
	output wire [MUX_WIDTH-1:0] mux_selection_o,
		// write Port
	input wire [ADDR_WIDTH-1:0]memory_write_addr_i,
	input wire [MEM_WIDTH-1:0] memory_line_i,
	input wire write_memory_en_i 
);


	reg [MEM_WIDTH-1:0] memory [1<<ADDR_WIDTH-1:0];
	reg [MEM_WIDTH-1:0] memory_out;

	always@(posedge clk_i) begin
		if(write_memory_en_i)
			memory[memory_write_addr_i] <= memory_line_i;
	end

	always@(posedge clk_i) begin
		memory_out = memory[addr_i];
	end

	assign {pe_selection_o, registerfile_addr_o} = memory_out;

	assign {live_out_selection_o, mux_selection_o} = memory_out[MEM_WIDTH-1:RF_ADDR_WIDTH];

	initial begin
		if(USE_INIT_PATH) begin
        		$readmemb(INIT_PATH, memory);
		end
	end

endmodule
