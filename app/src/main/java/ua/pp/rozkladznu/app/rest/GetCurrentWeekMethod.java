package ua.pp.rozkladznu.app.rest;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONException;
import org.json.JSONObject;

import ua.pp.rozkladznu.app.App;
import ua.pp.rozkladznu.app.rest.resource.CurrentWeek;

/**
 * @author Vojko Vladimir
 */
public class GetCurrentWeekMethod extends RESTMethod<CurrentWeek, JSONObject> {

    @Override
    public void prepare(int filter, String... params) {
        if (filter == Filter.NONE) {
            requestUrl = String.format(MODEL_URL_FORMAT, Model.CURRENT_WEEK);
        }
    }

    @Override
    public MethodResponse<CurrentWeek> executeBlocking() {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        JsonObjectRequest request = new JsonObjectRequest(requestUrl, null, future, future);
        App.getInstance().addToRequestQueue(request);

        try {
            return new MethodResponse<>(ResponseCode.OK, new CurrentWeek(future.get()));
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
            callback.onResponse(new CurrentWeek(response));
        } catch (JSONException e) {
            callback.onError(generateResponseCode(e));
        }
    }
}
