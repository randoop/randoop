package randoop.generation.ConstantMining;

import randoop.sequence.Sequence;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;
import randoop.util.SimpleList;

import java.util.HashMap;
import java.util.Map;

public class TfIdfSelector {
    Map<Sequence, Double> tfidfMap;

    int classCount;

    double totalWeight;

//    public TfIdfSelector(){
//        tfidfMap = new HashMap<>();
//        classCount = 0;
//        totalWeight = 0;
//    }

    public TfIdfSelector(Map<Sequence, Integer> sequenceFrequency, Map<Sequence, Integer> sequenceOccurrence, int classCount){
        tfidfMap = new HashMap<>();
        this.classCount = classCount;
        totalWeight = 0.0;
        assert sequenceFrequency.keySet().equals(sequenceOccurrence.keySet());
        for(Sequence sequence : sequenceFrequency.keySet()){
            int frequency = sequenceFrequency.get(sequence);
            int occurrence = sequenceOccurrence.get(sequence);
            double tfidf = (double)frequency * ((double)classCount + 1) / (((double)classCount + 1) - (double)occurrence);
            tfidfMap.put(sequence, tfidf);
            totalWeight += tfidf;
        }
    }

//    public Sequence selectSequence(){
//        SimpleList<Sequence> sequenceList = new SimpleArrayList<>(tfidfMap.keySet());
//        return selectSequence(sequenceList);
//    }

    public Sequence selectSequence(SimpleList<Sequence> candidates) {
        // POTENTIAL BUG: candidates have sequence that is not in tfidfMap
        return Randomness.randomMemberWeighted(candidates, tfidfMap, totalWeight);
    }
}
