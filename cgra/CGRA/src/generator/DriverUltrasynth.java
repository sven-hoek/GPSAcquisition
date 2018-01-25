package generator;

import java.util.List;

import cgramodel.CgraModelUltrasynth;
import graph.CDFG;
import graph.LG;
import scheduler.*;
import target.Processor;
import target.UltraSynth;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static target.Processor.Instance;

/**
 * Just like a compiler driver manages its underlying passes,
 * this class shall provide similar behaviour in building a complete
 * description of the provided Ultrasynth config.
 *
 * As there are more tasks than handled by the VerilogGenerators and
 * all other classes involved in printing code, this class is also responsible
 * for tasks not directly related to Verilog or testbench generation.
 *
 * Consider this as another (maybe more viable way)
 * of providing a public interface to people who don't know the framework.
 */
public class DriverUltrasynth extends Driver<CgraModelUltrasynth> {

    private Config config;
    private SchedulerDriver schedDriver;

    public DriverUltrasynth() {
        super();
        config = new Config(ConfigOption.Invalid);
        schedDriver = null;

        Processor.Instance = UltraSynth.Instance;
        super.config.toDefaults();
        super.config.modelConfigFolderPath += "/ultrasynth";
    }

    public void parseCliArguments(int argc, String... argv) {
        parseArgs(config, argv);
    }

    public void readConfigFile(String path) {
        // Not used atm
    }

    public Config getConfig() {
        return config;
    }

    @Override
    protected void runGenerators(CgraModelUltrasynth model) {
        Processor.Instance = UltraSynth.Instance;
        final Processor<?> ultra = Processor.Instance; // convenience assignment
        VerilogGeneratorUltrasynth gen = (VerilogGeneratorUltrasynth) ultra.getGenerator();

        if (config.generateCore) {
            gen.printVerilogDescription(super.config.outputFolderPath, model);
        }

        if (config.generateDriver) {
            UltrasynthCApiGenerator.generate(model, sourceFileLib, SourceIndex.CSources.ordinal(), config);
        }

        if (config.generateTestBench) {
            TestbenchGeneratorUltrasynth tbGen = new TestbenchGeneratorUltrasynth(model);
            tbGen.generate(model, sourceFileLib, SourceIndex.TestSources.ordinal(), config);
        }
    }

    @Override
    protected void runScheduler(CgraModelUltrasynth model) {
        String graphFile = super.config.dcsFilePath;

        if (graphFile.isEmpty()) {
            System.out.println("No DSC file path given, skipping Schduler");
            return;
        }

        schedDriver = new SchedulerDriver(graphFile, model);
        schedDriver.run();
    }

    @Override
    protected void applySchedulerResults(CgraModelUltrasynth model) {
        if (!config.generateDriverContexts && !config.generateDriver)
            return;

        if (schedDriver == null || !schedDriver.hasResults()) {
            System.out.println("No Scheduler results available");
            return;
        }

        if (config.generateDriver)
            UltrasynthCApiGenerator.applySchedulingInfo(
                    schedDriver.getResults(), sourceFileLib, SourceIndex.CSources.ordinal(), config);

        if (config.generateDriverContexts)
            UltrasynthCApiGenerator.generateContexts(schedDriver.getResults(), model, sourceFileLib,
                    SourceIndex.CSources.ordinal(), config);
    }

    @Override
    protected void assembleOutput() {
        List<Boolean> relativePathUsage = generateRelativePathUsage();
        sourceFileLib.writeFilesToTarget(SourceIndex.class, super.config.outputFolderPath, relativePathUsage);

        // We have to add the CGRA constraints here until another solution is viable
        Path source = Paths.get(target.Processor.Instance.getHardwareTemplatePathDataPath() + "/constraints.xdc");
        Path destination = Paths.get(super.config.outputFolderPath + "/constraints.xdc");

        try {
            Files.copy(source, destination);
        } catch (IOException e) {
            System.err.println("Error "+ e);
            System.err.println("IO Exception in addStaticFiles() with File " + source);
        }
    }

    @Override
    protected void generateScripts() {
        if (super.config.generateVivadoScript) {
            VivadoTclScript script = new VivadoTclScript(".","viva_pro", "Dummy");
            script.addSourceFile("*.v");
            script.addSourceFile("*.vh");
            script.addConstraintsFile("*.xdc");
            script.addIPFile("*.xcix");
            dumpVivadoScript(script);
        }
    }

