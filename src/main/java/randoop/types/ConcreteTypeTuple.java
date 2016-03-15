package randoop.types;

import java.util.ArrayList;
import java.util.List;

import randoop.types.ConcreteType;
import randoop.types.GeneralTypeTuple;

public class ConcreteTypeTuple implements GeneralTypeTuple {

  private ArrayList<ConcreteType> list;

  public ConcreteTypeTuple(List<ConcreteType> list) {
    this.list = new ArrayList<>(list);
  }
  
  public ConcreteTypeTuple() {
    list = new ArrayList<>();
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public ConcreteType get(int i) {
    assert 0 <= i && i < list.size();
    return list.get(i);
  }

  @Override
  public boolean isEmpty() {
    return list.isEmpty();
  }

}
