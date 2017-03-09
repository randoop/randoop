package randoop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Write this annotation on a method to specify that that the method checks a representation
 * invariant. The annotated method may have boolean return type, or it may be void and throw an
 * exception if the representation invariant is violated. See the Randoop user manual for more
 * information.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CheckRep {
  // No members.
}
