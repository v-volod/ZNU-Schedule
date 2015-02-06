package ua.zp.rozklad.app.rest;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import ua.zp.rozklad.app.App;
import ua.zp.rozklad.app.rest.resource.GlobalScheduleItem;
import ua.zp.rozklad.app.rest.resource.Lecturer;
import ua.zp.rozklad.app.rest.resource.ScheduleItem;

/**
 * @author Vojko Vladimir
 */
public class GetScheduleMethod extends RESTMethod<ArrayList<GlobalScheduleItem>, JSONObject> {

    @Override
    public void prepare(int filter, String... params) {
        final String MODEL = Model.TIMETABLE;
        switch (filter) {
            case Filter.BY_GROUP_ID:
                requestUrl = String.format(MODEL_URL_FORMAT, MODEL) +
                        buildModelFilter(Model.GROUP, params[0]);
                break;
            case Filter.BY_LECTURER_ID:
                requestUrl = String.format(MODEL_URL_FORMAT, MODEL) +
                        buildModelFilter(Model.TEACHER, params[0]);
                break;
            case Filter.BY_GROUP_AND_LECTURER_IDS:
                requestUrl = String.format(MODEL_URL_FORMAT, MODEL) +
                        buildModelFilter(Model.GROUP, params[0]) +
                        buildModelFilter(Model.TEACHER, params[1]);
                break;
        }
    }

    @Override
    public MethodResponse<ArrayList<GlobalScheduleItem>> executeBlocking() {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        JsonObjectRequest request = new JsonObjectRequest(requestUrl, null, future, future);
        App.getInstance().addToRequestQueue(request);

        try {
            JSONArray objects = future.get().getJSONArray(Key.OBJECTS);
            ArrayList<GlobalScheduleItem> scheduleItems = new ArrayList<>();

            for (int i = 0; i < objects.length(); i++) {
                try {
                    scheduleItems.add(new GlobalScheduleItem(objects.getJSONObject(i)));
                } catch (JSONException e) {
                    Log.d("RestLogs", e.toString());
                }
            }

            return new MethodResponse<>(ResponseCode.OK, scheduleItems);
        } catch (Exception e) {
            return new MethodResponse<>(generateResponseCode(e), null);
        }
    }

    @Override
    protected Request buildRequest() {
        return new JsonObjectRequest(requestUrl, null, this, this);
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            JSONArray objects = response.getJSONArray(Key.OBJECTS);
            ArrayList<GlobalScheduleItem> scheduleItems = new ArrayList<>();

            for (int i = 0; i < objects.length(); i++) {
                try {
                    scheduleItems.add(new GlobalScheduleItem(objects.getJSONObject(i)));
                } catch (JSONException e) {
                    Log.d("RestLogs", e.toString());
                }
            }

            callback.onResponse(scheduleItems);
        } catch (JSONException e) {
            callback.onError(generateResponseCode(e));
        }
    }
}
