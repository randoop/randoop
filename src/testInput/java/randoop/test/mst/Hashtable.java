package randoop.test.mst;

public class Hashtable {
  protected HashEntry[] array;
  protected int size;

  public Hashtable(int sz) {
    size = sz;
    array = new HashEntry[size];
    //    for (int i=0; i<size; i++)
    //      array[i] = null;
  }

  private int hashMap(Object key) {
    return ((key.hashCode() >> 3) % size);
  }

  public Object get(Object key) {
    int j = hashMap(key);

    HashEntry ent;

    for (ent = array[j]; ent != null && ent.key() != key; ent = ent.next()) {
      /* empty body */
    }

    if (ent != null) return ent.entry();
    return null;
  }

  public void put(Object key, Object value) {
    int j = hashMap(key);
    HashEntry ent = new HashEntry(key, value, array[j]);
    array[j] = ent;
  }

  public void remove(Object key) {
    int j = hashMap(key);
    HashEntry ent = array[j];
    if (ent != null && ent.key() == key) array[j] = ent.next();
    else {
      HashEntry prev = ent;
      for (ent = ent.next(); ent != null && ent.key() != key; prev = ent, ent = ent.next()) {
        /* empty body */
      }
      prev.setNext(ent.next());
    }
  }
}

class HashEntry {
  private Object key;
  private Object entry;
  private HashEntry next;

  public HashEntry(Object key, Object entry, HashEntry next) {
    this.key = key;
    this.entry = entry;
    this.next = next;
  }

  public Object key() {
    return key;
  }

  public Object entry() {
    return entry;
  }

  public HashEntry next() {
    return next;
  }

  public void setNext(HashEntry n) {
    next = n;
  }
}
