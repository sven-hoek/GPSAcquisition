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
 */

package target;

import java.util.Arrays;
import java.util.List;

import accuracy.Format;
import accuracy.Format.Integer;
import cgramodel.CgraModel;
import cgramodel.CgraModelAmidar;
import generator.VerilogGenerator;
import generator.VerilogGeneratorAmidar;
import io.AttributeParser;
import io.AttributeParserAmidar;
import io.AttributeWriter;
import io.AttributeWriterAmidar;
import operator.ADD;
import operator.CONST;
import operator.Compare;
import operator.Convert;
import operator.DIV;
import operator.Implementation;
import operator.Logic;
import operator.MUL;
import operator.MUX;
import operator.Memory;
import operator.NEG;
import operator.NOP;
import operator.Operator;
import operator.REM;
import operator.RegFile;
import operator.Relation;
import operator.SHL;
import operator.SHR;
import operator.SUB;
import operator.Trigonometric;
import operator.USHR;

/**
 * Accelerator for Java Virtual Machine with Synthesis.
 * <p>
 * The {@link Amidar} {@link Operator}s have fixed I/O precision {@link Format}s.
 * The {@link Amidar} data path width is fixed to 32 bit.
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class Amidar extends Processor<Amidar.OP> {

  /**
   * The singleton instance.
   */
  public static final Amidar Instance = new Amidar();

  /** Address for memory access */
  public static final Format A = new Integer(Instance.getDataPathWidth(), false);

  /** Java byte {@code Format} */
  public static final Format B = new Integer(8);

  /** Java char {@code Format} */
  public static final Format C = new Integer(16, false);

  /** Java short {@code Format} */
  public static final Format S = new Integer(16);

  /** Java int {@code Format} */
  public static final Format I = Format.INT;

  /** Java long {@code Format} */
  public static final Format L = Format.LONG;

  /** Java float {@code Format} */
  public static final Format F = Format.FLOAT;

  /** Java double {@code Format} */
  public static final Format D = Format.DOUBLE;
  
  /** Uninterpreted datapath word */
  public static final Format W = new Integer(Instance.getDataPathWidth());
  
  
  public static final int MAX_PES = 24;
  public static final int MAX_REGFILE_ADDR_WIDTH = 32 - MAX_PES;
  public static final int MUX_WIDTH = 5;
  public static final int VIA_WIDTH = 4;
  
  
  /**
   * List of {@code Operator}s supported by the {@code Amidar} {@code Processor}.
   * <p>
   * The semantic of the {@link Operator}s is described by using abbreviations of the interface signals
   * <ul>
   *   <li> R = RESULT_O
   *   <li> S = STATE_O
   *   <li> A = OP_A_I
   *   <li> B = OP_B_I
   *   <li> C = CBOX_I
   * </ul>
   * and the data formats
   * <ul>
   *   <li> W = word    = 32bit uninterpreted (see {@link Amidar#getDataPathWidth})
   *   <li> I = int     = 32bit integer
   *   <li> L = long    = 64bit integer
   *   <li> F = float   = 32bit floating point
   *   <li> D = double  = 64bit floating point
   *   <li> B = boolean =  1bit
   * </ul>
   * 
   * In case of a binary {@link Operator}, the second operand refers to the top-of-stack value. This conforms to the 
   * convention used by the Oracle documentation. 
   * @see    <a href='http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html#jvms-6.5'>JVM instruction set</a>
   *  
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  public static enum OP implements Operator {
    /** <a href='http://goo.gl/CFCf7o'>0x00</a> [W<-W]   R = A               */ NOP  (new NOP      (W)),
    /** <a href='http://goo.gl/JI9h7s'>0x60</a> [I<-IxI] R = A+B             */ IADD (new ADD      (I, I, I)),
    /** <a href='http://goo.gl/mgohFF'>0x64</a> [I<-IxI] R = A-B             */ ISUB (new SUB      (I, I, I)),
    /** <a href='http://goo.gl/ryHfRM'>0x68</a> [I<-IxI] R = A*B             */ IMUL (new MUL      (I, I, I)),
    /** <a href='http://goo.gl/EnQ7xO'>0x6C</a> [I<-IxI] R = A/B             */ IDIV (new DIV      (I, I, I)),
    /** <a href='http://goo.gl/2ImQpP'>0x70</a> [I<-IxI] R = A%B             */ IREM (new REM      (I, I, I)),
                                                                                                 
    /** <a href='http://goo.gl/xueGeC'>0x80</a> [I<-IxI] R = A|B             */ IOR  (new Logic.OR (I, I, I, false)),
    /** <a href='http://goo.gl/mKQ2uw'>0x7E</a> [I<-IxI] R = A&B             */ IAND (new Logic.AND(I, I, I, false)),
    /** <a href='http://goo.gl/2j9q7H'>0x82</a> [I<-IxI] R = A^B             */ IXOR (new Logic.XOR(I, I, I, false)),
    /** <a href='http://goo.gl/uhXPT0'>0x78</a> [I<-IxI] R = A&lt;&lt;B      */ ISHL (new SHL      (I, I, I)),
    /** <a href='http://goo.gl/1tIRcB'>0x7A</a> [I<-IxI] R = A>>B            */ ISHR (new SHR      (I, I, I)),
    /** <a href='http://goo.gl/uzG9IA'>0x7C</a> [I<-IxI] R = A>>>B           */ IUSHR(new USHR     (I, I, I)),
                                                                                                 
    /** <a href='http://goo.gl/s0Y9C7'>0x61</a> [L<-LxL] R = A+B             */ LADD (new ADD      (L, L, L)),
    /** <a href='http://goo.gl/agwsy2'>0x65</a> [L<-LxL] R = A-B             */ LSUB (new SUB      (L, L, L)),
    /** <a href='http://goo.gl/AkElEf'>0x69</a> [L<-LxL] R = A*B             */ LMUL (new MUL      (L, L, L)),
    /** <a href='http://goo.gl/uPSpb8'>0x6D</a> [L<-LxL] R = A/B             */ LDIV (new DIV      (L, L, L)),
    /** <a href='http://goo.gl/3nggZk'>0x71</a> [L<-LxL] R = A%B             */ LREM (new REM      (L, L, L)),
                                                                                                 
    /** <a href='http://goo.gl/1obfTH'>0x81</a> [L<-LxL] R = A|B             */ LOR  (new Logic.OR (L, L, L, false)),
    /** <a href='http://goo.gl/GNV4rK'>0x7F</a> [L<-LxL] R = A&B             */ LAND (new Logic.AND(L, L, L, false)),
    /** <a href='http://goo.gl/osMdbv'>0x83</a> [L<-LxL] R = A^B             */ LXOR (new Logic.XOR(L, L, L, false)),
    /** <a href='http://goo.gl/VfqAAB'>0x79</a> [L<-LxI] R = A&lt;&lt;B      */ LSHL (new SHL      (L, L, L)),
    /** <a href='http://goo.gl/oYWkAj'>0x7B</a> [L<-LxI] R = A>>B            */ LSHR (new SHR      (L, L, L)),
    /** <a href='http://goo.gl/SWs36a'>0x7D</a> [L<-LxI] R = A>>>B           */ LUSHR(new USHR     (L, L, L)),
    /** <a href='http://goo.gl/f0uU4k'>0x94</a> [I<-LXL] R = sgn(A-B)        */ LCMP (new Compare  (L, L, I)),
                                                                                     
    /** <a href='http://goo.gl/LyLOq9'>0x62</a> [F<-FxF] R = A+B             */  FADD(new ADD      (F, F, F)),
    /** <a href='http://goo.gl/A79oBz'>0x66</a> [F<-FxF] R = A-B             */  FSUB(new SUB      (F, F, F)),
    /** <a href='http://goo.gl/KIYEgi'>0x6A</a> [F<-FxF] R = A*B             */  FMUL(new MUL      (F, F, F)),
    /** <a href='http://goo.gl/cdl9SX'>0x6E</a> [F<-FxF] R = A/B             */  FDIV(new DIV      (F, F, F)),
                                                                                     
    /** <a href='http://goo.gl/jxwjUP'>0x63</a> [D<-DxD] R = A+B             */  DADD(new ADD      (D, D, D)),
    /** <a href='http://goo.gl/FcSegx'>0x67</a> [D<-DxD] R = A-B             */  DSUB(new SUB      (D, D, D)),
    /** <a href='http://goo.gl/piHnty'>0x6B</a> [D<-DxD] R = A*B             */  DMUL(new MUL      (D, D, D)),
    /** <a href='http://goo.gl/7LEVXb'>0x6F</a> [D<-DxD] R = A/B             */  DDIV(new DIV      (D, D, D)),
      
    /** 
     * Load 32bit value from local variable.
     * <p>
     * This combines 
     * {@code iload} (<a href='http://goo.gl/1dZ6ji'>0x15</a>) and
     * {@code fload} (<a href='http://goo.gl/39GIUk'>0x17</a>).
     * This {@link Operator} is just used for scheduling.                    */ LOAD       (new RegFile.LOAD(32)),
    
    /** 
     * Store 32bit value to local variable.
     * <p>
     * This combines 
     * {@code istore} (<a href='http://goo.gl/lI02pk'>0x36</a>) and
     * {@code fstore} (<a href='http://goo.gl/kx3ZP2'>0x38</a>).
     * This {@link Operator} is just used for scheduling.                    */ STORE      (new RegFile.STORE(32)),
     
    /** 
     * Load 64bit value from local variable.
     * <p>
     * This combines 
     * {@code lload} (<a href='http://goo.gl/QgvtHw'>0x16</a>) and
     * {@code dload} (<a href='http://goo.gl/svfmHM'>0x18</a>).
     * This {@link Operator} is just used for scheduling.                    */ LOAD64     (new RegFile.LOAD(64)),
     
    /** 
     * Store 64bit value to local variable.
     * <p>
     * This combines 
     * {@code lstore} (<a href='http://goo.gl/SoIQKn'>0x37</a>) and
     * {@code dstore} (<a href='http://goo.gl/c8Zd9k'>0x39</a>).
     * This {@link Operator} is just used for scheduling.                    */ STORE64    (new RegFile.STORE(64)),
      
    // These memory operations do not represent JVM instructions (TODO: rename DMA to CACHE)
    /** Fetch 32bit value       from cache (invalid on miss). */  CACHE_FETCH        (new Memory.FETCH(A,A, 32, false)),
    /** Load  32bit value       from cache (stall on miss).   */  DMA_LOAD           (new Memory.LOAD (A,A, 32, false)),
    /** Store 32bit value       to   cache.                   */  DMA_STORE          (new Memory.STORE(A,A, 32, false)),
    /** Fetch 64bit value       from cache (invalid on miss). */  CACHE_FETCH64      (new Memory.FETCH(A,A, 64, false)),
    /** Load  64bit value       from cache (stall on miss).   */  DMA_LOAD64         (new Memory.LOAD (A,A, 64, false)),
    /** Store 64bit value       to   cache.                   */  DMA_STORE64        (new Memory.STORE(A,A, 64, false)),
    /** Fetch 32bit array_entry from cache (invalid on miss). */  CACHE_ARRAY_FETCH  (new Memory.FETCH(A,A, 32, true)),
    /** Load  32bit array_entry from cache (stall on miss).   */  CACHE_ARRAY_LOAD   (new Memory.LOAD (A,A, 32, true)),
    /** Store 32bit array_entry to   cache.                   */  CACHE_ARRAY_STORE  (new Memory.STORE(A,A, 32, true)),
    /** Fetch 64bit array_entry from cache (invalid on miss). */  CACHE_ARRAY_FETCH64(new Memory.FETCH(A,A, 64, true)),
    /** Load  64bit array_entry from cache (stall on miss).   */  CACHE_ARRAY_LOAD64 (new Memory.LOAD (A,A, 64, true)),
    /** Store 64bit array_entry to   cache.                   */  CACHE_ARRAY_STORE64(new Memory.STORE(A,A, 64, true)),
    /** Load  32bit value       from ROM.                     */  ROM_LOAD           (new Memory.ROM  (A,A, 32, false)),
    /** Load  64bit value       from ROM.                     */  ROM_LOAD64         (new Memory.ROM  (A,A, 64, false)),
    /** Load  32bit array_entry from ROM.                     */  ROM_ARRAY_LOAD     (new Memory.ROM  (A,A, 32, true)),
    /** Load  64bit array_entry from ROM.                     */  ROM_ARRAY_LOAD64   (new Memory.ROM  (A,A, 64, true)),
    
    // The following six predicates are named as the IF<COND> JVM instructions 0x99 to 0x9E,
    // but behave as the IF_ICMP<COND> instructions 0x9F to 0xA4
    /**<a href='http://goo.gl/d7jNLc'>0x9F</a> [B<-IxI] S = A == B           */ IFEQ    (new Relation.EQ(I, I, true)),
    /**<a href='http://goo.gl/d7jNLc'>0xA0</a> [B<-IxI] S = A != B           */ IFNE    (new Relation.NE(I, I, true)),
    /**<a href='http://goo.gl/d7jNLc'>0xA1</a> [B<-IxI] S = A <  B           */ IFLT    (new Relation.LT(I, I, true)),
    /**<a href='http://goo.gl/d7jNLc'>0xA2</a> [B<-IxI] S = A >= B           */ IFGE    (new Relation.GE(I, I, true)),
    /**<a href='http://goo.gl/d7jNLc'>0xA3</a> [B<-IxI] S = A >  B           */ IFGT    (new Relation.GT(I, I, true)),
    /**<a href='http://goo.gl/d7jNLc'>0xA4</a> [B<-IxI] S = A <= B           */ IFLE    (new Relation.LE(I, I, true)),
                                                                                        
    /** binary 32bit compare which may     generate exception                */ CI_CMP    (new Compare(I, I, I, true)),
    /** binary 32bit compare which may not generate exception                */ HANDLE_CMP(new Compare(I, I, I)),
    /**<a href='http://goo.gl/HDRN4m'>0x96</a> [I<-FxF] R = sgn(A-B)         */ FCMPG     (new Compare(F, F, I)),
    /**<a href='http://goo.gl/HDRN4m'>0x95</a> [I<-FxF] R = sgn(A-B)         */ FCMPL     (new Compare(F, F, I)),
    /**<a href='http://goo.gl/Kr1ATU'>0x98</a> [I<-DxD] R = sgn(A-B)         */ DCMPG     (new Compare(D, D, I)),
    /**<a href='http://goo.gl/Kr1ATU'>0x97</a> [I<-DxD] R = sgn(A-B)         */ DCMPL     (new Compare(D, D, I)),
                                                                                                        
    /** 32bit Multiplexer                               R = C ? A : B        */ MUX     (new MUX        (I, I, I)),
    /** Load 32bit constant into REGFILE. Used for scheduling only.          */ CONST   (new CONST      (I)),
    /** Load 64bit constant into REGFILE. Used for scheduling only.          */ CONST64 (new CONST      (L)),
                                                                                                        
    /**<a href='http://goo.gl/lSooGm'>0x85</a> [L<-I] R = sExt64(A)          */ I2L     (new Convert    (I, L)),
    /**<a href='http://goo.gl/aGFKAP'>0x86</a> [F<-I] R = convert(A)         */ I2F     (new Convert    (I, F)),
    /**<a href='http://goo.gl/IpTGYR'>0x87</a> [D<-I] R = convert(A)         */ I2D     (new Convert    (I, D)),
    /**<a href='http://goo.gl/bN2KCR'>0x88</a> [I<-L] R = tunc32(A)          */ L2I     (new Convert    (L, I)),
    /**<a href='http://goo.gl/uTirB3'>0x89</a> [F<-L] R = convert(A)         */ L2F     (new Convert    (L, F)),
    /**<a href='http://goo.gl/jv5ssO'>0x8A</a> [D<-L] R = convert(A)         */ L2D     (new Convert    (L, D)),
    /**<a href='http://goo.gl/QgT9W5'>0x8B</a> [I<-F] R = convert(A)         */ F2I     (new Convert    (F, I)),
    /**<a href='http://goo.gl/qgt1Rn'>0x8C</a> [L<-F] R = convert(A)         */ F2L     (new Convert    (F, L)),
    /**<a href='http://goo.gl/dVo8cX'>0x8D</a> [D<-F] R = convert(A)         */ F2D     (new Convert    (F, D)),
    /**<a href='http://goo.gl/WhriZc'>0x8E</a> [I<-D] R = convert(A)         */ D2I     (new Convert    (D, I)),
    /**<a href='http://goo.gl/129QKe'>0x8F</a> [L<-D] R = convert(A)         */ D2L     (new Convert    (D, L)),
    /**<a href='http://goo.gl/lKI1k3'>0x90</a> [F<-D] R = convert(A)         */ D2F     (new Convert    (D, F)),
    /**<a href='http://goo.gl/FOmfyS'>0x91</a> [I<-I] R = sExt32(trunc8(A))  */ I2B     (new Convert    (I, B)),
    /**<a href='http://goo.gl/0XMoKs'>0x92</a> [I<-I] R = zExt32(trunc16(A)) */ I2C     (new Convert    (I, C)),
    /**<a href='http://goo.gl/y649q9'>0x93</a> [I<-I] R = sExt32(trunc16(A)) */ I2S     (new Convert    (I, S)),
                                                                                                        
    /**<a href='http://goo.gl/zPDTgw'>0x74</a> [I<-I] R = -A                 */ INEG    (new NEG        (I, I)),
    /**<a href='http://goo.gl/xbyjHu'>0x75</a> [L<-L] R = -A                 */ LNEG    (new NEG        (L, L)),
    /**<a href='http://goo.gl/6d1op6'>0x76</a> [F<-F] R = -A                 */ FNEG    (new NEG        (F, F)),
    /**<a href='http://goo.gl/tFD83j'>0x77</a> [D<-D] R = -A                 */ DNEG    (new NEG        (D, D)),
    
                                                                                FSIN    (new Trigonometric.SIN(F, F)),
                                                                                FCOS    (new Trigonometric.COS(F, F));
    
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
      //! we can not imp.fitLatency here, as the target.Processor.Instance might not have been configured yet
    }

    @Override
    public Implementation getImplementation() {
      return imp;
    }
    
