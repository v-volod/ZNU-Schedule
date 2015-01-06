package ua.zp.rozklad.app.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 * @author Vojko Vladimir
 */
public class ScheduleService extends IntentService {

    public static final String METHOD_GET = "GET";

    public interface Extra {
        String METHOD = "ua.zp.rozklad.app.service.METHOD_EXTRA";
        String RESOURCE_TYPE = "ua.zp.rozklad.app.service.service.RESOURCE_TYPE_EXTRA";
        String SERVICE_CALLBACK = "ua.zp.rozklad.app.service.service.SERVICE_CALLBACK_EXTRA";
    }

    private static final int INVALID_REQUEST = -1;

    public interface ResourceType {
        int GROUP = 1;
    }

    private ResultReceiver mCallback;

    public ScheduleService() {
        super("ScheduleService");
    }

    @Override
    protected void onHandleIntent(Intent requestIntent) {
        if (requestIntent != null) {

            String method = requestIntent.getStringExtra(Extra.METHOD);
            int resourceType = requestIntent.getIntExtra(Extra.RESOURCE_TYPE, -1);
            mCallback = requestIntent.getParcelableExtra(Extra.SERVICE_CALLBACK);

            switch (resourceType) {
                case ResourceType.GROUP:
                    if (METHOD_GET.equals(method)) {

                    } else {
                        mCallback.send(INVALID_REQUEST, null);
                    }
                    break;
                default:
                    mCallback.send(INVALID_REQUEST, null);
            }
        }
    }

}
