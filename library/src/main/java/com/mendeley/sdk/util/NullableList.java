package com.mendeley.sdk.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class NullableList<T> implements List<T>, Nullable {

    private final List<T> delegate;
    private final boolean isNull;

    public NullableList(List<T> delegate) {
        this.delegate = delegate == null ? new ArrayList<T>(0) : delegate;
        isNull = delegate == null;
    }

    @Override
    public boolean isNull() {
        return isNull;
    }

    /**
     * Inserts the specified object into this {@code List} at the specified location.
     * The object is inserted before the current element at the specified
     * location. If the location is equal to the size of this {@code List}, the object
     * is added at the end. If the location is smaller than the size of this
     * {@code List}, then all elements beyond the specified location are moved by one
     * position towards the end of the {@code List}.
     *
     * @param location
     *            the index at which to insert.
     * @param object
     *            the object to add.
     * @throws UnsupportedOperationException
     *                if adding to this {@code List} is not supported.
     * @throws ClassCastException
     *                if the class of the object is inappropriate for this
     *                {@code List}.
     * @throws IllegalArgumentException
     *                if the object cannot be added to this {@code List}.
     * @throws IndexOutOfBoundsException
     *                if {@code location < 0 || location > size()}
     */
    public void add(int location, T object) {
        delegate.add(location, object);
    }

    /**
     * Adds the specified object at the end of this {@code List}.
     *
     * @param object
     *            the object to add.
     * @return always true.
     * @throws UnsupportedOperationException
     *                if adding to this {@code List} is not supported.
     * @throws ClassCastException
     *                if the class of the object is inappropriate for this
     *                {@code List}.
     * @throws IllegalArgumentException
     *                if the object cannot be added to this {@code List}.
     */
    public boolean add(T object) {
        return delegate.add(object);
    }

    /**
     * Inserts the objects in the specified collection at the specified location
     * in this {@code List}. The objects are added in the order they are returned from
     * the collection's iterator.
     *
     * @param location
     *            the index at which to insert.
     * @param collection
     *            the collection of objects to be inserted.
     * @return true if this {@code List} has been modified through the insertion, false
     *         otherwise (i.e. if the passed collection was empty).
     * @throws UnsupportedOperationException
     *                if adding to this {@code List} is not supported.
     * @throws ClassCastException
     *                if the class of an object is inappropriate for this
     *                {@code List}.
     * @throws IllegalArgumentException
     *                if an object cannot be added to this {@code List}.
     * @throws IndexOutOfBoundsException
     *                if {@code location < 0 || location > size()}
     */
    public boolean addAll(int location, Collection<? extends T> collection) {
        return delegate.addAll(location, collection);
    }

    /**
     * Adds the objects in the specified collection to the end of this {@code List}. The
     * objects are added in the order in which they are returned from the
     * collection's iterator.
     *
     * @param collection
     *            the collection of objects.
     * @return {@code true} if this {@code List} is modified, {@code false} otherwise
     *         (i.e. if the passed collection was empty).
     * @throws UnsupportedOperationException
     *                if adding to this {@code List} is not supported.
     * @throws ClassCastException
     *                if the class of an object is inappropriate for this
     *                {@code List}.
     * @throws IllegalArgumentException
     *                if an object cannot be added to this {@code List}.
     */
    public boolean addAll(Collection<? extends T> collection) {
        return delegate.addAll(collection);
    }

    /**
     * Removes all elements from this {@code List}, leaving it empty.
     *
     * @throws UnsupportedOperationException
     *                if removing from this {@code List} is not supported.
     * @see #isEmpty
     * @see #size
     */
    @Override
    public void clear() {
        delegate.clear();
    }

    /**
     * Tests whether this {@code List} contains the specified object.
     *
     * @param object
     *            the object to search for.
     * @return {@code true} if object is an element of this {@code List}, {@code false}
     *         otherwise
     */
    @Override
    public boolean contains(Object object) {
        return delegate.contains(object);
    }

    /**
     * Tests whether this {@code List} contains all objects contained in the
     * specified collection.
     *
     * @param collection
     *            the collection of objects
     * @return {@code true} if all objects in the specified collection are
     *         elements of this {@code List}, {@code false} otherwise.
     */
    @Override
    public boolean containsAll(Collection<?> collection) {
        return delegate.containsAll(collection);
    }

    /**
     * Compares the given object with the {@code List}, and returns true if they
     * represent the <em>same</em> object using a class specific comparison. For
     * {@code List}s, this means that they contain the same elements in exactly the same
     * order.
     *
     * @param object
     *            the object to compare with this object.
     * @return boolean {@code true} if the object is the same as this object,
     *         and {@code false} if it is different from this object.
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        return delegate.equals(object);
    }

    /**
     * Returns the element at the specified location in this {@code List}.
     *
     * @param location
     *            the index of the element to return.
     * @return the element at the specified location.
     * @throws IndexOutOfBoundsException
     *                if {@code location < 0 || location >= size()}
     */
    @Override
    public T get(int location) {
        return delegate.get(location);
    }

    /**
     * Returns the hash code for this {@code List}. It is calculated by taking each
     * element' hashcode and its position in the {@code List} into account.
     *
     * @return the hash code of the {@code List}.
     */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * Searches this {@code List} for the specified object and returns the index of the
     * first occurrence.
     *
     * @param object
     *            the object to search for.
     * @return the index of the first occurrence of the object or -1 if the
     *         object was not found.
     */
    @Override
    public int indexOf(Object object) {
        return delegate.indexOf(object);
    }

    /**
     * Returns whether this {@code List} contains no elements.
     *
     * @return {@code true} if this {@code List} has no elements, {@code false}
     *         otherwise.
     * @see #size
     */
    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /**
     * Returns an iterator on the elements of this {@code List}. The elements are
     * iterated in the same order as they occur in the {@code List}.
     *
     * @return an iterator on the elements of this {@code List}.
     * @see Iterator
     */
    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    /**
     * Searches this {@code List} for the specified object and returns the index of the
     * last occurrence.
     *
     * @param object
     *            the object to search for.
     * @return the index of the last occurrence of the object, or -1 if the
     *         object was not found.
     */
    @Override
    public int lastIndexOf(Object object) {
        return delegate.lastIndexOf(object);
    }

    /**
     * Returns a {@code List} iterator on the elements of this {@code List}. The elements are
     * iterated in the same order that they occur in the {@code List}.
     *
     * @return a {@code List} iterator on the elements of this {@code List}
     *
     * @see ListIterator
     */
    @Override
    public ListIterator<T> listIterator() {
        return delegate.listIterator();
    }

    /**
     * Returns a list iterator on the elements of this {@code List}. The elements are
     * iterated in the same order as they occur in the {@code List}. The iteration
     * starts at the specified location.
     *
     * @param location
     *            the index at which to start the iteration.
     * @return a list iterator on the elements of this {@code List}.
     * @throws IndexOutOfBoundsException
     *                if {@code location < 0 || location > size()}
     * @see java.util.ListIterator
     */
    @Override
    public ListIterator<T> listIterator(int location) {
        return delegate.listIterator(location);
    }

    /**
     * Removes the object at the specified location from this {@code List}.
     *
     * @param location
     *            the index of the object to remove.
     * @return the removed object.
     * @throws UnsupportedOperationException
     *                if removing from this {@code List} is not supported.
     * @throws IndexOutOfBoundsException
     *                if {@code location < 0 || location >= size()}
     */
    @Override
    public T remove(int location) {
        return delegate.remove(location);
    }

    /**
     * Removes the first occurrence of the specified object from this {@code List}.
     *
     * @param object
     *            the object to remove.
     * @return true if this {@code List} was modified by this operation, false
     *         otherwise.
     * @throws UnsupportedOperationException
     *                if removing from this {@code List} is not supported.
     */
    @Override
    public boolean remove(Object object) {
        return delegate.remove(object);
    }

    /**
     * Removes all occurrences in this {@code List} of each object in the specified
     * collection.
     *
     * @param collection
     *            the collection of objects to remove.
     * @return {@code true} if this {@code List} is modified, {@code false} otherwise.
     * @throws UnsupportedOperationException
     *                if removing from this {@code List} is not supported.
     */
    @Override
    public boolean removeAll(Collection<?> collection) {
        return delegate.removeAll(collection);
    }

    /**
     * Removes all objects from this {@code List} that are not contained in the
     * specified collection.
     *
     * @param collection
     *            the collection of objects to retain.
     * @return {@code true} if this {@code List} is modified, {@code false} otherwise.
     * @throws UnsupportedOperationException
     *                if removing from this {@code List} is not supported.
     */
    @Override
    public boolean retainAll(Collection<?> collection) {
        return delegate.retainAll(collection);
    }

    /**
     * Replaces the element at the specified location in this {@code List} with the
     * specified object. This operation does not change the size of the {@code List}.
     *
     * @param location
     *            the index at which to put the specified object.
     * @param object
     *            the object to insert.
     * @return the previous element at the index.
     * @throws UnsupportedOperationException
     *                if replacing elements in this {@code List} is not supported.
     * @throws ClassCastException
     *                if the class of an object is inappropriate for this
     *                {@code List}.
     * @throws IllegalArgumentException
     *                if an object cannot be added to this {@code List}.
     * @throws IndexOutOfBoundsException
     *                if {@code location < 0 || location >= size()}
     */
    public T set(int location, T object) {
        return delegate.set(location, object);
    }

    /**
     * Returns the number of elements in this {@code List}.
     *
     * @return the number of elements in this {@code List}.
     */
    @Override
    public int size() {
        return delegate.size();
    }

    /**
     * Returns a {@code List} of the specified portion of this {@code List} from the given start
     * index to the end index minus one. The returned {@code List} is backed by this
     * {@code List} so changes to it are reflected by the other.
     *
     * @param start
     *            the index at which to start the sublist.
     * @param end
     *            the index one past the end of the sublist.
     * @return a list of a portion of this {@code List}.
     * @throws IndexOutOfBoundsException
     *                if {@code start < 0, start > end} or {@code end >
     *                size()}
     */
    @Override
    public List<T> subList(int start, int end) {
        return delegate.subList(start, end);
    }

    /**
     * Returns an array containing all elements contained in this {@code List}.
     *
     * @return an array of the elements from this {@code List}.
     */
    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    /**
     * Returns an array containing all elements contained in this {@code List}. If the
     * specified array is large enough to hold the elements, the specified array
     * is used, otherwise an array of the same type is created. If the specified
     * array is used and is larger than this {@code List}, the array element following
     * the collection elements is set to null.
     *
     * @param array
     *            the array.
     * @return an array of the elements from this {@code List}.
     * @throws ArrayStoreException
     *                if the type of an element in this {@code List} cannot be stored
     *                in the type of the specified array.
     */
    @Override
    public <T1> T1[] toArray(T1[] array) {
        return delegate.toArray(array);
    }

}
