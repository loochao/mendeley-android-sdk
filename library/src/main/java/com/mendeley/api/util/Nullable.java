package com.mendeley.api.util;

/**
 * Interface of those objects that could be considered as null by client code, whist still having
 * a not-null reference.
 * <p/>
 * Useful to implement the null-object pattern while being available to check if the object was
 * not initialised.
 */
public interface Nullable {

    boolean isNull();
}
