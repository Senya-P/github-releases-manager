package cz.cuni.mff.releasemanager.types;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Release(
    String url,
    String name,
    List<Asset> assets
) {}