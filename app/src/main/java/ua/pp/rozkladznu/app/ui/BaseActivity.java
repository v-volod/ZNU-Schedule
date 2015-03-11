package ua.pp.rozkladznu.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.yandex.metrica.YandexMetrica;

import ua.pp.rozkladznu.app.App;
import ua.pp.rozkladznu.app.BuildConfig;
import ua.pp.rozkladznu.app.account.GroupAccount;
import ua.pp.rozkladznu.app.account.GroupAuthenticatorHelper;

public abstract class BaseActivity extends ActionBarActivity {

    protected static final int REQUEST_LOGIN = 100;

    private GroupAuthenticatorHelper mGroupAuthenticatorHelper;
    private GroupAccount mGroupAccount;

    private boolean isLoginRequested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGroupAuthenticatorHelper = App.getInstance().getGroupAuthenticatorHelper();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (App.getInstance().isMetricaInitialized())
            YandexMetrica.onPauseActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (App.getInstance().isMetricaInitialized())
            YandexMetrica.onResumeActivity(this);

        authenticateAccount();
    }

    protected GroupAuthenticatorHelper getGroupAuthenticatorHelper() {
        return mGroupAuthenticatorHelper;
    }

    protected GroupAccount getAccount() {
        return mGroupAccount;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN) {
            switch (resultCode) {
                case LoginActivity.RESULT_CANCELED:
                    finish();
                    break;
                case LoginActivity.RESULT_OK:
                    authenticateAccount();
                    break;
            }

            isLoginRequested = false;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void authenticateAccount() {
        GroupAccount groupAccount = mGroupAuthenticatorHelper.getActiveAccount();

        if (groupAccount == null) {
            onAccountDeleted();
        } else if (mGroupAccount == null) {
            mGroupAccount = groupAccount;
            onAccountAuthenticated();
        } else if (!mGroupAccount.equals(groupAccount)) {
            mGroupAccount = groupAccount;
            onAccountChanged();
        }
    }

    protected void requestLogin() {
        if (!isLoginRequested) {
            mGroupAccount = null;
            startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_LOGIN);
            isLoginRequested = true;
        }
    }

    protected void onAccountDeleted() {
        requestLogin();
    }

    protected void onAccountChanged() {
        /*
        * Noop. subclasses implement this if needed.
        * */
    }

    protected void onAccountAuthenticated() {
        /*
        * Noop. subclasses implement this if needed.
        * */
    }
}
