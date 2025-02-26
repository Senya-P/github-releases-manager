package cz.cuni.mff.releasemanager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.tukaani.xz.XZInputStream;

public class LinuxHandler implements PlatformHandler {

    @Override
    public void install(Path asset) {
        try {
            // Set executable permissions
            Files.setPosixFilePermissions(asset, Set.of(
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

            Path target = targetDir.resolve(asset.getFileName());
            Files.move(asset, target, StandardCopyOption.REPLACE_EXISTING);
            // system-wide? -> sudo
            // entry in PATH
            System.out.println("AppImage installed to: " + target);

        } catch (IOException e) {
            System.out.println("AppImage installation failed");
        }
    }

    @Override
    public void extract(Path asset) {
        Path dest = Paths.get("dest");
        if (!Files.exists(dest)) {
            try {
                Files.createDirectories(dest);
            } catch (IOException e) {
            }
        }
        Path destination = dest.resolve(asset.toString().split("\\.")[0]);
        try (InputStream fi = Files.newInputStream(asset);
            InputStream bi = new BufferedInputStream(fi);
            InputStream xzi = new XZInputStream(bi);
            TarArchiveInputStream ti = new TarArchiveInputStream(xzi)) {
                TarArchiveEntry entry;
                while ((entry = ti.getNextTarEntry()) != null) {
                    Path outputPath = destination.resolve(entry.getName());

                    if (entry.isDirectory()) {
                        Files.createDirectories(outputPath);
                    } else {
                        Files.createDirectories(outputPath.getParent());
                        Files.copy(ti, outputPath);
                    }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public boolean verifyFormat(String fileName) {
        return fileName.toLowerCase().endsWith(".appimage");
    }

}
