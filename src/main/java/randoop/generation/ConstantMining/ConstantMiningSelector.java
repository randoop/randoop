package randoop.generation.ConstantMining;

import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;
import randoop.util.SimpleList;

import java.util.HashMap;
import java.util.Map;

public class ConstantMiningSelector<T> {
    private Map<T, WeightSelector> constantMap;

    public ConstantMiningSelector() {
        constantMap = new HashMap<>();
    }

    public Sequence selectSequence(SimpleList<Sequence> candidates, T type, Map<Sequence, Integer> sequenceFrequency, Map<Sequence, Integer> sequenceOccurrence, int classCount){
        WeightSelector weightSelector = constantMap.computeIfAbsent(type, __ ->
            new WeightSelector(sequenceFrequency, sequenceOccurrence, classCount));
        return weightSelector.selectSequence(candidates);
    }
}
