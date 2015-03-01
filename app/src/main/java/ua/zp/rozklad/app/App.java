package ua.zp.rozklad.app;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.yandex.metrica.YandexMetrica;

import ua.zp.rozklad.app.util.PreferencesUtils;

/**
 * @author Vojko Vladimir
 */
public class App extends Application {
    private static final String METRICA_API_KEY = "34091";
    private static final int METRICA_SESSION_TIMEOUT = 60;

    public static final String TAG = "ua.zp.rozklad.app.App";

    private static App mInstance;

    private RequestQueue mRequestQueue;
    private PreferencesUtils mPreferencesUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        if (!BuildConfig.DEBUG) {
            YandexMetrica.initialize(this, METRICA_API_KEY);
            YandexMetrica.setSessionTimeout(METRICA_SESSION_TIMEOUT);
        }
    }

    public static synchronized App getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public PreferencesUtils getPreferencesUtils() {
        if (mPreferencesUtils == null) {
            mPreferencesUtils = new PreferencesUtils(this);
        }
        return mPreferencesUtils;
    }

    public static void LOG_D(String message) {
        Log.d(TAG, message);
    }

    public static void LOG_E(String message) {
        Log.e(TAG, message);
    }

    public static void LOG_E(String message, Throwable throwable) {
        Log.e(TAG, message, throwable);
    }

    public static void LOG_I(String message) {
        Log.i(TAG, message);
    }
}
