package at.searles.commons.util;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ReverseListIterator<E> implements Iterator<E> {

    private final ListIterator<E> iterator;

    public ReverseListIterator(List<E> list) {
        this.iterator = list.listIterator(list.size());
    }

    @Override
    public boolean hasNext() {
        return iterator.hasPrevious();
    }

    @Override
    public E next() {
        return iterator.previous();
    }
}
