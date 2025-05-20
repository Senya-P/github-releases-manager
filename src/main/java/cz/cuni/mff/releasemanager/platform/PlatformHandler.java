package cz.cuni.mff.releasemanager.platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import cz.cuni.mff.releasemanager.types.ReleaseInfo;
import cz.cuni.mff.releasemanager.types.ReleasesList;
import cz.cuni.mff.releasemanager.utils.FileUtils;

/**
 * Abstract class for handling platform-specific operations.
 * This class provides methods for installing, uninstalling, and managing the list of installed releases.
 */
public abstract class PlatformHandler {
    protected static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .enable(SerializationFeature.INDENT_OUTPUT);
    protected static final String RELEASES_LIST_FILE = "releases.json";
    protected static final String APP_DATA_DIR = "github-release-manager";
    /**
     * * Installs the asset.
     * @param asset Path to the asset to install.
     * @return Path to the installed application which is then used for uninstallation.
     */
    public abstract Path install(Path asset);
    /**
     * Uninstalls the asset.
     * @param asset Path to the asset to uninstall.
     */
    public abstract void uninstall(Path asset);
    /**
     * Creates the config file with information about installed releases.
     */
    protected abstract void createReleasesListFile();
    /**
     * @return Supported file formats for installation.
     */
    public abstract String[] getFormats();
    /**
     * @return Path to the directory where the config file is stored.
     */
    protected abstract Path getReleasesListDirLocation();


    /**
     * @return Path to the config file with a list of installed releases.
     */
    protected Path getReleasesListFileLocation() {
        return getReleasesListDirLocation().resolve(RELEASES_LIST_FILE);
    }

    /**
     * Adds a release to the list of installed releases.
     * If file does not exist, it creates a new one.
     * If the release already exists, it updates the existing entry.
     * @param release ReleaseInfo object containing the information about the release to add.
     */
    public void addReleaseToList(ReleaseInfo release) throws IOException {
        ReleasesList releasesList = loadReleasesList();
        if (releasesList == null) {
            createReleasesListFile();
            releasesList = new ReleasesList(new ArrayList<>());
        }
        List<ReleaseInfo> currentReleases = new ArrayList<>(releasesList.releases());
        boolean found = false;
        for (int i = 0; i < currentReleases.size(); i++) {
            if (currentReleases.get(i).repo().equals(release.repo())) {
                currentReleases.set(i, release);
                found = true;
                break;
            }
        }
        if (!found) {
            currentReleases.add(release); 
        }

        Path releasesFile = getReleasesListFileLocation();
        mapper.writeValue(releasesFile.toFile(), new ReleasesList(currentReleases));
    }

    /**
     * Loads the list of installed releases from the config file.
     * @return ReleasesList object containing the list of installed releases.
     * @throws IOException
     */
    public ReleasesList loadReleasesList() throws IOException {
        Path releasesFile = getReleasesListFileLocation();
        if (!Files.exists(releasesFile)) {
            return null;
        }
        return mapper.readValue(releasesFile.toFile(), ReleasesList.class);
    }

    /**
     * Removes a release from the list of installed releases.
     * @param release ReleaseInfo object containing the information about the release to remove.
     * If the list is empty after removal, the file is deleted.
     */
    public void removeReleaseFromList(ReleaseInfo release) {
        try {
            ReleasesList releasesList = loadReleasesList();
            if (releasesList == null) {
                return;
            }
            List<ReleaseInfo> currentReleases = new ArrayList<>(releasesList.releases());
            currentReleases.removeIf(r -> r.repo().equals(release.repo()));
            if (currentReleases.isEmpty()) {
                Files.deleteIfExists(getReleasesListFileLocation());
                FileUtils.removeTempDir(getReleasesListFileLocation());
                return;
            }
            Path releasesFile = getReleasesListFileLocation();
            mapper.writeValue(releasesFile.toFile(), new ReleasesList(currentReleases));
        } catch (IOException e) {
            System.out.println("Failed to remove release from list: " + e.getMessage());
        }
    }
}