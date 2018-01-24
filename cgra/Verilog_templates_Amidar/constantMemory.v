module constantMemory #(
	parameter ADDR_WIDTH = 9,
	parameter INIT_PATH = "NO_PATH",
	parameter USE_INIT_PATH = 0
)(
	input wire clk_i,
	input wire [ADDR_WIDTH-1:0] addr_i,
	output wire [31:0] constant_o,
	// write Port
	input wire [ADDR_WIDTH-1:0] memory_write_addr_i,
	input wire [31:0] memory_line_i,
	input wire write_memory_en_i 

);


	reg [31:0] memory [1<<ADDR_WIDTH-1:0];
	reg [31:0] memory_out;

	always@(posedge clk_i) begin
		if(write_memory_en_i)
			memory[memory_write_addr_i] <= memory_line_i;
	end

	always@(posedge clk_i) begin
		memory_out = memory[addr_i];
	end

	assign constant_o = memory_out;

	initial begin
		if(USE_INIT_PATH) begin
        		$readmemb(INIT_PATH, memory);
		end
	end


endmodule
