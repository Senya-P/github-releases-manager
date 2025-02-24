package cz.cuni.mff.releasemanager;

public class Main { // extends Runnable
    public static void main(String[] args) {
        // ReleaseManager releaseManager = ReleaseManager.getInstance();
        // releaseManager.execute(args);

        GithubClient githubClient = new GithubClient();
        githubClient.getLatestRelease("keepassxreboot", "keepassxc");

        // LinuxHandler lh = new LinuxHandler();
        // lh.extract(Paths.get("/home/senya/Documents/code/github-releases-manager/releases/keepassxc-2.7.9-src.tar.xz"), Paths.get("/home/senya/Documents/code/github-releases-manager/releases/dest"));
    }
}

/*
 * blocking cmd iface?
 * multi-thread
 * version control: json?
 * resource bundle
 * 
 */