    protected void loadSources() {
        sourceFileLib.clear();

        if (config.generateCore) {
            System.out.println("Using old Verilog generation process, skipping core template source file loading");
            // loadSubPath(SourceIndex.CoreTemplates);
            loadSubPath(SourceIndex.CoreNonTemplates);
        }

        if (config.copyXilinxIPToTarget)
            loadSubPath(SourceIndex.ExternalIP);

        if (config.generateTestBench) {
            loadSubPath(SourceIndex.TestSources);
            loadSubPath(SourceIndex.TestExternalIP);
        }

        if (config.generateDriver || config.generateDriverContexts)
            loadSubPath(SourceIndex.CSources);
    }

    // Usable to gen a model
    protected CgraModelUltrasynth loadModel(String configName) {
        Processor.Instance = UltraSynth.Instance;
        final Processor<?> ultra = Processor.Instance; // convenience assignment
        final String configPath = super.config.modelConfigFolderPath;

        CgraModelUltrasynth model;
        model = (CgraModelUltrasynth) ultra.getAttributeParser().loadCgra(
                configPath + "/" + configName + ".json"
        );

        if (model == null) {
            System.err.println("Config File parsing failed - aborting.");
            return null;
        }

        model.setName(configName);
        model.finalizeCgra();
        return model;
    }

    private List<Boolean> generateRelativePathUsage() {
        ArrayList<Boolean> usage = new ArrayList<>(SourceIndex.values().length);
        for (SourceIndex sourceIndex : SourceIndex.values())
            usage.add(sourceIndex.useRelativeTargetPath);
        return usage;
    }

    public static CgraModelUltrasynth createModel(String configFileName) {
        DriverUltrasynth d = new DriverUltrasynth();
        return d.loadModel(configFileName);
    }

    enum SourceIndex {
        CoreTemplates("", false),
        CoreNonTemplates("static", false),
        ExternalIP("ip", false),
        TestSources("test", true),
        TestExternalIP("test_ip", true),
        CSources("c", true);

        public final String subFolderPath;
        public final boolean useRelativeTargetPath;
        SourceIndex(String str, boolean useRelativeTargetPath) {
            this.subFolderPath = str;
            this.useRelativeTargetPath = useRelativeTargetPath;
        }

        @Override
        public String toString() {
            return subFolderPath;
        }
    }

    enum ConfigOption {
        Invalid,
        GenerateCore,
        CopyXilinxIPToTarget,
        GenerateDriver,
        GenerateDriverContexts,
        GenerateTestBench,
        GenerateRandomContexts,
        RunTestBenchAfterCreation,
    }

    public class Config extends generator.Config<ConfigOption> {
        // Core switches
        boolean generateCore;
        boolean copyXilinxIPToTarget;

        // Driver switches
        boolean generateDriver;
        boolean generateDriverContexts;

        // TB switches
        boolean generateTestBench;
        boolean generateRandomContexts;
        boolean runTestBenchAfterCreation;

        Config(ConfigOption invalidOption) {
            super(invalidOption);
            toDefaults();
        }

        void toDefaults() {
            generateCore = true;
            copyXilinxIPToTarget = true;

            generateDriver = true;
            generateDriverContexts = true;

            generateTestBench = false;
            generateRandomContexts = false;
            runTestBenchAfterCreation = false;
        }

        private boolean parseBoolOption(ConfigOption option, String arg) {
            switch (arg) {
                case "true": return true;
                case "false": return false;
                default:
                    System.err.println("Expected boolean value for ConfigOption " + option.name());
                    return false;
            }
        }

        @Override
        protected boolean parseOption(ConfigOption option, String arg) {
            switch (option) {
                case Invalid:
                    System.err.println("Unable to parse Invalid Config option with arg " + arg);
                    return false;
                case GenerateCore:
                    generateCore = parseBoolOption(option, arg);
                    return true;
                case CopyXilinxIPToTarget:
                    copyXilinxIPToTarget = parseBoolOption(option, arg);
                    return true;
                case GenerateDriver:
                    generateDriver = parseBoolOption(option, arg);
                    return true;
                case GenerateDriverContexts:
                    generateDriverContexts = parseBoolOption(option, arg);
                    return true;
                case GenerateTestBench:
                    generateTestBench = parseBoolOption(option, arg);
                    return true;
                case GenerateRandomContexts:
                    generateRandomContexts = parseBoolOption(option, arg);
                    return true;
                case RunTestBenchAfterCreation:
                    runTestBenchAfterCreation = parseBoolOption(option, arg);
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public Class<ConfigOption> getEnumClass() {
            return ConfigOption.class;
        }
    }
}
