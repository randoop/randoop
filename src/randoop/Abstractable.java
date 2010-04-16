package randoop;

import java.util.List;

/**
 * Used by HeapLinearizer.
 * During heap traversal, if an object o is encountered that implements
 * this interface, the linearizer will visit o.getAbstraction() instead of o.
 */
public interface Abstractable {

  List<Object> getAbstraction();

  boolean shouldAbstract();

}
