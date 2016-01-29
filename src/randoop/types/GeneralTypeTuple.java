package randoop.types;

public interface GeneralTypeTuple {
  
  public abstract int size();
  
  public abstract GeneralType get(int i);

  boolean isEmpty();

}
