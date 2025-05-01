package randoop.reflection.accessibilitytest;

import java.util.List;
import java.util.Map;
import randoop.reflection.OperationExtractorTest;

/** Input class for {@link OperationExtractorTest#inaccessibleArgumentTest()} */
public class InaccessibleArgumentInput {
  private InaccessibleArgumentInput() {}

  public void mDirect(randoop.reflection.accessibilitytest.PackagePrivateBase base) {}

  public void mParameterized(List<PackagePrivateBase> baseList) {}

  public void mGenericArray(PackagePrivateBase[] baseArray) {}

  public <T> void mTypeVariable(T var) {}

  public void mWildcard(List<?> list) {}

  public <T> void mMixed(Map<T, List<PackagePrivateBase>> map) {}
}
