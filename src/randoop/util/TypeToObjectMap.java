package randoop.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains a map from types to objects of that type.
 * 
 * The objects stored are intended to be objects resulting from
 * arbitrary execution of code under test in Randoop. This class
 * ensures that no code of the objects stored is called, other
 * than the final native method getClass(). 
 */
public class TypeToObjectMap {

  public final Map<Class<?>, List<HotPotato>> theMap =
    new LinkedHashMap<Class<?>, List<HotPotato>>();

  // To make sure that no code in java.util.List ever calls
  // code from runtime objects, we create lists of HotPotatoes
  // instead of lists of actual runtime objects.
  public static final class HotPotato {
    public final Object o;
    public HotPotato(Object o) {
      this.o = o;
    }
  }

  /**
   * Requires: runtimeObject is not null.
   * Effects: adds runtimeObject to the map, at key T where T = runtimeObject.getClass().
   */
  public void addObject(Object runtimeObject) {
    if (runtimeObject == null)
      throw new IllegalArgumentException("runtimeObject cannot be null.");
    Class<?> type = runtimeObject.getClass();
    List<HotPotato> list = theMap.get(type);
    if (list == null) {
      list = new ArrayList<HotPotato>();
      theMap.put(type, list);
    }
    list.add(new HotPotato(runtimeObject));
  }

}
