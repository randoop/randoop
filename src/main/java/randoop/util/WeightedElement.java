package randoop.util;

public class WeightedElement<T extends Comparable<T>> {

  private T data;
  private double weight;

  public WeightedElement(T data, double weight) {
    this.data = data;
    this.weight = weight;
  }

  public double getWeight() {
    return weight;
  }

  public T getData() {
    return data;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof WeightedElement)) {
      return false;
    }
    // Weight values should not affect weighted element equality.
    WeightedElement element = (WeightedElement) o;
    return element.data.equals(this.data);
  }

  @Override
  public int hashCode() {
    return data.hashCode();
  }
}
