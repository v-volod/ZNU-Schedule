package ua.pp.rozkladznu.app;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.yandex.metrica.YandexMetrica;

import ua.pp.rozkladznu.app.account.GroupAuthenticatorHelper;
import ua.pp.rozkladznu.app.util.PreferencesUtils;

/**
 * @author Vojko Vladimir
 */
public class App extends Application {
    private static final String METRICA_API_KEY = "34091";
    private static final int METRICA_SESSION_TIMEOUT = 60;

    public static final String TAG = "ua.pp.rozkladznu.app.App";

    private static App mInstance;

    private RequestQueue mRequestQueue;
    private PreferencesUtils mPreferencesUtils;
    private GroupAuthenticatorHelper mGroupAuthenticatorHelper;

    private final Object MANUAL_SYNC_ACTIVE_LOCK = new Object();
    private boolean isManualSyncActive = false;

    private final Object MANUAL_SYNC_REQUESTED_LOCK = new Object();
    private boolean isManualSyncRequested = false;

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

    public GroupAuthenticatorHelper getGroupAuthenticatorHelper() {
        if (mGroupAuthenticatorHelper == null) {
            mGroupAuthenticatorHelper = new GroupAuthenticatorHelper(this);
        }

        return mGroupAuthenticatorHelper;
    }

    public boolean isManualSyncActive() {
        synchronized (MANUAL_SYNC_ACTIVE_LOCK) {
            return isManualSyncActive;
        }
    }

    public void setManualSyncActive(boolean isActive) {
        synchronized (MANUAL_SYNC_ACTIVE_LOCK) {
            isManualSyncActive = isActive;
        }
        setManualSyncRequested(false);
    }

    public boolean isManualSyncRequested() {
        synchronized (MANUAL_SYNC_REQUESTED_LOCK) {
            return isManualSyncRequested;
        }
    }

    public void setManualSyncRequested(boolean isRequested) {
        synchronized (MANUAL_SYNC_REQUESTED_LOCK) {
            isManualSyncRequested = isRequested;
        }
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
