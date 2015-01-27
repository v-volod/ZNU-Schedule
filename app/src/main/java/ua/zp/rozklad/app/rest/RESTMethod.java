package ua.zp.rozklad.app.rest;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;

import ua.zp.rozklad.app.App;

/**
 * @author Vojko Vladimir
 */
public abstract class RESTMethod<R, T> implements Response.ErrorListener, Response.Listener<T> {

    private static final String SITE_URL = "http://rozklad.5132.pp.ua/";
    private static final String API = "api/v1/";
    private static final String API_URL = SITE_URL + API;

    private static final String FORMAT_JSON = "format=json";
    private static final String ID_SUFFIX = "&id=%s";
    private static final String ID_IN_SUFFIX = "&id__in=%s";
    private static final String S_SUFFIX = "s=%s";

    protected static final String MODEL_URL_FORMAT =
            API_URL + "%s/?" + FORMAT_JSON;
    protected static final String MODEL_BY_ID_URL_FORMAT =
            API_URL + "%s/?" + FORMAT_JSON + ID_SUFFIX;
    protected static final String MODEL_BY_ID_IN_URL_FORMAT =
            API_URL + "%s/?" + FORMAT_JSON + ID_IN_SUFFIX;
    protected static final String MODEL_SEARCH_BY_NAME =
            API_URL + "%s/search/?" + FORMAT_JSON + "&" + S_SUFFIX;

    protected static interface Model {
        String DEPARTMENT = "department";
        String GROUP = "group";
        String TEACHER = "teacher";
        String LESSON = "lesson";
        String TIME = "time";
        String CAMPUS = "campus";
        String AUDIENCE = "audience";
        String TIMETABLE = "timetable";
    }

    public static interface Key {
        String OBJECTS = "objects";
        String ID = "id";
        String DEPARTMENT_ID = "department_id";
        String GROUP_ID = "group_id";
        String TEACHER_ID = "teacher_id";
        String LESSON_ID = "lesson_id";
        String CAMPUS_ID = "campus_id";
        String AUDIENCE = "audience";
        String AUDIENCE_ID = AUDIENCE + "_id";
        String TIME_ID = "time_id";
        String NAME = "name";
        String DAY = "day";
        String PERIODICITY = "periodicity";
        String DATE_START = "date_start";
        String DATE_END = "date_end";
        String LAST_UPDATE = "last_update";
        String NUM = "num";
        String TIME_START = "time_start";
        String TIME_END = "time_end";
        String LESSON_TYPE = "lesson_type";
        String SUBGROUP = "subgroup";
        String SUBGROUP_COUNT = "subgroup_count";
        String COURSE = "course";
    }

    public interface Filter {
        int NONE = 0;
        int BY_ID = 1;
        int BY_ID_IN = 3;
        int BY_NAME = 4;
        int BY_DEPARTMENT_ID = 5;
        int BY_GROUP_ID = 6;
        int BY_LECTURER_ID = 7;
        int BY_GROUP_AND_LECTURER_IDS = 8;
    }

    public static interface ResponseCode {
        int INVALID_REQUEST = -1;
        int PARSE_RESPONSE_ERROR = 101;
        // TODO: Define own result code for all subclasses of VolleyError
        int VOLLEY_ERROR = 102;
        int OK = 200;
        int UNKNOWN_ERROR = 500;
    }

    protected String requestUrl = null;
    protected ResponseCallback<R> callback;

    public abstract void prepare(int filter, String... params);

    public void execute(ResponseCallback<R> callback) {
        this.callback = callback;
        if (TextUtils.isEmpty(requestUrl)) {
            callback.onError(ResponseCode.INVALID_REQUEST);
        } else {
            App.getInstance().addToRequestQueue(buildRequest());
        }
    }

    public abstract MethodResponse<R> executeBlocking();

    protected abstract Request buildRequest();

    protected String buildModelFilter(String model, String param) {
        return "&" + model + "=" + param;
    }

    protected String generateIds(String... params) {
        String result = "";

        for (int i = 0; i < params.length; i++) {
            result += params[i];
            if (i < params.length - 1) {
                result += ",";
            }
        }

        return result;
    }

    protected static int generateResponseCode(Throwable throwable) {
        Log.d("ScheduleLOGS", throwable.toString());
        if (throwable instanceof ExecutionException) {
            return generateResponseCode(throwable.getCause());
        } else if (throwable instanceof VolleyError) {
            return ResponseCode.VOLLEY_ERROR;
        } else if (throwable instanceof JSONException) {
            return ResponseCode.PARSE_RESPONSE_ERROR;
        } else {
            return ResponseCode.UNKNOWN_ERROR;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        callback.onError(generateResponseCode(error));
    }
}