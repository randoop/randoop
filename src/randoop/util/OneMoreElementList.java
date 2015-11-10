package randoop.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class OneMoreElementList<T> extends SimpleList<T> implements Serializable {

  private static final long serialVersionUID = 1332963552183905833L;

  public final T lastElement;
  public final SimpleList<T> list;
  public final int size;

  public OneMoreElementList(SimpleList<T> list, T extraElement) {
    this.list = list;
    this.lastElement = extraElement;
    this.size = list.size() + 1;
  }

  @Override
  public int size() {
    return size; // XXX this is bogus: what if the list changes size?
  }

  @Override
  public T get(int index) {
    if (index < list.size())
      return list.get(index);
    if (index == list.size())
      return lastElement;
    throw new IndexOutOfBoundsException("No such element:" + index);
  }

  @Override
  public List<T> toJDKList() {
    List<T> result= new ArrayList<T>();
    result.addAll(list.toJDKList());
    result.add(lastElement);
    return result;
  }
  
  @Override
  public String toString() {
    return toJDKList().toString();
  }

  @Override
  public Iterator<T> iterator() {
    // TODO Auto-generated method stub
    return new OMEIterator(list.iterator(), lastElement);
  }

  private class OMEIterator implements Iterator<T> {
    
    private Iterator<T> listIterator;
    private boolean visitedLast;
    private T last;

    public OMEIterator(Iterator<T> listIterator, T last) {
      this.listIterator = listIterator;
      this.visitedLast = false;
      this.last = last;
    }

    @Override
    public boolean hasNext() {
      // TODO Auto-generated method stub
      return (listIterator.hasNext() || !visitedLast); 
    }

    @Override
    public T next() {
      // TODO Auto-generated method stub
      if (listIterator.hasNext()) {
        return listIterator.next();
      } else if (!visitedLast){
        visitedLast = false;
        return last;
      }
      return null;
    }
    
  }
}
