package misc;

import java.util.ArrayList;
import java.util.List;

public class NestedClasses {
  private int n = 1;
  private List<String> l = new ArrayList<String>();

  public NestedClasses() {}

  public void mutateViaInner() {
    class Inner {
      public void secretlyMutate() {
        n = 4;
      }
    }

    Inner x = new Inner();

    x.secretlyMutate();
  }

  // And because it's such a pain to set up a new test case, let's
  // test JDK-mutability-use here too.
  public void appendIt() {
    l.add("hi");
  }
}
