package cz.cuni.mff.releasemanager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.tukaani.xz.XZInputStream;

public class LinuxHandler implements PlatformHandler {

    @Override
    public void install(Path asset, Path destination) {
        //extract(asset, destination);
    }

    @Override
    public void extract(Path asset, Path destination) {
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
    public boolean verifyFormat(Path file) {
        return file.toString().toLowerCase().endsWith(".appimage");
    }

}
