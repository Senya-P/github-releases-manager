package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.util.List;

import cz.cuni.mff.releasemanager.types.ReleaseInfo;
import cz.cuni.mff.releasemanager.types.ReleasesList;
import cz.cuni.mff.releasemanager.types.Repo;
import cz.cuni.mff.releasemanager.types.SearchResult;


public class ReleaseManager {
    private static ReleaseManager instance; // volitile

    private final CmdParser cmdParser;
    private final GithubClient githubClient;
    private final PlatformHandler platformHandler;

    private ReleaseManager() {
        cmdParser = new CmdParser();
        githubClient = new GithubClient();
        platformHandler = Platform.getPlatformHandler();
    }

    public static ReleaseManager getInstance() {
        if (instance == null) {
            instance = new ReleaseManager();
        }
        return instance;
    }

    public void execute(String[] args) {
        Command command = cmdParser.parse(args);

        switch (command) {
            case SEARCH -> search(command);
            case INSTALL -> install(command);
            case UPDATE -> update(command);
            case LIST -> list();
            case HELP -> help(command);
        }
    }

    private void search(Command command) {
        // search for the release
        SearchResult searchResult = githubClient.searchRepoByName(command.argument);
        if (searchResult == null || searchResult.items().isEmpty()) {
            System.out.println("No results found.");
            return;
        }
        System.out.println("Found " + searchResult.items().size() + " repositories.");
        for (Repo repo : searchResult.items()) {
            System.out.println(repo.fullName() + ": " + repo.description());
        }
        System.out.println("To install a release, use the 'install' command followed by the repository name.");
    }

    private void install(Command command) {
        if (command.argument.split("/").length != 2) {
            System.out.println("Please specify the correct repository name of format 'owner/repo'.");
            return;
        }
        if (githubClient.getLatestRelease(command.argument)) {
            System.out.println("Installation successful.");
        } else {
            System.out.println("Installation failed.");
        }
    }

    private void update(Command command) {
        // verify installation
        // retrieve latest release
        // compare versions ??
        // install
        // update releases list
    }

    private void list() {
        try {
            ReleasesList releasesList = platformHandler.loadReleasesList();
            if (releasesList == null) {
                System.out.println("No releases installed.");
                return;
            }
            List<ReleaseInfo> releases = releasesList.releases();
            for (ReleaseInfo release : releases) {
                System.out.println(release.repo());
            }
        } catch (IOException e) {
            System.out.println("Failed to load releases list.");
        }
    }

    private void help(Command command) {
        if (command.argument != null) {
            System.out.println("Unknown command: " + command.argument);
        }
        System.out.println("Usage: java -jar github-releases-manager.jar [command] [options]");
        System.out.println("Commands:");
        System.out.println("  search [name] - search for a release by name");
        System.out.println("  install [name] - install the latest release by name");
        System.out.println("  update [name] - update the installed release by name");
        System.out.println("  list - list all installed releases");
        System.out.println("  help - display this help message");
    }
}
