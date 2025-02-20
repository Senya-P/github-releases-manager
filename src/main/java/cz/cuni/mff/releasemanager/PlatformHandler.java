package cz.cuni.mff.releasemanager;

import java.nio.file.Path;

public interface PlatformHandler {
    void install(Path assertPath);
    void extract(Path assertPath);
    void resolveExtension(String[] extensions);
    


}