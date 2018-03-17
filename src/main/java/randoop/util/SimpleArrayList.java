package randoop.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SimpleArrayList<T> implements SimpleList<T>, Serializable {

  private static final long serialVersionUID = 9155161101212598259L;

  public final ArrayList<T> theList;

  public SimpleArrayList(ArrayList<T> list) {
    theList = new ArrayList<>(list);
  }

  public SimpleArrayList() {
    theList = new ArrayList<>();
  }

  public SimpleArrayList(int capacity) {
    theList = new ArrayList<>(capacity);
  }

  @Override
  public int size() {
    return theList.size();
  }

  @Override
  public boolean isEmpty() {
    return theList.isEmpty();
  }

  @Override
  public T get(int index) {
    return theList.get(index);
  }

  @Override
  // Return the entire list.
  public SimpleList<T> getSublist(int index) {
    return this;
  }

  public boolean add(T element) {
    return theList.add(element);
  }

  @Override
  public List<T> toJDKList() {
    return new ArrayList<>(theList);
  }

  @Override
  public String toString() {
    return toJDKList().toString();
  }
}
