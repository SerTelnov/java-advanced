package ru.ifmo.rain.telnov.arrayset;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by Telnov Sergey on 20.02.2018.
 */
public class NavigableArraySet<E> extends ArraySet<E> implements NavigableSet<E> {

    public NavigableArraySet() {
        super();
    }

    public NavigableArraySet(final Collection<? extends E> collection) {
        super(collection);
    }

    public NavigableArraySet(final Collection<? extends E> collection, Comparator<? super E> comparator) {
        super(collection, comparator);
    }

    protected NavigableArraySet(NavigableArraySet<E> set, final int startIndex, final int endIndex) {
        super(set, startIndex, endIndex);
    }

    @Nullable
    @Override
    public E lower(E e) {
        final int index = getRealIndex(e);
        if (index == 0 || index == -1) {
            return null;
        } else if (index > 0) {
            return data.get(index - 1);
        } else {
            return data.get(-index - 2);
        }
    }

    @Nullable
    @Override
    public E higher(E e) {
        final int index = getRealIndex(e);
        if (index == size() - 1 || -index - 1 == size()) {
            return null;
        } else if (index >= 0 && index < size() - 1) {
            return data.get(index + 1);
        } else {
            return data.get(-index - 1);
        }
    }

    private int compare(final E a, final E b) {
        if (comparator != null) {
            return comparator.compare(a, b);
        } else {
            return ((Comparable) a).compareTo(b);
        }
    }

    private int binSearch(final E key, boolean isLeftPart) {
        int left = -1;
        int right = size();
        while (left < right - 1) {
            int middle = (left + right) / 2;

            int comp = compare(data.get(middle), key);
            if (comp == 0 && isLeftPart) {
                comp = -1;
            }

            if (comp < 0) {
                left = middle;
            } else {
                right = middle;
            }
        }
        return isLeftPart ? left : right;
    }

    @Nullable
    @Override
    public E floor(E e) {
        int index = binSearch(e, true);
        return index >= 0 ? data.get(index) : null;
    }

    @Nullable
    @Override
    public E ceiling(E e) {
        int index = binSearch(e, false);
        return index < size() ? data.get(index) : null;
    }

    private UnsupportedOperationException getUnsupportedMethodException(final String methodName) {
        return new UnsupportedOperationException("method '" + methodName + "'is unsupported");
    }

    @Nullable
    @Override
    public E pollFirst() {
        throw getUnsupportedMethodException("pollFirst");
    }

    @Nullable
    @Override
    public E pollLast() {
        throw getUnsupportedMethodException("pollLast");
    }

    @NotNull
    @Override
    public NavigableSet<E> descendingSet() {
        return new NavigableArraySet<>(
                data,
                comparator == null ?
                        Collections.reverseOrder() :
                        comparator.reversed()
        );
    }

    @NotNull
    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    private int getIndex(final E el, final boolean inclusive) {
        int index = getRealIndex(el);
        if (index >= 0) {
            if (inclusive) {
                index++;
            }
        } else {
            return invertIndex(index);
        }
        return index;
    }

    private NavigableSet<E> subSet(final int fromIndex, final int toIndex) {
        return new NavigableArraySet<>(this, fromIndex, toIndex);
    }

    @NotNull
    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int fromIndex = getIndex(fromElement, !fromInclusive);
        int toIndex = getIndex(toElement, toInclusive);

        fromIndex = Math.min(fromIndex, toIndex);
        return subSet(fromIndex, toIndex);
    }

    @NotNull
    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return subSet(0, getIndex(toElement, inclusive));
    }

    @NotNull
    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return subSet(getIndex(fromElement, !inclusive), size());
    }

    private class DescendingIterator implements Iterator<E> {
        private final ListIterator<E> iterator = data.listIterator(size());

        @Override
        public boolean hasNext() {
            return iterator.hasPrevious();
        }

         @Override
        public E next() {
            return iterator.previous();
        }
    }
}
