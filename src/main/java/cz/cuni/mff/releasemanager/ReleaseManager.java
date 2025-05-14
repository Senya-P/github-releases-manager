package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import cz.cuni.mff.releasemanager.types.Asset;
import cz.cuni.mff.releasemanager.types.ReleaseInfo;
import cz.cuni.mff.releasemanager.types.ReleasesList;
import cz.cuni.mff.releasemanager.types.Repo;


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
            case UNINSTALL -> uninstall(command);
            case UPDATE -> update(command);
            case LIST -> list();
            case HELP -> help(command);
        }
    }

    private void search(Command command) {
        // search for the release
        var searchResult = githubClient.searchRepoByName(command.argument);
        if (searchResult.isEmpty() || searchResult.get().items().isEmpty()) {
            System.out.println("No results found.");
            return;
        }
        var items = searchResult.get().items();
        System.out.println("Found " + items.size() + " repositories.");
        for (Repo repo : items) {
            System.out.println(repo.fullName() + ": " + repo.description());
        }
        System.out.println("To install a release, use the 'install' command followed by the repository name.");
    }

    private void install(Command command) {
        if (command.argument.split("/").length != 2) {
            System.out.println("Please specify the correct repository name of format 'owner/repo'.");
            return;
        }
        var result = githubClient.getLatestReleaseAsset(command.argument);
        if (result == null || result.isEmpty()) {
            System.out.println("Failed to retrieve the latest release.");
            return;
        }
        Asset asset = result.get();
        if (githubClient.installAsset(asset, command.argument)) {
            System.out.println("Installation successful.");
        } else {
            System.out.println("Installation failed.");
        }
    }

    private void uninstall(Command command) {
        if (command.argument.split("/").length != 2) {
            System.out.println("Please specify the correct repository name of format 'owner/repo'.");
            return;
        }
        ReleasesList releasesList;
        try {
            releasesList = platformHandler.loadReleasesList();
        } catch (IOException e) {
            System.out.println("Failed to find installed release.");
            return;
        }
        if (releasesList == null) {
            System.out.println("No releases installed.");
            return;
        }
        List<ReleaseInfo> releases = releasesList.releases();
        releases.stream()
                .filter(release -> release.repo().equals(command.argument))
                .findFirst()
                .ifPresentOrElse(release -> {
                    try {
                        platformHandler.uninstall(Path.of(release.installPath()));
                        platformHandler.removeReleaseFromList(release);
                    } catch (IOException e) {
                        System.out.println("Failed to uninstall.");
                    }
                    System.out.println("Successfully uninstalled.");
                }, () -> System.out.println("Release " + command.argument + " is not found."));
    }


    private void update(Command command) {
        // verify installation
        // retrieve latest release
        // compare versions ??
        // install
        // update releases list
        ReleasesList releasesList;
        try {
            releasesList = platformHandler.loadReleasesList();
        } catch (IOException e) {
            System.out.println("Failed to find installed release.");
            return;
        }
        if (releasesList == null) {
            System.out.println("No releases installed.");
            return;
        }
        List<ReleaseInfo> releases = releasesList.releases();
        releases.stream()
                .filter(release -> release.repo().equals(command.argument))
                .findFirst()
                .ifPresentOrElse(release -> {
                    var asset = githubClient.getLatestReleaseAsset(command.argument);
                    if (!asset.isPresent()) {
                        System.out.println("No asset found.");
                        return;
                    }
                    if (release.asset().url().equals(asset.get().url())) {
                        System.out.println("Already up to date.");
                    }
                    else {
                        try {
                            platformHandler.uninstall(Path.of(release.installPath()));
                        } catch (IOException e) {
                            System.out.println("Failed to uninstall.");
                        }
                        githubClient.installAsset(asset.get(), command.argument);
                        System.out.println("Successfully updated.");
                    }
                }, () -> System.out.println("Release " + command.argument + " is not found."));
    }

    private void list() {
        ReleasesList releasesList;
        try {
            releasesList = platformHandler.loadReleasesList();
        } catch (IOException e) {
            System.out.println("Failed to load releases list.");
            return;
        }
        if (releasesList == null) {
            System.out.println("No releases installed.");
            return;
        }
        List<ReleaseInfo> releases = releasesList.releases();
        for (ReleaseInfo release : releases) {
            System.out.println(release.repo());
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
        System.out.println("  uninstall [name] - uninstall the release by name");
        System.out.println("  update [name] - update the installed release by name");
        System.out.println("  list - list all installed releases");
        System.out.println("  help - display this help message");
    }
}
