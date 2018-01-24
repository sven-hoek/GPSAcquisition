module kernelTableCGRA #(
	parameter ADDR_WIDTH = 9,
	parameter CONST_POINTER_WIDTH = 9,
	parameter LOCATION_INFORMATION_POINTER_WIDTH = 9,
	parameter CONTEXT_POINTER_WIDTH = 9,
	parameter INIT_PATH = "NO_PATH",
	parameter USE_INIT_PATH = 0
) (
	input wire clk_i,
	input wire [ADDR_WIDTH-1:0] addr_i,
	output wire [CONST_POINTER_WIDTH-1:0] nr_of_constants_o,
	output wire [CONST_POINTER_WIDTH-1:0]constants_pointer_o,
	output wire [LOCATION_INFORMATION_POINTER_WIDTH-1:0] locationInformation_pointer_o,
	output wire [CONTEXT_POINTER_WIDTH-1:0] context_pointer_o, 
	// write Port
	input wire [ADDR_WIDTH-1:0]memory_write_addr_i,
	input wire [2*CONST_POINTER_WIDTH+LOCATION_INFORMATION_POINTER_WIDTH+CONTEXT_POINTER_WIDTH-1:0] memory_line_i,
	input wire write_memory_en_i 

);


	reg [2*CONST_POINTER_WIDTH+LOCATION_INFORMATION_POINTER_WIDTH+CONTEXT_POINTER_WIDTH-1:0] memory [1<<ADDR_WIDTH-1:0];
	reg [2*CONST_POINTER_WIDTH+LOCATION_INFORMATION_POINTER_WIDTH+CONTEXT_POINTER_WIDTH-1:0] memory_out;

	always@(posedge clk_i) begin
		if(write_memory_en_i)
			memory[memory_write_addr_i] <= memory_line_i;
	end

	always@(*) begin
		memory_out = memory[addr_i];
	end

	assign {nr_of_constants_o, constants_pointer_o, locationInformation_pointer_o, context_pointer_o} = memory_out;

	initial begin
		if(USE_INIT_PATH) begin
        		$readmemb(INIT_PATH, memory);
		end
	end

endmodule
