package util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A List (which does not implement the {@link List} interface).
 * It is indexed by the values of an {@link Enum} and supports multiple values corresponding to
 * the same {@link Enum} value (usually called enumIndex in the code).
 *
 * @param <E> the enum to value used as an index
 * @param <T> the data type
 */
public class EnumIndexedList<E extends Enum, T> implements Iterable {

    /**
     * Store the data in a simple list
     */
    private List<T> list;

    /**
     * Store offsets to the actual data (when accessing it with an {@link E} value)
     * Any enum value has to look add this offset to its ordinal value to find the first data
     * item corresponding to it.
     */
    private int[] offsets;

    public EnumIndexedList(Class<E> enumClass) {
        final int size = enumClass.getEnumConstants().length;
        this.list = new ArrayList<>(size);
        this.offsets = new int[size];
    }

    /**
     * Helper
     *
     * @param enumIndex the index to use for data access
     * @return the integer index
     */
    private int getIndex(E enumIndex) {
        final int index = enumIndex.ordinal();
        return index + offsets[index];
    }

    /**
     * Helper to get the next data index following a previous index
     *
     * @param enumIndex index before the returned index
     * @return the next integer index
     */
    private int getNextIndex(E enumIndex) {
        final int index = getIndex(enumIndex);
        return getNextIndex(index);
    }

    /**
     * Helper to get the next data index following a previous index
     *
     * @param index index before the returned index
     * @return the next integer index
     */
    private int getNextIndex(int index) {
        int nextIndex = list.size();

        if (index < offsets.length - 1) {
            nextIndex = index + 1;
            nextIndex = nextIndex + offsets[nextIndex];
        }

        return nextIndex;
    }

    /**
     * Insert data corresponding to the given index. Will not replace any existing data previously inserted.
     *
     * @param enumIndex the target index
     * @param data to insert
     */
    public void insert(E enumIndex, T data) {
        final int index = getIndex(enumIndex);

        while (index >= list.size())
            list.add(null);

        if (list.get(index) == null) {
            // There is no Item for this enum value
            list.set(index, data);
        } else {
            // There is an item present for this enum value
            list.add(index, data);

            // Update the offsets
            for (int i = index + 1; i < offsets.length; ++i)
                ++offsets[i];
        }
    }

    /**
     * Check if there is any data item associated with the given index
     *
     * @param enumIndex the index to check
     * @return true if something is present, false otherwise
     */
    public boolean contains(E enumIndex) {
        final int index = getIndex(enumIndex);
        if (index < list.size()) {
            T data = list.get(index);
            return data != null;
        } else {
            return false;
        }
    }

    /**
     * Data access helper
     */
    private T get(int i) {
        if (i < 0 || i >= list.size())
            throw new IndexOutOfBoundsException("Bad index");
        else if (list.get(i) == null)
            throw new IllegalArgumentException("No such Item present");
        else
            return list.get(i);
    }

    /**
     * Retrieve data corresponding to the given index.
     * Will throw exceptions if data is not present!
     *
     * @param enumIndex the index to access
     * @return the requested data
     */
    public T get(E enumIndex) {
        final int index = getIndex(enumIndex);
        return get(index);
    }

    /**
     * Get the data from the given index but search the whole range of the index
     * (if multiple data items are associated with it)
     * The given predicate is used to determine if the data is present.
     * Will throw exceptions if data is not present!
     *
     * @param enumIndex the index to access
     * @param pred the predicate to use
     * @return the requested data
     */
    public T get(E enumIndex, Function<T, Boolean> pred) {
        final int index = getIndex(enumIndex);
        final int nextIndex = getNextIndex(index);

        T res = null;
        for (int i = index; i < list.size() && i < nextIndex; ++i) {
            T tmp = list.get(i);
            if (tmp != null && pred.apply(tmp)) {
                res = tmp;
                break;
            }
        }

        if (res == null)
            throw new IllegalArgumentException("Requested data not present");
        else
            return res;
    }

    /**
     * Sort the entries corresponding to each EnumIndex separately by using
     * the provided comparator.
     * Does not do any sorting on the whole list.
     *
     * @param cmp the {@link Comparator} to use for this operation
     */
    public void sort(Comparator<? super T> cmp) {
        for (int i = 0; i < offsets.length && i < list.size(); ++i) {
            final int index = i + offsets[i];
            final int nextIndex = i < offsets.length - 1 ? i + offsets[i+1] + 1 : list.size();

            if (index == nextIndex - 1)
                continue; // Nothing to do, a one element range is trivially sorted

            Collections.sort(list.subList(index, nextIndex), cmp);
        }
    }

    /**
     * @return the lists size
     */
    public int size() {
        return list.size();
    }

    public List<T> getList() {
        List<T> ret = new ArrayList<>(list.size());

        for (T item : list)
            if (item != null)
                ret.add(item);

        return ret;
    }

    public void remove(int index) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Iterator iterator() {
        return list.iterator();
    }

    @Override
    public void forEach(Consumer action) {
        list.forEach(action);
    }

    @Override
    public Spliterator spliterator() {
        return list.spliterator();
    }
}
