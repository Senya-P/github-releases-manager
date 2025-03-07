package cz.cuni.mff.releasemanager;

import java.nio.file.Path;

public class MacHandler extends PlatformHandler {

    private static MacHandler instance;

    private MacHandler() {}

    public static MacHandler getInstance() {
        if (instance == null) {
            instance = new MacHandler();
        }
        return instance;
    }

    @Override
    public Path install(Path asset) {
        return null;
    }

    @Override
    public String getFormat() {
        return ".dmg";
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
