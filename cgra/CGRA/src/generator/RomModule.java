package generator;

import cgramodel.PEModel;

import static generator.RomModule.Index.*;

public class RomModule extends GenericModule<RomModule.Index> {

    /**
     *
     */
    private static final String BASE_NAME = "ROM";

    /**
     * ROM write enable
     */
    private static final Port WR_EN = new Port(Port.Type.IN, "WR_EN", "rom init data write enable");

    /**
     * ROM write address
     */
    private static final Port WR_ADDR = new Port(Port.Type.IN, "WR_ADDR", "rom init data write address");

    /**
     * ROM write data input
     */
    private static final Port WR_DATA = new Port(Port.Type.IN, "WR_DATA", "rom init data port");

    /**
     * ROM read enable (high active)
     */
    private static final Port RD_EN = new Port(Port.Type.IN, "RD_EN", "rom signals valid (high active)");

    /**
     * ROM read address
     * This is the base address of a ROM read. Just like all other address ports of this module,
     * addresses are memory indices (accessing a whole word) and not byte addresses.
     */
    private static final Port RD_ADDR = new Port(Port.Type.IN, "RD_ADDR", "rom read address");

    /**
     * ROM offset
     * Has different meaning depending on the value (or existence) of {@link #RD_ARRAY}.
     */
    private static final Port RD_OFFSET = new Port(Port.Type.IN, "RD_OFFSET", "rom offset");

    /**
     * Additional ROM access words
     * This input determines how many subsequent memory words are read by a single operation.
     * Example:
     *      If this port drives the value 1, two memory words are read.
     *      One at {@code {@link #RD_ADDR} + {@link #RD_OFFSET}}
     *      and the second from {@code {@link #RD_ADDR} + {@link #RD_OFFSET} + 1}.
     *
     * This is an optional port and therefore not part of a default {@link RomModule}
     */
    static final Port RD_WIDE = new Port(Port.Type.IN, "RD_WIDE_ACCESS", "additional rom access words");

    /**
     * Indexed ROM access (high active)
     * Determines if {@link #RD_OFFSET} is interpreted as an array index rather than a memory address.
     *
     * This is an optional port and therefore not part of a default {@link RomModule}
     */
    static final Port RD_ARRAY = new Port(Port.Type.IN, "RD_ARRAY_ACCESS", "indexed rom access (high active)");

    /**
     * ROM read data
     */
    private static final Port RD_DATA = new Port(Port.Type.OUT, "RD_DATA", "rom read data");

    /**
     * ROM read data output wire targeting a PE
     */
    private static final Wire W_DATA_OUT = new Wire(Wire.Type.Wire, "rom_data_pe", "rom output data wire");

    /**
     * Order and index for the ports of this module.
     */
    public enum Index {
        Clock,
        Rst,
        WrEn,
        WrAddr,
        WrData,
        RdAddr,
        RdOffset,
        RdEn,
        RdWide,
        RdArray,
        RdData,
    }

    private RomModule(int associatedPEid) {
        super(Index.class, getName(associatedPEid));
    }

    /**
     * Get the name of a specific {@link RomModule} by providing a suffix object.
     *
     * @param obj the suffix
     * @return the base name extended by the given suffix
     */
    static public String getName(Object obj) { return BASE_NAME + "_" + obj; }

    /**
     * Connect the input ports of the given PE to this {@link RomModule}.
     *
     * @param peModule the module representation to connect to
     */
    public void connectPE(PEModule peModule) {
        connectByWire(RdAddr, PEModule.Ports.RomAddr, peModule);
        connectByWire(RdOffset, PEModule.Ports.RomOffset, peModule);
        connectByWire(RdEn, PEModule.Ports.RomEn, peModule);

        if (hasPort(RdWide))
            connectByWire(RdWide, PEModule.Ports.RomWide, peModule);

        if (hasPort(RdArray))
            connectByWire(RdArray, PEModule.Ports.RomArray, peModule);
    }

    /**
     * Create a {@link RomModule} with default Port and Wire configuration, depending
     * on the given parameters.
     *
     * Excludes some {@link generator.Module.Port}s which have to be added manually (if required):
     * <ul>
     *   <li> {@link #RD_WIDE}
     *   <li> {@link #RD_ARRAY}
     * </ul>
     *
     * @param id the associated PE ID
     * @param addrWidth the address width of the internal memory
     * @param dataWidth the CGRA data path width
     * @return the created {@link RomModule}
     */
    static public RomModule createDefault(int id, int addrWidth, int dataWidth) {
        RomModule newed = new RomModule(id);
        newed.createDefaultPorts(addrWidth, dataWidth);
        newed.createDefaultOutputWires(id, dataWidth);
        return newed;
    }

    /**
     * Create and add the set of default Ports for this module.
     *
     * @param addrWidth the address width of the internal memory
     * @param dataWidth the CGRA data path width
     */
    private void createDefaultPorts(int addrWidth, int dataWidth) {
        createCommonPorts(Clock, Rst);
        addPort(WrEn, WR_EN);
        addPort(RdEn, RD_EN);
        addPort(WrAddr, new Port(WR_ADDR, addrWidth));
        addPort(RdAddr, new Port(RD_ADDR, addrWidth));
        addPort(RdOffset, new Port(RD_OFFSET, addrWidth));
        addPort(WrData, new Port(WR_DATA, dataWidth));
        addPort(RdData, new Port(RD_DATA, dataWidth));
    }

    /**
     * Create and add the set of default Wires for this module.
     *
     * @param id the associated PE ID
     * @param dataWidth the CGRA data path width
     */
    private void createDefaultOutputWires(int id, int dataWidth) {
        addWire(RdData, new Wire(W_DATA_OUT, dataWidth, id));
    }
}
