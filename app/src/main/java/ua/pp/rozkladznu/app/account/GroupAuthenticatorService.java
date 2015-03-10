package ua.pp.rozkladznu.app.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author Vojko Vladimir
 */
public class GroupAuthenticatorService extends Service {

    private GroupAuthenticator authenticator;

    @Override
    public void onCreate() {
        authenticator = new GroupAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
