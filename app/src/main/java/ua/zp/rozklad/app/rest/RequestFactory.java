package ua.zp.rozklad.app.rest;

import com.android.volley.Response.ErrorListener;
import com.android.volley.toolbox.JsonObjectRequest;

/**
 * @author Vojko Vladimir
 */
public class RequestFactory {

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


    public static JsonObjectRequest getJsonObjectRequest(ErrorListener errorListener) {
        return null;
    }

    public static enum Method {
        GET
    }

}
