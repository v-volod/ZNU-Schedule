package ua.zp.rozklad.app.rest;

import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ua.zp.rozklad.app.App;
import ua.zp.rozklad.app.rest.resources.Resources;

/**
 * @author Vojko Vladimir
 */
public class RESTMethod {

    private static final String SITE_URL = "http://rozklad.5132.pp.ua/";
    private static final String API = "api/v1/";
    private static final String API_URL = SITE_URL + API;

    private static final String FORMAT_JSON = "format=json";
    private static final String ID_SUFFIX = "&id=%s";
    private static final String ID_IN_SUFFIX = "&id__in=%s";
    private static final String OBJECTS = "objects";

    private static final String MODEL_URL_FORMAT =
            API_URL + "%s/?" + FORMAT_JSON;
    private static final String MODEL_ID_URL_FORMAT =
            API_URL + "%s/?" + FORMAT_JSON + ID_SUFFIX;
    private static final String MODEL_ID_IN_URL_FORMAT =
            API_URL + "%s/?" + FORMAT_JSON + ID_IN_SUFFIX;

    private interface Model {
        String GROUP = "group";
    }

    public interface ResponseCallback<T>
            extends Response.Listener<T>, Response.ErrorListener {
    }

    public static void GET(final ResponseCallback<JSONObject> callback,
                           final int resourcesType, String... params) {
        switch (resourcesType) {
            case Resources.Type.GROUP_BY_ID:
                JsonObjectRequest request = new JsonObjectRequest(
                        String.format(MODEL_ID_URL_FORMAT, Model.GROUP, params[0]),
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    callback.onResponse(getObject(response));
                                } catch (JSONException e) {
                                    callback.onErrorResponse(new ParseError());
                                }
                            }
                        },
                        callback);
                App.getInstance().addToRequestQueue(request);
                break;
        }
    }

    private static JSONObject getObject(JSONObject response) throws JSONException {
        JSONArray objects = getObjects(response);
        if (objects.length() == 1) {
            return objects.getJSONObject(0);
        }

        throw new JSONException("Invalid response");
    }

    private static JSONArray getObjects(JSONObject response) throws JSONException {
        return response.getJSONArray("objects");
    }

}