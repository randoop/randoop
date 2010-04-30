package seqs;

/**
 * Driver for generated test sequences.
 */
class Driver {

  public static void main (String args[]) {

    try {
      Seqs.seq();
    } catch (Exception e) {
      System.out.println("Exception in seq: "+e);
      daikon.dcomp.DCRuntime.exit_exception = e;
      System.out.flush();
      System.exit (255);
    }
  }
}
