package randoop.generation.exhaustive;

import com.google.common.collect.Lists;

import java.util.*;

public class CombinationIterator<T> implements Iterator<Set<T>> {
  private List<T> items;
  private int choose;
  private int[] current;
  private boolean shouldUseInitialIndicesAsACombination;
  private int totalSteps = 0;
  private Set<T> nextCombination;

  public CombinationIterator(Collection<T> items, int choose) {
    this(items, choose, null);
  }

  public CombinationIterator(Collection<T> items, int choose, int[] currentCombinationIndices) {
    if (items == null) {
      throw new IllegalArgumentException("items");
    }
    if (choose <= 0 || choose > items.size()) {
      throw new IllegalArgumentException("choose");
    }
    this.items = Lists.newArrayList(items);
    this.choose = choose;

    if (currentCombinationIndices == null) {
      current = getInitialCombinationIndices(choose);
      this.shouldUseInitialIndicesAsACombination = true;

    } else {
      if (currentCombinationIndices.length != choose) {
        throw new IllegalArgumentException(
            "Current combination must have the same length of choose variable.");
      }

      this.shouldUseInitialIndicesAsACombination = false;

      for (int i = 0; i < choose; i++) {
        if (currentCombinationIndices[i] < 0 || currentCombinationIndices[i] >= this.items.size()) {
          throw new IllegalArgumentException(
              "Current combination have an illegal value at position " + i + ".");
        }
      }
      this.current = currentCombinationIndices;
    }
  }

  public static int[] getInitialCombinationIndices(int choose) {
    int[] indices = new int[choose];
    for (int i = 0; i < choose; i++) {
      indices[i] = i;
    }
    return indices;
  }

  /**
   * Return the indices used to generate the last combination returned via next() method.
   * @apiNote If used to resume generation, it is assumed the combination corresponding to the returned indices
   * has already been stored.
   * @return indices used to generate the last element returned.
   */
  public int[] getCurrentIndices() {
    return Arrays.copyOf(current, current.length);
  }

  public int[] getNextCombination() {
    return getNextCombinationIndices(this.items.size(), choose, current);
  }

  public boolean hasNext() {
    if (shouldUseInitialIndicesAsACombination) {
      return totalSteps == 0 || getNextCombination() != null;
    } else {
      return getNextCombination() != null;
    }
  }

  private void loadCombination() {
    nextCombination = new HashSet<>(choose);

    for (int i = 0; i < choose; i++) {
      nextCombination.add(items.get(current[i]));
    }
  }

  private void generateNextCombination() {
    this.current = getNextCombination();

    if (this.current == null) {
      nextCombination = null;
    } else {
      loadCombination();
    }
  }

  public Set<T> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    if (totalSteps == 0 && shouldUseInitialIndicesAsACombination) {
      loadCombination();
    } else {
      generateNextCombination();
    }

    totalSteps++;

    return nextCombination;
  }

  public static int[] getNextCombinationIndices(
      int numberOfItems, int choose, int[] currentCombinationIndices) {
    if (currentCombinationIndices == null) {
      throw new IllegalArgumentException("currentCombinationIndices");
    }

    if (choose <= 0 || choose > numberOfItems) {
      throw new IllegalArgumentException("choose");
    }

    int[] current = Arrays.copyOf(currentCombinationIndices, choose);

    boolean finished = true;

    int n = numberOfItems;
    for (int i = choose - 1; i >= 0; i--) {
      if (current[i] < n - choose + i) {
        current[i]++;
        for (int j = i + 1; j < choose; j++) {
          current[j] = current[i] - i + j;
        }
        finished = false;
        break;
      }
    }

    if (finished) {
      return null;
    } else {
      return current;
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}
