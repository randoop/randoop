package randoop;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import randoop.util.ListOfLists;
import randoop.util.SimpleList;

public class SpecialLiterals<T> {

 Map<T, SequenceCollection> literals;
  
  public SpecialLiterals() {
    this.literals = new LinkedHashMap<T, SequenceCollection>();
  }

  public void addSequence(Sequence seq, T key) {
    if (seq == null) throw new IllegalArgumentException("literal is null");
    if (key == null) throw new IllegalArgumentException("key is null");
    SequenceCollection c = literals.get(key);
    if (c == null) {
      c = new SequenceCollection();
      literals.put(key, c);
    }
    // TODO Sequence.create does not memoize!
    c.add(seq);
  }
  
  public SimpleList<Sequence> getLiterals(Class<?> literalType, T key) {
    if (key == null) throw new IllegalArgumentException("key is null");
    SequenceCollection c = literals.get(key);
    if (c == null) {
      return emptyList;
    }
    return literals.get(key).getSequencesForType(literalType, true);
  }

  private static final SimpleList<Sequence> emptyList;
  static {
    List<SimpleList<Sequence>> emptyJDKList = Collections.emptyList();
    emptyList = new ListOfLists<Sequence>(emptyJDKList);
  }

}
