package ua.zp.rozklad.app.rest;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ua.zp.rozklad.app.rest.resource.AcademicHour;

/**
 * @author Vojko Vladimir
 */
public class GetAcademicHoursMethod extends RESTMethod<ArrayList<AcademicHour>, JSONObject> {

    public GetAcademicHoursMethod(ResponseCallback<ArrayList<AcademicHour>> callback) {
        super(callback);
    }

    @Override
    public void prepare(int filter, String... params) {
        final String MODEL = Model.TIME;
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
    protected Request buildRequest() {
        return new JsonObjectRequest(requestUrl, null, this, this);
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            JSONArray objects = response.getJSONArray(Key.OBJECTS);
            ArrayList<AcademicHour> academicHours = new ArrayList<>();

            for (int i = 0; i < objects.length(); i++) {
                academicHours.add(new AcademicHour(objects.getJSONObject(i)));
            }

            callback.onResponse(ResponseCode.OK, academicHours);
        } catch (JSONException e) {
            callback.onError(getResponseCode(e));
        }
    }
}
