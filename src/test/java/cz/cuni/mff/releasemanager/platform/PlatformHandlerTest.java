package cz.cuni.mff.releasemanager.platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cz.cuni.mff.releasemanager.types.Asset;
import cz.cuni.mff.releasemanager.types.ReleaseInfo;
import cz.cuni.mff.releasemanager.types.ReleasesList;

class PlatformHandlerTest {

    class TestPlatformHandler extends PlatformHandler {
        private final String temp = "temp";

        @Override
        public Path install(Path asset) { return null; }

        @Override
        public void uninstall(Path asset) {}

        @Override
        protected void createReleasesListFile() {
            try {
                Files.createDirectories(getReleasesListDirLocation());
                Path releasesListFile = getReleasesListFileLocation();
                if (!Files.exists(releasesListFile)) {
                    Files.createFile(releasesListFile);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String[] getFormats() { return new String[] { "zip" }; }

        @Override
        protected Path getReleasesListDirLocation() { return tempDir.resolve(temp); }
    }

    private Path tempDir;
    private PlatformHandler handler;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createDirectories(Path.of("releases_test"));
        handler = new TestPlatformHandler();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a)) // files before directories
                .forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException e) {}
                });
            }
        Files.deleteIfExists(tempDir);
    }

    private ReleaseInfo createRelease(String repo) {
        Asset asset = new Asset("url", "name");
        return new ReleaseInfo(repo,  Instant.now(), "path", asset);
    }

    @Test
    void testAddReleaseToList() throws IOException {
        ReleaseInfo release = createRelease("repo1");
        handler.addReleaseToList(release);

        ReleasesList list = handler.loadReleasesList();
        assertNotNull(list);
        assertEquals(1, list.releases().size());
        assertEquals("repo1", list.releases().get(0).repo());
    }

    @Test
    void testAddReleaseToList_updateExisting() throws IOException {
        ReleaseInfo release1 = createRelease("repo1");
        handler.addReleaseToList(release1);

        ReleaseInfo release2 = createRelease("repo1");
        handler.addReleaseToList(release2);

        ReleasesList list = handler.loadReleasesList();
        assertNotNull(list);
        assertEquals(1, list.releases().size());
        assertEquals("repo1", list.releases().get(0).repo());
    }

    @Test
    void testRemoveReleaseFromList() throws IOException {
        ReleaseInfo release1 = createRelease("repo1");
        ReleaseInfo release2 = createRelease("repo2");
        handler.addReleaseToList(release1);
        handler.addReleaseToList(release2);

        handler.removeReleaseFromList(release1);

        ReleasesList list = handler.loadReleasesList();
        assertNotNull(list);
        assertEquals(1, list.releases().size());
        assertEquals("repo2", list.releases().get(0).repo());
    }

    @Test
    void testRemoveReleaseFromList_deletesFileIfEmpty() throws IOException {
        ReleaseInfo release = createRelease("repo1");
        handler.addReleaseToList(release);

        handler.removeReleaseFromList(release);

        assertFalse(Files.exists(handler.getReleasesListFileLocation()));
    }

    @Test
    void testLoadReleasesList_returnsNullIfFileDoesNotExist() throws IOException {
        assertNull(handler.loadReleasesList());
    }
}