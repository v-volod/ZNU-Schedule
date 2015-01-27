package ua.zp.rozklad.app.rest;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ua.zp.rozklad.app.App;
import ua.zp.rozklad.app.rest.resource.Lecturer;
import ua.zp.rozklad.app.rest.resource.ScheduleItem;

/**
 * @author Vojko Vladimir
 */
public class GetScheduleMethod extends RESTMethod<ArrayList<ScheduleItem>, JSONObject> {

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
    public MethodResponse<ArrayList<ScheduleItem>> executeBlocking() {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        JsonObjectRequest request = new JsonObjectRequest(requestUrl, null, future, future);
        App.getInstance().addToRequestQueue(request);

        try {
            JSONArray objects = future.get().getJSONArray(Key.OBJECTS);
            ArrayList<ScheduleItem> groups = new ArrayList<>();

            for (int i = 0; i < objects.length(); i++) {
                groups.add(new ScheduleItem(objects.getJSONObject(i)));
            }

            return new MethodResponse<>(ResponseCode.OK, groups);
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
            ArrayList<ScheduleItem> groups = new ArrayList<>();

            for (int i = 0; i < objects.length(); i++) {
                groups.add(new ScheduleItem(objects.getJSONObject(i)));
            }

            callback.onResponse(groups);
        } catch (JSONException e) {
            callback.onError(generateResponseCode(e));
        }
    }
}
