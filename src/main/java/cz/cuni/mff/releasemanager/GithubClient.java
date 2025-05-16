package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.cuni.mff.releasemanager.types.Asset;
import cz.cuni.mff.releasemanager.types.Release;
import cz.cuni.mff.releasemanager.types.ReleaseInfo;
import cz.cuni.mff.releasemanager.types.SearchResult;

public class GithubClient {

    private static final String API_URL = "https://api.github.com";
    private static final String ACCEPT_JSON_HEADER = "application/vnd.github.v3+json";
    private static final String ACCEPT_STREAM_HEADER = "application/octet-stream";
    private static final String RESULT_COUNT = "5";
    private final HttpClient client;
    private final PlatformHandler platformHandler;

    public GithubClient() {
        client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(java.time.Duration.ofSeconds(30))
            .build();
        platformHandler = Platform.getPlatformHandler();
        //platformHandler.createReleasesListFile();
    }

    public Optional<SearchResult> searchRepoByName(String name) {
        String url = API_URL + "/search/repositories?q=" + name + "&per_page=" + RESULT_COUNT;
        try {
            var jsonResponse = request(URI.create(url));
            if (!jsonResponse.isPresent()) {
                return Optional.empty();
            }
            String json = jsonResponse.get();
            SearchResult searchResult = getSearchResult(json);
            return Optional.ofNullable(searchResult);

        } catch (IOException | InterruptedException ex) {
            System.out.println(ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Asset> getLatestReleaseAsset(String repoFullName) {
        String[] parts = repoFullName.split("/");
        if (parts.length != 2) {
            return Optional.empty();
        }
        String owner = parts[0];
        String repo = parts[1];

        String url = API_URL + "/repos/" + owner + "/" + repo + "/releases/latest";
        try {
            var jsonResponse = request(URI.create(url));
            if (!jsonResponse.isPresent()) {
                return Optional.empty(); // custom exceptions?
            }
            String json = jsonResponse.get();
            return findAsset(json);
        } catch (IOException | InterruptedException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    //extract
    public boolean installAsset(Asset asset, String repoFullName) { // extract adding release info to file
        InputStream assetStream;
        try {
            assetStream = getAsset(asset.url());
        } catch (IOException | InterruptedException e) {
            return false;
        }

        final Path assetPath = platformHandler.saveInputStreamToFile(assetStream, asset.name());
        Path installedRelease = platformHandler.install(assetPath);
        if (installedRelease == null) {
            return false;
        }

        ReleaseInfo release = new ReleaseInfo(
            repoFullName,
            //"v1.0",
            Instant.now(),
            installedRelease.toString(), // accurate?
            asset
        );
        try {
            platformHandler.addReleaseToList(release);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private Optional<String> request(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .header("Accept", ACCEPT_JSON_HEADER)
            .build();
        HttpResponse<String> response = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        handleResponseCode(response);
        return Optional.of(response.body());
    }

    private InputStream getAsset(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", ACCEPT_STREAM_HEADER)
            .build();
        HttpResponse<InputStream> response = client.send(
            request,
            HttpResponse.BodyHandlers.ofInputStream()
        );

        handleResponseCode(response);
        return response.body();
    }

    private void handleResponseCode(HttpResponse<?> response) throws IOException {
        int statusCode = response.statusCode();
        if (statusCode >= 400) {
            switch (statusCode) {
                case 403 -> {
                    handleRateLimit(response.headers());
                    throw new IOException("Rate limit exceeded. Try again later.");
                }
                case 404 -> throw new IOException("Resource not found: " + response.body());
                default -> throw new IOException("HTTP Error " + statusCode + ": " + response.body());
            }
        }
    }

    private void handleRateLimit(HttpHeaders headers) {
        long remaining = headers.firstValueAsLong("X-RateLimit-Remaining").orElse(0);
        long resetTime = headers.firstValueAsLong("X-RateLimit-Reset").orElse(0);

        System.out.printf("Rate limit exceeded: Remaining: %d, Reset: %tT UTC%n",
            remaining, new Date(resetTime * 1000));
    }

    private Optional<Asset> findAsset(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Release release = mapper.readValue(json, Release.class);
        Optional<Asset> asset = release.assets().stream()
            .filter(a -> a.name().toLowerCase().contains(platformHandler.getFormat()))
            .findFirst();
        return asset;
    }

    private SearchResult getSearchResult(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, SearchResult.class);
    }
}
