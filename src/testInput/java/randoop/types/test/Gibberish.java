package randoop.types.test;

/** Input class for capture conversion used in {@link randoop.types.CaptureConversionTest}. */
public class Gibberish extends Nonsense implements Comparable<Gibberish> {
  @Override
  public int compareTo(Gibberish o) {
    return 0;
  }
}
