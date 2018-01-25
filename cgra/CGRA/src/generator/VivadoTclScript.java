package generator;

public class VivadoTclScript {
    private String outputDirTclVarName;
    private String outputDir;
    private String inputDir;

    private String topModuleName;
    private String preamble;
    private StringBuilder fileDirectives;
    private StringBuilder constrDirectives;
    private StringBuilder ipDirectives;

    public VivadoTclScript(String inputDir, String outputDir, String topModuleName) {
        this.outputDirTclVarName = "outputDir";
        this.outputDir = outputDir;
        this.inputDir = inputDir;

        this.topModuleName = topModuleName;
        this.preamble = null;
        this.fileDirectives = new StringBuilder();
        this.constrDirectives = new StringBuilder();
        this.ipDirectives = new StringBuilder();
    }

    public void createPreamble(String projectName, String targetPlatform) {
        String str = "";
        str += ("set " + outputDirTclVarName + " " + outputDir + "\n");
        str += ("file mkdir $" + outputDirTclVarName + "\n");
        str += ("create_project project_" + projectName + " " + outputDir + " \\\n");
        str += ("-part " + targetPlatform + " -force" + "\n");
        preamble = str;
    }

    public void addSourceFile(String filePath) {
        fileDirectives.append("add_files -norecurse -fileset [get_filesets sources_1] ");
        addFile(fileDirectives, filePath);
    }

    public void addConstraintsFile(String filePath) {
        constrDirectives.append("add_files -norecurse -fileset [get_filesets constrs_1] ");
        addFile(constrDirectives, filePath);
    }

    public void addIPFile(String filePath) {
        ipDirectives.append("add_files -norecurse -fileset [get_filesets sources_1] ");
        addFile(ipDirectives, filePath);
    }

    private void addFile(StringBuilder sb, String filePath) {
        sb.append("[glob ");
        sb.append(inputDir);
        sb.append("/");
        sb.append(filePath);
        sb.append("]");
        sb.append("\n");
    }

    public String compile(String runProperties, String reportRequests,
                          boolean runSynth, boolean runImpl)
    {
        if (preamble == null) {
            System.out.println("Missing Vivado TCL script preamble");
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(preamble);
        sb.append(fileDirectives.toString());
        sb.append(constrDirectives.toString());
        sb.append(ipDirectives.toString());

        // Set top module
        sb.append("set_property top ");
        sb.append(topModuleName);
        sb.append(" [get_filesets sources_1]\n");

        // Compile order and IP upgrade
        sb.append("update_compile_order -fileset [get_filesets sources_1]\n");
        sb.append("upgrade_ip [get_ips]\n");

        // Add run properties
        sb.append(runProperties);

        // Add reports
        sb.append(reportRequests);

        if (runSynth || runImpl) {
            sb.append("launch_runs synth_1\n");
            sb.append("wait_on_run synth_1\n");
        }

        if (runImpl) {
            sb.append("launch_runs impl_1\n");
            sb.append("wait_on_run impl_1\n");
        }

        sb.append("exit\n");
        return sb.toString();
    }
}
