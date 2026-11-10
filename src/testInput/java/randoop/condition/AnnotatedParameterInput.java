package randoop.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An input class whose methods have parameters annotated with an annotation named {@code @NonNull}.
 * Randoop derives a precondition from such an annotation.
 */
public class AnnotatedParameterInput {

  /** Randoop derives a non-null precondition from any annotation with this simple name. */
  @Target(ElementType.TYPE_USE)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface NonNull {}

  /** A parameter type that is not accessible outside {@link AnnotatedParameterInput}. */
  private static class Hidden {}

  /**
   * Randoop cannot compile the precondition that it derives for this method, because the generated
   * expression class cannot refer to the type of the parameter.
   *
   * @param h an inaccessible value
   */
  public void inaccessibleParameterType(@NonNull Hidden h) {}

  /**
   * Randoop can compile the precondition that it derives for this method.
   *
   * @param s a string
   */
  public void accessibleParameterType(@NonNull String s) {}
}
