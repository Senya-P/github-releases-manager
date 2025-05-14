package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import cz.cuni.mff.releasemanager.types.ReleaseInfo;
import cz.cuni.mff.releasemanager.types.ReleasesList;

public abstract class PlatformHandler {
    protected static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .enable(SerializationFeature.INDENT_OUTPUT);
    protected static final String RELEASES_LIST_FILE = "releases.json";
    abstract Path install(Path asset);
    abstract void createReleasesListFile();
    abstract String getFormat();
    protected abstract Path getReleasesListDirLocation();

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
            Files.copy(stream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return null;
            // file already exists
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
            System.out.println("Failed to remove directory: " + e.getMessage());
        }
    }

    protected Path createDirectory(String directoryName) throws IOException {
        Path path = Paths.get(directoryName);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    protected Path getReleasesListFileLocation() {
        return getReleasesListDirLocation().resolve(RELEASES_LIST_FILE);
    }

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

    public ReleasesList loadReleasesList() throws IOException {
        Path releasesFile = getReleasesListFileLocation();
        if (!Files.exists(releasesFile)) {
            return null;
        }
        return mapper.readValue(releasesFile.toFile(), ReleasesList.class);
    }

    public void uninstall(Path asset) throws IOException {
        if (Files.isRegularFile(asset)) {
            Files.deleteIfExists(asset);
        }
    }

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
                removeTempDir(getReleasesListFileLocation());
                return;
            }
            Path releasesFile = getReleasesListFileLocation();
            mapper.writeValue(releasesFile.toFile(), new ReleasesList(currentReleases));
        } catch (IOException e) {
            System.out.println("Failed to remove release from list: " + e.getMessage());
        }
    }
}