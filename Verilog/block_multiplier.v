// N bit block multiplier (using N/2 blocks)

`timescale 1 ns / 1 ps

module block_multiplier (
  input clk,
  input en,
  input signed[31:0] op_a,
  input signed[31:0] op_b,
  output reg signed [31:0] result
);

reg [31:0] low,mid1,mid2;


always@(*)begin
  result [15:0] = low [15:0];
  result [31:16] = low [31:16] + mid1[15:0] + mid2[15:0];
end

always@(posedge clk) begin
  if(en) begin
    low <= op_a[15:0] * op_b[15:0];
    mid1 <= op_a[31:16] * op_b[15:0];
    mid2 <= op_b[31:16] * op_a[15:0];
  end else begin
    low <= low;
    mid1 <= mid1;
    mid2 <= mid2;
  end
end

endmodule

