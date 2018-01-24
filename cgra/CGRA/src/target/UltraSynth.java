package target;

import accuracy.Format;
import accuracy.Format.Integer;
import cgramodel.CgraModel;
import cgramodel.CgraModelUltrasynth;
import generator.VerilogGenerator;
import generator.VerilogGeneratorUltrasynth;
import io.AttributeParser;
import io.AttributeParserUltrasynth;
import io.AttributeWriter;
import io.AttributeWriterUltrasynth;
import operator.ABS;
import operator.ADD;
import operator.CONST;
import operator.Compare;
import operator.DIV;
import operator.EXP;
import operator.Implementation;
import operator.Logarithm;
import operator.Logic;
import operator.MOD;
import operator.MUL;
import operator.MUX;
import operator.Memory;
import operator.NEG;
import operator.NOP;
import operator.NOT;
import operator.Operator;
import operator.POW;
import operator.RAND;
import operator.REM;
import operator.ROL;
import operator.ROR;
import operator.RegFile;
import operator.Relation;
import operator.SGN;
import operator.SHL;
import operator.SHR;
import operator.SQR;
import operator.SQRT;
import operator.SUB;
import operator.Select;
import operator.Trigonometric;

/**
 * Accelerator for iXtronics CAMeLView applications.
 * <p>
 * The I/O precision of the {@link UltraSynth} {@link Operator}s can be configured with a wide range of numeric 
 * {@link Format}s. The {@link UltraSynth} data path width is also adjustable.
 * 
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 * @see    <a href='http://www.ixtronics.de/21/index.html'>iXtronics CAMeLView</a>
 */
public class UltraSynth extends Processor<UltraSynth.OP> {
  
  /** The singleton instance. */
  public static final UltraSynth Instance = new UltraSynth();
  
  /** Address for memory access. */
  public static final Format A = new Integer(Instance.getDataPathWidth(), false);

  /** Uninterpreted datapath word. */
  public static final Format W = new Integer(Instance.getDataPathWidth());
  
  public static final int MAX_PES = 24;
  public static final int MAX_REGFILE_ADDR_WIDTH = 32 - MAX_PES;
  public static final int MUX_WIDTH = 5;
  public static final int VIA_WIDTH = 4;
    
    
  /**
   * List of {@code Operator}s supported by the {@code UltraSynth} CGRA.
   * <p>
   * The semantic of the {@link OP}s is described by using abbreviations of the interface signals:
   * <ul>
   *   <li> R = RESULT_O
   *   <li> S = STATE_O
   *   <li> A = OP_A_I
   *   <li> B = OP_B_I
   *   <li> C = CBOX_I
   * </ul>
   * and the data formats
   * <ul>
   *   <li> W = word    = 64bit uninterpreted (see {@link #getDataPathWidth})
   *   <li> A = address = 64bit unsigned      (see {@link #getDataPathWidth})
   * </ul>
   *  
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  public enum OP implements Operator {
    
    // binary arithmetic
    /** addition:           R = A  +  B; no overflow trap                     */  ADD(new ADD          (W, W, W)),
    /** subtraction:        R = A  -  B; no overflow trap                     */  SUB(new SUB          (W, W, W)),
    /** multiplication:     R = A  *  B; no overflow trap                     */  MUL(new MUL          (W, W, W)),
    /** division:           R = A  /  B; no division by zero trap (=> R := 0) */  DIV(new DIV          (W, W, W)),
    /** modulo:             R = A  %  B; no division by zero trap (=> R := x) */  MOD(new MOD          (W, W, W)),
    /** remainder:          R = A rem B; no division by zero trap (=> R := x) */  REM(new REM          (W, W, W)),
    /** power:              R = A **  B;                                      */  POW(new POW          (W, W, W)),
    /** minimum:            R = A  <  B ? A : B;                              */  MIN(new Select.MIN   (W, W, W)),
    /** maximum:            R = A  <  B ? B : A;                              */  MAX(new Select.MAX   (W, W, W)),

