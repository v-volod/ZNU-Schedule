package ua.zp.rozklad.app;

import android.app.Application;

/**
 * @author Vojko Vladimir
 */
public class App extends Application {

    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized App getInstance() {
        return mInstance;
    }

}
