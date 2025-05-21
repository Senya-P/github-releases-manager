package cz.cuni.mff.releasemanager;

import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.cuni.mff.releasemanager.platform.PlatformHandler;
import cz.cuni.mff.releasemanager.types.Asset;
import cz.cuni.mff.releasemanager.types.SearchResult;

class GithubClientTest {

    private GithubClient githubClient;
    private HttpClient mockHttpClient;
    private PlatformHandler mockPlatformHandler;

    @BeforeEach
    void setUp() throws Exception {
        githubClient = new GithubClient();

        // use reflection to set the private fields - replace with dependency injection framework
        mockHttpClient = mock(HttpClient.class);
        Field clientField = GithubClient.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(githubClient, mockHttpClient);

        mockPlatformHandler = mock(PlatformHandler.class);
        Field handlerField = GithubClient.class.getDeclaredField("platformHandler");
        handlerField.setAccessible(true);
        handlerField.set(githubClient, mockPlatformHandler);
    }

    @Test
    void searchRepoByName_returnsSearchResultOnSuccess() throws Exception {
        String repoName = "testrepo";
        String json = """
            {
              "items": [
                {
                  "full_name": "owner/testrepo",
                  "description": "Test repository"
                }
              ]
            }
            """;
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(json);
        when(mockHttpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockResponse);

        Optional<SearchResult> result = githubClient.searchRepoByName(repoName);

        assertTrue(result.isPresent());
        assertEquals("owner/testrepo", result.get().items().get(0).fullName());
        assertEquals("Test repository", result.get().items().get(0).description());
    }

    @Test
    void getLatestReleaseAssets_returnsAssetsOnSuccess() throws Exception {
        String repoFullName = "owner/repo";
        String json = """
            {
              "assets": [
                { "name": "file1.exe", "url": "http://example.com/file1.exe" },
                { "name": "file2.msi", "url": "http://example.com/file2.msi" }
              ]
            }
            """;
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(json);
        when(mockPlatformHandler.getFormats()).thenReturn(new String[]{"exe", "msi"});
        when(mockHttpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockResponse);

        List<Asset> assets = githubClient.getLatestReleaseAssets(repoFullName);

        assertEquals(2, assets.size());
        assertEquals("file1.exe", assets.get(0).name());
        assertEquals("file2.msi", assets.get(1).name());
        assertEquals("http://example.com/file1.exe", assets.get(0).url());
        assertEquals("http://example.com/file2.msi", assets.get(1).url());
    }

    @Test
    void getLatestReleaseAssets_returnsEmptyForInvalidRepoName() {
        List<Asset> assets = githubClient.getLatestReleaseAssets("invalidRepoName");
        assertTrue(assets.isEmpty());
    }

}