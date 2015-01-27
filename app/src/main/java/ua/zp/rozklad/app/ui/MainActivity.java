package ua.zp.rozklad.app.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.provider.ScheduleContract;
import ua.zp.rozklad.app.account.GroupAuthenticator;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();
        final AccountManager manager = AccountManager.get(getApplicationContext());

        Account[] accounts = manager.getAccountsByType(GroupAuthenticator.ACCOUNT_TYPE);

        if (accounts.length == 0) {
            final Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 0);
        } else {
            ContentResolver
                    .setSyncAutomatically(accounts[0], ScheduleContract.CONTENT_AUTHORITY, true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case LoginActivity.RESULT_CANCELED:
                finish();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