    // binary comparisons (with data or status output)                                                           
    /** equal:              R = A == B;                                       */  EQ  (new Relation.EQ (W, W, false)),
    /** not equal:          R = A != B;                                       */  NE  (new Relation.NE (W, W, false)),
    /** less than:          R = A <  B;                                       */  LT  (new Relation.LT (W, W, false)),
    /** less or equal:      R = A <= B;                                       */  LE  (new Relation.LE (W, W, false)),
    /** greater than:       R = A >  B;                                       */  GT  (new Relation.GT (W, W, false)),
    /** greater or equal:   R = A >= B;                                       */  GE  (new Relation.GE (W, W, false)),
    /** equal:              S = A == B;                                       */  IFEQ(new Relation.EQ (W, W, true)),
    /** not equal:          S = A != B;                                       */  IFNE(new Relation.NE (W, W, true)),
    /** less than:          S = A <  B;                                       */  IFLT(new Relation.LT (W, W, true)),
    /** less or equal:      S = A <= B;                                       */  IFLE(new Relation.LE (W, W, true)),
    /** greater than:       S = A >  B;                                       */  IFGT(new Relation.GT (W, W, true)),
    /** greater or equal:   S = A >= B;                                       */  IFGE(new Relation.GE (W, W, true)),
    /** compare:            R = A <=> B;                                      */  CMP (new Compare     (W, W, W)),
    
    // binary bitwise logic                                                                            
    /** or:                 R = A | B;                                        */  OR (new Logic.OR     (W, W, W,false)),
    /** and:                R = A & B;                                        */  AND(new Logic.AND    (W, W, W,false)),
    /** xor:                R = A ^ B;                                        */  XOR(new Logic.XOR    (W, W, W,false)),
    /** shift left:         R = A << B;  no rotation                          */  SHL(new SHL          (W, W, W)),
    /** shift right:        R = A >> B;  no rotation                          */  SHR(new SHR          (W, W, W)),
    /** rotate left:        R = A << B   | MSBit(A);                          */  ROL(new ROL          (W, W, W)),
    /** rotate right:       R = LSBit(A) | A >> B;                            */  ROR(new ROR          (W, W, W)),
    
    // unary operations
    /** identity:           R = A;                                            */  NOP  (new NOP             (W)),
    /** absolute:           R = A < 0 ? -A : A;                               */  ABS  (new ABS             (W, W)),
    /** negate:             R = - A;                                          */  NEG  (new NEG             (W, W)),
    /** bitwise not:        R = !A;                                           */  NOT  (new NOT             (W, W)),
    /** signum:             R = A < 0 ? 1  : 0;                               */  SGN  (new SGN             (W, W)),
    // /** signum2:            R = A < 0 ? 1  : 0; TODO: what's the difference?  */  SGN2 (new SGN             (W, W)), Naming problem with SGN, see above, see previous EQ and IFEQ problem
    /** square              R = A * A;                                        */  SQR  (new SQR             (W, W)),
    /** square root         R = A ** 0.5;                                     */  SQRT (new SQRT            (W, W)),
    /** exponentiation:     R = e ** A;                                       */  EXP  (new EXP             (W, W)),
    /** binary  logarithm:  R = lg_2(A);                                      */  LOG  (new Logarithm.LOG   (W, W)),
    /** natural logarithm:  R = lg_e(A);                                      */  LN   (new Logarithm.LN    (W, W)),
    /** decimal logarithm:  R = lg_10(A);                                     */  LOG10(new Logarithm.LOG10 (W, W)),

