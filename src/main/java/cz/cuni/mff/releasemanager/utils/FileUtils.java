package cz.cuni.mff.releasemanager.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for file operations.
 */
public class FileUtils {
    /**
     * Saves an InputStream to a file with the given filename in the "releases" directory.
     * @param stream
     * @param filename
     * @return
    */
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
    /**
     * Creates a directory with the given name if it does not already exist.
     * @param directoryName
     * @return Path to the created directory
     * @throws IOException
     */
    public static Path createDirectory(String directoryName) throws IOException {
        Path path = Paths.get(directoryName);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }
    /**
     * Creates a short name for the given file
     * @param file Path to the file
     * @return Path to the short name
     */
    public static String getShortCut(Path file) {
        String fileName = file.getFileName().toString();
        Pattern pattern = Pattern.compile("^[A-Za-z]+");
        Matcher matcher = pattern.matcher(fileName);

        return matcher.find() ? matcher.group().toLowerCase() : "";
    }

    /**
     * Removes the parent temporary directory if it is empty.
     * @param file Path to the file
     */
    public static void removeTempDir(Path file) {
        try {
            Path dir = file.getParent();
            if (Files.isDirectory(dir) && Files.list(dir).findAny().isEmpty()) {
                Files.delete(dir);
            }
        } catch (IOException e) {
            System.out.println("Failed to remove directory: " + e.getMessage());
        }
    }
}
