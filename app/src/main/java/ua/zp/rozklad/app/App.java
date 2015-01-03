package ua.zp.rozklad.app;

import android.app.Application;

/**
 * @author Vojko Vladimir
 */
public class App extends Application {

    private static App mInstance = new App();

    public static final String PACKAGE_NAME = mInstance.getPackageName();

    public static App getInstance() {
        return mInstance;
    }


}
