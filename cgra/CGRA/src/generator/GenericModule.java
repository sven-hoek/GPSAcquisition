package generator;

import util.EnumIndexedList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a generic module within the generator.
 */
public class GenericModule <E extends Enum> implements Module {

    /**
     * The ports of this module
     */
    private EnumIndexedList<E, Port> ports;

    /**
     * The wires connecting to the ports.
     * If a port is at index n of the port list ({@link #ports}) its corresponding
     * wire can be found at index n of the wire list ({@link #wires}).
     * The module implementation provides a default set output wires but leaves
     * input wires with a null value (to be connected later).
     */
    private EnumIndexedList<E, Wire> wires;

    /**
     * The name of this module
     */
    private String name;

    protected GenericModule(Class<E> enumClass, String name) {
        this.ports = new EnumIndexedList<>(enumClass);
        this.wires = new EnumIndexedList<>(enumClass);
        this.name = name;
    }

    /**
     * Connect an input {@link Port} of this {@link GenericModule} with its corresponding
     * output port of the other {@link GenericModule} using the wire provided by the
     * other {@link GenericModule}.
     *
     * @param thisIndex index of the wire (or the corresponding input port) to connect
     * @param otherIndex index of the output port to connect to (the associated {@link generator.Module.Wire is used}
     * @param other the other module holding the output port to connect to
     */
    public <EOther extends Enum>
    void connectByWire(E thisIndex, EOther otherIndex, GenericModule<EOther> other) {
        if (getPort(thisIndex).getType() != Port.Type.IN) {
            throw new IllegalArgumentException("This port is not an input port");
        } else {
            Wire outputWire = other.getWire(otherIndex);
            wires.insert(thisIndex, outputWire);
        }
    }

    /**
     * Connect an input {@link Port} with another {@link GenericModule}'s input port,
     * forwarding the signal by doing so.
     * Especially useful for clock and reset propagation.
     *
     * @param thisIndex index of the wire (or the corresponding input port) to connect
     * @param otherIndex index of the input port to connect to
     * @param other the other module holding the input port to connect to
     */
    public <EOther extends Enum>
    void connectByPort(E thisIndex, EOther otherIndex, GenericModule<EOther> other) {
        if (getPort(thisIndex).getType() != Port.Type.IN) {
            throw new IllegalArgumentException("This port is not an input port");
        } else {
            Port otherInputPort = other.getPort(otherIndex);
            Wire wire = new Wire(otherInputPort);
            wires.insert(thisIndex, wire);
        }
    }

    /**
     * Create some ports which are present in almost all the modules.
     *
     * @param clkIndex clock index of this Module
     * @param rstIndex reset index of this Module
     */
    protected void createCommonPorts(E clkIndex, E rstIndex) {
        addPort(clkIndex, Module.CLOCK);
        addPort(rstIndex, Module.RESET);
    }

    /**
     * Simplifies the {@link Module#getInstance(String, HashMap)} interface,
     * but will yield the same kind of Verilog code.
     *
     * TODO: ? There is currently no formatting!
     *
     * @param instanceName The name of the instance to create.
     * @return Verilog Code
     */
    public String getInstance(String instanceName) {
        List<Port> portList = new ArrayList<>(ports.size());
        List<Wire> wireList = new ArrayList<>(wires.size());
        fillSortedEqualSizedLists(portList, wireList);

        StringBuilder sb = new StringBuilder();

        // Handle the instance name
        sb.append(getName()).append(' ').append(instanceName).append("(\n");

        // Add all ports with their connected wires
        for (int i = 0; i < portList.size(); ++i) {
            Port port = portList.get(i);
            Wire wire = wireList.get(i);
            boolean isLast = i == portList.size() - 1;
            sb.append(port.getInstanceString(wire, isLast));
        }

        sb.append(");\n");
        return sb.toString();
    }

    /**
     * Collect all required declarations of wires connected to the output ports of this module
     *
     * @return the Verilog string representing all the declarations
     */
    public String getWireDeclarations() {
        List<Port> portList = new ArrayList<>(ports.size());
        List<Wire> wireList = new ArrayList<>(wires.size());
        fillSortedEqualSizedLists(portList, wireList);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < portList.size(); ++i) {
            Port port = portList.get(i);

            if (port.getType() != Port.Type.IN) {
                Wire wire = wireList.get(i);
                sb.append(wire.getDeclaration()).append(";\n");
            }
        }

        return sb.toString();
    }

    private void fillSortedEqualSizedLists(List<Port> portList, List<Wire> wireList) {
        Comparator<Net> cmp = (Net n1, Net n2) -> {
            if (!n1.hasID() || !n2.hasID())
                throw new RuntimeException("Unable to sort without any ID");

            return n1.getId() - n2.getId();
        };

        // Make sure that wires and ports of equal ID are at the same Index
        ports.sort(cmp);
        wires.sort(cmp);

        portList.addAll(ports.getList());
        wireList.addAll(wires.getList());

        if (portList.size() == 0)
            throw new RuntimeException("A module with without ports does not make sense");

        if (portList.size() != wireList.size())
            throw new RuntimeException("Unequal amount of wires and ports during module instance creation");
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean hasPort(E index) {
        return ports.contains(index);
    }

    public void addPort(E index, Port port) {
        ports.insert(index, port);
    }

    public void addPortIfEq(E index, Port p1, Port p2) {
        if (p1.equals(p2))
            addPort(index, p1);
    }

    public Port getPort(E index) {
        return ports.get(index);
    }

    public Port getPort(E index, int id) {
        return ports.get(index, (Port p) -> p.getId() == id);
    }

    @Override
    public List<Port> getPorts() {
        return ports.getList();
    }

    public void addWire(E index, Wire wire) {
        wires.insert(index, wire);
    }

    public Wire getWire(E index) {
        return wires.get(index);
    }

    public Wire getWire(E index, int id) {
        return wires.get(index, (Wire w) -> w.getId() == id);
    }

}
