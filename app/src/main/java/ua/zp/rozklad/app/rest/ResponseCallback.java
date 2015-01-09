package ua.zp.rozklad.app.rest;

/**
 * @author Vojko Vladimir
 */
public interface ResponseCallback<T> {

    void onResponse(int responseCode, T t);

    void onError(int responseCode);
}
