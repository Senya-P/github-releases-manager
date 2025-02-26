package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GithubClient {

    private static final String API_URL = "https://api.github.com/repos/";
    private static final String ACCEPT_JSON_HEADER = "application/vnd.github.v3+json";
    private static final String ACCEPT_STREAM_HEADER = "application/octet-stream";
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

    public boolean getLatestRelease(String owner, String repo) {
        // get release
        // determine platform
        // extract asset id
        // request asset
        // download asset
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + owner + "/" + repo + "/releases/latest"))
                .header("Accept", ACCEPT_JSON_HEADER)
                .build();
        HttpResponse<InputStream> response;
        try {
            response = client.send(request, BodyHandlers.ofInputStream());
            handleResponseCode(response);
            String jsonResponse = new String(response.body().readAllBytes());

            Asset asset = findAppImageAsset(jsonResponse);
            //Files.write(Paths.get("output"), jsonResponse.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            platformHandler.verifyFormat(asset.name);

            final Path assetPath = saveInputStreamToFile(getAsset(asset.url), asset.name);
            platformHandler.install(assetPath);
        } catch (IOException | InterruptedException ex) {
            System.out.println(ex.getMessage());
            return false;
        }

        return true;
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

    private void handleResponseCode(HttpResponse<InputStream> response) throws IOException {
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
        }
    }

    private Path saveInputStreamToFile(InputStream stream, String filename) {
        Path dir;
        try {
            dir = createDirectory("releases");
        } catch (IOException ex) {
            return null;
        }
        Path destination = dir.resolve(filename);
        try {
            Files.copy(stream, destination);
        } catch (IOException e) {
            return null;
        }
        return destination;
    }

    private Path createDirectory(String directoryName) throws IOException {
        Path path = Paths.get(directoryName);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    private static Asset findAppImageAsset(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Release release = mapper.readValue(json, Release.class);
        Asset asset = release.assets().stream()
            .filter(a -> a.name().toLowerCase().contains(".appimage"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No AppImage asset found"));
        return asset;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Release(
        String url,
        String name,
        List<Asset> assets
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Asset(
        String url,
        String name
    ) {}

}
