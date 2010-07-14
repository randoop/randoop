package randoop.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ArrayListSimpleList<T> extends SimpleList<T> implements Serializable {

  private static final long serialVersionUID = 9155161101212598259L;

  public final ArrayList<T> theList;
  
  public ArrayListSimpleList(ArrayList<T> list) {
    theList = new ArrayList<T>(list);
  }
  
  public ArrayListSimpleList() {
    theList = new ArrayList<T>();
  }

  public ArrayListSimpleList(int capacity) {
    theList = new ArrayList<T>(capacity);
  }

  @Override
  public int size() {
    return theList.size();
  }

  @Override
  public T get(int index) {
    return theList.get(index);
  }

  public boolean add(T element) {
    return theList.add(element);
  }

  @Override
  public List<T> toJDKList() {
    return new ArrayList<T>(theList);
  }
  
  @Override
  public String toString() {
    return toJDKList().toString();
  }

}
