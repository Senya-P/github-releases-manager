package cz.cuni.mff.releasemanager;

public class Main {
    public static void main(String[] args) {
        ReleaseManager releaseManager = ReleaseManager.getInstance();
        releaseManager.execute(args);
    }
}