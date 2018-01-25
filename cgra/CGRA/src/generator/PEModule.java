package generator;

import cgramodel.PEModel;
import operator.Implementation;
import operator.Operator;

import static generator.PEModule.Ports.*;

public class PEModule extends GenericModule<PEModule.Ports> {

    /**
     * Base name of this Module, expanded by the PE ID
     */
    private static final String BASE_NAME = "PE";

    private static final Wire W_ROM_ADDR = new Wire(Wire.Type.Wire, "rom_addr_pe", "rom output data wire");

    private static final Wire W_ROM_OFFSET = new Wire(Wire.Type.Wire, "rom_offset_pe", "rom output data wire");

    private static final Wire W_ROM_EN = new Wire(Wire.Type.Wire, "rom_en_pe", "rom output data wire");

    private static final Wire W_ROM_WIDE = new Wire(Wire.Type.Wire, "rom_wide_pe", "rom output data wire");

    private static final Wire W_ROM_ARRAY = new Wire(Wire.Type.Wire, "rom_array_pe", "rom output data wire");

    public enum Ports {
        RomAddr,
        RomOffset,
        RomEn,
        RomWide,
        RomArray,
        RomData,
    }

    public PEModule(Class<Ports> enumClass, int id) {
        super(enumClass, getName(id));
    }

    public static String getName(int peID) {
        return BASE_NAME + "_" + peID;
    }

    /**
     * A temp method to create a {@link PEModule} usable to connect ROM modules.
     * To be removed when a complete PE module is implemented in this manner.
     *
     * @param pe the PE to create the wires for
     * @return the created {@link PEModule}
     */
    public static PEModule createRomOnlyModule(PEModel pe) {
        final int id = pe.getID();
        PEModule peMod = new PEModule(Ports.class, id);

        // All this info should be generated when building the currently not existing ALU module
        boolean romAccess = false;
        boolean arrayAccess = false;
        int maxRomWideAccessWidth = 0;

        for (Operator op : pe.getAvailableNonNativeOperators().keySet()) {
            Implementation imp = pe.getAvailableOperators().get(op);
            if (imp.isRomAccess()) {
                romAccess = true;
                if (imp.isIndexedMemAccess())
                    arrayAccess = true;
                if (imp.isWideMemAccess())
                    maxRomWideAccessWidth = Math.max(maxRomWideAccessWidth, imp.getWideMemAccessPortWidth());
            }
        }

        if (romAccess) {
            peMod.addWire(RomAddr, new Wire(W_ROM_ADDR, pe.getRomAddrWidth(), id));
            peMod.addPort(RomAddr, new Port(Port.Type.OUT, "DUMMY"));
            peMod.addWire(RomOffset, new Wire(W_ROM_OFFSET, pe.getRomAddrWidth(), id));
            peMod.addPort(RomOffset, new Port(Port.Type.OUT, "DUMMY"));
            peMod.addWire(RomEn, new Wire(W_ROM_EN, id));
            peMod.addPort(RomEn, new Port(Port.Type.OUT, "DUMMY"));
        }

        if (maxRomWideAccessWidth > 0) {
            peMod.addWire(RomWide, new Wire(W_ROM_WIDE, maxRomWideAccessWidth, id));
            peMod.addPort(RomWide, new Port(Port.Type.OUT, "DUMMY"));
        }

        if (arrayAccess) {
            peMod.addWire(RomArray, new Wire(W_ROM_ARRAY, id));
            peMod.addPort(RomArray, new Port(Port.Type.OUT, "DUMMY"));
        }

        return peMod;
    }
}
