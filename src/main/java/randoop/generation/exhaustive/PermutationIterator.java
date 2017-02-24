package randoop.generation.exhaustive;

import java.util.*;

public class PermutationIterator<T> implements Iterator<List<T>> {

  private List<T> nextPermutation;
  private final List<T> allElements = new ArrayList<>();
  private int[] indices;
  private int totalSteps = 0;
  private boolean shouldUseInitialIndicesAsAPermutation;

  PermutationIterator(Collection<T> allElements, int[] currentPermutationIndices) {

    if (allElements.isEmpty()) {
      return;
    }

    this.allElements.addAll(allElements);

    if (currentPermutationIndices == null) {
      shouldUseInitialIndicesAsAPermutation = true;
      this.indices = getInitialPermutationIndices(allElements.size());

    } else {
      shouldUseInitialIndicesAsAPermutation = false;
      this.indices = currentPermutationIndices;
    }
  }

  public static int[] getInitialPermutationIndices(int numberOfElements) {
    int[] indices = new int[numberOfElements];

    for (int i = 0; i < indices.length; ++i) {
      indices[i] = i;
    }

    return indices;
  }

  PermutationIterator(List<T> allElements) {
    this(allElements, null);
  }

  @Override
  public boolean hasNext() {
    if (shouldUseInitialIndicesAsAPermutation) {
      return totalSteps == 0 || getNextIndexToPermute() >= 0;
    } else {
      return getNextIndexToPermute() >= 0;
    }
  }

  @Override
  public List<T> next() {
    if (!hasNext()) {
      throw new NoSuchElementException("No permutations left.");
    }

    if (totalSteps == 0 && shouldUseInitialIndicesAsAPermutation) {
      loadPermutation();
    } else {
      generateNextPermutation();
    }
    totalSteps++;
    return nextPermutation;
  }

  public int[] getCurrentIndices() {
    return Arrays.copyOf(indices, indices.length);
  }

  public static int[] getNextPermutationIndices(int[] currentIndices) {
    if (currentIndices == null) {
      throw new IllegalArgumentException("currentIndices");
    }

    int[] next = getIndicesForNextPermutation(currentIndices);

    return next;
  }

  private static int[] getIndicesForNextPermutation(int[] currentIndices) {
    int i = getNextPositionToPermute(currentIndices);

    if (i == -1) {
      return null;
    }

    int j = i + 1;
    int min = currentIndices[j];
    int minIndex = j;
    int[] indices = Arrays.copyOf(currentIndices, currentIndices.length);

    while (j < currentIndices.length) {
      if (indices[i] < indices[j] && indices[j] < min) {
        min = indices[j];
        minIndex = j;
      }

      ++j;
    }

    swap(indices, i, minIndex);

    ++i;
    j = indices.length - 1;

    while (i < j) {
      swap(indices, i++, j--);
    }

    return indices;
  }

  private void generateNextPermutation() {
    this.indices = getIndicesForNextPermutation(this.getCurrentIndices());

    if (indices == null) {
      nextPermutation = null;
    } else {
      loadPermutation();
    }
  }

  private static int getNextPositionToPermute(int[] indices) {
    int i = indices.length - 2;

    while (i >= 0 && indices[i] > indices[i + 1]) {
      --i;
    }
    return i;
  }

  private int getNextIndexToPermute() {
    int i = getNextPositionToPermute(this.indices);
    return i;
  }

  private void loadPermutation() {
    List<T> newPermutation = new ArrayList<>(indices.length);

    for (int i : indices) {
      newPermutation.add(allElements.get(i));
    }

    this.nextPermutation = newPermutation;
  }

  private static void swap(int[] array, int a, int b) {
    int tmp = array[a];
    array[a] = array[b];
    array[b] = tmp;
  }

  public int getTotalSteps() {
    return totalSteps;
  }
}
