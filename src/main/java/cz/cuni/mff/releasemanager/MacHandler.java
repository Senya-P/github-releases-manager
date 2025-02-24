package cz.cuni.mff.releasemanager;

import java.nio.file.Path;

public class MacHandler implements PlatformHandler {

    @Override
    public void install(Path asset, Path destination) {
        
    }

    @Override
    public void extract(Path asset, Path destination) {
        
    }

    @Override
    public boolean verifyFormat(Path file) {
        return false;
    }

}
