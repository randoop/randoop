package randoop.generation;

import java.util.Objects;

/**
 * Represents a pair of lifecycle methods: a start method and its corresponding stop method.
 */
public class MethodPair {
    private final String startMethodName;
    private final String stopMethodName;

    /**
     * Constructs a MethodPair with the specified start and stop method names.
     *
     * @param startMethodName the name of the start method (e.g., "start")
     * @param stopMethodName  the name of the corresponding stop method (e.g., "stop")
     */
    public MethodPair(String startMethodName, String stopMethodName) {
        this.startMethodName = Objects.requireNonNull(startMethodName, "Start method name cannot be null");
        this.stopMethodName = Objects.requireNonNull(stopMethodName, "Stop method name cannot be null");
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
        if (o == null || getClass() != o.getClass()) return false;

        MethodPair that = (MethodPair) o;

        if (!startMethodName.equals(that.startMethodName)) return false;
        return stopMethodName.equals(that.stopMethodName);
    }

    @Override
    public int hashCode() {
        int result = startMethodName.hashCode();
        result = 31 * result + stopMethodName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MethodPair{" +
                "startMethodName='" + startMethodName + '\'' +
                ", stopMethodName='" + stopMethodName + '\'' +
                '}';
    }
}
