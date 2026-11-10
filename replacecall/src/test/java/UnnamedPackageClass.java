/** A class in the unnamed package, used by {@code randoop.instrument.ReplacementFileTest}. */
@SuppressWarnings({
  "PMD.NoPackage", // tests a class in the unnamed package
  "PMD.UseUtilityClass" // the constructor is replaced in a replacement file
})
public class UnnamedPackageClass {

  /**
   * Creates an UnnamedPackageClass.
   *
   * @param i an integer
   */
  public UnnamedPackageClass(int i) {}

  /**
   * Replacement for the {@code UnnamedPackageClass(int)} constructor.
   *
   * @param i an integer
   * @return a new UnnamedPackageClass
   */
  public static UnnamedPackageClass make(int i) {
    return new UnnamedPackageClass(i);
  }
}
