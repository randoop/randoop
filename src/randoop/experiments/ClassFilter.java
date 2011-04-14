package randoop.experiments;

import org.apache.bcel.classfile.JavaClass;

/**
 * A ClassFilter is used by a ClassListPrinter to determine which
 * classes from a list of classes to print.
 */
public interface ClassFilter {

  /**
   * @return true if the ClassListPrinter should print the given class. 
   */
  boolean include(JavaClass cls);

}
