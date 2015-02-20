package ua.zp.rozklad.app.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.support.annotation.Nullable;

import ua.zp.rozklad.app.App;
import ua.zp.rozklad.app.util.PreferencesUtils;

import static java.lang.String.valueOf;

/**
 * @author Vojko Vladimir
 */
public class GroupAuthenticatorHelper {

    private static final String TYPE = GroupAuthenticator.ACCOUNT_TYPE;

    private AccountManager mAccountManager;
    private PreferencesUtils mPreferencesUtils;

    public GroupAuthenticatorHelper(Context context) {
        mAccountManager = AccountManager.get(context);
        mPreferencesUtils = App.getInstance().getPreferencesUtils();
    }

    public boolean hasAccount() {
        return mAccountManager.getAccountsByType(TYPE).length > 0;
    }

    /**
     * Get active Group account.
     * @return active Group account or or null if no one account exists.
     * */
    @Nullable
    public GroupAccount getActiveAccount() {
        Account account = getAccountByName(mPreferencesUtils.getActiveAccount());

        if (account == null) {
            Account[] accounts = getAccounts();
            if (accounts.length > 0) {
                mPreferencesUtils.saveActiveAccount(accounts[0].name);
                return new GroupAccount(mAccountManager, accounts[0]);
            } else {
                mPreferencesUtils.removeActiveAccount();
                return null;
            }
        }

        return new GroupAccount(mAccountManager, account);
    }

    @Nullable
    public Account getAccountByName(String accountName) {
        Account[] accounts = getAccounts();

        for (Account account : accounts) {
            if (accountName.equals(account.name)) {
                return account;
            }
        }

        return null;
    }

    public Account[] getAccounts() {
        return mAccountManager.getAccountsByType(TYPE);
    }

    public void setSubgroup(GroupAccount groupAccount) {
        Account account = groupAccount.getBaseAccount();
        mAccountManager.setUserData(account, GroupAuthenticator.KEY_SUBGROUP,
                valueOf(groupAccount.getSubgroup()));
    }
}