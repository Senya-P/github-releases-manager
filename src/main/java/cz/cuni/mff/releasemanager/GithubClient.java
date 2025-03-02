package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.cuni.mff.releasemanager.types.Asset;
import cz.cuni.mff.releasemanager.types.Release;
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
    }

    public SearchResult searchRepoByName(String name) {
        String url = API_URL + "/search/repositories?q=" + name + "&per_page=" + RESULT_COUNT;
        try {
            var jsonResponse = request(URI.create(url));
            if (!jsonResponse.isPresent()) {
                return null;
            }
            String json = jsonResponse.get();
            // extract list? of owner - repo
            SearchResult searchResult = getSearchResult(json);
            return searchResult;

            //Files.write(Paths.get("output-search"), jsonResponse.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException | InterruptedException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public boolean getLatestRelease(String owner, String repo) {
        // get release
        // determine platform
        // extract asset id
        // request asset
        // download asset
        //Files.write(Paths.get("output"), jsonResponse.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        String url = API_URL + "/repos/" + owner + "/" + repo + "/releases/latest";
        try {
            var jsonResponse = request(URI.create(url));
            if (!jsonResponse.isPresent()) {
                return false; // custom exceptions?
            }
            String json = jsonResponse.get();
            var result = findAppImageAsset(json);
            if (!result.isPresent()) {
                return false; //
            }
            Asset asset = result.get();
            InputStream assetStream = getAsset(asset.url());
            final Path assetPath = platformHandler.saveInputStreamToFile(assetStream, asset.name()); //
            //platformHandler.verifyFormat(assetPath); //
            platformHandler.install(assetPath);
        } catch (IOException | InterruptedException ex) {
            System.out.println(ex.getMessage());
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

    InputStream getAsset(String url) throws IOException, InterruptedException {
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

    private Optional<Asset> findAppImageAsset(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Release release = mapper.readValue(json, Release.class);
        Optional<Asset> asset = release.assets().stream()
            .filter(a -> a.name().toLowerCase().contains(".appimage")) // platform-specific
            .findFirst();
        return asset;
    }

    private SearchResult getSearchResult(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, SearchResult.class);
    }
}
