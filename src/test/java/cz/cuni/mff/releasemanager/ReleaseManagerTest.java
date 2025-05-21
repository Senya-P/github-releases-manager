package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cz.cuni.mff.releasemanager.cmd.CmdParser;
import cz.cuni.mff.releasemanager.cmd.Command;
import cz.cuni.mff.releasemanager.platform.PlatformHandler;
import cz.cuni.mff.releasemanager.types.Asset;
import cz.cuni.mff.releasemanager.types.ReleaseInfo;
import cz.cuni.mff.releasemanager.types.ReleasesList;
import cz.cuni.mff.releasemanager.types.Repo;
import cz.cuni.mff.releasemanager.types.SearchResult;


class ReleaseManagerTest {

    private ReleaseManager releaseManager;
    private CmdParser mockCmdParser;
    private GithubClient mockGithubClient;
    private PlatformHandler mockPlatformHandler;

    @BeforeEach
    void setUp() throws Exception {
        releaseManager = ReleaseManager.getInstance();

        mockCmdParser = mock(CmdParser.class);
        mockGithubClient = mock(GithubClient.class);
        mockPlatformHandler = mock(PlatformHandler.class);

         // use reflection to set the private fields - replace with dependency injection framework
        Field parserField = ReleaseManager.class.getDeclaredField("cmdParser");
        parserField.setAccessible(true);
        parserField.set(releaseManager, mockCmdParser);

        Field clientField = ReleaseManager.class.getDeclaredField("githubClient");
        clientField.setAccessible(true);
        clientField.set(releaseManager, mockGithubClient);

        Field handlerField = ReleaseManager.class.getDeclaredField("platformHandler");
        handlerField.setAccessible(true);  
        handlerField.set(releaseManager, mockPlatformHandler);
    }

    @Test
    void testExecuteSearch() {
        Command cmd = Command.SEARCH;
        cmd.argument = "example";
        when(mockCmdParser.parse(any())).thenReturn(cmd);

        var repo = new Repo("user/example", "Example repo");
        var searchResult = Optional.of(new SearchResult(List.of(repo)));

        when(mockGithubClient.searchRepoByName("example")).thenReturn(searchResult);

        releaseManager.execute(new String[] {"search", "example"});

        verify(mockGithubClient).searchRepoByName("example");
    }

    @Test
    void testExecuteInstall() {
        Command cmd = Command.INSTALL;
        cmd.argument = "user/example";
        when(mockCmdParser.parse(any())).thenReturn(cmd);

        Asset asset = new Asset("http://url", "example.exe");
        when(mockGithubClient.getLatestReleaseAssets("user/example")).thenReturn(List.of(asset));
        when(mockGithubClient.installAsset(asset)).thenReturn(Path.of("/ProgramFiles/example.exe"));

        releaseManager.execute(new String[]{"install", "user/example"});

        verify(mockGithubClient).installAsset(asset);
    }

    @Test
    void testExecuteUninstall() throws IOException {
        Command cmd = Command.UNINSTALL; 
        cmd.argument = "user/example";
        when(mockCmdParser.parse(any())).thenReturn(cmd);

        Asset asset = new Asset("http://url", "example.exe");
        ReleaseInfo info = new ReleaseInfo("user/example", Instant.now(), "/ProgramFiles/uninstall.exe", asset);
        ReleasesList releases = new ReleasesList(List.of(info));
        when(mockPlatformHandler.loadReleasesList()).thenReturn(releases);

        releaseManager.execute(new String[]{"uninstall", "user/example"});

        verify(mockPlatformHandler).uninstall(Path.of("/ProgramFiles/uninstall.exe"));
        verify(mockPlatformHandler).removeReleaseFromList(info);
    }

    @Test
    void testExecuteUpdate() throws IOException {
        Command cmd = Command.UPDATE; 
        cmd.argument = "user/example";
        when(mockCmdParser.parse(any())).thenReturn(cmd);

        Asset oldAsset = new Asset("http://oldurl", "example.exe");
        ReleaseInfo info = new ReleaseInfo("user/example", Instant.now(), "/ProgramFiles/uninstall.exe", oldAsset);
        ReleasesList releases = new ReleasesList(List.of(info));
        Asset newAsset = new Asset("http://newurl", "example.exe");

        when(mockPlatformHandler.loadReleasesList()).thenReturn(releases);
        when(mockGithubClient.getLatestReleaseAssets("user/example")).thenReturn(List.of(newAsset));
        when(mockGithubClient.installAsset(newAsset)).thenReturn(Path.of("/ProgramFiles/uninstall.exe"));

        releaseManager.execute(new String[]{"update", "user/example"});

        verify(mockGithubClient).installAsset(newAsset);
    }

    @Test
    void testExecuteList() throws IOException {
        Command cmd = Command.LIST;
        when(mockCmdParser.parse(any())).thenReturn(cmd);

        ReleaseInfo info = new ReleaseInfo("user/example", Instant.now(), "/ProgramFiles/uninstall.exe", null);
        ReleasesList releases = new ReleasesList(List.of(info));
        when(mockPlatformHandler.loadReleasesList()).thenReturn(releases);

        releaseManager.execute(new String[]{"list"});
        verify(mockPlatformHandler).loadReleasesList();
    }

    @Test
    void testExecuteHelp() {
        Command cmd = Command.HELP;
        when(mockCmdParser.parse(any())).thenReturn(cmd);

        releaseManager.execute(new String[]{"help"});
        verify(mockCmdParser).parse(any());
    }
}