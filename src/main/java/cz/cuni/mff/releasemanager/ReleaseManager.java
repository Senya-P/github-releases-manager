package cz.cuni.mff.releasemanager;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import cz.cuni.mff.releasemanager.types.Asset;
import cz.cuni.mff.releasemanager.types.ReleaseInfo;
import cz.cuni.mff.releasemanager.types.ReleasesList;
import cz.cuni.mff.releasemanager.types.Repo;

/**
 * ReleaseManager class manages GitHub release installation, uninstallation,
 * searching, updating, and listing of installed releases.
 *
 * <p>This class provides a command-line interface for interacting with GitHub
 * repositories and handling platform-specific release installation logic.
 * It acts as the central coordinator for parsing commands, accessing GitHub APIs,
 * and manipulating local release data.</p>
 *
 * Supported commands include:
 * <ul>
 *   <li>search</li>
 *   <li>install</li>
 *   <li>uninstall</li>
 *   <li>update</li>
 *   <li>list</li>
 *   <li>help</li>
 * </ul>
 */
public class ReleaseManager {
    private static ReleaseManager instance;

    private final CmdParser cmdParser;
    private final GithubClient githubClient;
    private final PlatformHandler platformHandler;

    private ReleaseManager() {
        cmdParser = new CmdParser();
        githubClient = new GithubClient();
        platformHandler = Platform.getPlatformHandler();
    }
    /**
     * @return the singleton {@code ReleaseManager} instance
     */
    public static ReleaseManager getInstance() {
        if (instance == null) {
            instance = new ReleaseManager();
        }
        return instance;
    }
    /**
     * Executes the command based on the provided arguments.
     *
     * @param args command-line arguments specifying the command and parameters
     */
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
    /**
     * Searches for a GitHub repository by name and prints the results.
     *
     * @param command the command containing the search term
     */
    private void search(Command command) {
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
    /**
     * Installs the latest release of a specified GitHub repository.
     * If multiple assets are found, the user is prompted to select one.
     *
     * @param command the command containing the repository name
     */
    private void install(Command command) {
        if (command.argument.split("/").length != 2) {
            System.out.println("Please specify the correct repository name of format 'owner/repo'.");
            return;
        }
        var result = githubClient.getLatestReleaseAssets(command.argument);
        if (result == null || result.isEmpty()) {
            System.out.println("Failed to retrieve the latest release.");
            return;
        }
        Asset asset = getSingleAsset(result);
        Path installedAsset = githubClient.installAsset(asset);
        if (installedAsset != null) {
            System.out.println("Installation successful.");
            addReleaseToList(command.argument, installedAsset, asset);
        } else {
            System.out.println("Installation failed.");
        }
    }
    /**
     * Prompts the user to choose a single asset from a list of assets.
     *
     * @param assets list of available release assets
     * @return the selected asset
     */
    private Asset getSingleAsset(List<Asset> assets) {
        if (assets.size() == 1) {
            return assets.get(0);
        }
        System.out.println("Multiple assets found. Please select one:");
        for (int i = 0; i < assets.size(); i++) {
            System.out.println((i + 1) + ": " + assets.get(i).name());
        }
        int choice = getChoice(1, assets.size());
        return assets.get(choice - 1);
    }
    /**
     * Prompts the user for a choice between a minimum and maximum integer value.
     *
     * @param min the minimum valid choice
     * @param max the maximum valid choice
     * @return the user's choice
     */
    private int getChoice(int min, int max) {
        int input = cmdParser.getUserInput();
        if (input < min || input > max) {
            System.out.println("Invalid choice. Valid options are: " + min + " - " + max);
            return getChoice(min, max);
        }
        return input;
    }
    /**
     * Adds a release to the list of installed releases.
     *
     * @param repoFullName the full name of the repository (e.g., "owner/repo")
     * @param installedRelease the path to the installed asset
     * @param asset the asset
     */
    private void addReleaseToList(String repoFullName, Path installedRelease, Asset asset) {
        ReleaseInfo release = new ReleaseInfo(
            repoFullName,
            Instant.now(),
            installedRelease.toString(),
            asset
        );
        try {
            platformHandler.addReleaseToList(release);
        }
        catch (IOException e) {
            System.out.println("Failed to add release to list: " + e.getMessage());
        }
    }
    /**
     * Uninstalls a previously installed release based on the repository name.
     *
     * @param command the command containing the repository name
     */
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
                    platformHandler.uninstall(Path.of(release.uninstallPath()));
                    platformHandler.removeReleaseFromList(release);
                    System.out.println("Successfully uninstalled.");
                }, () -> System.out.println("Release " + command.argument + " is not found."));
    }

    /**
     * Updates an installed release if a newer version is available.
     *
     * @param command the command containing the repository name
     */
    private void update(Command command) {
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
                    List<Asset> assets = githubClient.getLatestReleaseAssets(command.argument);
                    if (assets.isEmpty()) {
                        System.out.println("No asset found.");
                        return;
                    }
                    Asset newAsset = null;
                    for (Asset asset : assets) {
                        if (asset.name().equals(release.asset().name())) {
                            newAsset = asset;
                            break;
                        }
                    }
                    if (newAsset == null) {
                        newAsset = getSingleAsset(assets);
                    }
                    if (release.asset().url().equals(newAsset.url())) {
                        System.out.println("Already up to date.");
                    }
                    else {
                        platformHandler.uninstall(Path.of(release.uninstallPath()));
                        Path installedAsset = githubClient.installAsset(newAsset);
                        if (installedAsset != null) {
                            System.out.println("Successfully updated.");
                            addReleaseToList(command.argument, installedAsset, newAsset);
                        } else {
                            System.out.println("Installation failed.");
                        }
                    }
                }, () -> System.out.println("Release " + command.argument + " is not found."));
    }

    /**
     * Lists all installed releases.
     */
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

    /**
     * Prints usage help text or a message for an unknown command.
     *
     * @param command the provided command
     */
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
