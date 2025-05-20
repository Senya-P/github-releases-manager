package cz.cuni.mff.releasemanager.types;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the information about an installed release.
 * This record class is used to serialize the release information to JSON for the {@code ReleasesList} config file.
 * @param repo The name of the repository where the release is installed.
 * @param installedAt The timestamp when the release was installed.
 * @param uninstallPath The path to the file for application uninstallation.
 */
public record ReleaseInfo(
    String repo,
    @JsonProperty("installed_at") Instant installedAt,
    @JsonProperty("uninstall_path") String uninstallPath,
    Asset asset
) {}
