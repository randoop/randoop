package randoop.reflection;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;

public class PureClass {
  public void a(@NonNull String s, @NonNull Integer i) {}

  @Pure
  public @NonNull String b(@NonNull String s, @NonNull Integer i) {
    return "a";
  }

  @Pure
  public static @NonNull String c(@NonNull String s, @NonNull Integer i) {
    return "a";
  }
}
