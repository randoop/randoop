package randoop.generation;

/** Used by Randoop to determine whether generation should stop. */
public interface IStopper {
  /**
   * Returns true if generation should stop.
   *
   * @return true if generation should stop
   */
  boolean shouldStop();
}
