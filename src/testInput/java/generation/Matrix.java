package generation;

/**
 * Emulates an example from ejml library in Pascali corpus.
 * Results in reflection exception due to bad types after application of copy method.
 */
public interface Matrix {
  public <T extends Matrix> T copy();
}
