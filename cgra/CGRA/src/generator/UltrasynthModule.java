package generator;

import cgramodel.CgraModelUltrasynth;
import cgramodel.PEModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static generator.UltrasynthModule.Ports.*;

/**
 * Represents the top level module of the Ultrasynth project.
 */
public class UltrasynthModule extends GenericModule<UltrasynthModule.Ports> {

    public enum Ports {
        Clk,
        Rst,
    }

    /**
     * Manages write operations of the AXI slave interface internal to Ultrasynth
     */
    private WriteControl writeControl;

    /**
     * Manages the AXI salve interface.
     */
    private ComUnit comUnit;

    /**
     * PEModules for Ultrasynth
     */
    private List<PEModule> peModules;

    private UltrasynthModule(CgraModelUltrasynth ultrasynthModel) {
        super(Ports.class, ultrasynthModel.getName());
        peModules = new ArrayList<>(ultrasynthModel.getNrOfPEs());
    }

    /**
     * Create a Ultrasynth top level module with Ports {@link Ports#Clk} and {@link Ports#Rst}.
     *
     * @param ultrasynthModel the model to create this module for
     * @return the created module
     */
    static UltrasynthModule createFromModel(CgraModelUltrasynth ultrasynthModel) {
        UltrasynthModule module = new UltrasynthModule(ultrasynthModel);

        module.addPort(Clk, Module.CLOCK);
        module.addPort(Rst, Module.RESET);

        module.writeControl = WriteControl.createSimple();
        module.comUnit = ComUnit.createSimple(ultrasynthModel.getMaxContextWidth(), ultrasynthModel.getOffsetAddrWidth());

        for (PEModel pe : ultrasynthModel.getPEs())
            module.peModules.add(PEModule.createRomOnlyModule(pe));

        return module;
    }

    List<PEModule> getPeModules() { return peModules; }

    WriteControl getWriteControl() {
        return writeControl;
    }

    ComUnit getComUnit() {
        return comUnit;
    }

    /**
     * Connect all contained modules.
     *
     * @param ultrasynthModel currently needed to loop the PEModels
     * @param modules modules which are currently not in this module but should be in here
     */
    void connectModules(CgraModelUltrasynth ultrasynthModel, Map<String, Module> modules) {
        // ROM
        for (PEModel pe : ultrasynthModel.getPEs()) {
            if (pe.getRomAccess()) {
                Module peMod = modules.get("PE" + pe.getID());
                PEModule peModule = peModules.get(pe.getID());
                RomModule romModule = (RomModule) modules.get(RomModule.getName(pe.getID()));
                connectRomModule(romModule, peModule);
            }
        }
    }

    /**
     * Helper to connect the given RomModule to all required other modules
     *
     * @param romModule the module to connect to its surroundings
     * @param peMod temporary param, will be removed one these modules are available in this module
     */
    private void connectRomModule(RomModule romModule, PEModule peMod) {
        // General connection
        romModule.connectPE(peMod);

        // Ultrasynth specific
        romModule.connectByPort(RomModule.Index.Clock, Ports.Clk, this);
        romModule.connectByPort(RomModule.Index.Rst, Ports.Rst, this);
        romModule.connectByWire(RomModule.Index.WrEn, WriteControl.Ports.RomWrEn, writeControl);
        romModule.connectByWire(RomModule.Index.WrData, ComUnit.Ports.IncomingData, comUnit);
        romModule.connectByWire(RomModule.Index.WrAddr, ComUnit.Ports.IncomingAddr, comUnit);
    }

    // Nested classes, should be moved into their own file once they are really used

    public static class WriteControl extends GenericModule<WriteControl.Ports> {

        public enum Ports {
            RomWrEn
        }

        private WriteControl() { super(WriteControl.Ports.class, "WriteControl"); }

        /**
         * Create a WriteControl object with only a ROM wire. To be expanded...
         * @return the created module
         */
        public static WriteControl createSimple() {
            WriteControl module = new WriteControl();
            module.addWire(
                Ports.RomWrEn,
                new Wire(Wire.Type.Wire, "constBufferWrEn", 1, "Rom write enable")
            );
            return module;
        }
    }

    public static class ComUnit extends GenericModule<ComUnit.Ports> {

        public enum Ports {
            IncomingData,
            IncomingAddr,
        }

        private ComUnit() { super(ComUnit.Ports.class, "ComUnit"); }

        /**
         * Create a WriteControl object with only a ROM wire. To be expanded...
         * @return the created module
         */
        public static ComUnit createSimple(int maxIncomingDataWidth, int maxIncomingAddrWidth) {
            ComUnit module = new ComUnit();
            module.addWire(
                Ports.IncomingData,
                new Wire(Wire.Type.Wire, "context_data",
                         maxIncomingDataWidth, "Bundled data from the axi interface")
            );
            module.addWire(
                Ports.IncomingAddr,
                new Wire(Wire.Type.Wire, "offset_addr", maxIncomingAddrWidth, "Internal address selection")
            );
            return module;
        }
    }
}
