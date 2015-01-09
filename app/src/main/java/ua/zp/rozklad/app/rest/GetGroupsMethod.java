package ua.zp.rozklad.app.rest;

import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ua.zp.rozklad.app.App;
import ua.zp.rozklad.app.rest.resource.Group;

/**
 * @author Vojko Vladimir
 */
public class GetGroupsMethod extends RESTMethod<ArrayList<Group>> {

    public static interface Filter {
        int NONE = 0;
        int BY_ID = 1;
        int BY_ID_IN = 2;
        int BY_NAME = 3;
        int BY_DEPARTMENT_ID = 4;
    }

    public GetGroupsMethod(ResponseCallback<ArrayList<Group>> callback, int filter,
                           String... params) {
        super(callback);
        final String MODEL = Model.GROUP;
        switch (filter) {
            case Filter.BY_ID:
                requestUrl = String.format(MODEL_BY_ID_URL_FORMAT, MODEL, params[0]);
                break;
            case Filter.BY_ID_IN:
                requestUrl = String.format(MODEL_BY_ID_IN_URL_FORMAT, MODEL, generateIds(params));
                break;
            case Filter.BY_NAME:
                requestUrl = String.format(MODEL_SEARCH_BY_NAME, MODEL, params[0]);
                break;
            case Filter.BY_DEPARTMENT_ID:
                requestUrl = String.format(MODEL_BY_MODEL_URL_FORMAT, MODEL, Model.DEPARTMENT,
                        params[0]);
            case Filter.NONE:
                requestUrl = String.format(MODEL_URL_FORMAT, MODEL);
                break;
        }
    }

    @Override
    public void execute() {
        super.execute();
        JsonObjectRequest request = new JsonObjectRequest(
                requestUrl,
                null,
                new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray objects = response.getJSONArray(Key.OBJECTS);
                            ArrayList<Group> groups = new ArrayList<>();

                            for (int i = 0; i < objects.length(); i++) {
                                groups.add(new Group(objects.getJSONObject(i)));
                            }

                            callback.onResponse(ResponseCode.OK, groups);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                this);
        App.getInstance().addToRequestQueue(request);
    }
}
