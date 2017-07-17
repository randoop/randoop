package randoop.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ArrayListSimpleList<T> extends SimpleList<T> implements Serializable {

  private static final long serialVersionUID = 9155161101212598259L;

  public final ArrayList<T> theList;

  public ArrayListSimpleList(ArrayList<T> list) {
    theList = new ArrayList<>(list);
  }

  public ArrayListSimpleList() {
    theList = new ArrayList<>();
  }

  public ArrayListSimpleList(int capacity) {
    theList = new ArrayList<>(capacity);
  }

  @Override
  public int size() {
    return theList.size();
  }

  @Override
  public T get(int index) {
    return theList.get(index);
  }

  @Override
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
