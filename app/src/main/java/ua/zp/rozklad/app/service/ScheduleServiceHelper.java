package ua.zp.rozklad.app.service;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Vojko Vladimir
 */
public class ScheduleServiceHelper {

    public static final String ACTION_REQUEST_RESULT = "ACTION_REQUEST_RESULT";

    public static interface Extra {
        String REQUEST_ID = "REQUEST_ID";
        String RESULT_CODE = "RESULT_CODE";
    }

    private static ScheduleServiceHelper mInstance;

    private static final Object lock = new Object();

    private Map<String, Long> pendingRequests = new HashMap<>();
    private Context context;

    private ScheduleServiceHelper(Context context) {
        this.context = context;
    }

    public static ScheduleServiceHelper getInstance(Context context) {
        synchronized (lock) {
            if (null == mInstance) {
                mInstance = new ScheduleServiceHelper(context);
            }

        }

        return mInstance;
    }

    private long generateRequestID() {
        return UUID.randomUUID().getLeastSignificantBits();
    }

    public boolean isRequestPending(long requestId) {
        return pendingRequests.containsValue(requestId);
    }

}
