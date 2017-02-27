package randoop.generation.exhaustive;

import com.google.common.collect.Lists;
import com.google.common.math.BigIntegerMath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import randoop.sequence.Sequence;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

public class SequenceGenerator<T> implements Iterator<List<T>> {
  BigInteger numberOfSequences = BigInteger.ZERO;
  int sequenceLength;
  int maximumLength;
  Collection<T> elements;
  List<T> elementsToPermute;
  PermutationIterator<T> permutationGenerator;
  CombinationIterator<T> combinationsGenerator;

  public static BigInteger getExpectedNumberOfSequences(long numberOfElements, int maximumLength) {
    BigInteger sum = BigInteger.ZERO;

    int n = (int) numberOfElements;
    for (int i = 1; i <= maximumLength; i++) {
      sum = sum.add(BigIntegerMath.factorial(n).divide(BigIntegerMath.factorial(n - i)));
    }
    return sum;
  }

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

  public BigInteger getTotalSequencesIterated() {
    return numberOfSequences;
  }

  private Set<List<T>> previousElementsToPermute = new HashSet<>();

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

    numberOfSequences = numberOfSequences.add(BigInteger.ONE);

    if (elementsToPermute != null) {
      previousElementsToPermute.add(elementsToPermute);
    }
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

  public static class SequenceIndex implements Serializable {
    private static final long serialVersionUID = 2496849987065726718L;

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }

      if (!(obj instanceof SequenceIndex)) {
        return false;
      }

      SequenceIndex other = (SequenceIndex) obj;

      boolean sameLength = this.sequenceLength == other.getSequenceLength();
      boolean sameCombinationIndices =
          this.currentCombinationIndices.equals(other.getCurrentCombinationIndices());
      boolean samePermutationIndices =
          this.getCurrentPermutationIndices().equals(other.getCurrentPermutationIndices());

      return sameLength && sameCombinationIndices && samePermutationIndices;
    }

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

    public static SequenceIndex deserializeFromFile(File file) throws IOException {
      if (file == null || !file.exists()) {
        throw new RuntimeException(
            "File must exist to proceed with sequence index deserialization");
      }

      byte[] data = FileUtils.readFileToByteArray(file);
      SequenceIndex index = SerializationUtils.deserialize(data);

      return index;
    }

    public BigInteger getNumberOfStepsSinceInitialIndex(
        int numberOfItems, int initialSequenceLength) {

      BigInteger steps = BigInteger.ZERO;

      for (int i = initialSequenceLength; i < this.getSequenceLength(); i++) {
        BigInteger numberOfPermutationsChoosingI =
            BigIntegerMath.factorial(numberOfItems)
                .divide(BigIntegerMath.factorial(numberOfItems - i));
        steps = steps.add(numberOfPermutationsChoosingI);
      }

      int[] combinationSteps =
          CombinationIterator.getInitialCombinationIndices(this.getSequenceLength());
      while (!Arrays.equals(combinationSteps, this.getCurrentCombinationIndices())) {
        BigInteger numberOfPermutations = BigIntegerMath.factorial(this.getSequenceLength());
        steps = steps.add(numberOfPermutations);
        combinationSteps =
            CombinationIterator.getNextCombinationIndices(
                numberOfItems, this.getSequenceLength(), combinationSteps);
      }

      int[] permSteps = PermutationIterator.getInitialPermutationIndices(combinationSteps.length);
      while (!Arrays.equals(permSteps, this.getCurrentPermutationIndices())) {
        steps = steps.add(BigInteger.ONE);
        permSteps = PermutationIterator.getNextPermutationIndices(permSteps);
      }

      // When indices are equal, the last sum above is not made, so correct it now:
      steps = steps.add(BigInteger.ONE);
      return steps;
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

        // Let the instantiation of combinations iterator and permutations iterator to the hasNext method.
        combinationsGenerator = null;
        permutationGenerator = null;
      }
    }
  }
}
