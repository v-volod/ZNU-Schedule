package ua.zp.rozklad.app.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.util.ArrayList;

import ua.zp.rozklad.app.rest.GetGroupsMethod;
import ua.zp.rozklad.app.rest.ResponseCallback;
import ua.zp.rozklad.app.rest.resource.Group;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 * @author Vojko Vladimir
 */
public class ScheduleService extends IntentService {

    public static final String METHOD_GET = "GET";

    public interface Method {
        String GET = "GET";
        String UPDATE = "UPDATE";
    }

    public interface Extra {
        String METHOD = "ua.zp.rozklad.app.service.METHOD_EXTRA";
        String RESOURCE_TYPE = "ua.zp.rozklad.app.service.service.RESOURCE_TYPE_EXTRA";
        String SERVICE_CALLBACK = "ua.zp.rozklad.app.service.service.SERVICE_CALLBACK_EXTRA";
        String REQUEST_PARAMS = "ua.zp.rozklad.app.service.REQUEST_PARAMS_EXTRA";

        String ORIGINAL_INTENT = "ORIGINAL_INTENT_EXTRA";
    }

    private ResultReceiver mCallback;

    public ScheduleService() {
        super("ScheduleService");
    }

    @Override
    protected void onHandleIntent(final Intent requestIntent) {
        if (requestIntent != null) {

        }
    }

    private Bundle getResultData(Intent requestIntent) {
        Bundle resultData = new Bundle();
        resultData.putParcelable(Extra.ORIGINAL_INTENT, requestIntent);
        return resultData;
    }

}
