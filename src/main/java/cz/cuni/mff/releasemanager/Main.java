package cz.cuni.mff.releasemanager;

public class Main { // extends Runnable
    public static void main(String[] args) {
        // ReleaseManager releaseManager = ReleaseManager.getInstance();
        // releaseManager.execute(args);

        GithubClient githubClient = new GithubClient();
        githubClient.getLatestRelease("keepassxreboot", "keepassxc");
    }
}

/*
 * blocking cmd iface?
 * multi-thread
 * version control: json?
 * resource bundle
 * 
 */