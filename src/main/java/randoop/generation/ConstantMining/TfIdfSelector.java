package randoop.generation.ConstantMining;

import randoop.sequence.Sequence;
import randoop.util.Log;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;
import randoop.util.SimpleList;

import java.util.HashMap;
import java.util.Map;

public class TfIdfSelector {
    Map<Sequence, Double> tfidfMap;

    int classCount;

    // TODO: This field might not be useful, since we need weight that dependent on the type
//    double totalWeight;

//    public TfIdfSelector(){
//        tfidfMap = new HashMap<>();
//        classCount = 0;
//        totalWeight = 0;
//    }

    public TfIdfSelector(Map<Sequence, Integer> sequenceFrequency, Map<Sequence, Integer> sequenceOccurrence, int classCount){
        tfidfMap = new HashMap<>();
        this.classCount = classCount;
//        totalWeight = 0.0;
//        assert sequenceFrequency.keySet().equals(sequenceOccurrence.keySet());
        for(Sequence sequence : sequenceFrequency.keySet()){
            int frequency = sequenceFrequency.get(sequence);
            int occurrence = 1;
            if (sequenceOccurrence != null) { // Which means the literal level is not CLASS
                // Optimization: Change it to getOrDefault with 1 as default value
                occurrence = sequenceOccurrence.get(sequence);
            }
            double tfidf = (double)frequency * ((double)classCount + 1) / (((double)classCount + 1) - (double)occurrence);
            tfidfMap.put(sequence, tfidf);
//            totalWeight += tfidf;
            Log.logPrintf("Sequence: " + sequence + "%n" + "Frequency: " + frequency + "%n" + "Occurrence: " + occurrence + "%n" + "TfIdf: " + tfidf + "%n");
        }

//        Log.logPrintf("TfIdf map: " + tfidfMap + "%n" + "Total weight: " + totalWeight + "%n");
        Log.logPrintf("TfIdf map: " + tfidfMap + "%n");
    }

    public Sequence selectSequence() {
        return Randomness.randomMemberWeighted(new SimpleArrayList<Sequence>(tfidfMap.keySet()), tfidfMap);
    }

    public Sequence selectSequence(SimpleList<Sequence> candidates) {
        Log.logPrintf("Selecting sequence: " + candidates + "%n" + "tfidf map: " + tfidfMap + "%n");
        // POTENTIAL BUG: candidates have sequence that is not in tfidfMap
        if (candidates == null) {
            Log.logPrintf("TFIDF Selector: Candidates is null");
            return null;
        }
        Log.logPrintf("Constant Mining success: Candidates: " + candidates + "%n" + "tfidf map: " + tfidfMap + "%n");
        return Randomness.randomMemberWeighted(candidates, tfidfMap);
    }
}
