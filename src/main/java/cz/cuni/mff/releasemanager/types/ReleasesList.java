package cz.cuni.mff.releasemanager.types;

import java.util.List;

public record ReleasesList(
    List<ReleaseInfo> releases
    // version/timestamp for consistency check
) {}
