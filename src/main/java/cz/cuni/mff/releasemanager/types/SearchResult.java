package cz.cuni.mff.releasemanager.types;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the result of a repository search query on Github.
 * @param items The list of repositories returned by the search query.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResult(
    @JsonProperty("items") List<Repo> items
) {}