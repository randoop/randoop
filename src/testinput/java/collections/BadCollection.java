package collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A class that violates the collection.toArray().length == collection.size()
 *  contract.
 */
public class BadCollection<E> implements Collection<E> {
    private final List<E> backendList;
    
    public BadCollection() {
        backendList = new ArrayList<>();
    }
    
    @Override
    public void clear() {
        this.backendList.clear();
    }

    @Override
    public int size() {
        return this.backendList.size() + 1;
    }

    @Override
    public boolean isEmpty() {
       return this.backendList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
       return this.backendList.contains(o);
    }

    @Override
    public Iterator iterator() {
       return this.backendList.iterator();
    }

    @Override
    public Object[] toArray() {
       return this.backendList.toArray();
    }

    @Override
    public Object[] toArray(Object[] ts) {
       return this.backendList.toArray(ts);
    }

    @Override
    public boolean add(Object e) {
       return this.backendList.add((E)e);
    }

    @Override
    public boolean remove(Object o) {
       return this.backendList.remove(o);
    }

    @Override
    public boolean containsAll(Collection clctn) {
       return this.backendList.containsAll(clctn);
    }

    @Override
    public boolean addAll(Collection clctn) {
       return this.backendList.addAll(clctn);
    }

    @Override
    public boolean removeAll(Collection clctn) {
       return this.backendList.removeAll(clctn);
    }

    @Override
    public boolean retainAll(Collection clctn) {
       return this.backendList.retainAll(clctn);
    }
}
