package ua.zp.rozklad.app.processor.dependency;

import java.util.ArrayList;

/**
 * @author Vojko Vladimir
 */
public interface ResolveDependencies<D, R> {

    D resolveDependencies(ArrayList<R> rs);
}
