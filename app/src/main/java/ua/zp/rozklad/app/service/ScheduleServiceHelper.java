package ua.zp.rozklad.app.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Vojko Vladimir
 */
public class ScheduleServiceHelper {

    private static final String GROUP_HASH_KEY = "GROUP";
    private static final String REQUEST_ID = "REQUEST_ID";

    private static ScheduleServiceHelper mInstance;

    private static final Object lock = new Object();

    private Map<String, Long> pendingRequests = new HashMap<String, Long>();
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

    public long getGroup(String group) {
        long requestId = generateRequestID();
        pendingRequests.put(GROUP_HASH_KEY, requestId);

        ResultReceiver serviceCallback = new ResultReceiver(null){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {

            }
        };

        Intent intent = new Intent(context, ScheduleService.class);
        intent.putExtra(ScheduleService.Extra.METHOD, ScheduleService.METHOD_GET);
        intent.putExtra(ScheduleService.Extra.RESOURCE_TYPE, ScheduleService.ResourceType.GROUP);
        intent.putExtra(ScheduleService.Extra.SERVICE_CALLBACK, serviceCallback);
        intent.putExtra(REQUEST_ID, requestId);

        context.startService(intent);

        return requestId;
    }

    private long generateRequestID() {
        return UUID.randomUUID().getLeastSignificantBits();
    }

    public boolean isRequestPending(long requestId) {
        return pendingRequests.containsValue(requestId);
    }

}
