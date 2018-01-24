package operator;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import accuracy.Format;


/**
 * Common interface for the operator enumerations of all
 * {@code target.Processor}s.
 * <p>
 * By implementing this interface, the {@link Operator}s are linked to their
 * (Verilog) {@link Implementation}s.
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public interface Operator {
  
  /**
   * {@code Comparator} used to sort {@code Operator} lists by name. The
   * {@link Operator} interface can not extend {@link Comparable} due to a
   * conflict for the generic types of {@link target.Processor}.
   */
  public static final Comparator<Operator> COMPARATOR = new Comparator<Operator>() {
    @Override
    public int compare(Operator a, Operator b) {
      if (a == null && b == null) {
        return 0;
      }
      if (a == null) {
        return -1;
      }
      if (b == null) {
        return 1;
      }
      return a.toString().compareTo(b.toString());
    }
  };
 
  
/*
 * Interface between operators and their (Verilog) implementation
 **********************************************************************************************************************/
  
  /**
   * Get the underlying {@code Implementation} of this {@code Operator}.
   * 
   * This I/O accuracy of this {@link Implementation} must not be adjusted. 
   * Use {@link #createDefaultImplementation} to get a modifiable {@link Implementation}.
   *  
   * @return the underlying {@link Implementation}
   */
  public Implementation getImplementation();
  
  /**
   * Create the default {@code Implementation} of this {@code Operator}.
   * <p>
   * The {@link Implementation} details (e.g., I/O accuracy) may be altered before the code generation. 
   * Therefore, a new {@link Implementation} object is created.
   * 
   * @return the {@link Implementation} of this {@link Operator} with the default configuration for I/O accuracy
   */
  default public Implementation createDefaultImplementation() {
    Implementation imp = getImplementation();
    if (imp == null) return null;
    Implementation res = imp.clone();
    res.fitLatency(); // now the target.Processor.INSTANCE should be configured correctly
    return res;
  }
  
  /**
   * Create a specific {@code Implementation} of this {@code Operator} for a specific I/O accuracy.
   * 
   * The latency of the {@link Implementation} is also adjusted to the accuracy configuration.
   * 
   * @param f the common or port-specific I/O {@code Format}(s)
   * @return the {@link Implementation} for specified accuracy, or {@code null}, if configuration is not supported 
   */
  default public Implementation createImplementation(Format... f) {
    Implementation imp = createDefaultImplementation();
    
    // common Format for all operands and results
    if (f.length == 1) {
      imp.setCommonFormat(f[0]);
      
    // operand/result-specific formats
    } else if (f.length == imp.getNumberOfOperands() + imp.getNumberOfResults()) {
      for (int i=0; i<imp.getNumberOfOperands(); i++) imp.setOperandFormat(i, f[i]);
      for (int i=0; i<imp.getNumberOfResults();  i++) imp.setResultFormat(i, f[imp.getNumberOfOperands()+i]);
    } else {
      throw new IllegalArgumentException("single common or operand/result specific format expected");
    }
    
    // adjust latency
    imp.fitLatency();
    
    // check, if configuration can be implemented
    return imp.isValidConfiguration() ? imp : null;
  }
  
  /**
   * Get supported I/O accuracy, this {code Operator} can be implemented for.
   * 
   * @return list of supported I/O {@link Format}s, which may contain null.
   * @see Implementation#getSupportedFormats()
   */
  default public List<Class<? extends Format>> getSupportedFormats() {
    Implementation imp = getImplementation();
    if (imp == null) return Arrays.asList();
    return imp.getSupportedFormats();
  }
  
  /**
   * Check, whether this {@code Operator} can be implemented.
   */
  default public boolean hasImplementation() {
    return !getSupportedFormats().isEmpty();
  }
  
  /**
   * Find all supported {@code Implementation}s of this {@code Operator} with a certain {@code Format} width.
   * @param width the bitwidth of all operand and result {@link Format}s
   * @return {@link List} of supported {@link Implementation}s
   */
  default public List<Implementation> getAllImplementations(int width) {
    List<Implementation> res = new LinkedList<Implementation>();
    for (Class<? extends Format> fc : getSupportedFormats()) {
      // Mixed Format supported
      if (fc == null) {
        // we really do not support crazy stuff yet 
      
      // common format
      } else {
        try {
          Implementation imp = createImplementation(fc.getConstructor(int.class).newInstance(width));
          if (imp != null) res.add(imp);
        } catch (Exception e) {}
        try {
          Constructor<? extends Format> con = fc.getConstructor(int.class, int.class);
          for (int i=0; i<=width; i++) {
            try {
              Format f = con.newInstance(width-i, i);
              Implementation imp = createImplementation(f);
              if (imp != null) res.add(imp);
            } catch (Exception e) {}
          }
        } catch (Exception e) {}
      }
    }
    return res;
  }
  
  /**
   * Find all supported {@code Implementation}s of this {@code Operator} with a certain {@code Format} width.
   * @param minWidth the minimum bitwidth of all operand and result {@link Format}s
   * @param maxWidth the minimum bitwidth of all operand and result {@link Format}s
   * @return {@link List} of supported {@link Implementation}s
   */
  default public List<Implementation> getAllImplementations(int minWidth, int maxWidth) {
    List<Implementation> res = new LinkedList<Implementation>();
    for (int i=minWidth; i<=maxWidth; i++) res.addAll(getAllImplementations(i));
    return res;
  }
  
  /**
   * Find all supported {@code Implementation}s of this {@code Operator} with a {@code Format} width matching the
   * single or double datapath width.
   * 
   * @return {@link List} of supported {@link Implementation}s
   */
  default public List<Implementation> getAllImplementations() {
    int dpw = target.Processor.Instance.getDataPathWidth();
    List<Implementation> res = new LinkedList<Implementation>();
    for (int i=1; i<=2; i++) res.addAll(getAllImplementations(dpw*i));
    return res;
  }
  
  /**
   * Find a random supported {@code Implementation} for this {@code Operator}.
   * @return a supported {@link Implementation}
   */
  default public Implementation getRandomImplementation() {
    Random random = new Random();
    List<Implementation> list = getAllImplementations();
    
    // Prevent calls to nextInt with arguments of value 0
    if (list.isEmpty())
    	return null;
    else
    	return list.get(random.nextInt(list.size())); 
  }

