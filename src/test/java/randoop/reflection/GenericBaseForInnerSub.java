package randoop.reflection;

/** Created by bjkeller on 10/13/16. */
public abstract class GenericBaseForInnerSub<T> implements GenericInterfaceForInnerSub<T> {
  @Override
  public int getOne() {
    return 1;
  }
}
