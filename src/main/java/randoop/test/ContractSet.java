package randoop.test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import randoop.contract.ObjectContract;

/** Manages the set of {@link ObjectContract} objects. Contracts are organized by arity. */
public class ContractSet {

  /** Maps from arity to all all contracts of that arity. */
  private final Map<Integer, List<ObjectContract>> contractMap; // used only for containment check

  /** The maximum arity of a contract; the maximum key in the map. */
  private int maxArity;

  /** Creates an contract set with no elements. */
  public ContractSet() {
    contractMap = new LinkedHashMap<>();
    maxArity = 0;
  }

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
    List<ObjectContract> contractList = contractMap.get(arity);
    if (contractList == null) {
      contractList = new ArrayList<>();
      if (arity > maxArity) {
        maxArity = contract.getArity();
      }
    }
    contractList.add(contract);
    contractMap.put(arity, contractList);
  }

  /**
   * Returns the maximum arity of any contract in this set.
   *
   * @return the maximum contract arity
   */
  int getMaxArity() {
    return maxArity;
  }

  public boolean isEmpty() {
    return contractMap.isEmpty();
  }

  @Override
  public String toString() {
    int cardinality = 0;
    StringBuilder contractString = new StringBuilder("");
    for (int i = 0; i <= maxArity; i++) {
      List<ObjectContract> contracts = contractMap.get(i);
      if (contracts != null) {
        contractString.append(String.format("  arity %d: %s%n", i, contracts));
        cardinality += contracts.size();
      }
    }
    return String.format("ContractSet[size=%d]%n%s", cardinality, contractString.toString());
  }
}
