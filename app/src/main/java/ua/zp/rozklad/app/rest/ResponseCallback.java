package ua.zp.rozklad.app.rest;

/**
 * @author Vojko Vladimir
 */
public interface ResponseCallback<T> {

    void onResponse(T t);

    void onError(int responseCode);
}
