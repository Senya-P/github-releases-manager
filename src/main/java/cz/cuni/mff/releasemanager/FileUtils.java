package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {
    public static Path saveInputStreamToFile(InputStream stream, String filename) {
        Path dir;
        try {
            dir = createDirectory("releases");
        } catch (IOException ex) {
            return null;
        }
        Path destination = dir.resolve(filename);
        try {
            Files.copy(stream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return null;
        }
        return destination.toAbsolutePath();
    }

    public static Path createDirectory(String directoryName) throws IOException {
        Path path = Paths.get(directoryName);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    public static Path getShortCut(Path asset) {
        String fileName = asset.getFileName().toString();
        Pattern pattern = Pattern.compile("^[A-Za-z]+");
        Matcher matcher = pattern.matcher(fileName);

        String shortCut = matcher.find() ? matcher.group().toLowerCase() : "";
        return Paths.get(shortCut);
    }

    public static void removeTempDir(Path assetPath) {
        try {
            Path dir = assetPath.getParent();
            if (Files.isDirectory(dir) && Files.list(dir).findAny().isEmpty()) {
                Files.delete(dir);
            }
        } catch (IOException e) {
            System.out.println("Failed to remove directory: " + e.getMessage());
        }
    }
}
