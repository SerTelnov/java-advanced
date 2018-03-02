package ru.ifmo.rain.telnov.arrayset;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * Created by Telnov Sergey on 19.02.2018.
 */
public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {

    protected List<E> data = emptyList();
    protected Comparator<? super E> comparator = null;

    public ArraySet() { }

    public ArraySet(final Collection<? extends E> collection) {
        this(collection, null);
    }

    public ArraySet(final Collection<? extends E> collection, Comparator<? super E> comparator) {
        this.comparator = comparator;

        if (!collection.isEmpty()) {
            List<E> list = new ArrayList<>(collection);
            list.sort(comparator);

            List<E> listWithoutDuplicates = new ArrayList<>(collection.size());
            listWithoutDuplicates.add(list.get(0));

            for (E el : list) {
                final E curr = listWithoutDuplicates.get(
                        listWithoutDuplicates.size() - 1
                );
                if (comparator != null && comparator.compare(curr, el) != 0 ||
                        comparator == null && !curr.equals(el)
                        ) {
                    listWithoutDuplicates.add(el);
                }
            }

            data = unmodifiableList(listWithoutDuplicates);
            list.clear();
        }
    }

    protected ArraySet(final ArraySet<E> that, final int startIndex, final int endIndex) {
        this.data = that.data.subList(startIndex, endIndex);
        this.comparator = that.comparator;
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return data.iterator();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Nullable
    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    @SuppressWarnings("unchecked cast")
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (E) o, comparator) >= 0;
    }

    protected int getRealIndex(final E el) {
        if (el == null) {
            throw new NullPointerException("element is null");
        }
        return Collections.binarySearch(data, el, comparator);
    }

    protected int invertIndex(final int index) {
        return index < 0 ? -index - 1 : index;
    }

    protected int getIndex(final E el) {
        return invertIndex(
                getRealIndex(el)
        );
    }

    private SortedSet<E> subSet(final int startIndex, final int endIndex) {
        return new ArraySet<>(this, startIndex, endIndex);
    }

    @NotNull
    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(
                getIndex(fromElement),
                getIndex(toElement)
        );
    }

    @NotNull
    @Override
    public SortedSet<E> headSet(E toElement) {
        return subSet(0, getIndex(toElement));
    }

    @NotNull
    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return subSet(getIndex(fromElement), size());
    }

    protected E getElement(final int index) {
        if (isEmpty()) {
            throw new NoSuchElementException("Set is empty");
        }
        return data.get(index);
    }

    @Override
    public E first() {
        return getElement(0);
    }

    @Override
    public E last() {
        return getElement(size() - 1);
    }
}
