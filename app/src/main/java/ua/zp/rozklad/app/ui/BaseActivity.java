package ua.zp.rozklad.app.ui;

import android.support.v7.app.ActionBarActivity;

import com.yandex.metrica.YandexMetrica;

import ua.zp.rozklad.app.BuildConfig;

public abstract class BaseActivity extends ActionBarActivity {

    @Override
    protected void onPause() {
        super.onPause();
        if (!BuildConfig.DEBUG)
            YandexMetrica.onPauseActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!BuildConfig.DEBUG)
            YandexMetrica.onResumeActivity(this);
    }
}
