`include "axiinterface.vh"

module ResultMaster #
(
	parameter NEW_TRANSACTION_SET_DATA_WIDTH = `MASTER_DATA_WIDTH
)
(
	input wire CGRA_CLK_I,
	input wire AXI_ACLK_I,
	input wire RST_N_I,
	input wire EN_I,
	//
	// --- Target address selection ports:
	//
	input wire DEST_WREN_I,
	input wire DEST_BOUND_WREN_I,
	input wire DEST_INC_WREN_I,
	input wire [`SYSTEM_ADDR_WIDTH-1:0] DEST_DATA_I,
	//
	// Control ports:
	//
	input wire START_TRANSACTION_I,
	input wire START_IS_NEW_TRANSACTION_SET_I,
	input wire INCREMENT_TARGET_ADDR_I,
	input wire [8-1:0] START_TRANSACTION_LENGTH_I,
	input wire [`MASTER_DATA_WIDTH-1:0] DATA_TO_SEND_I,
	// Data to be send on every new set of transactions
	input wire [NEW_TRANSACTION_SET_DATA_WIDTH-1:0] NEW_TRANSACTION_SET_DATA_I,
	//
	// Status ports:
	//
	output wire MORE_DATA_REQUEST_O,
	output wire DATA_TRANSFER_COMPLETED_O,
	output wire WAITING_FOR_DATA_TRANSFER_O,
	output wire WAITING_FOR_ADDR_TRANSFER_O,
	//
	// AXI ports:
	//
	output wire [8-1:0] AXI_AWLEN_O,
	output wire [3-1:0] AXI_AWSIZE_O,
	output wire [2-1:0] AXI_AWBURST_O,
	output wire [`SYSTEM_ADDR_WIDTH-1:0] AXI_AWADDR_O,
	output wire AXI_AWVALID_O,
	input wire  AXI_AWREADY_I,
	output wire [`MASTER_DATA_WIDTH-1:0] AXI_WDATA_O,
	output wire [(`MASTER_DATA_WIDTH/8)-1:0] AXI_WSTRB_O,
	output wire AXI_WLAST_O,
	output wire AXI_WVALID_O,
	input wire AXI_WREADY_I,
	output wire AXI_BREADY_O,
	input wire AXI_BVALID_I,
	input wire [2-1:0] AXI_BRESP_I
);

//
// --- Transaction process related signals:
//
wire [`MASTER_DATA_WIDTH-1:0] wdata;
reg [8-1:0] transfer_counter;
reg [8-1:0] transaction_length;
reg awvalid;
reg wvalid;
reg wlast;
reg wvalid_was_deasserted;

wire awready;
wire wready;
wire bvalid;

assign MORE_DATA_REQUEST_O = (wvalid && wready && ~wlast) || 
							 (awvalid && awready && ~START_IS_NEW_TRANSACTION_SET_I) || 
							  wvalid_was_deasserted;
assign DATA_TRANSFER_COMPLETED_O = wvalid && wready;
assign WAITING_FOR_DATA_TRANSFER_O = wvalid;
assign WAITING_FOR_ADDR_TRANSFER_O = awvalid;

//
// --- Target address related signals:
//
// Used to start transactions (incremented by transfer size)
reg [`SYSTEM_ADDR_WIDTH-1:0] awaddr; 
// Incremented by awaddr_inc, assigned to awaddr on each completed transaction set
reg [`SYSTEM_ADDR_WIDTH-1:0] transaction_set_start_addr; 
reg [`SYSTEM_ADDR_WIDTH-1:0] awaddr_inc;
reg [`SYSTEM_ADDR_WIDTH-1:0] awaddr_lower; 
reg [`SYSTEM_ADDR_WIDTH-1:0] awaddr_upper;
reg reset_addr;

//
// --- Transaction process:
//

assign wdata = /*START_TRANSACTION_I && */START_IS_NEW_TRANSACTION_SET_I ? 
			   NEW_TRANSACTION_SET_DATA_I : DATA_TO_SEND_I;

always @(posedge CGRA_CLK_I) begin
	if (~RST_N_I) begin
		awvalid <= 1'b0;
		wvalid <= 1'b0;
		wlast <= 1'b0;
		transfer_counter <= 0;
		transaction_length <= 0;
		wvalid_was_deasserted <= 1'b0;
	end else begin
		// wlast, wvalid, transfer length and counter
		if (wvalid && wready) begin // used during a log transaction
			wlast <= transfer_counter == transaction_length - 1;
			wvalid <= ~wlast && EN_I; // assert if not done
			transfer_counter <= transfer_counter + 1;
			wvalid_was_deasserted <= ~EN_I;
		end else if (START_TRANSACTION_I) begin // at the start of a log transaction
			wlast <= START_TRANSACTION_LENGTH_I == 0;
			wvalid <= START_IS_NEW_TRANSACTION_SET_I; // only directly valid if writing tag data
			transfer_counter <= 0;
			transaction_length <= START_TRANSACTION_LENGTH_I; // if (c_out_awvalid) -> context_out[8-1:0] == the transaction_length to use, for the burst to setup;
		end else if (awvalid && awready) begin
			wvalid <= EN_I; // assert wvalid here if no tag data was written
			wvalid_was_deasserted <= ~EN_I;
		end else if (EN_I && wvalid_was_deasserted) begin
			wvalid <= 1'b1; // get back on track
			wvalid_was_deasserted <= 1'b0;
		end

		// awvalid
		if (START_TRANSACTION_I)
			awvalid <= 1'b1;
		else if (awready)
			awvalid <= 1'b0;
	end
end

//
// --- Target address handling:
//
always @(posedge CGRA_CLK_I) begin
	if (~RST_N_I) begin
		reset_addr <= 1'b0;
		awaddr <= 0;
		transaction_set_start_addr <= 0;
		awaddr_inc <= 0;
		awaddr_lower <= 0;
		awaddr_upper <= 0;
	end else if (EN_I) begin
		if (DEST_WREN_I) begin
			awaddr_lower <= DEST_DATA_I[`SYSTEM_ADDR_WIDTH-1:0];
			reset_addr <= 1'b1;
		end else if (DEST_BOUND_WREN_I) begin
			awaddr_upper <= DEST_DATA_I[`SYSTEM_ADDR_WIDTH-1:0];
			reset_addr <= 1'b1;
		end else if (DEST_INC_WREN_I) begin
			awaddr_inc <= DEST_DATA_I[`SYSTEM_ADDR_WIDTH-1:0];
			reset_addr <= 1'b1;
		end else begin
			reset_addr <= 1'b0;
		end

		if (reset_addr) begin
			awaddr <= awaddr_lower;
			transaction_set_start_addr <= awaddr_lower + awaddr_inc;
		end else if (INCREMENT_TARGET_ADDR_I) begin
			awaddr <= transaction_set_start_addr;

			if (transaction_set_start_addr + (awaddr_inc << 1) <= awaddr_upper)
				transaction_set_start_addr <= transaction_set_start_addr + awaddr_inc;
			else
				transaction_set_start_addr <= awaddr_lower;
		end else if (wvalid && wready) begin
			awaddr <= awaddr + `MASTER_DATA_WIDTH/8;
		end
	end
end

axi_clock_converter masterClkConverter (
  .m_axi_aclk(AXI_ACLK_I),
  .m_axi_aresetn(RST_N_I),
  .s_axi_aclk(CGRA_CLK_I),
  .s_axi_aresetn(RST_N_I),
	// .m_aclk(AXI_ACLK_I),
 //  .s_aclk(CGRA_CLK_I),
 //  .s_aresetn(RST_N_I),
  .s_axi_awburst(2'b01),
  .s_axi_awaddr(awaddr),
  .s_axi_awsize(3'b011),
  .s_axi_awlen(transaction_length),
  .s_axi_awvalid(awvalid),
  .s_axi_awready(awready),
  .s_axi_wdata(wdata),
  .s_axi_wstrb(8'hff),
  .s_axi_wlast(wlast),
  .s_axi_wvalid(wvalid),
  .s_axi_wready(wready),
  .s_axi_bvalid(bvalid),
  .s_axi_bready(1'b1),
  .m_axi_awburst(AXI_AWBURST_O),
  .m_axi_awaddr(AXI_AWADDR_O),
  .m_axi_awsize(AXI_AWSIZE_O),
  .m_axi_awlen(AXI_AWLEN_O),
  .m_axi_awvalid(AXI_AWVALID_O),
  .m_axi_awready(AXI_AWREADY_I),
  .m_axi_wdata(AXI_WDATA_O),
  .m_axi_wstrb(AXI_WSTRB_O),
  .m_axi_wlast(AXI_WLAST_O),
  .m_axi_wvalid(AXI_WVALID_O),
  .m_axi_wready(AXI_WREADY_I),
  .m_axi_bvalid(AXI_BVALID_I),
  .m_axi_bready(AXI_BREADY_O),
  .m_axi_bresp(AXI_BRESP_I),
  .s_axi_awlock(1'b0),
  .s_axi_awcache(4'b0),
  .s_axi_awprot(3'b010),
  .s_axi_awqos(4'b0),
  .s_axi_awregion(4'b0),
  .s_axi_bresp(),
  .m_axi_awlock(),
  .m_axi_awcache(),
  .m_axi_awprot(),
  .m_axi_awqos(),
  .m_axi_awregion()
);

endmodule
