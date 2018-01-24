package io;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

public class TemplateSourceFile extends SourceFile {

    /**
     * The String Template Group File of the loaded source file.
     * Has to hold a main template which will be rendered and written
     * to the target file once all generators are done with it.
     * May contain as many additional templates as required by the generators.
     */
    private STGroupFile stGroupFile;

    /**
     * The main template of the stg file {@link #stGroupFile}
     * It should have the same name as the source file (without extension).
     */
    private ST targetTemplate;

    /**
     * Name of the File to write the template to, without extension!
     * If this name is not set, the source file name is used with the
     * extension in {@link #targetExtension}
     */
    private String targetFileName;

    /**
     * The extension of the File to write the filled out template to.
     */
    private Extension targetExtension;

    TemplateSourceFile(String name, String folderPath, String relativePath, Extension sourceExtension)
    {
        super(name, folderPath, relativePath, sourceExtension);
        this.stGroupFile = null;
        this.targetTemplate = null;
        this.targetFileName = null;
        this.targetExtension = Extension.Undefined;
    }

    void createTemplates(Map<String, Character> specialDelimMap) {
        if (stGroupFile != null || targetTemplate != null)
            System.out.println("Recreating templates for " + getName());

        if (specialDelimMap != null && specialDelimMap.containsKey(getName())) {
            char delim = specialDelimMap.get(getName());
            stGroupFile = new STGroupFile(fullSourcePath(), delim, delim);
        } else {
            stGroupFile = new STGroupFile(fullSourcePath(), '§', '§');
        }

        targetTemplate = stGroupFile.getInstanceOf(nameWithoutExt());

        if (targetTemplate == null)
            throw new RuntimeException("Could not get main template from file " + getName()
                                        + ", see that the main template name matches the file name");
    }

    public ST getST(String name) {
        return stGroupFile.getInstanceOf(name);
    }

    @Override
    public String fullTargetPath(String basePath) {
        String targetPath = basePath;

        if (getRelativePath().isEmpty())
            targetPath += "/" + getRelativePath();

        String fileName = targetFileName == null ? nameWithoutExt() : targetFileName;
        targetPath += "/" + fileName + "." + targetExtension.ext;

        return targetPath;
    }

    @Override
    public void writeToTarget(String targetFolderPath) {
        if (targetTemplate == null || targetExtension == Extension.Undefined)
            return;

        writeStringToFile(targetTemplateToString(), fullTargetPath(targetFolderPath));
    }

    private String targetTemplateToString() {
        return targetTemplate.render();
    }

    public Extension getTargetExtension() {
        return targetExtension;
    }

    public void setTargetExtension(Extension targetExtension) {
        this.targetExtension = targetExtension;
    }

    public STGroupFile getStGroupFile() {
        return stGroupFile;
    }

    public ST getTargetTemplate() {
        return targetTemplate;
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }
}

