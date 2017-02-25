package randoop.generation.exhaustive;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.*;

public class SequenceGenerator<T> implements Iterator<List<T>> {
  long totalSequences;
  int sequenceLength;
  int maximumLength;
  Collection<T> elements;
  List<T> elementsToPermute;
  PermutationIterator<T> permutationGenerator;
  CombinationIterator<T> combinationsGenerator;

  public SequenceGenerator(Collection<T> elements) {
    this(elements, elements == null ? 0 : elements.size());
  }

  public SequenceGenerator(Collection<T> elements, int maximumSequenceLength) {
    this(elements, maximumSequenceLength, null);
  }

  public SequenceGenerator(Collection<T> elements, SequenceIndex currentIndex) {
    this(elements, elements == null ? 0 : elements.size(), currentIndex);
  }

  public SequenceGenerator(
      Collection<T> elements, int maximumSequenceLength, SequenceIndex currentIndex) {

    if (elements == null) {
      throw new IllegalArgumentException("Elements set must be not null.");
    }

    if (elements.isEmpty()) {
      throw new IllegalArgumentException("Cannot generate sequences for empty sets.");
    }

    this.totalSequences = 0;
    this.sequenceLength = 1;
    this.maximumLength = maximumSequenceLength;
    this.elements = elements;

    if (currentIndex != null) {
      this.setCurrentIndex(currentIndex);
    }
  }

  @Override
  public boolean hasNext() {
    if (sequenceLength < maximumLength) {
      return true;
    } else {
      return permutationGenerator == null
          || permutationGenerator.hasNext()
          || combinationsGenerator.hasNext()
          || (!permutationGenerator.hasNext() && combinationsGenerator.hasNext())
          || (!combinationsGenerator.hasNext() && permutationGenerator.hasNext());
    }
  }

  public long getTotalSequencesIterated() {
    return totalSequences;
  }

  @Override
  public List<T> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    if (combinationsGenerator == null) {
      combinationsGenerator = new CombinationIterator<>(elements, sequenceLength);
      elementsToPermute = Lists.newArrayList(combinationsGenerator.next());
    }

    if (permutationGenerator == null) {
      permutationGenerator = new PermutationIterator<>(elementsToPermute);
    }

    if (!permutationGenerator.hasNext()) {
      if (!combinationsGenerator.hasNext()) {
        sequenceLength++;
        combinationsGenerator = new CombinationIterator<T>(elements, sequenceLength);
      }
      elementsToPermute = Lists.newArrayList(combinationsGenerator.next());
      permutationGenerator = new PermutationIterator<>(elementsToPermute);
    }

    totalSequences++;

    List<T> next = permutationGenerator.next();

    return next;
  }

  private static SequenceIndex getNextIndex(
      SequenceIndex current, int maximumSequenceLength, int numberOfItems) {
    if (current == null) {
      throw new IllegalArgumentException("'current' cannot be null.");
    }

    if (current.getSequenceLength() > maximumSequenceLength) {
      return null;
    }

    int sequenceSize = current.getSequenceLength();
    int[] nextCombIndices =
        CombinationIterator.getNextCombinationIndices(
            numberOfItems, current.getSequenceLength(), current.getCurrentCombinationIndices());
    int[] nextPermutationIndices;

    nextPermutationIndices =
        PermutationIterator.getNextPermutationIndices(current.getCurrentPermutationIndices());

    if (nextPermutationIndices == null) {

      // No more permutations of the current size. Checks if it's possible to generate sequences of size n+1
      sequenceSize++;

      if (sequenceSize > maximumSequenceLength) {
        return null;
      }

      nextCombIndices = CombinationIterator.getInitialCombinationIndices(sequenceSize);
      nextPermutationIndices =
          PermutationIterator.getInitialPermutationIndices(nextCombIndices.length);
    } else {
      nextCombIndices = Arrays.copyOf(nextCombIndices, nextCombIndices.length);
    }

    return new SequenceIndex(sequenceSize, nextCombIndices, nextPermutationIndices);
  }

  static class SequenceIndex implements Serializable {
    private static final long serialVersionUID = 2496849987065726718L;

    private int[] currentCombinationIndices;
    private int[] currentPermutationIndices;
    private int sequenceLength;

    public SequenceIndex(int sequenceLength, int[] combinationIndices, int[] permutationIndices) {
      this.currentCombinationIndices = combinationIndices;
      this.currentPermutationIndices = permutationIndices;
      this.sequenceLength = sequenceLength;
    }

    public int[] getCurrentCombinationIndices() {
      return currentCombinationIndices;
    }

    public int[] getCurrentPermutationIndices() {
      return currentPermutationIndices;
    }

    public int getSequenceLength() {
      return sequenceLength;
    }
  }

  public SequenceIndex getCurrentIndex() {
    return new SequenceIndex(
        this.sequenceLength,
        this.combinationsGenerator.getCurrentIndices(),
        this.permutationGenerator.getCurrentIndices());
  }

  private void setCurrentIndex(SequenceIndex index) {
    if (index == null
        || index.getCurrentCombinationIndices() == null
        || index.getCurrentPermutationIndices() == null) {
      throw new IllegalArgumentException("Index and its members cannot be null.");
    }

    this.sequenceLength = index.getSequenceLength();

    combinationsGenerator =
        new CombinationIterator<>(
            elements, index.getSequenceLength(), index.getCurrentCombinationIndices());
    permutationGenerator =
        new PermutationIterator<>(elements, index.getCurrentPermutationIndices());

    boolean isThereMoreSequencesForCurrentLength =
        permutationGenerator.hasNext() || combinationsGenerator.hasNext();

    if (isThereMoreSequencesForCurrentLength) {
      // Assuming that the element produced by current index has already been collected, skip it:
      if (permutationGenerator.hasNext()) {
        this.next();
      }
    } else {
      SequenceIndex newIndex = getNextIndex(index, maximumLength, elements.size());
      if (newIndex != null) {
        this.sequenceLength = newIndex.getSequenceLength();
        combinationsGenerator =
            new CombinationIterator<>(
                elements, newIndex.getSequenceLength(), newIndex.getCurrentCombinationIndices());

        // Does not inform current index, so it starts from the initial index.
        permutationGenerator = new PermutationIterator<>(elements);
      }
    }
  }
}
