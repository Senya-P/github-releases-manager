package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MacHandler extends PlatformHandler {

    private static MacHandler instance;
    private static final Path MOUNT_DIR = Paths.get(
        System.getProperty("user.home"),
        "Volumes"
    );

    private MacHandler() {}

    public static MacHandler getInstance() {
        if (instance == null) {
            instance = new MacHandler();
        }
        return instance;
    }

    @Override
    public Path install(Path asset) {
        try {
            Process process = new ProcessBuilder()
                .command("hdiutil", "attach", "-nobrowse", asset.toString())
                .inheritIO()
                .start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Installation failed with exit code: " + exitCode);
            }
            Path appName = getShortCut(asset);

            Path targetDir = Paths.get(System.getProperty("user.home"), "Applications");
            Files.createDirectories(targetDir);

            Path app = findAppInMountDir(appName);
            Process copyProcess = new ProcessBuilder()
                .command("cp", "-rf", app.toString(), targetDir.toString())
                .start();
            if (copyProcess.waitFor() != 0) {
                throw new IOException("Failed to copy application.");
            }
            detach(app);
            return targetDir.resolve(appName);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Installation error: " + e.getMessage());
            return null;
        }
    }

    private Path findAppInMountDir(Path appName) throws IOException {
        return Files.walk(MOUNT_DIR)
            .filter(path -> {
                String fileName = path.getFileName().toString();
                if (!Files.isDirectory(path)) {
                    return fileName.equalsIgnoreCase(appName.toString());
                }
                return fileName.substring(0, fileName.length() - 4).equalsIgnoreCase(appName.toString());
            })
            .findFirst()
            .orElseThrow(() -> new IOException("Error finding app."));
    }

    private void detach(Path mountPath) throws IOException, InterruptedException {
        Process process = new ProcessBuilder()
                .command("hdiutil", "detach", mountPath.toString())
                .inheritIO()
                .start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Installation failed with exit code: " + exitCode);
        }
    }

    @Override
    public void uninstall(Path asset) {
        try {
            Process process = new ProcessBuilder()
                .command("rm", "-rf", asset.toString())
                .start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Uninstallation failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Uninstallation error: " + e.getMessage());
        }
    }

    @Override
    public String getFormat() {
        return ".dmg";
    }

    @Override
    void createReleasesListFile() {
        Path releasesFile = getReleasesListFileLocation();
        Path releasesDir = getReleasesListDirLocation();
        if (Files.exists(releasesFile)) {
            return;
        }
        try {
            Files.createDirectories(releasesDir);
            Files.createFile(releasesFile);
        } catch (IOException e) {
            System.out.println("Failed to create : " + e.getMessage());
        }
    }

    @Override
    protected Path getReleasesListDirLocation() {
        return Paths.get(System.getProperty("user.home"), "Library", "Application Support", APP_DATA_DIR);
    }

}
