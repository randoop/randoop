package randoop.generation;

import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;

import java.util.HashMap;
import java.util.Map;

public class ConstantMiningStorage<T> {

    Map<T, Map<Sequence, Integer>> frequency;
    @Nullable Map<T, Map<Sequence, Integer>> classesWithConstant;
    Map<T, Integer> totalClasses;

    public ConstantMiningStorage() {
        frequency = new HashMap<>();
        switch (GenInputsAbstract.literals_level) {
            case CLASS:
                // Since CLASS level regard the class that the constant locate as its scope, no need to store the classesWithConstant and totalClasses
                classesWithConstant = null;
                totalClasses = null;
                break;
            case PACKAGE:
                classesWithConstant = new HashMap<>();
                totalClasses = new HashMap<>();
                break;
            case ALL:
                // Since the ALL level uses the whole project as its scope, the null key is used to store the classesWithConstant and totalClasses
                frequency.put(null, new HashMap<>());
                classesWithConstant = new HashMap<>();
                classesWithConstant.put(null, new HashMap<>());
                totalClasses = new HashMap<>();
                totalClasses.put(null, 0);
                break;
        }
    }

    public void addFrequency(T t, Sequence seq, int frequency) {
        Map<Sequence, Integer> map;
        switch (GenInputsAbstract.literals_level) {
            case CLASS:
                map = this.frequency.computeIfAbsent(null, __ -> new HashMap<>());
                map.put(seq, map.getOrDefault(seq, 0) + frequency);
                break;
            case PACKAGE:
            case ALL:
                map = this.frequency.computeIfAbsent(t, __ -> new HashMap<>());
                map.put(seq, map.getOrDefault(seq, 0) + frequency);
                break;
        }
    }

    public void addClassesWithConstant(T t, Sequence seq, int classesWithConstant) {
        Map<Sequence, Integer> map;
        switch (GenInputsAbstract.literals_level) {
            case CLASS:
                throw new RuntimeException("Should not update classesWithConstant in CLASS level");
            case PACKAGE:
                map = this.classesWithConstant.computeIfAbsent(t, __ -> new HashMap<>());
                map.put(seq, map.getOrDefault(seq, 0) + classesWithConstant);
                break;
            case ALL:
                map = this.classesWithConstant.computeIfAbsent(null, __ -> new HashMap<>());
                map.put(seq, map.getOrDefault(seq, 0) + classesWithConstant);
                break;
        }
    }

    public void addTotalClasses(T t, int totalClasses) {
        switch (GenInputsAbstract.literals_level) {
            case CLASS:
                throw new RuntimeException("Should not update totalClasses in CLASS level");
            case PACKAGE:
                this.totalClasses.put(t, this.totalClasses.getOrDefault(t, 0) + totalClasses);
                break;
            case ALL:
                this.totalClasses.put(null, this.totalClasses.getOrDefault(null, 0) + totalClasses);
                break;
        }
    }
}
