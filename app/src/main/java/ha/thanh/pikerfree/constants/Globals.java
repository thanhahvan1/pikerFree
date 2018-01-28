package ha.thanh.pikerfree.constants;

/**
 * Created by HaVan on 5/24/2017.
 */

public class Globals {
    private static Globals ourInstance = null;
    private Config config;


    private Globals() {
        config = new Config();
    }

    public static Globals getIns() {
        if (ourInstance == null) {
            ourInstance = new Globals();
        }
        return ourInstance;
    }

    public Config getConfig() {
        return config;
    }
}
