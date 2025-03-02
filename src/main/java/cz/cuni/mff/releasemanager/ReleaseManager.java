package cz.cuni.mff.releasemanager;

import cz.cuni.mff.releasemanager.types.Repo;
import cz.cuni.mff.releasemanager.types.SearchResult;


public class ReleaseManager {
    private static ReleaseManager instance; // volitile

    private final CmdParser cmdParser;
    private final GithubClient githubClient;
    private final Platform platform;

    private ReleaseManager() {
        platform = Platform.detectPlatform();
        cmdParser = new CmdParser();
        githubClient = new GithubClient();
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
            case HELP -> help();
        }
    }

    private void search(Command command) {
        // search for the release
        SearchResult searchResult = githubClient.searchRepoByName(command.argument);
        if (searchResult == null) {
            System.out.println("No results found.");
            return;
        }
        System.out.println("Found " + searchResult.items().size() + " repositories.");
        for (Repo repo : searchResult.items()) {
            String owner = repo.fullName().split("/")[0];
            String repoName = repo.fullName().split("/")[1];
            System.out.println(repoName + ": " + repo.description());
        }
        System.out.println("To install a release, use the 'install' command followed by the repository name.");
    }

    private void install(Command command) {
        if (githubClient.getLatestRelease("", "")) {
            System.out.println("Installation successful.");
        } else {
            System.out.println("Installation failed.");
        }
    }

    private void update(Command command) {
        // update the release
    }

    private void list() {
        // list the releases
    }

    private void help() {
        System.out.println("Usage: java -jar github-releases-manager.jar [command] [options]");
        System.out.println("Commands:");
        System.out.println("  search [name] - search for a release by name");
        System.out.println("  install [name] - install the latest release by name");
        System.out.println("  update [name] - update the installed release by name");
        System.out.println("  list - list all installed releases");
        System.out.println("  help - display this help message");
    }
}
