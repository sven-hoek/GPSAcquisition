package io;

public class GeneratedSourceFile extends SourceFile {

    private String sourceString;

    public GeneratedSourceFile(String name, String sourceFolderPath, String relativePath, Extension sourceExtension) {
        super(name, sourceFolderPath, relativePath, sourceExtension);
        this.sourceString = null;
    }

    public void setSourceString(String source) {
        this.sourceString = source;
    }

    @Override
    public void writeToTarget(String targetFolderPath) {
        writeStringToFile(sourceString, fullTargetPath(targetFolderPath));
    }
}
