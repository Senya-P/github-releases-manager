package cz.cuni.mff.releasemanager.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Asset(
    String url,
    String name
) {}
