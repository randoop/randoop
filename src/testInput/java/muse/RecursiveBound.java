package muse;

/**
 * Based on example from issue raised by Huascar Sanchez
 */
public class RecursiveBound {

  public <W1 extends Comparable<? super W1>> boolean m5(W1 w, W1 w2) {
    return w.compareTo(w2) == 0;
  }

}
