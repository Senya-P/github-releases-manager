package cz.cuni.mff.releasemanager.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;




public class FileUtilsTest {

    private static final String TEST_FILE = "TestFile123.txt";
    private static final String TEST_DIR = "tempTestDir";
    private static final String TEMP_DIR = "releases";

    @Test
    public void testSaveInputStreamToFile() throws IOException {
        String content = "Hello, world!";
        InputStream stream = new ByteArrayInputStream(content.getBytes());
        Path savedPath = FileUtils.saveInputStreamToFile(stream, TEST_FILE);

        assertNotNull(savedPath);
        assertTrue(Files.exists(savedPath));
        assertTrue(savedPath.getParent().getFileName().toString().equals(TEMP_DIR));
        assertEquals(content, new String(Files.readAllBytes(savedPath)));

        Files.deleteIfExists(savedPath);
        Files.deleteIfExists(Paths.get(TEMP_DIR));
    }

    @Test
    public void testCreateDirectory_createsAndReturnsPath() throws IOException {
        Path dir = FileUtils.createDirectory(TEST_DIR);

        assertTrue(Files.exists(dir));
        assertTrue(Files.isDirectory(dir));

        Files.deleteIfExists(dir);
    }

    @Test
    public void testCreateDirectory_alreadyExists() throws IOException {
        Path dir1 = FileUtils.createDirectory(TEST_DIR);
        Path dir2 = FileUtils.createDirectory(TEST_DIR);

        assertEquals(dir1, dir2);

        Files.deleteIfExists(dir1);
        Files.deleteIfExists(dir2);
    }

    @Test
    public void testGetShortCut_withAlphaPrefix() {
        Path file = Paths.get("Release_v1.0.txt");
        String shortcut = FileUtils.getShortCut(file);

        assertEquals("release", shortcut);
    }

    @Test
    public void testGetShortCut_withoutAlphaPrefix() {
        Path file = Paths.get("123file.txt");
        String shortcut = FileUtils.getShortCut(file);

        assertEquals("", shortcut);
    }

    @Test
    public void testGetShortCut_emptyFileName() {
        Path file = Paths.get("");
        String shortcut = FileUtils.getShortCut(file);

        assertEquals("", shortcut);
    }

    @Test
    public void testRemoveTempDir() throws IOException {
        Path dir = Files.createTempDirectory(TEST_DIR);
        Path file = dir.resolve(TEST_FILE);
        Files.createFile(file);
        Files.delete(file);
        FileUtils.removeTempDir(file);

        assertFalse(Files.exists(dir));

        Files.deleteIfExists(dir);
    }

    @Test
    public void testRemoveTempDir_dirIsNotEmpty() throws IOException {
        Path dir = Files.createTempDirectory(TEST_DIR);
        Path file1 = dir.resolve("dummy1.txt");
        Path file2 = dir.resolve("dummy2.txt");
        Files.createFile(file1);
        Files.createFile(file2);
        FileUtils.removeTempDir(file1);

        assertTrue(Files.exists(dir));

        Files.deleteIfExists(file1);
        Files.deleteIfExists(file2);
        Files.deleteIfExists(dir);
    }
}