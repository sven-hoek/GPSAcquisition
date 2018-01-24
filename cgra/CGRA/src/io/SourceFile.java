package io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class SourceFile {

    public enum Extension {
        Undefined(""),
        ExtH("h"),
        ExtC("c"),
        ExtV("v"),
        ExtVH("vh"),
        ExtSV("sv"),
        ExtSTG("stg"),
        ExtXCIX("xcix"),
        ExtTF("tf");

        public final String ext;

        Extension(String ext) {
            this.ext = ext;
        }
    }

    final private String name;
    final private String sourceFolderPath;
    final private String relativePath;

    final private Extension extension;

    SourceFile(String name, String sourceFolderPath, String relativePath, Extension sourceExtension) {
        this.name = name;
        this.sourceFolderPath = sourceFolderPath;
        this.relativePath = relativePath;
        this.extension = sourceExtension;
    }

    public String fullSourcePath() {
        return fullPath(sourceFolderPath);
    }

    public String fullTargetPath(String targetFolderPath) {
        return fullPath(targetFolderPath);
    }

    public void writeToTarget(String targetFolderPath) {
        Path source = Paths.get(fullSourcePath());
        Path destination = Paths.get(fullTargetPath(targetFolderPath));

        try {
            Files.copy(source, destination, REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Could not copy file " + source);
        }
    }

    /**
     * Find all {@link SourceFile}s in the given path (recursively).
     * @param path the path to search
     * @return a list of the found {@link SourceFile}s
     */
    public static List<SourceFile> findInPath(String path, boolean recursive) {
        ArrayList<SourceFile> foundSourceFiles = new ArrayList<>(32);
        findInPathInner(path, "", foundSourceFiles, recursive);
        return foundSourceFiles;
    }

    /**
     * Makes sure that the target path exists.
     * @param path to create if necessary
     */
    public static void ensurePath(String path) {
        File file = new File(path);

        if (file.exists())
            return;

        if (!file.mkdirs())
            System.err.println("Could not create dir \"" + path + "\"");
    }

    private static void findInPathInner(final String basePath, final String currentRelativePath,
                                        final List<SourceFile> foundSources, boolean recursive)
    {
        final String pathToSearch = basePath + "/" + currentRelativePath;
        final File dir = new File(pathToSearch);

        if (!dir.exists()) {
            System.err.printf("Specified folder %s does not exist.\n", pathToSearch);
            return;
        }

        if (!dir.isDirectory()) {
            System.err.println("Expected a directory to search for source files!");
            return;
        }

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                if (recursive)
                    findInPathInner(basePath, currentRelativePath + "/" + file.getName(),
                            foundSources, true);
                else
                    continue;
            }

            int dot = file.getName().lastIndexOf('.');
            Extension extension = Extension.Undefined;

            if (dot > 0) {
                String ext = file.getName().substring(dot + 1);

                switch (ext) {
                    case "h":       extension = Extension.ExtH; break;
                    case "c":       extension = Extension.ExtC; break;
                    case "v":       extension = Extension.ExtV; break;
                    case "vh":      extension = Extension.ExtVH; break;
                    case "sv":      extension = Extension.ExtSV; break;
                    case "stg":     extension = Extension.ExtSTG; break;
                    case "xcic":    extension = Extension.ExtXCIX; break;
                    case "tf":      extension = Extension.ExtTF; break;
                    default:        extension = Extension.Undefined; break;
                }
            }

            if (extension == Extension.ExtSTG) {
                TemplateSourceFile source;
                source = new TemplateSourceFile(file.getName(), basePath, currentRelativePath, extension);
                source.createTemplates(null);
                foundSources.add(source);
            } else
                foundSources.add(new SourceFile(file.getName(), basePath, currentRelativePath, extension));
        }
    }

    protected void writeStringToFile(String str, String filePath) {
        try {
            FileWriter writer = new FileWriter(new File(filePath));
            writer.write(str);
            writer.close();
        } catch (IOException e) {
            System.err.printf("Could not write file \"%s\"", filePath);
            e.printStackTrace();
        }
    }

    String fullPath(String basePath) {
        if (relativePath.isEmpty())
            return basePath + "/" + name;
        else
            return basePath + "/" + relativePath + "/" + name;
    }

    String nameWithoutExt() {
        int dot = name.lastIndexOf('.');
        return name.substring(0, dot);
    }

    public String getName() {
        return name;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public Extension getExtension() {
        return extension;
    }


}
