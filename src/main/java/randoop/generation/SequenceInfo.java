package randoop.generation;

import randoop.types.ClassOrInterfaceType;

import java.util.HashMap;
import java.util.Map;

public class SequenceInfo {
    /**
     * The number of times this sequence occurs, in any class. Only used when the literal level is
     * CLASS.
     */
    public int globalFrequency;

    /**
     * The number of classes in which this sequence occurs. Only used when the literal level is
     * CLASS.
     */
    public int globalOccurrence;

    /* How many times the sequence occurs in the class. */
    public Map<ClassOrInterfaceType, Integer> classFrequency;

    /* How many times the sequence occurs in the package. */
    public Map<Package, Integer> packageFrequency;

    /* How many classes the sequence occurs in the package. */
    public Map<Package, Integer> packageOccurrence;

    public SequenceInfo() {
        globalFrequency = 0;
        globalOccurrence = 0;
        classFrequency = new HashMap<>();
        packageFrequency = new HashMap<>();
        packageOccurrence = new HashMap<>();
    }

    /**
     * Update data structures to account for the fact that {@code seq} has been observed {@code
     * frequency} times in class {@code type}.
     *
     * @param hasOccurredInClass true if this is the second or subsequent occurrence of {@code seq}
     *     in the current class
     */
    public void update(
        ClassOrInterfaceType type,
        Package pkg,
        boolean hasOccurredInClass,
        int frequency) {
        globalFrequency += frequency;
        classFrequency.put(type, classFrequency.getOrDefault(type, 0) + frequency);
        packageFrequency.put(pkg, packageFrequency.getOrDefault(pkg, 0) + frequency);
        if (!hasOccurredInClass) {
            globalOccurrence++;
            packageOccurrence.put(pkg, packageOccurrence.getOrDefault(pkg, 0) + 1);
        }
    }

    // TODO: add comments
    public int getGlobalFrequency() {
        return globalFrequency;
    }

    // TODO: add comments
    public int getGlobalOccurrence() {
        return globalOccurrence;
    }

    // TODO: add comments
    public int getClassLevelFrequency(ClassOrInterfaceType type) {
        return classFrequency.getOrDefault(type, 0);
    }

    // TODO: add comments
    public int getPackageLevelFrequency(Package pkg) {
        return packageFrequency.getOrDefault(pkg, 0);
    }

    // TODO: add comments
    public int getPackageLevelOccurrence(Package pkg) {
        return packageOccurrence.getOrDefault(pkg, 0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SequenceInfo: ");
        sb.append("globalFrequency: ").append(globalFrequency).append(System.lineSeparator());
        sb.append("globalOccurrence: ").append(globalOccurrence).append(System.lineSeparator());
        sb.append("classFrequency: ").append(classFrequency).append(System.lineSeparator());
        sb.append("packageFrequency: ").append(packageFrequency).append(System.lineSeparator());
        sb.append("packageOccurrence: ").append(packageOccurrence).append(System.lineSeparator());
        return sb.toString();
    }
}
