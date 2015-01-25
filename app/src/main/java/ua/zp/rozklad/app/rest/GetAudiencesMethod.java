package ua.zp.rozklad.app.rest;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ua.zp.rozklad.app.App;
import ua.zp.rozklad.app.rest.resource.Audience;

/**
 * @author Vojko Vladimir
 */
public class GetAudiencesMethod extends RESTMethod<ArrayList<Audience>, JSONObject> {

    @Override
    public void prepare(int filter, String... params) {
        final String MODEL = Model.AUDIENCE;
        switch (filter) {
            case Filter.BY_ID:
                requestUrl = String.format(MODEL_BY_ID_URL_FORMAT, MODEL, params[0]);
                break;
            case Filter.BY_ID_IN:
                requestUrl = String.format(MODEL_BY_ID_IN_URL_FORMAT, MODEL, generateIds(params));
                break;
            case Filter.NONE:
                requestUrl = String.format(MODEL_URL_FORMAT, MODEL);
                break;
        }
    }

    @Override
    public MethodResponse<ArrayList<Audience>> executeBlocking() {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        JsonObjectRequest request = new JsonObjectRequest(requestUrl, null, future, future);
        App.getInstance().addToRequestQueue(request);

        try {
            JSONArray objects = future.get().getJSONArray(Key.OBJECTS);
            ArrayList<Audience> audiences = new ArrayList<>();

            for (int i = 0; i < objects.length(); i++) {
                audiences.add(new Audience(objects.getJSONObject(i)));
            }

            return new MethodResponse<>(ResponseCode.OK, audiences);
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
            ArrayList<Audience> audiences = new ArrayList<>();

            for (int i = 0; i < objects.length(); i++) {
                audiences.add(new Audience(objects.getJSONObject(i)));
            }

            callback.onResponse(audiences);
        } catch (JSONException e) {
            callback.onError(generateResponseCode(e));
        }
    }
}
