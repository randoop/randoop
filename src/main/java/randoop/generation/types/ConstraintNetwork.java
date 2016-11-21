package randoop.generation.types;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import plume.Pair;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.TypeVariable;
import randoop.util.MultiMap;

/**
 * Created by bjkeller on 11/3/16.
 */
public class ConstraintNetwork {

  public List<Substitution<ReferenceType>> search(
      List<TypeVariable> parameters, List<Propagator> propagators) {
    return null;
  }

  /**
   * Implements a Constraint solver for type constraints.
   * Returns the family of maps of domains for each variable each defining free selections of
   * values that satisfy the original constraints.
   * If there are not propagators for constraints will return the domains, and if constraints
   * cannot be satisfied returns the empty list.
   *
   * @param domains  the type domains for each variable
   * @param propagators  the list of remaining constraint propagators
   * @return a list of domain maps satisfying the constraints
   */
  private List<Map<TypeVariable, TypeDomain>> search(
      Map<TypeVariable, TypeDomain> domains, List<Propagator> propagators) {
    List<Map<TypeVariable, TypeDomain>> resultList = new ArrayList<>();
    if (propagators.isEmpty()) {
      resultList.add(domains);
      return resultList;
    }
    Map<TypeVariable, TypeDomain> refinedDomains = propagate(domains, propagators);
    if (refinedDomains.isEmpty()) { //failed
      return new ArrayList<>();
    }
    List<Substitution<ReferenceType>> branches = branch(refinedDomains);
    for (Substitution<ReferenceType> substitution : branches) {
      resultList.addAll(search(refinedDomains, update(propagators, substitution)));
    }
    return resultList;
  }

  /**
   * Performs constraint propagation on the list of domains.
   * Based on the AC3 algorithm.
   *
   * @param domains
   * @param propagators
   * @return
   */
  private Map<TypeVariable, TypeDomain> propagate(
      Map<TypeVariable, TypeDomain> domains, List<Propagator> propagators) {
    MultiMap<TypeVariable, Propagator> propagatorMultiMap = new MultiMap<>();
    PriorityQueue<Pair<TypeVariable, Propagator>> queue =
        new PriorityQueue<>(domains.size() * propagators.size(), new PairComparator());
    for (Propagator propagator : propagators) {
      for (TypeVariable variable : propagator.getParameters()) {
        queue.add(new Pair<>(variable, propagator));
        propagatorMultiMap.add(variable, propagator);
      }
    }
    while (!queue.isEmpty()) {
      Pair<TypeVariable, Propagator> pair = queue.poll();
      TypeVariable variable = pair.a;
      Propagator propagator = pair.b;
      if (propagator.filter(variable, domains)) {
        if (domains.get(variable).isEmpty()) {
          return new HashMap<>();
        }
        for (TypeVariable otherVariable : propagator.getParameters()) {
          if (!otherVariable.equals(variable)) {
            for (Propagator propagator1 : propagatorMultiMap.getValues(otherVariable)) {
              queue.add(new Pair<>(otherVariable, propagator1));
            }
          }
        }
      }
    }
    return domains;
  }

  /**
   * Implements the branching heuristic.
   *
   * @param domains
   * @return
   */
  private List<Substitution<ReferenceType>> branch(Map<TypeVariable, TypeDomain> domains) {
    TypeVariable minVariable = null;
    int minSize = Integer.MAX_VALUE;
    for (Map.Entry<TypeVariable, TypeDomain> entry : domains.entrySet()) {
      /*
      if (entry.getValue().size() < minSize) {
        minVariable = entry.getKey();
        minSize = entry.getValue().size();
      }
      */
    }
    if (minSize == Integer.MAX_VALUE) {
      return new ArrayList<>();
    }
    assert minVariable != null;

    List<Substitution<ReferenceType>> branches = new ArrayList<>();
    for (ReferenceType type : domains.get(minVariable)) {
      branches.add(Substitution.forArg(minVariable, type));
    }
    return branches;
  }

  private List<Propagator> update(
      List<Propagator> propagators, Substitution<ReferenceType> substitution) {
    assert !substitution.isEmpty() : "should not have empty substitution here";
    List<Propagator> updatedPropagators = new ArrayList<>();
    for (Propagator propagator : propagators) {
      updatedPropagators.add(propagator.apply(substitution));
    }
    return updatedPropagators;
  }

  /**
   * Comparator for ordering variable-propagator pairs in priority queue
   */
  private class PairComparator implements Comparator<Pair<TypeVariable, Propagator>> {
    @Override
    public int compare(Pair<TypeVariable, Propagator> pair1, Pair<TypeVariable, Propagator> pair2) {
      int arityDiff = pair1.b.arity() - pair2.b.arity();
      if (arityDiff < 0) {
        return -1;
      }
      if (arityDiff > 0) {
        return 1;
      }
      return pair1.a.getName().compareTo(pair2.a.getName());
    }
  }
}
