package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class MacHandler extends PlatformHandler {

    private static MacHandler instance;
    private static final Path MOUNT_DIR = Paths.get("/Volumes");

    private MacHandler() {}

    public static MacHandler getInstance() {
        if (instance == null) {
            instance = new MacHandler();
        }
        return instance;
    }

    @Override
    public Path install(Path asset) {
        System.out.println("install - asset: " + asset.toString());
        try {
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
                .command("hdiutil", "attach", "-nobrowse", "-verbose", "-mountpoint", MOUNT_DIR.toAbsolutePath().toString(), asset.toString())
                .inheritIO()
                .start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Installation failed with exit code: " + exitCode);
            }
            Path appName = getShortCut(asset);
                System.out.println("appName: " + appName);
            Path targetDir = Paths.get("/Applications");
            Files.createDirectories(targetDir);

            Path app = findAppInMountDir(appName);
                System.out.println("app: " + app.toString());
            Process copyProcess = new ProcessBuilder()
                .command("cp", "-rf", app.toString(), targetDir.toString())
                .start();
            if (copyProcess.waitFor() != 0) {
                throw new IOException("Failed to copy application.");
            }
            detach(app);
                System.out.println("return: " + targetDir.resolve(app).toString());
            return targetDir.resolve(app);
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
                    System.out.println("filename: " + fileName);
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
        return Paths.get("/Library", "Application Support", APP_DATA_DIR);
    }

}
