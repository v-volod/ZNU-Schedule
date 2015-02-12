package ua.zp.rozklad.app.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.support.annotation.Nullable;

import ua.zp.rozklad.app.App;
import ua.zp.rozklad.app.util.PreferencesUtils;

/**
 * @author Vojko Vladimir
 */
public class GroupAuthenticatorHelper {

    private static final String TYPE = GroupAuthenticator.ACCOUNT_TYPE;

    private AccountManager mAccountManager;
    private PreferencesUtils mPreferencesUtils;

    public GroupAuthenticatorHelper(Context context) {
        Context mContext = context;
        mAccountManager = AccountManager.get(mContext);
        mPreferencesUtils = App.getInstance().getPreferencesUtils();
    }

    public boolean hasAccount() {
        return mAccountManager.getAccountsByType(TYPE).length > 0;
    }

    @Nullable
    public GroupAccount getActiveAccount() {
        return getAccountByName(mPreferencesUtils.getActiveAccount());
    }

    @Nullable
    public GroupAccount getAccountByName(String accountName) {
        Account[] accounts = getAccounts();

        for (Account account : accounts) {
            if (accountName.equals(account.name)) {
                return new GroupAccount(mAccountManager, account);
            }
        }

        return null;
    }

    public Account[] getAccounts() {
        return mAccountManager.getAccountsByType(TYPE);
    }
}
