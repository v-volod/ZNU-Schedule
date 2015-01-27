package ua.zp.rozklad.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author Vojko Vladimir
 */
public class ScheduleSyncService extends Service {

    private static final Object lock = new Object();
    private static ScheduleSyncAdapter syncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (lock) {
            if (syncAdapter == null)
                syncAdapter = new ScheduleSyncAdapter(getApplicationContext(), true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
