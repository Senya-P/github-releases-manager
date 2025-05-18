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
            String fileName = assetPath.getFileName().toString();
            if (fileName.endsWith(".msi")) {
                return installMsi(assetPath);
            } else if (fileName.endsWith(".exe")) {
                return installExe(assetPath);
            } else {
                System.out.println("Unsupported file format: " + fileName);
                return null;
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Installation error: " + e.getMessage());
            return null;
        }
    }
    private Path installMsi(Path assetPath) throws IOException, InterruptedException {
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
    }

    private Path installExe(Path assetPath) throws IOException, InterruptedException {
        Path shortCut = getShortCut(assetPath);
        Path targetDir = Paths.get(System.getenv("ProgramFiles"), shortCut.toString());
        String command = String.format("\"%s\" /S /D=%s", assetPath.toString(), targetDir.toString());
        Process process = new ProcessBuilder("cmd.exe", "/c", command)
            .inheritIO()
            .start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Installation failed with exit code: " + exitCode);
        }

        Files.deleteIfExists(assetPath);
        removeTempDir(assetPath);
        System.out.println("Installed to: " + targetDir.toString());

        Path uninstallPath = findUninstallPath(targetDir);
        return uninstallPath;
    }

    private Path findUninstallPath(Path targetDir) throws IOException {
        return Files.walk(targetDir)
            .filter(path -> {
                String fileName = path.getFileName().toString();
                if (!Files.isDirectory(path)) {
                    return fileName.contains("uninst") && fileName.endsWith(".exe");
                }
                return false;
            })
            .findFirst()
            .orElseThrow(() ->new IOException("Uninstall path not found in: " + targetDir.toString()));
    }

    @Override
    public void uninstall(Path asset) {

        try {
            String fileName = asset.getFileName().toString();
            if (fileName.endsWith(".msi")) {
                uninstallMsi(asset);
            } else if (fileName.endsWith(".exe")) {
                uninstallExe(asset);
            } else {
                System.out.println("Unsupported file format: " + fileName);
            }
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
            System.out.println("Uninstallation error: " + e.getMessage());
        }
    }

    private void uninstallExe(Path asset) throws IOException, InterruptedException {
        Process process = new ProcessBuilder()
            .command("cmd.exe", "/c", asset.toString(), "/S")
            .inheritIO()
            .start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Uninstallation failed with exit code: " + exitCode);
        }
    }

    private void uninstallMsi(Path asset) throws IOException, InterruptedException {
        Process process = new ProcessBuilder()
            .command("msiexec", "/x", asset.toString())
            .inheritIO()
            .start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Uninstallation failed with exit code: " + exitCode);
        }
        Files.deleteIfExists(asset);
        removeTempDir(asset);
    }

    @Override
    public String[] getFormats() {
        return new String[] {".msi", ".exe"};
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
