package generator;

import org.stringtemplate.v4.ST;

/**
 * Representation of a Verilog module {@code Port}.
 * 
 * TODO: should be replaced by {@link Module.Port}.
 * 
 * @author Marcel Heinlein [marcel.hnln@gmail.com]
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class VPort {
  
  public enum Type {
    IN ("input  wire", "_I"), 
    OUT("output wire", "_O"),
    REG("output reg ", "_O");
    
    public final String declare;
    public final String suffix;
    
    Type(String declare, String suffix) {
      this.declare = declare;
      this.suffix  = suffix;
    }
  }
  
  protected Type   type;
  protected int    width;
  protected String name;
  protected String wire;

  public VPort(Type type, String name, int width, String wire) {
    this.type  = type;
    this.width = width;
    this.name  = name;
    this.wire  = wire;
  }
  
  public VPort(Type type, String name, int width) {
    this(type, name, width, name.toLowerCase());
  }
  
  public String getName() {
    return name + type.suffix;
  }
  
  public String getWire() {
    return wire;
  }
  
  public String getInstanciation() {
    return "." + getName() + "(" + getWire() + "),\n";
  }
  
  public String getWidthDeclaration() {
    return width == 1 ? " " : " [" + (width-1) + ":0] ";
  }

  public String getDeclaration() {
    return "(* dont_touch = \"true\" *) " + type.declare + getWidthDeclaration() + getName() + ",\n";
  }
  
  public boolean drive(VPort other) {
    if (!other.name.startsWith(this.name)) return false;
    other.wire = this.wire;
    return true;
  }
  
  public static class Template extends VPort {
    
    public Template(Type type, String name, int width, String wire) {
      super(type, name, width, wire);
    }
    
    public Template(Type type, String name, int width) {
      super(type, name, width);
    }
    
    
    private Object[] args;
    
    public void set(Object ...args) {
      this.args = args;
    }
    
    private String render(String template) {
      ST st = new ST(template);
      for (int i=0; i<args.length; i++) st.add(Integer.toString(i), args[i]);
      return st.render();
    }
    
    @Override
    public String getName() {
      return render(name) + type.suffix;
    }
    
    @Override
    public String getWire() {
      return render(wire);
    }
  }

}
