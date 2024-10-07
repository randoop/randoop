package randoop.generation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import randoop.operation.MethodCall;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.Type;

/**
 * Manages and tracks paired methods within generated test sequences. This class is responsible for
 * identifying and enforcing pairs of related methods (e.g., {@code start()} and {@code stop()}.
 * Related methods are methods that should be invoked in proper pairs to maintain resource
 * integrity, prevent leaks, and enhance the reliability and stability of the generated tests.
 *
 * <p>Example of method pairs:
 *
 * <ul>
 *   <li>{@code start()} and {@code stop()}
 *   <li>{@code open()} and {@code close()}
 *   <li>{@code run()} and {@code end()} ...
 * </ul>
 */
public class MethodPairManager {
  /** List of method pairs */
  private final List<MethodPair> methodPairs = new ArrayList<>();

  /** Maps start operations to their corresponding stop operations. */
  private final Map<TypedOperation, TypedOperation> methodPairsMap = new HashMap<>();

  /** Maps types to their corresponding stop operations. */
  private final Map<Type, TypedOperation> stopOperationsByType = new HashMap<>();

  /** The set of stop operations, for quick lookup. */
  private final Set<TypedOperation> stopOperations = new HashSet<>();

  /** Constructs a new MethodPairManager with default method pairs. */
  public MethodPairManager() {
    // Default method pairs
    methodPairs.add(new MethodPair("start", "stop"));
  }

  /**
   * Determines if the given operation is a start method. If it is, the corresponding stop method is
   * cached for future reference.
   *
   * @param operation the operation to check
   * @return true if it is a start method, false otherwise
   */
  public boolean isStartMethod(TypedOperation operation) {
    if (methodPairsMap.containsKey(operation)) {
      return true; // Already cached
    }

    if (!(operation instanceof TypedClassOperation)) {
      return false;
    }

    TypedClassOperation classOperation = (TypedClassOperation) operation;
    if (!classOperation.isMethodCall()) {
      return false;
    }

    Method method = ((MethodCall) classOperation.getOperation()).getMethod();

    // Check if the method matches any start method in the method pairs
    for (MethodPair pair : methodPairs) {
      if (method.getName().equals(pair.getStartMethodName()) && isValidPairMethod(method)) {
        cacheStopOperation(classOperation, pair);
        return true;
      }
    }
    return false;
  }

