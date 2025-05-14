package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class LinuxHandler extends PlatformHandler {

    private static LinuxHandler instance;

    private LinuxHandler() {}

    public static LinuxHandler getInstance() {
        if (instance == null) {
            instance = new LinuxHandler();
        }
        return instance;
    }

    @Override
    public Path install(Path assetPath) {
        try {
            Files.setPosixFilePermissions(assetPath, Set.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_READ,
                PosixFilePermission.GROUP_EXECUTE,
                PosixFilePermission.OTHERS_READ,
                PosixFilePermission.OTHERS_EXECUTE
            ));

            // install for user
            Path targetDir = Paths.get(System.getProperty("user.home"), ".local/bin");
            Files.createDirectories(targetDir);
            Path shortCut = getShortCut(assetPath);
            Path target = targetDir.resolve(shortCut);
            Files.move(assetPath, target, StandardCopyOption.REPLACE_EXISTING);
            // system-wide? -> sudo
            // entry in PATH
            System.out.println("Installed to: " + target);
            removeTempDir(assetPath);
            return target;

        } catch (IOException e) {
            System.out.println("Installation failed");
        }
        return null;
    }

    @Override
    public void uninstall(Path asset) {
        if (Files.isRegularFile(asset)) {
            try {
                Files.deleteIfExists(asset);
            } catch (IOException e) {
                System.out.println("Failed to uninstall: " + e.getMessage());
            }
        }
    }

    @Override
    public String getFormat() {
        return ".appimage";
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
        return Paths.get(System.getProperty("user.home"), ".local", "share", APP_DATA_DIR);
    }
}