/*
 * Static (implementation configuration independent) properties
 **********************************************************************************************************************/

  /**
   * Number of arithmetic inputs of this {@code Operator}.
   * @return input arity.
   */
  default public int getNumberOfOperands() {
    return getImplementation().getNumberOfOperands();
  }

  /**
   * Number of arithmetic outputs of this {@code Operator}.
   * @return output arity.
   */
  default public int getNumberOfResults() {
    return getImplementation().getNumberOfResults();
  }
  
  /**
   * Check for control flow {@code Operator}.
   * @return true, if this {@link Operator} generates a status flag
   */
  default public boolean isControlFlow() {
    return getImplementation().isControlFlow();
  }
  
  /**
   * Check for memory (register, cache or ROM) access {@code Operator}.
   * @return true, if this {@link Operator} accesses a memory
   */
  default public boolean isMemAccess() {
    return getImplementation().isMemAccess();
  }
  
  /**
   * Check for regfile access {@code Operator}.
   * @return true, if this {@link Operator} accesses a register
   */
  default public boolean isRegfileAccess() {
    return getImplementation().isRegfileAccess();
  }
  
  /**
   * Check for regfile write {@code Operator}.
   * @return true, if this {@code Operator} writes to the regfile
   */
  default public boolean isRegfileStore() {
    return isRegfileAccess() && getImplementation().isStore();
  }

  /**
   * Check for regfile read {@code Operator}.
   * @return true, if this {@code Operator} reads from the regfile
   */
  default public boolean isRegfileLoad() {
    return isRegfileAccess() && !getImplementation().isStore();
  }
  
  /**
   * Check for cache access {@code Operator}.
   * @return true, if this {@link Operator} accesses the cache
   */
  default public boolean isCacheAccess() {
    return getImplementation().isCacheAccess();
  }
  
  /**
   * Check for cache write {@code Operator}.
   * @return true, if this {@code Operator} writes to the cache
   */
  default public boolean isCacheStore() {
    return isCacheAccess() && getImplementation().isStore();
  }

  /**
   * Check for cache read {@code Operator}.
   * @return true, if this {@code Operator} reads from the cache
   */
  default public boolean isCacheLoad() {
    return isCacheAccess() && !getImplementation().isStore();
  }
  
  /**
   * Check for non-blocking cache prefetch {@code Operation}.
   * @return true, if this {@link Implementation} represents a cache prefetch
   */
  default public boolean isCachePrefetch() {
    return getImplementation().isCachePrefetch();
  }
  
  /**
   * Check for ROM access {@code Operator}.
   * @return true, if this {@link Operator} accesses a ROM
   */
  default public boolean isRomAccess() {
    return getImplementation().isRomAccess();
  }
  
  /**
   * Check for indexed memory access {@code Operator}.
   * @return true, if this {@link Operator} accesses an array in a memory
   */
  default public boolean isIndexedMemAccess() {
    return getImplementation().isIndexedMemAccess();
  }
  
  /**
   * Check for constant {@code Operator}
   * @return true, if this {@code Operator} is a constant value;
   */
  default public boolean isConst() {
    return getImplementation() instanceof CONST;
  }

  /**
   * @return true, if this {@link Operator} is not implemented by a dedicated Verilog module
   */
  default public boolean isNative() {
    return getImplementation().isNative();
  }

}

/*
 * Copyright (c) 2016, Embedded Systems and Applications Group, Department of
 * Computer Science, TU Darmstadt, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of the
 * institute nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **********************************************************************************************************************/