  /**
   * Determines if the given operation is a {@code stop()} method.
   *
   * @param operation the operation to check
   * @return true if it is a stop method, false otherwise
   */
  public boolean isStopMethod(TypedOperation operation) {
    if (stopOperations.contains(operation)) {
      return true;
    }

    if (!(operation instanceof TypedClassOperation)) {
      return false;
    }

    TypedClassOperation classOperation = (TypedClassOperation) operation;
    if (!classOperation.isMethodCall()) {
      return false;
    }

    Method method = ((MethodCall) classOperation.getOperation()).getMethod();

    // Check if the method matches any stop method in the method pairs
    for (MethodPair pair : methodPairs) {
      if (method.getName().equals(pair.getStopMethodName()) && isValidPairMethod(method)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines if the given method is a valid pair method. Currently only public, non-static, void
   * methods with no parameters are considered valid. This is due to the original purpose of this
   * class, which was to manage lifecycle methods.
   *
   * @param method the method to check
   * @return {@code true} if it is a valid pair method, {@code false} otherwise
   */
  private boolean isValidPairMethod(Method method) {
    return Modifier.isPublic(method.getModifiers())
        && !Modifier.isStatic(method.getModifiers())
        && method.getReturnType() == void.class
        && method.getParameterCount() == 0;
  }

  /**
   * Caches the stop operation for the given start operation and method pair.
   *
   * @param startOperation the start operation to cache the stop operation for
   * @param pair the MethodPair containing the stop method name
   */
  private void cacheStopOperation(TypedClassOperation startOperation, MethodPair pair) {
    TypedOperation stopOperation = findMatchingStopMethod(startOperation, pair);
    methodPairsMap.put(startOperation, stopOperation);
    if (stopOperation != null) {
      // Cache the stop operation by type
      stopOperationsByType.put(startOperation.getDeclaringType(), stopOperation);
      stopOperations.add(stopOperation);
    }
  }

  /**
   * Finds a matching stop/close method for the given start operation and method pair.
   *
   * @param startOperation the start operation
   * @param pair the MethodPair containing the stop method name
   * @return the corresponding stop operation, or null if not found
   */
  public TypedOperation findMatchingStopMethod(
      TypedClassOperation startOperation, MethodPair pair) {
    ClassOrInterfaceType declaringType = startOperation.getDeclaringType();
    Class<?> declaringClass = declaringType.getRuntimeClass();

    String stopMethodName = pair.getStopMethodName();
    Class<?>[] parameterTypes = {};

    try {
      Method method = declaringClass.getMethod(stopMethodName, parameterTypes);
      if (isValidPairMethod(method)) {
        // Create the TypedOperation for the stop method
        MethodCall methodCall = new MethodCall(method);
        TypedOperation stopOperation =
            new TypedClassOperation(
                methodCall, declaringType, startOperation.getInputTypes(), JavaTypes.VOID_TYPE);
        return stopOperation;
      }
    } catch (NoSuchMethodException e) {
      // Method not found, return null
    }

    return null;
  }

  /**
   * Returns the stop operation for the given type.
   *
   * @param type the type to get the stop operation for
   * @return the stop operation for the given type
   */
  public TypedOperation getStopOperationForType(Type type) {
    Set<Type> visitedTypes = new HashSet<>();
    return getStopOperationForTypeRecursive(type, visitedTypes);
  }

  /**
   * Returns the stop operation for the given type, recursively checking supertypes.
   *
   * @param type the type to get the stop operation for
   * @param visitedTypes the set of visited types to avoid cycles
   * @return the stop operation for the given type
   */
  private TypedOperation getStopOperationForTypeRecursive(Type type, Set<Type> visitedTypes) {
    if (visitedTypes.contains(type)) {
      return null; // Avoid cycles
    }
    visitedTypes.add(type);

    TypedOperation stopOp = stopOperationsByType.get(type);
    if (stopOp != null) {
      return stopOp;
    }

    // Check supertypes
    if (type instanceof ClassOrInterfaceType) {
      ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
      for (ClassOrInterfaceType superType : classType.getSuperTypes()) {
        stopOp = getStopOperationForTypeRecursive(superType, visitedTypes);
        if (stopOp != null) {
          return stopOp;
        }
      }
    }
    return null;
  }

  /**
   * Appends stop methods to the given sequence for each start statement.
   *
   * @param originalSequence the original sequence
   * @return the extended sequence with stop methods appended
   */
  public Sequence appendStopMethods(Sequence originalSequence) {
    Set<Integer> receiverVarIndices = new LinkedHashSet<>();
    for (int i = 0; i < originalSequence.size(); i++) {
      Statement stmt = originalSequence.getStatement(i);
      // If the stmt is a start method, record the receiver variable index for later processing
      if (stmt.isPairStart()) {
        Variable receiverVar = originalSequence.getInputs(i).get(0);
        int receiverVarIndex = receiverVar.getDeclIndex();
        receiverVarIndices.add(receiverVarIndex);
      }
      // If the stmt is a stop method, remove the receiver variable index
      if (stmt.isPairStop()) {
        Variable receiverVar = originalSequence.getInputs(i).get(0);
        int receiverVarIndex = receiverVar.getDeclIndex();
        receiverVarIndices.remove(receiverVarIndex);
      }
    }

    Sequence extendedSequence = originalSequence;
    List<Integer> receiverVarIndicesList = new ArrayList<>(receiverVarIndices);
    Collections.reverse(receiverVarIndicesList); // Reverse for LIFO order

    for (Integer receiverVarIndex : receiverVarIndicesList) {
      Variable receiverVar = extendedSequence.getVariable(receiverVarIndex);
      TypedOperation stopOp = getStopOperationForType(receiverVar.getType());
      if (stopOp != null) {
        extendedSequence =
            extendedSequence.extend(stopOp, Collections.singletonList(receiverVar), false, true);
      }
    }

    return extendedSequence;
  }
}
