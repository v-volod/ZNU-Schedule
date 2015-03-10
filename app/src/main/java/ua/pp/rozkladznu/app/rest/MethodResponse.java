package ua.pp.rozkladznu.app.rest;

/**
 * @author Vojko Vladimir
 */
public class MethodResponse<T> {

    private int responseCode;
    private T response;

    public MethodResponse(int responseCode, T response) {
        this.responseCode = responseCode;
        this.response = response;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public T getResponse() {
        return response;
    }
}
