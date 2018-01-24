module AXI_master_sim #
(
parameter integer C_S_AXI_ID_WIDTH	= 1,
parameter integer C_S_AXI_DATA_WIDTH = 32,
parameter integer C_S_AXI_ADDR_WIDTH	= 32
)
(
input wire CLK_I,
input wire RSTN_I,
input wire EN_I,

input wire WRITE_I,
input wire READ_I,
output wire READY_O,
output wire STARTED_O,
input wire [2-1:0] SETUPTYPE_I,
input wire [8-1:0] LEN_I,
input wire [C_S_AXI_ADDR_WIDTH-1 : 0] ADDR_I,
input wire [32-1:0] DATA_I,

// write address
output wire S_AXI_AWID,
output wire [8-1:0] S_AXI_AWLEN,
output wire [C_S_AXI_ADDR_WIDTH-1 : 0] S_AXI_AWADDR,
output wire [2 : 0] S_AXI_AWSIZE,
output wire [1 : 0] S_AXI_AWBURST,
output wire S_AXI_AWLOCK,
output wire [3 : 0] S_AXI_AWCACHE,
output wire [2 : 0] S_AXI_AWPROT,
output wire [3 : 0] S_AXI_AWQOS,
output wire [3 : 0] S_AXI_AWREGION,
output wire S_AXI_AWVALID,
input wire  S_AXI_AWREADY,
// write data
output wire [C_S_AXI_DATA_WIDTH-1 : 0] S_AXI_WDATA,
output wire [(C_S_AXI_DATA_WIDTH/8)-1 : 0] S_AXI_WSTRB,
output wire S_AXI_WLAST,
output wire S_AXI_WVALID,
input wire S_AXI_WREADY,
// write respose
output wire S_AXI_BREADY
);

assign S_AXI_AWID = 1'b0;
assign S_AXI_AWSIZE = 3'b010;
assign S_AXI_AWBURST = 2'b01;
assign S_AXI_AWLOCK = 1'b0;
assign S_AXI_AWCACHE = 4'b0;
assign S_AXI_AWPROT = 3'b0;
assign S_AXI_AWQOS = 3'b0;
assign S_AXI_AWREGION = 3'b0;
assign S_AXI_WSTRB = 4'b1111;

reg [8-1:0] count;
reg [8-1:0] length;
reg [2-1:0] setuptype;
reg idle;
reg datawasfirst;
reg addrwasfirst;
reg finished;
reg started;

assign READY_O = finished || idle;
assign STARTED_O = started;

reg wvalid;
reg awvalid;
reg [C_S_AXI_ADDR_WIDTH-1:0] awaddr;
reg [C_S_AXI_DATA_WIDTH-1:0] wdata;
reg wlast;
reg bready;

assign S_AXI_WVALID = wvalid;
assign S_AXI_AWVALID = awvalid;
assign S_AXI_AWADDR = awaddr;
assign S_AXI_AWLEN = length;
assign S_AXI_WDATA = wdata;
assign S_AXI_WLAST = wlast;
assign S_AXI_BREADY = bready;

parameter START_ADDR_FIRST = 0,
					START_DATA_FIRST = 1,
					START_DATA_ADDR = 2;

reg address_done;
reg data_done;
reg last_finished;

always @(posedge CLK_I) begin
	if (~RSTN_I) begin
		address_done <= 1'b0;
		data_done <= 1'b0;
		last_finished <= 1'b0;
	end 
	else if (1/*EN_I*/) begin
		address_done <= awvalid && S_AXI_AWREADY;
		data_done <= wvalid && S_AXI_WREADY;
		last_finished <= finished;
	end
end

always @(*) begin
	if (length == 0) begin
		case (setuptype)
			START_ADDR_FIRST: begin
				finished = (~last_finished && address_done) && wvalid && S_AXI_WREADY;
			end
			START_DATA_FIRST: begin
				finished = (~last_finished && data_done) && awvalid && S_AXI_AWREADY;
			end
			default: begin
				finished = (awvalid && S_AXI_AWREADY && wvalid && S_AXI_WREADY) ||
						 ( (~last_finished && data_done) && awvalid && S_AXI_AWREADY) ||
						 ( (~last_finished && address_done) && wvalid && S_AXI_WREADY);
			end
		endcase
	end else begin
		finished = length == count && S_AXI_WREADY && wvalid;
	end
end

// com block 
always @(posedge CLK_I) begin
	if (~RSTN_I) begin
		awaddr <= 0;
		wdata <= 0;
		length <= 0;
		setuptype <= 0;
		started <= 1'b0;
	end 
	else if (1/*EN_I*/) begin
		if ( idle || finished ) begin
			awaddr <= ADDR_I;
			wdata <= DATA_I;
			length <= LEN_I;
			setuptype <= SETUPTYPE_I;
			started <= 1'b1;
		end
		else if (S_AXI_WREADY && S_AXI_WVALID) begin
			wdata <= DATA_I;
			started <= 1'b0; 
		end else begin
			started <= 1'b0; 
		end
	end
end

// burst control 
always @(posedge CLK_I) begin
	if (~RSTN_I) begin
		count <= 0;
		wlast <= 1'b0;
		idle <= 1'b1;
		bready <= 1'b1;
	end 
	else if (1/*EN_I*/) begin
		if ( idle || finished ) begin
			if (WRITE_I) begin
				count <= 0;
				idle <= 1'b0;
				wlast <= LEN_I == 0 ? 1'b1 : 1'b0;
			end else begin
				idle <= 1'b1;
				wlast <= 1'b0;
			end
		end else if (S_AXI_WREADY && S_AXI_WVALID) begin
			if (count == length - 1)
				wlast <= 1'b1;
			else 
				wlast <= 1'b0;

			if (count == length) begin
				idle <= 1'b1;
			end else begin
				count <= count + 1;
			end
		end
	end
end

// awvalid gen
always @(posedge CLK_I) begin
	if (~RSTN_I) begin
		awvalid <= 1'b0;
		datawasfirst <= 1'b0;
	end 
	else if (1/*EN_I*/) begin
		if ( idle || finished ) begin
			if (WRITE_I) begin
				if (START_DATA_FIRST == SETUPTYPE_I) begin
					awvalid <= 1'b0;
					datawasfirst <= 1'b1;
				end else begin
					awvalid <= 1'b1;
					datawasfirst <= 1'b0;
				end
			end else begin
				awvalid <= 1'b0;
			end
		end else
			if (datawasfirst) begin
				datawasfirst <= 1'b0;
				awvalid <= 1'b1;
			end else if (S_AXI_AWREADY) begin
				datawasfirst <= 1'b0;
				awvalid <= 1'b0;
			end
	end
end

// wvalid gen
always @(posedge CLK_I) begin
	if (~RSTN_I) begin
		wvalid <= 1'b0;
		addrwasfirst <= 1'b0;
	end 
	else if (1/*EN_I*/) begin
		if (idle || finished ) begin
			if (WRITE_I) begin
				if (START_ADDR_FIRST == SETUPTYPE_I) begin
					addrwasfirst <= 1'b1;
					wvalid <= 1'b0;
				end else begin
					addrwasfirst <= 1'b0;
					wvalid <= 1'b1;
				end
			end else begin
				wvalid <= 1'b0;
			end
		end else if (addrwasfirst) begin
			wvalid <= 1'b1;
			addrwasfirst <= 1'b0;
		end else if (count == length && S_AXI_WREADY) begin
			wvalid <= 1'b0;
			addrwasfirst <= 1'b0;
		end
	end
end

endmodule