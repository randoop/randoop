package randoop.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import randoop.contract.ObjectContract;

/**
 * Manages the set of {@link ObjectContract} objects.
 * Contracts are organized by arity.
 */
public class ContractSet {

  /** the collection of contracts */
  private final Map<Integer, List<ObjectContract>> contractMap;

  /** the maximum arity of a contract */
  private int maxArity;

  /**
   * Creates an contract set with no elements.
   */
  public ContractSet() {
    contractMap = new HashMap<>();
    maxArity = 0;
  }

  /**
   * Returns the list of contracts with the given arity.
   *
   * @param arity  the arity
   * @return the list of contracts with the given arity
   */
  public List<ObjectContract> getArity(int arity) {
    List<ObjectContract> contractList = contractMap.get(arity);
    if (contractList == null) {
      contractList = new ArrayList<>();
    }
    return contractList;
  }

  /**
   * Adds a contract to this set.
   *
   * @param contract  the contract
   */
  public void add(ObjectContract contract) {
    List<ObjectContract> contractList = contractMap.get(contract.getArity());
    if (contractList == null) {
      contractList = new ArrayList<>();
    }
    contractList.add(contract);
    contractMap.put(contract.getArity(), contractList);
    if (contract.getArity() > maxArity) {
      maxArity = contract.getArity();
    }
  }

  /**
   * Returns the maximum arity of a contract in this set.
   *
   * @return the maximum contract arity
   */
  int getMaxArity() {
    return maxArity;
  }

  public boolean isEmpty() {
    return contractMap.isEmpty();
  }
}
