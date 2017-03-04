package randoop.reflection;

import randoop.contract.*;
import randoop.generation.ComponentManager;
import randoop.main.ClassNameErrorHandler;
import randoop.main.GenInputsAbstract;
import randoop.operation.*;
import randoop.sequence.Sequence;
import randoop.test.ContractSet;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;
import randoop.util.MultiMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by Michael on 3/3/2017.
 */
public abstract class DigDogOperationModel {

  /**
   * Adds literals to the component manager, by parsing any literals files
   * specified by the user.
   * Includes literals at different levels indicated by {@link GenInputsAbstract.ClassLiteralsMode}.
   *
   * @param compMgr  the component manager
   * @param literalsFile  the list of literals file names
   * @param literalsLevel  the level of literals to add
   */
  public abstract void addClassLiterals(
      ComponentManager compMgr,
      List<String> literalsFile,
      GenInputsAbstract.ClassLiteralsMode literalsLevel);

  /**
   * Gets observer methods from the set of signatures.
   *
   * @param observerSignatures  the set of method signatures
   * @return the map to observer methods from their declaring class type
   * @throws OperationParseException if a method signature cannot be parsed
   */
  public abstract MultiMap<Type, TypedOperation> getObservers(Set<String> observerSignatures)
      throws OperationParseException;

  /**
   * Returns the set of types for classes under test.
   *
   * @return the set of class types
   */
  public abstract Set<ClassOrInterfaceType> getClassTypes();

  /**
   * Returns the set of identified {@code Class<?>} objects for the exercised class heuristic.
   *
   * @return the set of exercised classes
   */
  public abstract Set<Class<?>> getExercisedClasses();

  /**
   * Returns the set of input types that occur as parameters in classes under test.
   * @see TypeExtractor
   *
   * @return the set of input types that occur in classes under test
   */
  abstract Set<Type> getInputTypes();

  /**
   * Indicate whether the model has class types.
   *
   * @return true if the model has class types, and false if the class type set is empty
   */
  public abstract boolean hasClasses();

  public abstract List<TypedOperation> getOperations();

  /**
   * Returns all {@link ObjectContract} objects for this run of Randoop.
   * Includes Randoop defaults and {@link randoop.CheckRep} annotated methods.
   *
   * @return the list of contracts
   */
  public abstract ContractSet getContracts();

  public abstract Set<Sequence> getAnnotatedTestValues();

  public abstract Map<Sequence, Integer> getTfFrequency();
}
