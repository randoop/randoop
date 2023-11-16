package randoop.generation.ConstantMining;

import randoop.sequence.Sequence;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;
import randoop.util.SimpleList;

import java.util.HashMap;
import java.util.Map;

public class ConstantMiningSelector<T> {
    private Map<T, WeightSelector> constantMap;

    public ConstantMiningSelector(){
        constantMap = new HashMap<>();
    }

    public Sequence selectSequence(T type, Map<Sequence, Integer> sequenceFrequency, Map<Sequence, Integer> sequenceOccurrence, int classCount){
        WeightSelector weightSelector = constantMap.computeIfAbsent(type, __ ->
            new WeightSelector(sequenceFrequency, sequenceOccurrence, classCount));
        return weightSelector.selectSequence();
    }



    class WeightSelector{
        Map<Sequence, Double> tfidfMap;

        int classCount;

        int totalWeight;

        public WeightSelector(){
            tfidfMap = new HashMap<>();
            classCount = 0;
            totalWeight = 0;
        }

        public WeightSelector(Map<Sequence, Integer> sequenceFrequency, Map<Sequence, Integer> sequenceOccurrence, int classCount){
            tfidfMap = new HashMap<>();
            this.classCount = classCount;
            totalWeight = 0;
            assert sequenceFrequency.keySet().equals(sequenceOccurrence.keySet());
            for(Sequence sequence : sequenceFrequency.keySet()){
                int frequency = sequenceFrequency.get(sequence);
                int occurrence = sequenceOccurrence.get(sequence);
                double tfidf = (double)frequency * ((double)classCount + 1) / (((double)classCount + 1) - (double)occurrence);
                tfidfMap.put(sequence, tfidf);
                totalWeight += tfidf;
            }
        }

        public Sequence selectSequence(){
            SimpleList<Sequence> sequenceList = new SimpleArrayList<>(tfidfMap.keySet());
            return Randomness.randomMemberWeighted(sequenceList, tfidfMap, totalWeight);
        }
    }
}
