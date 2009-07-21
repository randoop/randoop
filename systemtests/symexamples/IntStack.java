package symexamples;

public class IntStack {
  private Integer [] store;
  private int size;
  private static final int INITIAL_CAPACITY = 5;
  public IntStack() {
    this.store = new Integer[INITIAL_CAPACITY];
    this.size = 0;
  }
  public void push(Integer value) {
    if (this.size == this.store.length) {
      Integer[] store = new Integer[this.store.length * 2];
      System.arraycopy(this.store, 0, store, 0, this.size);
      this.store = store;
    }
    this.store[this.size] = value;
    this.size++;
  }
  public Integer pop() {
    Integer result = this.store[this.size - 1];
    this.size--;
    if (this.store.length > INITIAL_CAPACITY
     && this.size * 2 < this.store.length) {
      Integer[] store = new Integer[this.store.length / 2];
      System.arraycopy(this.store, 0, store, 0, this.size);
      this.store = store;
    }
    return result;
  }
  /*
  public boolean isEmpty() {
    return (this.size == 0);
  }
  */
  /*
  public boolean equals(Object other) {
    if (other == null) return false;
    if (!(other instanceof IntStack)) return false;
    IntStack s = (IntStack)other;
    if (this.size != s.size) return false;
    for (int i = 0; i < this.size; i++)  {
      if (this.store[i] != s.store[i])
        return false;
    }
    return true;
  }
  */
}
