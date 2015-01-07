package ua.zp.rozklad.app.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.android.volley.ParseError;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import ua.zp.rozklad.app.processor.GroupProcessor;
import ua.zp.rozklad.app.rest.RESTMethod;
import ua.zp.rozklad.app.rest.resources.Resources;

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
        String GROUP_ID = "ua.zp.rozklad.app.service.GROUP_ID_EXTRA";

        String ORIGINAL_INTENT = "ORIGINAL_INTENT_EXTRA";
    }

    public interface ResultCode {
        int INVALID_REQUEST = -1;

        int PARSE_RESPONSE_ERROR = 101;

        int OK = 200;
    }

    private ResultReceiver mCallback;

    public ScheduleService() {
        super("ScheduleService");
    }

    @Override
    protected void onHandleIntent(final Intent requestIntent) {
        if (requestIntent != null) {

            String method = requestIntent.getStringExtra(Extra.METHOD);
            int resourceType = requestIntent.getIntExtra(Extra.RESOURCE_TYPE, -1);
            mCallback = requestIntent.getParcelableExtra(Extra.SERVICE_CALLBACK);

            switch (resourceType) {
                case Resources.Type.GROUP_BY_ID:
                    if (Method.GET.equals(method)) {
                        String groupId = requestIntent.getStringExtra(Extra.GROUP_ID);
                        RESTMethod.GET(new RESTMethod.ResponseCallback<JSONObject>() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                int resultCode;

                                if (error instanceof ParseError) {
                                    resultCode = ResultCode.PARSE_RESPONSE_ERROR;
                                } else {
                                    return;
                                }

                                sendToCallBack(resultCode, getResultData(requestIntent));
                                Log.e("MyLogs", "onErrorResponse: " + error);
                            }

                            @Override
                            public void onResponse(JSONObject response) {
                                GroupProcessor processor =
                                        new GroupProcessor(getApplicationContext());
                                try {
                                    Log.d("MyLogs", "RESPONSE: " + response);
                                    processor.insert(response);
                                    sendToCallBack(ResultCode.OK, getResultData(requestIntent));
                                } catch (JSONException e) {
                                    sendToCallBack(ResultCode.PARSE_RESPONSE_ERROR,
                                            getResultData(requestIntent));
                                    Log.e("MyLogs", "catch JSONException: " + e);
                                }
                            }
                        }, resourceType, groupId);
                    } else {
                        sendToCallBack(ResultCode.INVALID_REQUEST, getResultData(requestIntent));
                    }
                    break;
                default:
                    sendToCallBack(ResultCode.INVALID_REQUEST, getResultData(requestIntent));
            }
        }
    }

    private void sendToCallBack(int resultCode, Bundle resultData) {
        if (mCallback != null) {
            mCallback.send(resultCode, resultData);
        }
    }

    private Bundle getResultData(Intent requestIntent) {
        Bundle resultData = new Bundle();
        resultData.putParcelable(Extra.ORIGINAL_INTENT, requestIntent);
        return resultData;
    }

}
