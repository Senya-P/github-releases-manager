package cz.cuni.mff.releasemanager;

import java.nio.file.Path;

public class WindowsHandler extends PlatformHandler {

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
