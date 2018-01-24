package io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceFileLib {

    private ArrayList<HashMap<String, SourceFile>> sources;

    public SourceFileLib() {
        sources = new ArrayList<>(4);
    }

    public void addSourceFiles(int index, List<SourceFile> newSources) {
        HashMap<String, SourceFile> fileMap = getSourceMap(index);
        for (SourceFile source : newSources)
            addSourceFileToMap(source, fileMap);
    }

    public void addSourceFile(int index, SourceFile source) {
        HashMap<String, SourceFile> fileMap = getSourceMap(index);
        addSourceFileToMap(source, fileMap);
    }

    public <T> T getSourceFile(int index, String relativeName, Class<T> type) {
        if (index < sources.size()) {
            HashMap<String, SourceFile> foundMap = sources.get(index);

            if (foundMap == null) {
                System.err.println("Requested invalid source index");
                return null;
            } else {
                SourceFile sourceFile = foundMap.get(relativeName);

                if (sourceFile == null) {
                    System.err.println("No such source file \"" + relativeName + "\"");
                    return null;
                }

                if (type.isInstance(sourceFile)) {
                    @SuppressWarnings("unchecked")
                    T ret = (T) sourceFile;
                    return ret;
                }
                else {
                    System.err.println("Requested source file "
                            + relativeName + " is not of type " + type.getName());
                    return null;
                }
            }
        } else {
            System.err.println("Requested source index is out of bounds");
            return null;
        }
    }

    public <E extends Enum<E>> void writeFilesToTarget(Class<E> eEnum, String targetFolderPath, List<Boolean> useRelative) {
        for (E sourceIndex : eEnum.getEnumConstants()) {
            int index = sourceIndex.ordinal();
            if (index < sources.size()) {
                HashMap<String, SourceFile> sourceMap = sources.get(index);

                if (sourceMap == null)
                    continue;

                for (SourceFile file : sourceMap.values()) {
                    String targetFilePath;

                    if (useRelative == null || index >= useRelative.size() || !useRelative.get(index))
                        targetFilePath = targetFolderPath;
                    else
                        targetFilePath = targetFolderPath + "/" + sourceIndex.toString();

                    SourceFile.ensurePath(targetFilePath);
                    file.writeToTarget(targetFilePath);
                }
            }
        }
    }

    public void clear() {
        sources.clear();
    }

    private HashMap<String, SourceFile> getSourceMap(int index) {
        sources.ensureCapacity(index + 1);
        while (sources.size() <= index)
            sources.add(null);

        HashMap<String, SourceFile> fileMap = sources.get(index);
        if (fileMap == null) {
            fileMap = new HashMap<>(16);
            sources.set(index, fileMap);
        }

        return fileMap;
    }

    private void addSourceFileToMap(SourceFile source, HashMap<String, SourceFile> fileMap) {
        final String relPath = source.getRelativePath();
        String key;

        if (relPath.isEmpty())
            key = source.getName();
        else
            key = relPath + "/" + source.getName();

        if (fileMap.containsKey(key)) {
            String prevFileName = fileMap.get(key).getName();
            System.out.println("Replacing source file \"" + prevFileName + "\" with \""
                    + source.getName() + "\", path: " + source.fullSourcePath());
            fileMap.replace(key, source);
        } else {
            fileMap.put(key, source);
        }
    }
}
