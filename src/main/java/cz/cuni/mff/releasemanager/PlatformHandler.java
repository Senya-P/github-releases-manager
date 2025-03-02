package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PlatformHandler {
    abstract void install(Path asset);
    abstract void extract(Path asset);
    abstract boolean verifyFormat(Path asset);

    protected Path getShortCut(Path asset) {
        String fileName = asset.getFileName().toString();
        Pattern pattern = Pattern.compile("^[A-Za-z]+");
        Matcher matcher = pattern.matcher(fileName);

        String shortCut = matcher.find() ? matcher.group().toLowerCase() : "";
        return Paths.get(shortCut);
    }
    // refactor - extract 
    public Path saveInputStreamToFile(InputStream stream, String filename) {
        Path dir;
        try {
            dir = createDirectory("releases");
        } catch (IOException ex) {
            return null;
        }
        Path destination = dir.resolve(filename);
        try {
            Files.copy(stream, destination);
        } catch (IOException e) {
            return null;
        }
        return destination;
    }
    // cleanup
    protected void removeTempDir(Path assetPath) {
        try {
            Path dir = assetPath.getParent();
            if (Files.isDirectory(dir) && Files.list(dir).findAny().isEmpty()) {
                Files.delete(dir);
            }
        } catch (IOException e) {
            System.out.println("Failed to remove temporary directory: " + e.getMessage());
        }
    }

    protected Path createDirectory(String directoryName) throws IOException {
        Path path = Paths.get(directoryName);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }
}