package ua.zp.rozklad.app.rest.resource;

import java.io.Serializable;

/**
 * @author Vojko Vladimir
 */
public abstract class Resource implements Serializable {

    public static interface Type {
        int GROUP = 1;
    }

}
