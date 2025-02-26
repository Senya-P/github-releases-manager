package cz.cuni.mff.releasemanager;

import java.nio.file.Path;

public class WindowsHandler implements PlatformHandler {

    @Override
    public void install(Path asset) {
        
    }

    @Override
    public void extract(Path asset) {
        
    }

    @Override
    public boolean verifyFormat(String fileName) {
        return fileName.toLowerCase().endsWith(".msi");
    }

}
