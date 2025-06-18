package randoop.reflection;

import java.lang.reflect.Method;

/** Miscellaneous reflection constants shared across Randoop. */
public final class ReflectionUtil {

  /** The {@code java.lang.Object#getClass()} method. */
  public static final Method OBJECT_GETCLASS;

  static {
    try {
      OBJECT_GETCLASS = Object.class.getMethod("getClass");
    } catch (NoSuchMethodException e) {
      // Impossible on a sane JDK; turn the checked error into an unchecked one.
      throw new AssertionError(e);
    }
  }

  private ReflectionUtil() {} // utility class – no instances
}
