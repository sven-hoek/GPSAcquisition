// (c) Copyright 1995-2017 Xilinx, Inc. All rights reserved.
// 
// This file contains confidential and proprietary information
// of Xilinx, Inc. and is protected under U.S. and
// international copyright and other intellectual property
// laws.
// 
// DISCLAIMER
// This disclaimer is not a license and does not grant any
// rights to the materials distributed herewith. Except as
// otherwise provided in a valid license issued to you by
// Xilinx, and to the maximum extent permitted by applicable
// law: (1) THESE MATERIALS ARE MADE AVAILABLE "AS IS" AND
// WITH ALL FAULTS, AND XILINX HEREBY DISCLAIMS ALL WARRANTIES
// AND CONDITIONS, EXPRESS, IMPLIED, OR STATUTORY, INCLUDING
// BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY, NON-
// INFRINGEMENT, OR FITNESS FOR ANY PARTICULAR PURPOSE; and
// (2) Xilinx shall not be liable (whether in contract or tort,
// including negligence, or under any other theory of
// liability) for any loss or damage of any kind or nature
// related to, arising under or in connection with these
// materials, including for any direct, or any indirect,
// special, incidental, or consequential loss or damage
// (including loss of data, profits, goodwill, or any type of
// loss or damage suffered as a result of any action brought
// by a third party) even if such damage or loss was
// reasonably foreseeable or Xilinx had been advised of the
// possibility of the same.
// 
// CRITICAL APPLICATIONS
// Xilinx products are not designed or intended to be fail-
// safe, or for use in any application requiring fail-safe
// performance, such as life-support or safety devices or
// systems, Class III medical devices, nuclear facilities,
// applications related to the deployment of airbags, or any
// other applications that could lead to death, personal
// injury, or severe property or environmental damage
// (individually and collectively, "Critical
// Applications"). Customer assumes the sole risk and
// liability of any use of Xilinx products in Critical
// Applications, subject only to applicable laws and
// regulations governing limitations on product liability.
// 
// THIS COPYRIGHT NOTICE AND DISCLAIMER MUST BE RETAINED AS
// PART OF THIS FILE AT ALL TIMES.
// 
// DO NOT MODIFY THIS FILE.


// IP VLNV: xilinx.com:ip:axi_protocol_checker:1.1
// IP Revision: 12

