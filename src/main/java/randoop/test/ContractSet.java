package randoop.test;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import randoop.contract.ObjectContract;

/** Manages the set of {@link ObjectContract} objects. Contracts are organized by arity. */
public class ContractSet {

  /**
   * Each element is a list of all contracts of that arity. For example, the element at index 2 is a
   * list of contracts of arity 2. Used only for containment check.
   */
  private final List<List<ObjectContract>> contractMap = new ArrayList<>(1);

  /**
   * Returns the list of contracts with the given arity.
   *
   * @param arity the arity
   * @return the list of contracts with the given arity
   */
  public List<ObjectContract> getWithArity(int arity) {
    List<ObjectContract> contractList = contractMap.get(arity);
    if (contractList == null) {
      contractList = new ArrayList<>();
    }
    return contractList;
  }

  /**
   * Adds a contract to this set.
   *
   * @param contract the contract
   */
  public void add(ObjectContract contract) {
    int arity = contract.getArity();
    while (arity >= contractMap.size()) {
      contractMap.add(new ArrayList<>(1));
    }
    List<ObjectContract> contractList = contractMap.get(arity);
    contractList.add(contract);
  }

  public boolean isEmpty() {
    return contractMap.isEmpty();
  }

  @Override
  public String toString() {
    int cardinality = 0;
    StringJoiner contractString = new StringJoiner(System.lineSeparator());
    int size = contractMap.size();
    for (int i = 0; i < size; i++) {
      List<ObjectContract> contractList = contractMap.get(i);
      if (!contractList.isEmpty()) {
        contractString.add(String.format("    arity %d: %s", i, contractList));
        cardinality += contractList.size();
      }
    }
    return String.format("ContractSet[size=%d]%n%s", cardinality, contractString.toString());
  }
}
