package randoop.util;

public class WeightedElement<T> {

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
}
