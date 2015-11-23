package com.mendeley.sdk.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class NullableMap<K, V> implements Map<K, V>, Nullable {

    private final Map<K, V> delegate;
    private final boolean isNull;

    public NullableMap(Map<K, V> delegate) {
        isNull = delegate == null;
        this.delegate = delegate == null ? new HashMap<K, V>(0) : delegate;
    }

    @Override
    public boolean isNull() {
        return isNull;
    }

    /**
     * Removes all elements from this {@code Map}, leaving it empty.
     *
     * @throws UnsupportedOperationException
     *                if removing elements from this {@code Map} is not supported.
     * @see #isEmpty()
     * @see #size()
     */
    @Override
    public void clear() {
        delegate.clear();
    }

    /**
     * Returns whether this {@code Map} contains the specified key.
     *
     * @param key
     *            the key to search for.
     * @return {@code true} if this map contains the specified key,
     *         {@code false} otherwise.
     */
    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    /**
     * Returns whether this {@code Map} contains the specified value.
     *
     * @param value
     *            the value to search for.
     * @return {@code true} if this map contains the specified value,
     *         {@code false} otherwise.
     */
    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    /**
     * Returns a {@code Set} containing all of the mappings in this {@code Map}. Each mapping is
     * an instance of {@link java.util.Map.Entry}. As the {@code Set} is backed by this {@code Map},
     * changes in one will be reflected in the other.
     *
     * @return a set of the mappings
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return delegate.entrySet();
    }

    /**
     * Compares the argument to the receiver, and returns {@code true} if the
     * specified object is a {@code Map} and both {@code Map}s contain the same mappings.
     *
     * @param object
     *            the {@code Object} to compare with this {@code Object}.
     * @return boolean {@code true} if the {@code Object} is the same as this {@code Object}
     *         {@code false} if it is different from this {@code Object}.
     * @see #hashCode()
     * @see #entrySet()
     */
    @Override
    public boolean equals(Object object) {
        return delegate.equals(object);
    }

    /**
     * Returns the value of the mapping with the specified key.
     *
     * @param key
     *            the key.
     * @return the value of the mapping with the specified key, or {@code null}
     *         if no mapping for the specified key is found.
     */
    @Override
    public V get(Object key) {
        return delegate.get(key);
    }

    /**
     * Returns an integer hash code for the receiver. {@code Object}s which are equal
     * return the same value for this method.
     *
     * @return the receiver's hash.
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * Returns whether this map is empty.
     *
     * @return {@code true} if this map has no elements, {@code false}
     *         otherwise.
     * @see #size()
     */
    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /**
     * Returns a set of the keys contained in this {@code Map}. The {@code Set} is backed by
     * this {@code Map} so changes to one are reflected by the other. The {@code Set} does not
     * support adding.
     *
     * @return a set of the keys.
     */
    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    /**
     * Maps the specified key to the specified value.
     *
     * @param key
     *            the key.
     * @param value
     *            the value.
     * @return the value of any previous mapping with the specified key or
     *         {@code null} if there was no mapping.
     * @throws UnsupportedOperationException
     *                if adding to this {@code Map} is not supported.
     * @throws ClassCastException
     *                if the class of the key or value is inappropriate for
     *                this {@code Map}.
     * @throws IllegalArgumentException
     *                if the key or value cannot be added to this {@code Map}.
     * @throws NullPointerException
     *                if the key or value is {@code null} and this {@code Map} does
     *                not support {@code null} keys or values.
     */
    public V put(K key, V value) {
        return delegate.put(key, value);
    }

    /**
     * Copies every mapping in the specified {@code Map} to this {@code Map}.
     *
     * @param map
     *            the {@code Map} to copy mappings from.
     * @throws UnsupportedOperationException
     *                if adding to this {@code Map} is not supported.
     * @throws ClassCastException
     *                if the class of a key or a value of the specified {@code Map} is
     *                inappropriate for this {@code Map}.
     * @throws IllegalArgumentException
     *                if a key or value cannot be added to this {@code Map}.
     * @throws NullPointerException
     *                if a key or value is {@code null} and this {@code Map} does not
     *                support {@code null} keys or values.
     */
    public void putAll(Map<? extends K, ? extends V> map) {
        delegate.putAll(map);
    }

    /**
     * Removes a mapping with the specified key from this {@code Map}.
     *
     * @param key
     *            the key of the mapping to remove.
     * @return the value of the removed mapping or {@code null} if no mapping
     *         for the specified key was found.
     * @throws UnsupportedOperationException
     *                if removing from this {@code Map} is not supported.
     */
    @Override
    public V remove(Object key) {
        return delegate.remove(key);
    }

    /**
     * Returns the number of mappings in this {@code Map}.
     *
     * @return the number of mappings in this {@code Map}.
     */
    @Override
    public int size() {
        return delegate.size();
    }

    /**
     * Returns a {@code Collection} of the values contained in this {@code Map}. The {@code Collection}
     * is backed by this {@code Map} so changes to one are reflected by the other. The
     * {@code Collection} supports {@link Collection#remove}, {@link Collection#removeAll},
     * {@link Collection#retainAll}, and {@link Collection#clear} operations,
     * and it does not support {@link Collection#add} or {@link Collection#addAll} operations.
     * <p>
     * This method returns a {@code Collection} which is the subclass of
     * {@link AbstractCollection}. The {@link AbstractCollection#iterator} method of this subclass returns a
     * "wrapper object" over the iterator of this {@code Map}'s {@link #entrySet()}. The {@link AbstractCollection#size} method
     * wraps this {@code Map}'s {@link #size} method and the {@link AbstractCollection#contains} method wraps this {@code Map}'s
     * {@link #containsValue} method.
     * <p>
     * The collection is created when this method is called at first time and
     * returned in response to all subsequent calls. This method may return
     * different Collection when multiple calls to this method, since it has no
     * synchronization performed.
     *
     * @return a collection of the values contained in this map.
     */
    @Override
    public Collection<V> values() {
        return delegate.values();
    }
}
