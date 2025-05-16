package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Instant;
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
        platformHandler = MacHandler.getInstance(); // TODO: get platform handler from command line args
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

        if (handleResponseCode(response)) {
            return Optional.of(response.body());
        }
        return Optional.empty();
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

    private boolean handleResponseCode(HttpResponse<?> response) {
        int statusCode = response.statusCode();
        if (statusCode >= 400) {
            System.out.println("Http response status code: " + statusCode);
            // switch (statusCode) {
            //     case 401 -> throw new IOException("Unauthorized: " + errorBody);
            //     case 403 -> throw new IOException("Rate limit exceeded: " + errorBody);
            //     case 404 -> throw new IOException("Resource not found: " + errorBody);
            //     default -> throw new IOException(
            //         "HTTP Error " + statusCode + ": " + errorBody
            //     );
            // }
            return false;
        }
        return true;
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
