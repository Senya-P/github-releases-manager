package cz.cuni.mff.releasemanager;

import java.nio.file.Path;

public interface PlatformHandler {
    void install(Path asset);
    void extract(Path asset);
    boolean verifyFormat(String fileName);

}