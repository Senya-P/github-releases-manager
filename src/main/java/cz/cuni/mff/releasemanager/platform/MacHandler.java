package cz.cuni.mff.releasemanager.platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import cz.cuni.mff.releasemanager.utils.FileUtils;

/**
 * MacHandler is responsible for handling the installation and uninstallation of applications on macOS systems.
 */
public final class MacHandler extends PlatformHandler {

    private static MacHandler instance;
    // using a mount point under the home directory to avoid permission issues
    private static final Path MOUNT_DIR = Paths.get(System.getProperty("user.home"), "Volumes");

    private MacHandler() {}

    /**
     * @return  Singleton instance of MacHandler.
     */
    public static MacHandler getInstance() {
        if (instance == null) {
            instance = new MacHandler();
        }
        return instance;
    }

    @Override
    public Path install(Path asset) {
        try {
            mount(asset);

            Path targetDir = Paths.get("/Applications");
            Files.createDirectories(targetDir);

            String appName = FileUtils.getShortCut(asset);
            Path app = findAppInMountDir(appName);

            Process copyProcess = new ProcessBuilder()
                .command("cp", "-rf", app.toString(), targetDir.toString())
                .start();
            if (copyProcess.waitFor() != 0) {
                throw new IOException("Failed to copy application.");
            }
            detach();
            return targetDir.resolve(app.getFileName());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Installation error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Mounts the asset to a mount directory.
     * @param asset Path to the asset.
     * @throws IOException
     * @throws InterruptedException
     */
    private void mount(Path asset) throws IOException, InterruptedException {
        Files.createDirectories(MOUNT_DIR);
        Files.setPosixFilePermissions(MOUNT_DIR, Set.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_READ,
                PosixFilePermission.GROUP_EXECUTE,
                PosixFilePermission.OTHERS_READ,
                PosixFilePermission.OTHERS_EXECUTE
        ));
        Process process = new ProcessBuilder()
                .command("hdiutil", "attach", "-nobrowse", "-mountpoint",
                    MOUNT_DIR.toAbsolutePath().toString(), 
                    asset.toString())
                .inheritIO()
                .start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Installation failed with exit code: " + exitCode);
        }
    }

    /**
     * Finds the downloaded application in the mount directory.
     * @param appName Name of the application.
     * @return Path to the application.
     * @throws IOException
     */
    private Path findAppInMountDir(String appName) throws IOException {
        return Files.walk(MOUNT_DIR)
            .filter(path -> {
                String fileName = path.getFileName().toString();
                if (!Files.isDirectory(path)) {
                    return fileName.equalsIgnoreCase(appName);
                }
                return fileName.substring(0, fileName.length() - 4).equalsIgnoreCase(appName);
            })
            .findFirst()
            .orElseThrow(() -> new IOException("Error finding app."));
    }

    /**
     * Detaches the mounted application.
     * @throws IOException
     * @throws InterruptedException
     */
    private void detach() throws IOException, InterruptedException {
        Process process = new ProcessBuilder()
                .command("hdiutil", "detach", MOUNT_DIR.toString(), "-force")
                .inheritIO()
                .start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Detaching failed with exit code: " + exitCode);
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
    public String[] getFormats() {
        return new String[] { ".dmg" };
    }

    @Override
    protected void createReleasesListFile() {
        Path releasesFile = getReleasesListFileLocation();
        Path releasesDir = getReleasesListDirLocation();
        if (Files.exists(releasesFile)) {
            return;
        }
        try {
            Files.createDirectories(releasesDir);
            Files.createFile(releasesFile);
            Files.setPosixFilePermissions(releasesFile, Set.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE
            ));
        } catch (IOException e) {
            System.out.println("Failed to create : " + e.getMessage());
        }
    }

    @Override
    protected Path getReleasesListDirLocation() {
        return Paths.get(System.getProperty("user.home"), "Library", "Application Support", APP_DATA_DIR);
    }
}
