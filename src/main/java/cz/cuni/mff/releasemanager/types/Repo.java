package cz.cuni.mff.releasemanager.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Repo(
    @JsonProperty("full_name") String fullName,
    @JsonProperty("description") String description
) {}