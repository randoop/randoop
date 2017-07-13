package randoop.generation;

import java.util.LinkedList;
import java.util.List;
import randoop.sequence.ExecutableSequence;

public class RandoopListenerManager {

  private List<IEventListener> listeners;

  public RandoopListenerManager() {
    listeners = new LinkedList<>();
  }

  public void addListener(IEventListener listener) {
    if (listener == null) {
      throw new IllegalArgumentException("listener is null");
    }
    listeners.add(listener);
  }

  public void generationStepPre() {
    for (IEventListener n : listeners) {
      n.generationStepPre();
    }
  }

  // es can be null.
  public void generationStepPost(ExecutableSequence es) {
    for (IEventListener n : listeners) {
      n.generationStepPost(es);
    }
  }

  public void progressThreadUpdateNotify() {
    for (IEventListener n : listeners) {
      n.progressThreadUpdate();
    }
  }

  public boolean shouldStopGeneration() {
    for (IEventListener n : listeners) {
      if (n.shouldStopGeneration()) {
        return true;
      }
    }
    return false;
  }

  public void explorationStart() {
    for (IEventListener n : listeners) {
      n.explorationStart();
    }
  }

  public void explorationEnd() {
    for (IEventListener n : listeners) {
      n.explorationEnd();
    }
  }
}
