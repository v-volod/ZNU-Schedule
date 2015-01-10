package ua.zp.rozklad.app.rest;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ua.zp.rozklad.app.App;
import ua.zp.rozklad.app.rest.resource.ScheduleItem;

/**
 * @author Vojko Vladimir
 */
public class GetScheduleItemsMethod extends RESTMethod<ArrayList<ScheduleItem>> {

    public interface Filter {
        int BY_GROUP_ID = 1;
        int BY_LECTURER_ID = 2;
        int BY_GROUP_AND_LECTURER_IDS = 3;
    }

    public GetScheduleItemsMethod(ResponseCallback<ArrayList<ScheduleItem>> callback, int filter,
                                  String... params) {
        super(callback);
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
    public void execute() {
        super.execute();
        JsonObjectRequest request = new JsonObjectRequest(
                requestUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray objects = response.getJSONArray(Key.OBJECTS);
                            ArrayList<ScheduleItem> groups = new ArrayList<>();

                            for (int i = 0; i < objects.length(); i++) {
                                groups.add(new ScheduleItem(objects.getJSONObject(i)));
                            }

                            callback.onResponse(ResponseCode.OK, groups);
                        } catch (JSONException e) {
                            callback.onError(getResponseCode(e));
                        }
                    }
                },
                this);
        App.getInstance().addToRequestQueue(request);
    }
}
