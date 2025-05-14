package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class WindowsHandler extends PlatformHandler {

    private static WindowsHandler instance;

    private static final Path INSTALLER_STORAGE = Paths.get(
        System.getenv("ProgramData"),
        APP_DATA_DIR
    );

    private WindowsHandler() {}

    public static WindowsHandler getInstance() {
        if (instance == null) {
            instance = new WindowsHandler();
        }
        return instance;
    }

    @Override
    public Path install(Path assetPath) {
        try {
            Process process = new ProcessBuilder()
                .command("msiexec", "/i", assetPath.toString())
                .inheritIO()
                .start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Installation failed with exit code: " + exitCode);
            }

            Path targetDir = INSTALLER_STORAGE;
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(assetPath.getFileName());
            Files.move(assetPath, target, StandardCopyOption.REPLACE_EXISTING);
            removeTempDir(assetPath);

            Path shortCut = getShortCut(assetPath);
            System.out.println("Installed to: " + Paths.get(System.getenv("ProgramFiles"), shortCut.toString()).toString());
            return target;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Installation error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void uninstall(Path asset) {
        Process process;
        try {
            process = new ProcessBuilder()
                    .command("msiexec", "/x", asset.toString())
                    .inheritIO()
                    .start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Uninstallation failed with exit code: " + exitCode);
            }
            Files.deleteIfExists(asset);
            removeTempDir(asset);
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
            System.out.println("Uninstallation error: " + e.getMessage());
        }
    }

    @Override
    public String getFormat() {
        return ".msi";
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
        return Paths.get(
            System.getenv("APPDATA"),
            APP_DATA_DIR
        );
    }

}
