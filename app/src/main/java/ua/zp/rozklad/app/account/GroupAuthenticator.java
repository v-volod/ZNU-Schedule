package ua.zp.rozklad.app.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.ui.LoginActivity;

/**
 * @author Vojko Vladimir
 */
public class GroupAuthenticator extends AbstractAccountAuthenticator {
    public static final String ACCOUNT_TYPE = "rozklad.zp.ua.group";

    public static final String KEY_AUTH_TOKEN_TYPE = "AUTH_TOKEN_TYPE";
    public static final String KEY_GROUP_ID = "KEY_GROUP_ID";
    public static final String KEY_SUBGROUP = "KEY_SUBGROUP";
    public static final String KEY_SUBGROUP_COUNT = "KEY_SUBGROUP_COUNT";
    public static final String KEY_DEPARTMENT_NAME = "KEY_DEPARTMENT_NAME";

    public static final int ERROR_CODE_ONE_ACCOUNT_ALLOWED = 101;

    private Context context;
    private final Handler handler = new Handler();

    public GroupAuthenticator(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
                             String authTokenType, String[] requiredFeatures, Bundle options)
            throws NetworkErrorException {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);

        final Bundle result = new Bundle();

        if (accounts.length > 0) {
            final String ERROR_MASSAGE = context.getString(R.string.one_account_allowed);

            result.putInt(AccountManager.KEY_ERROR_CODE, ERROR_CODE_ONE_ACCOUNT_ALLOWED);
            result.putString(AccountManager.KEY_ERROR_MESSAGE, ERROR_MASSAGE);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, ERROR_MASSAGE, Toast.LENGTH_SHORT).show();
                }
            });

            return result;

        } else {
            final Intent intent = new Intent(context, LoginActivity.class);
            intent.putExtra(KEY_AUTH_TOKEN_TYPE, authTokenType);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

            result.putParcelable(AccountManager.KEY_INTENT, intent);

            return result;
        }
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
                                     Bundle options)
            throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
                               String authTokenType, Bundle options)
            throws NetworkErrorException {
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
                                    String authTokenType, Bundle options)
            throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
                              String[] features)
            throws NetworkErrorException {
        return null;
    }
}
