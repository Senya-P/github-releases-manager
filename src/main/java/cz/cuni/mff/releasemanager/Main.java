package cz.cuni.mff.releasemanager;

public class Main { // extends Runnable
    public static void main(String[] args) {
        ReleaseManager releaseManager = ReleaseManager.getInstance();
        //String[] cmd = {"list"};
        //String[] cmd = {"install", "keepassxreboot/keepassxc"};
        //String[] cmd = {"search", "keepass"};
        releaseManager.execute(args);
        //releaseManager.list();
    }
}

/*
 * blocking cmd iface?
 * multi-thread
 * version control: json?
 * resource bundle
 * filter unsupported repos
 * no internet conn
 */