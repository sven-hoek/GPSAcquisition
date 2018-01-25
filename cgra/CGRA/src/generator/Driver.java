package generator;

import io.SourceFile;
import io.SourceFileLib;
import target.Processor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public abstract class Driver<ModelType> {

    protected Config config;
    protected SourceFileLib sourceFileLib;

    Driver() {
        this.config = new Config(ConfigOption.Invalid);
        this.sourceFileLib = new SourceFileLib();
    }

    public void parseCliArguments(int argc, String... argv) {
        parseArgs(config, argv);
    }

    public Config getBaseConfig() {
        return config;
    }

    public void generateOutput(String configFileName) {
        ModelType model = loadModel(configFileName);
        generateOutput(model);
    }

    public void generateOutput(ModelType model) {
        loadSources();

        if (model == null) {
        	System.out.println("Skipping generator runs as the provided model is null");
        } else {
        	runGenerators(model);

        	if (config.runScheduler) {
                runScheduler(model);
                applySchedulerResults(model);
            }
        }

        assembleOutput();
        generateScripts();
    }

    protected abstract ModelType loadModel(String configFileName);
    protected abstract void runGenerators(ModelType model);
    protected abstract void runScheduler(ModelType model);
    protected abstract void applySchedulerResults(ModelType model);
    protected abstract void generateScripts();

    protected void assembleOutput() {
        sourceFileLib.writeFilesToTarget(DefaultSourceIndex.class, config.outputFolderPath, null);
    }

    protected void dumpVivadoScript(VivadoTclScript script) {
        if (config.generateVivadoScript) {
            script.createPreamble(config.vivadoProjectName, config.targetPlatform);
            String str = script.compile(
                config.synthAndImplVivadoProperties, config.requestedSynthAndImpVivadoReports,
                config.runSynth, config.runImpl
            );
            dump(config.vivadoScriptFileName, str);
        } else {
            System.out.println("Skipped Vivado TCL script writing, not set in config");
        }
    }

    protected <ConfOpt extends Enum<?>, Conf extends generator.Config<ConfOpt>>
    void parseArgs(Conf config, String... argv)
    {
        ConfOpt option = config.getInvalid();
        String arg = null;

        for (String cliStr : argv) {
            if (option != config.getInvalid() && arg != null) {
                config.parseOption(option, arg);
                option = config.getInvalid();
                arg = null;
            }

            if (option == config.getInvalid() && cliStr.startsWith("-")) {
                for (ConfOpt optToCheck : config.getEnumClass().getEnumConstants()) {
                    if (cliStr.equals(optToCheck.name())) {
                        option = optToCheck;
                        break;
                    }
                }
            } else if (option != config.getInvalid() && !cliStr.startsWith("-")) {
                arg = cliStr;
            }
        }
    }

    protected void loadSources() {
        sourceFileLib.clear();
        loadSubPath(DefaultSourceIndex.InputPath);
    }

    protected void dump(String path, String str) {
        try {
            FileWriter writer = new FileWriter(new File(config.outputFolderPath + "/" + path));
            writer.write(str);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected <SourceIndex extends Enum<?>>
    void loadSubPath(SourceIndex index) {
        String pathToSearch;
        List<SourceFile> foundSources;

        pathToSearch = config.inputFolderPath + "/" + index.toString();
        foundSources = SourceFile.findInPath(pathToSearch, false);
        sourceFileLib.addSourceFiles(index.ordinal(), foundSources);
    }

    enum DefaultSourceIndex {
        InputPath;
        @Override
        public String toString() {
            return "";
        }
    }

    enum ConfigOption {
        Invalid,
        ModelConfigFolderPath,
        InputFolderPath,
        OutputFolderPath,
    }

    public class Config extends generator.Config<ConfigOption> {
        public String modelConfigFolderPath;
        public String dcsFilePath;
        public String inputFolderPath;
        public String outputFolderPath;

        // Implementation script properties
        public String synthAndImplVivadoProperties;
        public String requestedSynthAndImpVivadoReports;
        public String vivadoScriptFileName;
        public String vivadoProjectName;
        public String targetPlatform;
        boolean generateVivadoScript;

        public boolean runScheduler;
        boolean runSynth;
        boolean runImpl;

        Config(ConfigOption invalidOption) {
            super(invalidOption);
        }

        void toDefaults() {
            modelConfigFolderPath = Processor.Instance.getConfigurationPath();
            dcsFilePath = "";
            inputFolderPath = Processor.Instance.getHardwareTemplatePathProcessorRelated();
            outputFolderPath = Processor.Instance.getHardwareDestinationPath() + "/default_out";

            synthAndImplVivadoProperties = "";
            requestedSynthAndImpVivadoReports = "";
            vivadoScriptFileName = "vivado_synth_impl.tcl";
            vivadoProjectName = "cgra_project";
            targetPlatform = "xc7z045ffg900-2";
            generateVivadoScript = true;

            runScheduler = true;
            runSynth = true;
            runImpl = true;
        }

        @Override
        public Class<ConfigOption> getEnumClass() {
            return ConfigOption.class;
        }

        @Override
        protected boolean parseOption(ConfigOption option, String arg) {
            switch (option) {
                case ModelConfigFolderPath:
                    modelConfigFolderPath = arg;
                    return true;
                case InputFolderPath:
                    inputFolderPath = arg;
                    return true;
                case OutputFolderPath:
                    outputFolderPath = arg;
                    return true;
                default:
                    System.err.println("Unable to parse Invalid Config option with arg " + arg);
                    return false;
            }
        }
    }
}
