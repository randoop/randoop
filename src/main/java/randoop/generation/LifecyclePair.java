package randoop.generation;

import randoop.main.RandoopBug;

/** Represents a pair of lifecycle methods: a start method and its corresponding stop method. */
public class LifecyclePair {
  private final String startMethodName;
  private final String stopMethodName;

  /**
   * Constructs a LifecyclePair with the specified start and stop method names.
   *
   * @param startMethodName the simple name of the start method (e.g., "start")
   * @param stopMethodName the simple name of the corresponding stop method (e.g., "stop")
   */
  public LifecyclePair(String startMethodName, String stopMethodName) {
    if (startMethodName == null) {
      throw new RandoopBug("Start method name cannot be null");
    }
    if (stopMethodName == null) {
      throw new RandoopBug("Stop method name cannot be null");
    }
    this.startMethodName = startMethodName;
    this.stopMethodName = stopMethodName;
  }

  public String getStartMethodName() {
    return startMethodName;
  }

  public String getStopMethodName() {
    return stopMethodName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LifecyclePair)) {
      return false;
    }

    LifecyclePair other = (LifecyclePair) o;

    return startMethodName.equals(other.startMethodName)
        && stopMethodName.equals(other.stopMethodName);
  }

  @Override
  public int hashCode() {
    int result = startMethodName.hashCode();
    result = 31 * result + stopMethodName.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "LifecyclePair{" + startMethodName + ", " + stopMethodName + '}';
  }
}
