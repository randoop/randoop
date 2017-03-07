package randoop.generation;

import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class WeightedComponentManager extends ComponentManager {

  private Map<Sequence, Integer> frequencyMap;

  public WeightedComponentManager() {
    super();
    frequencyMap = new LinkedHashMap<>();
  }

  public WeightedComponentManager(Collection<Sequence> generalSeeds) {
    super(generalSeeds);
    frequencyMap = new LinkedHashMap<>();
  }

  @Override
  public void addGeneratedSequence(Sequence seq) {
    gralComponents.add(seq);
    if (GenInputsAbstract.weighted_constants) {
      if (frequencyMap.containsKey(seq)) {
        frequencyMap.put(seq, frequencyMap.get(seq) + 1);
      } else {
        frequencyMap.put(seq, 1);
      }
    }
  }

  public Map<Sequence, Integer> getFrequencyMap() {
    return frequencyMap;
  }
}