    // unary trigonometric operations
    /** sine:                         R = sin(A);                             */  SIN  (new Trigonometric. SIN (W, W)),
    /** cosine:                       R = cos(A);                             */  COS  (new Trigonometric. COS (W, W)),
    /** tangent:                      R = tan(A);                             */  TAN  (new Trigonometric. TAN (W, W)),
    /** cotangent:                    R = cot(A);                             */  COT  (new Trigonometric. COT (W, W)),
    /** hyperbolic sine:              R = sinh(A);                            */  SINH (new Trigonometric. SINH(W, W)),
    /** hyperbolic cosine:            R = cosh(A);                            */  COSH (new Trigonometric. COSH(W, W)),
    /** hyperbolic tangent:           R = tanh(A);                            */  TANH (new Trigonometric. TANH(W, W)),
    /** hyperbolic cotangent:         R = coth(A);                            */  COTH (new Trigonometric. COTH(W, W)),
    /** arc sine:                     R = asin(A);                            */  ASIN (new Trigonometric.ASIN (W, W)),
    /** arc cosine:                   R = acos(A);                            */  ACOS (new Trigonometric.ACOS (W, W)),
    /** arc tangent:                  R = atan(A);                            */  ATAN (new Trigonometric.ATAN (W, W)),
    /** arc cotangent:                R = acot(A);                            */  ACOT (new Trigonometric.ACOT (W, W)),
    /** arc hyperbolic sine:          R = asinh(A);                           */  ASINH(new Trigonometric.ASINH(W, W)),
    /** arc hyperbolic cosine:        R = acosh(A);                           */  ACOSH(new Trigonometric.ACOSH(W, W)),
    /** arc hyperbolic tangent:       R = atanh(A);                           */  ATANH(new Trigonometric.ATANH(W, W)),
    /** arc hyperbolic cotangent:     R = acoth(A);                           */  ACOTH(new Trigonometric.ACOTH(W, W)),
    
    // zero input operations
    /** (pseudo) random               R = rand()                              */  RAND (new RAND (W)),
    /** constant                      R = fixed value                         */  CONST(new CONST(W)),
    
    // control flow  
    /** CBOX controlled multiplexer   R = C ? A : B;                          */  MUX  (new MUX(W, W, W)),
    
    // memory access (no operands, no results, data and addressing is handled externally)
    /** load from local variable                          */  LOAD      (new RegFile.LOAD( W.getBitWidth())),
    /** store to  local variable                          */  STORE     (new RegFile.STORE(W.getBitWidth())),
    /** load from external ROM (aka Kennlinienfeld)       */  ROM_LOAD  (new Memory.ROM(A,A,W.getBitWidth(),false));
    

    /**
     * Default {@code Implementation} of this {@code Operator}.
     */
    private Implementation imp;
    
    /**
     * Associate this {@code Operator} with a specific {@code Implementation}.
     * @param imp the underlying {@link Implementation} of this {@link Operator}.
     */
    private OP(Implementation imp) {
      this.imp = imp;
    }
    
    @Override
    public Implementation getImplementation() {
      return imp;
    }
    
/*
 * Static (implementation independent) operator properties
 **********************************************************************************************************************/

    
  }
  
/*
 * Implementation of the processor interface 
 **********************************************************************************************************************/

  /**
   * {@code UltraSynth} is a singleton.
   */
  protected UltraSynth() {
    super(OP.class);
  }
  
  @Override
  public int getDataPathWidth() {
    return 32;
  }
  
  @Override
  public boolean isStallable() {
    return false;
  }
  
  @Override
  public VerilogGenerator getGenerator() {
    return new VerilogGeneratorUltrasynth();
  }

  @Override
  public AttributeParser getAttributeParser() {
    return new AttributeParserUltrasynth();
  }
  
  @Override
  public AttributeWriter getAttributeWriter(){
    return new AttributeWriterUltrasynth();
  }
  
  public CgraModel getEmptyCgraModel(){
    return new CgraModelUltrasynth();
  }
  
  public String getExternalIPPath() {
    return getRelativeSubPath("Xilinx_ip");
  }

}


/*
 * Copyright (c) 2016,
 * Embedded Systems and Applications Group,
 * Department of Computer Science,
 * TU Darmstadt,
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *    disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the institute nor the names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **********************************************************************************************************************/