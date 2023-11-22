package randoop.generation.ConstantMining;

import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;
import randoop.util.SimpleList;

import java.util.HashMap;
import java.util.Map;

public class ConstantMiningSelector<T> {
    private Map<T, TfIdfSelector> constantMap;

    public ConstantMiningSelector() {
        constantMap = new HashMap<>();
    }

    public Sequence selectSequence(SimpleList<Sequence> candidates, T type, Map<Sequence, Integer> sequenceFrequency, Map<Sequence, Integer> sequenceOccurrence, int classCount){
        // TODO: This can be also implemented by validation in ForwardGenerator before calling this method
        if (candidates == null || sequenceFrequency == null) {
            return null;
        }
        System.out.println("Selecting sequence: " + candidates + "%n" + "tfidf map: " + constantMap + "%n" + "type: " + (ClassOrInterfaceType) type + "%n");
        TfIdfSelector weightSelector = constantMap.computeIfAbsent(type, __ ->
            new TfIdfSelector(sequenceFrequency, sequenceOccurrence, classCount));
        return weightSelector.selectSequence(candidates);
    }
}
