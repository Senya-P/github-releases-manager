package cz.cuni.mff.releasemanager;

import java.nio.file.Path;

public interface PlatformHandler {
    void install(Path asset, Path destination);
    void extract(Path asset, Path destination);
    boolean verifyFormat(Path file);

}