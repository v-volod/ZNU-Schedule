package ua.zp.rozklad.app.processor.dependency;

/**
 * @author Vojko Vladimir
 */
public interface ResolveDependency<D, R> {

    void resolveDependency(D d, R r);
}
