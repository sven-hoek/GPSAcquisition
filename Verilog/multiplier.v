// Unsigned/Signed multiplication based on Patterson and Hennessy's algorithm.
// Copyrighted 2002 by studboy-ga / Google Answers.  All rights reserved.
// Description: Calculates product.  The "sign" input determines whether
//              signs (two's complement) should be taken into consideration.

module multiply(result,multiplier,multiplicand,sign,clk,enable,start); 

   input         clk;
   input 	 enable;
   input 	start;
   input         sign;
   input [31:0]  multiplier, multiplicand;
   output [32:0] result;


   reg [63:0]    product, product_temp;
   assign result = product[31:0];
   reg [31:0]    multiplier_copy;
   reg [63:0]    multiplicand_copy;
   reg           negative_output;
   
   reg [5:0]     bit; 
   wire          ready = !bit;

   initial bit = 0;
   initial negative_output = 0;

   always @( posedge clk)

     if( ready && enable && start) begin

        bit               = 6'd32;
        product           = 0;
        product_temp      = 0;
        multiplicand_copy = (!sign || !multiplicand[31]) ? 
                            { 32'd0, multiplicand } : 
                            { 32'd0, ~multiplicand + 1'b1};
        multiplier_copy   = (!sign || !multiplier[31]) ?
                            multiplier :
                            ~multiplier + 1'b1; 

        negative_output = sign && 
                          ((multiplier[31] && !multiplicand[31]) 
                        ||(!multiplier[31] && multiplicand[31]));
        
     end 
     else if ( bit > 0  && enable) begin

        if( multiplier_copy[0] == 1'b1 ) product_temp = product_temp + multiplicand_copy;

        product = (!negative_output) ? 
                  product_temp : 
                  ~product_temp + 1'b1;

        multiplier_copy = multiplier_copy >> 1;
        multiplicand_copy = multiplicand_copy << 1;
        bit = bit - 1'b1;

     end
endmodule
