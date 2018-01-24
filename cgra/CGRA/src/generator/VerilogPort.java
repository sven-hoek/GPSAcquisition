package generator;

/**
 * Port is a typed object that represents a port of the toplevel module of a Cgra. There are several attributes.
 * @author wolf
 *
 */
class VerilogPort {
	
	/**
	 * Name of the port
	 */
	String name;
	
	public String getName() {
		return name;
	}
	
	/**
	 * Width of the port
	 */
	int portwidth;

	public int getPortwidth() {
		return portwidth;
	}
	
	/**
	 * ID if the Port. This is important when there are several ports of this kind - e.g. valid inputs of caches.
	 */
	int id;

	public int getId() {
		return id;
	}

	/**
	 * direction of the Port. The enumeration {@link portdirectionality} lists all possibilities.
	 */
	portdirectionality direction;
	

	public portdirectionality getDirection() {
		return direction;
	}
	
	/**
	 * The type of a port declares whether a port is a register or a wire, as defined in {@link porttype}.
	 */
	porttype type;
	

	public porttype getType() {
		return type;
	}
	
	public String getTypeDeclaration() {
		return type.getDeclaration();
	}
	
	/**
	 * Uniqueness determines whether there is only kind of this type of port. 
	 */
	boolean uniqueness;
	
	VerilogPort(String name, int portwidth, int id, portdirectionality direction,porttype type, boolean uniqueness){
		this.portwidth = portwidth;
		this.name = name;
		this.id = id;
		this.direction = direction;
		this.type = type;
		this.uniqueness = uniqueness;
	}
	
	public String getPortDeclaration(){
		return name + (uniqueness? ("_"+id):"") + direction.getSuffix();
	}

}

/**
 * This enumeration holds all possible directionalities of CGRA port.
 * @author wolf
 *
 */
enum portdirectionality{
	input("_I"),
	output("_O"),
	inout("_IO");
	
	/**
	 * A suffix is used in a Verilog description for better readability. It implies whether a signal is an input, output
	 * or inout.
	 */
	private String suffix;

	portdirectionality(String suffix){
		this.suffix = suffix;
	}
	
	public String getSuffix(){
		return suffix;
	}
}
/**
 * The port port type determines whether a input or output is an sequential element.
 * @author wolf
 *
 */
enum porttype{
	wire("wire"),
	register("reg");
	
	/**
	 * The Verilog declaration of port type.
	 */
	private String verilogdeclaration;

	porttype(String verilogdeclaration){
		this.verilogdeclaration = verilogdeclaration;
	}
	
	public String getDeclaration(){
		return verilogdeclaration;
	}
}
