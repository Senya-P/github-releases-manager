package cz.cuni.mff.releasemanager.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Github repository.
 * @param fullName The full name of the repository (owner/repo).
 * @param description The description of the repository.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Repo(
    @JsonProperty("full_name") String fullName,
    @JsonProperty("description") String description
) {}