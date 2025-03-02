package cz.cuni.mff.releasemanager.types;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResult(
    //@JsonProperty("total_count") int totalCount, // further paging
    @JsonProperty("items") List<Repo> items
) {}