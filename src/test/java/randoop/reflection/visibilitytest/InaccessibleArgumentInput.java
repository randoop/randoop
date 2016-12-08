package randoop.reflection.visibilitytest;

import java.util.List;

/**
 * Created by bjkeller on 12/8/16.
 */
public class InaccessibleArgumentInput {
  private InaccessibleArgumentInput() {}

  public void m(randoop.reflection.visibilitytest.PackagePrivateBase base) {}

  public void n(List<PackagePrivateBase> baseList) {}
}
