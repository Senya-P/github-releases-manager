package cz.cuni.mff.releasemanager;

import java.nio.file.Path;

public class MacHandler extends PlatformHandler {

    @Override
    public void install(Path asset) {
    }

    @Override
    public void extract(Path asset) {
        
    }

    @Override
    public boolean verifyFormat(Path asset) {
        return asset.toString().toLowerCase().endsWith(".dmg");
    }

}
