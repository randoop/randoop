package randoop.reflection;

import java.util.ArrayList;
import java.util.List;

/** A generic class for testing type and operation harvesting. */
@SuppressWarnings("unchecked")
public class GenericClass<T> {
  public T theField;
  public List<T> theGenericList;
  public List<Integer> theIntegerList;
  public T[] theGenericArray;
  public Integer[] theIntegerArray;

  public GenericClass(T theValue) {
    this.theField = theValue;
    this.theGenericArray = (T[]) new Object[10];
    this.theIntegerArray = new Integer[10];
    this.theGenericList = new ArrayList<>();
    this.theIntegerList = new ArrayList<>();
  }

  public T getTheField() {
    return theField;
  }

  public void setTheField(T theValue) {
    theField = theValue;
  }

  public List<T> getTheList() {
    return theGenericList;
  }

  public List<Integer> getTheIntegerList() {
    return theIntegerList;
  }

  public T[] getTheGenericArray() {
    return theGenericArray;
  }

  public Integer[] getTheIntegerArray() {
    return theIntegerArray;
  }

  public void addAll(T[] a) {
    for (T t : a) {
      theGenericList.add(t);
    }
  }

  public void addAll(List<? extends T> l) {
    theGenericList.addAll(l);
  }

  // this is a concrete operation inside of a generic class should be in generic operations pool
  public int getTheGenericListLength() {
    return theGenericList.size();
  }
}