/*
 * Restrict the design space exploration to default implementation
 **********************************************************************************************************************/
    
    @Override
    public boolean hasImplementation() {
      // we can not ask imp.isValidConfiguration, as imp.fitLatency has not been called
      return createDefaultImplementation().isValidConfiguration();
    }
    
    @Override
    public List<Implementation> getAllImplementations(int width) {
      Format f = imp.getCommonFormat();
      if (f == null) f = imp.getResultFormat();
      return (f == null || f.getBitWidth() != width) ? Arrays.asList() : Arrays.asList(createDefaultImplementation());
    }
    
    @Override
    public List<Implementation> getAllImplementations() {
      return getAllImplementations(1, 2*Amidar.Instance.getDataPathWidth());
    }
    
  }
  
/*
 * Implementation of the processor interface 
 **********************************************************************************************************************/

  /**
   * {@code Amidar} is a singleton.
   */
  protected Amidar() {
    super(OP.class);
  }
  
  @Override
  public int getDataPathWidth() {
    return 32;
  }
  
  @Override
  public boolean isStallable() {
    return true;
  }
  
  @Override
  public VerilogGenerator getGenerator(){
    return new VerilogGeneratorAmidar();
  }
  
  @Override
  public AttributeParser getAttributeParser(){
    return new AttributeParserAmidar();
  }
  
  @Override
  public AttributeWriter getAttributeWriter(){
    return new AttributeWriterAmidar();
  }
  
  @Override
  public CgraModel getEmptyCgraModel(){
    return new CgraModelAmidar();
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