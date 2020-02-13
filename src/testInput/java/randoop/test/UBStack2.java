package randoop.test;

public class UBStack2 {
  private int[] elems;
  private int numberOfElements;
  private int max;

  public UBStack2() {
    numberOfElements = 0;
    max = 5;
    elems = new int[max];
  }

  public void push(int k) {
    int index;
    boolean alreadyMember;

    alreadyMember = false;

    for (index = 0; index < numberOfElements; index++) {
      if (k == elems[index]) {
        alreadyMember = true;
        break;
      }
    }

    if (alreadyMember) {
      for (int j = index; j < numberOfElements - 1; j++) {
        elems[j] = elems[j + 1];
      }
      elems[numberOfElements - 1] = k;
    } else {
      if (numberOfElements < max) {
        elems[numberOfElements] = k;
        numberOfElements++;
        return;
      } else {
        //System.out.println("Stack full, cannot push");
        return;
      }
    }
  }

  public void pop() {
    numberOfElements--;
  }

  public int top() {
    if (numberOfElements < 1) {
      //System.out.println("Empty Stack");
      return -1;
    } else return elems[numberOfElements - 1];
  }

  public boolean isEmpty() {
    if (numberOfElements == 0) return true;
    else return false;
  }

  public int maxSize() {
    return max;
  }

  public boolean isMember(int k) {
    for (int index = 0; index < numberOfElements; index++) if (k == elems[index]) return true;
    return false;
  }

  @SuppressWarnings("EqualsHashCode")
  @Override
  public boolean equals(Object o) {
    UBStack2 s = (UBStack2) o;
    if (s.maxSize() != max) return false;
    if (s.getNumberOfElements() != numberOfElements) return false;
    int[] sElems = s.getArray();
    for (int j = 0; j < numberOfElements; j++) {
      if (elems[j] != sElems[j]) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "stack";
  }

  public int[] getArray() {
    int[] a;
    a = new int[max];
    for (int j = 0; j < numberOfElements; j++) a[j] = elems[j];
    return a;
  }

  public int getNumberOfElements() {
    return numberOfElements;
  }

  public boolean isFull() {
    return numberOfElements == max;
  }
}
