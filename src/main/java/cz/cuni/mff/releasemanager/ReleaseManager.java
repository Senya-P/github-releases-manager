package cz.cuni.mff.releasemanager;


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
            case LIST -> list(command);
        }
    }

    private void search(Command command) {
        // search for the release
    }

    private void install(Command command) {
        // install the release
    }

    private void update(Command command) {
        // update the release
    }

    private void list(Command command) {
        // list the releases
    }
}
