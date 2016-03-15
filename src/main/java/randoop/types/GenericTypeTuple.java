package randoop.types;

import java.util.ArrayList;
import java.util.List;

import randoop.types.GeneralTypeTuple;
import randoop.types.GenericType;

public class GenericTypeTuple implements GeneralTypeTuple {

  private final ArrayList<GenericType> list;

  public GenericTypeTuple(List<GenericType> list) {
    this.list = new ArrayList<>(list);
  }

  public GenericTypeTuple() {
    this.list = new ArrayList<>();
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public boolean isEmpty() {
    return list.isEmpty();
  }

  @Override
  public GenericType get(int i) {
    assert 0 <= i && i < list.size();
    return list.get(i);
  }

}
