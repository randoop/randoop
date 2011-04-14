package randoop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that a user of Randoop can place next to a method to specify
 * that that the method should be used as a representation invariant. See
 * Randoop user manual for more information.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CheckRep {
  // No members.
}