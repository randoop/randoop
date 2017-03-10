package randoop.main;

import java.util.ArrayList;
import java.util.List;
import randoop.generation.AbstractGenerator;
import randoop.reflection.StaticCache;
import randoop.util.ReflectionExecutor;

/** Manages the static state of Randoop classes with Options annotations. */
public class OptionsCache {

  /** The list of caches for classes with Options annotations. */
  private final List<StaticCache> cacheList;

  /** Creates an object for caching the state of command-line arguments. */
  public OptionsCache() {
    cacheList = new ArrayList<>();
    cacheList.add(new StaticCache(GenInputsAbstract.class));
    cacheList.add(new StaticCache(ReflectionExecutor.class));
    cacheList.add(new StaticCache(AbstractGenerator.class));
  }

  /** Prints the saved state of all command-line arguments. */
  public void printState() {
    for (StaticCache cache : cacheList) {
      cache.printCache();
    }
  }

  /** Saves the state of all command-line arguments. */
  public void saveState() {
    for (StaticCache cache : cacheList) {
      cache.saveState();
    }
  }

  /** Restores the previously saved state of the command-line arguments. */
  public void restoreState() {
    for (StaticCache cache : cacheList) {
      cache.restoreState();
    }
  }
}
