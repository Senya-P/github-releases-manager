package cz.cuni.mff.releasemanager.types;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReleaseInfo(
    String repo,
    //String version,
    @JsonProperty("installed_at") Instant installedAt,
    @JsonProperty("uninstall_path") String uninstallPath,
    Asset asset
    //List<String> dependencies
) {}
