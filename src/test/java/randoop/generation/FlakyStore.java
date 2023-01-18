package randoop.generation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

public class FlakyStore<E> {
  private Collection<E>[] store;

  @SuppressWarnings({"unchecked", "rawtypes"})
  public FlakyStore() {
    store = new ArrayList[1];
  }

  public boolean assign(ArrayList<E> list) {
    store[0] = list;
    return true;
  }

  /** should result in an ArrayStoreException */
  public boolean assign(LinkedHashSet<E> set) {
    store[0] = set;
    return false;
  }
}