`timescale 1ns/1ps

(* DowngradeIPIdentifiedWarnings = "yes" *)
module axi_protocol_checker_write_64 (
  pc_status,
  pc_asserted,
  system_resetn,
  aclk,
  aresetn,
  pc_axi_awaddr,
  pc_axi_awlen,
  pc_axi_awsize,
  pc_axi_awburst,
  pc_axi_awlock,
  pc_axi_awcache,
  pc_axi_awprot,
  pc_axi_awqos,
  pc_axi_awregion,
  pc_axi_awvalid,
  pc_axi_awready,
  pc_axi_wlast,
  pc_axi_wdata,
  pc_axi_wstrb,
  pc_axi_wvalid,
  pc_axi_wready,
  pc_axi_bresp,
  pc_axi_bvalid,
  pc_axi_bready
);

output wire [96 : 0] pc_status;
output wire pc_asserted;
(* X_INTERFACE_INFO = "xilinx.com:signal:reset:1.0 system_resetn RST" *)
input wire system_resetn;
(* X_INTERFACE_INFO = "xilinx.com:signal:clock:1.0 aclk CLK" *)
input wire aclk;
(* X_INTERFACE_INFO = "xilinx.com:signal:reset:1.0 aresetn RST" *)
input wire aresetn;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI AWADDR" *)
input wire [31 : 0] pc_axi_awaddr;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI AWLEN" *)
input wire [7 : 0] pc_axi_awlen;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI AWSIZE" *)
input wire [2 : 0] pc_axi_awsize;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI AWBURST" *)
input wire [1 : 0] pc_axi_awburst;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI AWLOCK" *)
input wire [0 : 0] pc_axi_awlock;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI AWCACHE" *)
input wire [3 : 0] pc_axi_awcache;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI AWPROT" *)
input wire [2 : 0] pc_axi_awprot;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI AWQOS" *)
input wire [3 : 0] pc_axi_awqos;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI AWREGION" *)
input wire [3 : 0] pc_axi_awregion;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI AWVALID" *)
input wire pc_axi_awvalid;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI AWREADY" *)
input wire pc_axi_awready;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI WLAST" *)
input wire pc_axi_wlast;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI WDATA" *)
input wire [63 : 0] pc_axi_wdata;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI WSTRB" *)
input wire [7 : 0] pc_axi_wstrb;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI WVALID" *)
input wire pc_axi_wvalid;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI WREADY" *)
input wire pc_axi_wready;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI BRESP" *)
input wire [1 : 0] pc_axi_bresp;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI BVALID" *)
input wire pc_axi_bvalid;
(* X_INTERFACE_INFO = "xilinx.com:interface:aximm:1.0 PC_AXI BREADY" *)
input wire pc_axi_bready;

  axi_protocol_checker_v1_1_12_top #(
    .C_AXI_PROTOCOL(0),
    .C_AXI_ID_WIDTH(1),
    .C_AXI_DATA_WIDTH(64),
    .C_AXI_ADDR_WIDTH(32),
    .C_AXI_AWUSER_WIDTH(1),
    .C_AXI_ARUSER_WIDTH(1),
    .C_AXI_WUSER_WIDTH(1),
    .C_AXI_RUSER_WIDTH(1),
    .C_AXI_BUSER_WIDTH(1),
    .C_PC_MAXRBURSTS(2),
    .C_PC_MAXWBURSTS(64),
    .C_PC_EXMON_WIDTH(0),
    .C_PC_AW_MAXWAITS(0),
    .C_PC_AR_MAXWAITS(0),
    .C_PC_W_MAXWAITS(0),
    .C_PC_R_MAXWAITS(0),
    .C_PC_B_MAXWAITS(0),
    .C_PC_MESSAGE_LEVEL(2),
    .C_PC_SUPPORTS_NARROW_BURST(0),
    .C_PC_MAX_BURST_LENGTH(256),
    .C_PC_HAS_SYSTEM_RESET(1),
    .C_PC_STATUS_WIDTH(97)
  ) inst (
    .pc_status(pc_status),
    .pc_asserted(pc_asserted),
    .system_resetn(system_resetn),
    .aclk(aclk),
    .aresetn(aresetn),
    .pc_axi_awid(1'H0),
    .pc_axi_awaddr(pc_axi_awaddr),
    .pc_axi_awlen(pc_axi_awlen),
    .pc_axi_awsize(pc_axi_awsize),
    .pc_axi_awburst(pc_axi_awburst),
    .pc_axi_awlock(pc_axi_awlock),
    .pc_axi_awcache(pc_axi_awcache),
    .pc_axi_awprot(pc_axi_awprot),
    .pc_axi_awqos(pc_axi_awqos),
    .pc_axi_awregion(pc_axi_awregion),
    .pc_axi_awuser(1'H0),
    .pc_axi_awvalid(pc_axi_awvalid),
    .pc_axi_awready(pc_axi_awready),
    .pc_axi_wid(1'H0),
    .pc_axi_wlast(pc_axi_wlast),
    .pc_axi_wdata(pc_axi_wdata),
    .pc_axi_wstrb(pc_axi_wstrb),
    .pc_axi_wuser(1'H0),
    .pc_axi_wvalid(pc_axi_wvalid),
    .pc_axi_wready(pc_axi_wready),
    .pc_axi_bid(1'H0),
    .pc_axi_bresp(pc_axi_bresp),
    .pc_axi_buser(1'H0),
    .pc_axi_bvalid(pc_axi_bvalid),
    .pc_axi_bready(pc_axi_bready),
    .pc_axi_arid(1'H0),
    .pc_axi_araddr(32'H00000000),
    .pc_axi_arlen(8'D0),
    .pc_axi_arsize(3'D0),
    .pc_axi_arburst(2'D0),
    .pc_axi_arlock(1'D0),
    .pc_axi_arcache(4'D0),
    .pc_axi_arprot(3'D0),
    .pc_axi_arqos(4'D0),
    .pc_axi_arregion(4'D0),
    .pc_axi_aruser(1'H0),
    .pc_axi_arvalid(1'D0),
    .pc_axi_arready(1'D0),
    .pc_axi_rid(1'H0),
    .pc_axi_rlast(1'D1),
    .pc_axi_rdata(64'H0000000000000000),
    .pc_axi_rresp(2'D0),
    .pc_axi_ruser(1'H0),
    .pc_axi_rvalid(1'D0),
    .pc_axi_rready(1'D0)
  );
endmodule
