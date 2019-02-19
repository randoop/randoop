package randoop.reflection.visibilitytest;

/**
 * Class for testing reflective collection of visibility bridge method in
 * randoop.reflection.PackageVisibilityTest.
 */
class PackagePrivateBase {
  public int thePublicMethod(Object obj) {
    return obj.toString().length();
  }

  String thePackagePrivateMethod(int i) {
    return "" + i;
  }
}
