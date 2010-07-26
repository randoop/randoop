package lpf.model.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class ArraySet<E> implements Set<E>, Serializable{
	private static final long serialVersionUID = 5053814703200797581L;
	
	ArrayList<E> list;

	public ArraySet() {
		list = new ArrayList<E>();
	}

	public boolean add(E e) {
		if (!list.contains(e)) {
			return list.add(e);
		} else {
			return false;
		}
	}

	public boolean addAll(Collection<? extends E> c) {
		return list.addAll(c);
	}

	public void clear() {
		list.clear();
	}

	public boolean contains(Object o) {
		return list.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public Iterator<E> iterator() {
		return list.iterator();
	}

	public boolean remove(Object o) {
		return list.remove(o);
	}

	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	public int size() {
		return list.size();
	}

	public Object[] toArray() {
		return list.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}
}