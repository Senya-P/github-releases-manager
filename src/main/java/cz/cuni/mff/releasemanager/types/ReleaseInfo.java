package cz.cuni.mff.releasemanager.types;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReleaseInfo(
    String repo,
    //String version,
    @JsonProperty("installed_at") Instant installedAt,
    @JsonProperty("install_path") String installPath,
    Asset asset
    //List<String> dependencies
) {}
