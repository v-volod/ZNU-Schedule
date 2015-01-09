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

    public static final String ACTION_REQUEST_RESULT = "REQUEST_ID";

    private static final String GROUP_HASH_KEY = "GROUP";

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

    public long getGroupById(int groupId) {
        long requestId;

        if (pendingRequests.containsKey(GROUP_HASH_KEY)) {
            requestId = pendingRequests.get(GROUP_HASH_KEY);
        } else {
            requestId = generateRequestID();
            pendingRequests.put(GROUP_HASH_KEY, requestId);
        }

        ResultReceiver serviceCallback = new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                Intent requestIntent =
                        resultData.getParcelable(ScheduleService.Extra.ORIGINAL_INTENT);
                if (null != requestIntent) {
                    long requestId = requestIntent.getLongExtra(Extra.REQUEST_ID, 0);

                    pendingRequests.remove(GROUP_HASH_KEY);

                    Intent resultBroadcast = new Intent(ACTION_REQUEST_RESULT);
                    resultBroadcast.putExtra(Extra.REQUEST_ID, requestId);
                    resultBroadcast.putExtra(Extra.RESULT_CODE, resultCode);

                    context.sendBroadcast(resultBroadcast);
                }
            }
        };

        // Initiate get groups
//        Intent intent = new Intent(context, ScheduleService.class);
////        intent.putExtra(ScheduleService.Extra.METHOD, ScheduleService.METHOD_GET);
////        intent.putExtra(ScheduleService.Extra.RESOURCE_TYPE, Resource.Type.GROUP_BY_ID);
////        intent.putExtra(ScheduleService.Extra.REQUEST_PARAMS, params);
//        intent.putExtra(ScheduleService.Extra.SERVICE_CALLBACK, serviceCallback);
//        intent.putExtra(Extra.REQUEST_ID, requestId);
//
//        context.startService(intent);

        return requestId;
    }

    private long generateRequestID() {
        return UUID.randomUUID().getLeastSignificantBits();
    }

    public boolean isRequestPending(long requestId) {
        return pendingRequests.containsValue(requestId);
    }

}
