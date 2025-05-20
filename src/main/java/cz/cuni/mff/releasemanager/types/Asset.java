package cz.cuni.mff.releasemanager.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents an asset of a Github release.
 * @param url The URL of the asset.
 * @param name The name of the asset.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Asset(
    String url,
    String name
) {}
