package cz.cuni.mff.releasemanager.types;

import java.util.List;

/**
 * Represents a list of releases.
 * This record class is used to serialize the information about all installed releases to JSON for the config file.
 * @param releases The list of release information.
 */
public record ReleasesList(
    List<ReleaseInfo> releases
) {}
