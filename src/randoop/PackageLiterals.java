package randoop;

/**
 * For a given package P, PackageLiterals maps P (if present) to a
 * collection of literals (represented as single-element sequences)
 * that can be used as inputs to classes in the given package.
 */
public class PackageLiterals extends MappedSequences<Package> {

  @Override
  public void addSequence(Package key, Sequence seq) {
    if (seq == null) throw new IllegalArgumentException("seq is null");
    if (!seq.isPrimitive()) {
      throw new IllegalArgumentException("seq is not a primitive sequence");
    }
    super.addSequence(key, seq);
  }

  
}
