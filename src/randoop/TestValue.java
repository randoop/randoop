package randoop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Signals to Randoop that the value of a primitive (or primitive array) field 
 * in a class under test should be used as an input value to methods under test.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TestValue {
  // no members
}
