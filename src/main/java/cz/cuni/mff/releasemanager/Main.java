package cz.cuni.mff.releasemanager;

/**
 * Entry point for the Github Release Manager application.
 */
public class Main {
    /**
     * Main method to start the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        ReleaseManager releaseManager = ReleaseManager.getInstance();
        releaseManager.execute(args);
    }
}