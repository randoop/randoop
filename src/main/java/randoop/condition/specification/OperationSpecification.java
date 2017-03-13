package randoop.condition.specification;

import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Created by bjkeller on 3/13/17. */
public class OperationSpecification {
  private final AccessibleObject operation;
  private final List<ConditionSpecification> specifications;

  public OperationSpecification(AccessibleObject operation) {
    this.operation = operation;
    this.specifications = new ArrayList<>();
  }

  public OperationSpecification(
      AccessibleObject operation, List<ConditionSpecification> specifications) {
    this.operation = operation;
    this.specifications = specifications;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof OperationSpecification)) {
      return false;
    }
    OperationSpecification other = (OperationSpecification) object;
    return this.specifications.equals(other.specifications);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.specifications);
  }

  @Override
  public String toString() {
    return specifications.toString();
  }

  public void addSpecification(ConditionSpecification specification) {
    specifications.add(specification);
  }

  public AccessibleObject getOperation() {
    return operation;
  }

  public List<ConditionSpecification> getSpecifications() {
    return specifications;
  }
}
