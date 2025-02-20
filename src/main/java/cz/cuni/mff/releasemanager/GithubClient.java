package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GithubClient {

    private static final String API_URL = "https://api.github.com/repos/";
    private static final String ACCEPT_JSON_HEADER = "application/vnd.github.v3+json";
    private static final String ACCEPT_STREAM_HEADER = "application/octet-stream";
    private final HttpClient client;
    private final PlatformHandler platformHandler;

    public GithubClient() {
        client = HttpClient.newHttpClient();
        platformHandler = new WindowsHandler(); // REWRITE
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
            String jsonResponse = new String(response.body().readAllBytes());
            System.out.println(jsonResponse);
            handleResponseCode(response);
            // parse response
            // get correct asset id for corresponding platform
            // String assetId = getAssetId(response.body());
            platformHandler.resolveExtension(null);
            final String assetName = "keepassxc-2.7.9-src.tar.xz";
            final Path assetPath = saveInputStreamToFile(getAsset(owner, repo, "174789063"), assetName);
            platformHandler.install(assetPath);
        } catch (IOException | InterruptedException ex) {
            return false;
        }

        return true;
    }
    // https://github.com/keepassxreboot/keepassxc/releases/download/2.7.9/KeePassXC-2.7.9-Win64-LegacyWindows.zip
    // 
    // 174789063
    // https://github.com/keepassxreboot/keepassxc/releases/download/2.7.9/keepassxc-2.7.9-src.tar.xz

    private InputStream getAsset(String owner, String repo, String assetId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL + owner + "/" + repo + "/releases/assets/" + assetId))
            .header("Accept", ACCEPT_STREAM_HEADER)
            .build();
        return client.send(request, BodyHandlers.ofInputStream()).body();
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
        // Save the InputStream to a file
        Path dir;
        try {
            dir = createDirectory("releases");
            Path destination = dir.resolve(filename);
            OutputStream out = Files.newOutputStream(destination);
            stream.transferTo(out);
            return destination;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private Path createDirectory(String directoryName) throws IOException {
        Path path = Paths.get(directoryName);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

}
