package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.cuni.mff.releasemanager.types.Asset;
import cz.cuni.mff.releasemanager.types.Release;
import cz.cuni.mff.releasemanager.types.SearchResult;

/**
 * This class is responsible for interacting with the Github API.
 * It provides methods to search for repositories, get the latest release assets,
 * and install those assets.
 */
public class GithubClient {

    private static final String API_URL = "https://api.github.com";
    private static final String ACCEPT_JSON_HEADER = "application/vnd.github.v3+json";
    private static final String ACCEPT_STREAM_HEADER = "application/octet-stream";
    private static final String RESULT_COUNT = "5";
    private final HttpClient client;
    private final PlatformHandler platformHandler;

    /**
     * Constructor for GithubClient.
     * Initializes the HttpClient and PlatformHandler.
     */
    public GithubClient() {
        client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(java.time.Duration.ofSeconds(30))
            .build();
        platformHandler = Platform.getPlatformHandler();
    }

    /**
     * Searches for repositories by name using the Github API.
     * @param name The name of the repository to search for.
     * @return An Optional containing the SearchResult if found, otherwise empty.
     */
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

    /**
     * Retrieves the list of latest release assets for a given repository.
     * @param repoFullName The full name of the repository (owner/repo).
     * @return A list of Asset objects.
     */
    public List<Asset> getLatestReleaseAssets(String repoFullName) {
        String[] parts = repoFullName.split("/");
        if (parts.length != 2) {
            return List.of();
        }
        String owner = parts[0];
        String repo = parts[1];

        String url = API_URL + "/repos/" + owner + "/" + repo + "/releases/latest";
        try {
            var jsonResponse = request(URI.create(url));
            if (jsonResponse.isEmpty()) {
                System.out.println("No releases found for this repository.");
                return List.of();
            }
            String json = jsonResponse.get();
            List<Asset> assets = findAssets(json);
            if (assets.isEmpty()) {
                System.out.println("No suitable asset found for this repository.");
            }
            return assets;
        } catch (IOException | InterruptedException ex) {
            System.out.println(ex.getMessage());
            return List.of();
        }
    }

    /**
     * Installs the asset by downloading it and passing it to the platform handler for installation.
     * @param asset The asset to install.
     * @return The path to the file for uninstall.
     */
    public Path installAsset(Asset asset) {
        InputStream assetStream;
        try {
            assetStream = getAsset(asset.url());
        } catch (IOException | InterruptedException e) {
            System.out.println("Error retrieving asset: " + e.getMessage());
            return null;
        }

        final Path assetPath = FileUtils.saveInputStreamToFile(assetStream, asset.name());
        return platformHandler.install(assetPath);
    }

    /**
     * Requests the given URI and returns the response body as a string.
     * @param uri The URI to request.
     * @return Optional containing the response body as a string.
     * @throws IOException
     * @throws InterruptedException
     */
    private Optional<String> request(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .header("Accept", ACCEPT_JSON_HEADER)
            .build();
        HttpResponse<String> response;
        try {
            response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );
        } catch (ConnectException e) {
            throw new IOException("Connection error.", e);
        }
        handleResponseCode(response);
        return Optional.of(response.body());
    }

    /**
     * Retrieves the asset from the given URL and returns it as an InputStream.
     * @param url The URL of the asset.
     * @return InputStream of the asset.
     * @throws IOException
     * @throws InterruptedException
     */
    private InputStream getAsset(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", ACCEPT_STREAM_HEADER)
            .build();
        HttpResponse<InputStream> response;
        try {
            response = client.send(
                request,
                HttpResponse.BodyHandlers.ofInputStream()
            );
        } catch (ConnectException e) {
            throw new IOException("Connection error.", e);
        }
        handleResponseCode(response);
        return response.body();
    }

    /**
     * Handles the response code from the HTTP request. Used for logging.
     * @param response The HTTP response.
     * @throws IOException
     */
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

    /**
     * Handles the rate limit response from the Github API. Used for logging.
     * @param headers The HTTP headers from the response.
     */
    private void handleRateLimit(HttpHeaders headers) {
        long remaining = headers.firstValueAsLong("X-RateLimit-Remaining").orElse(0);
        long resetTime = headers.firstValueAsLong("X-RateLimit-Reset").orElse(0);

        System.out.printf("Rate limit exceeded: Remaining: %d, Reset: %tT UTC%n",
            remaining, new Date(resetTime * 1000));
    }

    /**
     * Finds suitable assets in the JSON response from the Github API.
     * @param json The JSON response.
     * @return A list of Asset objects.
     * @throws IOException
     */
    private List<Asset> findAssets(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Release release = mapper.readValue(json, Release.class);
        String[] formats = platformHandler.getFormats();
        // add platform check
        List<Asset> assets = new ArrayList<>();
        for (String format : formats) {
            assets.addAll(release.assets().stream()
                .filter(a -> a.name().toLowerCase().contains(format))
                .toList()
            );
        }
        return assets;
    }

    /**
     * Converts the JSON string to a SearchResult object.
     * @param json The JSON string.
     * @return The SearchResult object.
     * @throws IOException
     */
    private SearchResult getSearchResult(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, SearchResult.class);
    }
}
