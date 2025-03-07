package cz.cuni.mff.releasemanager;

import java.nio.file.Path;

public class WindowsHandler extends PlatformHandler {

    private static WindowsHandler instance;

    private WindowsHandler() {}

    public static WindowsHandler getInstance() {
        if (instance == null) {
            instance = new WindowsHandler();
        }
        return instance;
    }

    @Override
    public Path install(Path asset) {
        return null;
    }

    @Override
    public String getFormat() {
        return ".msi";
    }

    @Override
    void createReleasesListFile() {
        throw new UnsupportedOperationException("Unimplemented method 'createConfig'");
    }

    @Override
    protected Path getReleasesListDirLocation() {
        throw new UnsupportedOperationException("Unimplemented method 'getReleasesListLocation'");
    }

}
