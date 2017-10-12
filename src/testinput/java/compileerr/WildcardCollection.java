package compileerr;

import java.util.ArrayList;
import java.util.List;

public class WildcardCollection {
  public static <O> List<? extends O> munge(List<? extends O> l1, List<? extends O> l2) {
    return l1;
  }

  public static List<String> getAStringList() {
    return new ArrayList<>();
  }

  public static List<Integer> getAnIntegerList() {
    return new ArrayList<>();
  }

  public static <T> int doThis(Comparable<T> comparable) {
    return 0;
  }
}
