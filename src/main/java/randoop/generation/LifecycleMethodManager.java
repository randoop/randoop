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
 * Manages and tracks lifecycle methods within generated test sequences, specifically focusing
 * on pairing lifecycle {@code start()} methods with their corresponding {@code stop()} or {@code close()}
 * methods. This ensures that resources initiated during test sequence execution are properly
 * terminated, preventing resource leaks and ensuring the reliability and stability of the generated tests.
 */
public class LifecycleMethodManager {
  /** Maps lifecycle start operations to their corresponding stop operations. */
  private final Map<TypedOperation, TypedOperation> lifecycleMethods = new HashMap<>();

  /** Maps types to their corresponding stop operations. */
  private final Map<Type, TypedOperation> stopOperationsByType = new HashMap<>();

  /** Constructs a new LifecycleMethodManager. */
  public LifecycleMethodManager() {}

  /**
   * Determines if the given operation is a lifecycle start method.
   *
   * @param operation the operation to check
   * @return true if it is a lifecycle start method, false otherwise
   */
  public boolean isLifecycleStartMethod(TypedOperation operation) {
    if (lifecycleMethods.containsKey(operation)) {
      return false; // Already cached
    }

    if (!(operation instanceof TypedClassOperation)) {
      return false;
    }

    TypedClassOperation classOperation = (TypedClassOperation) operation;
    if (!classOperation.isMethodCall() || classOperation.isStatic()) {
      return false;
    }

    Method method = ((MethodCall) classOperation.getOperation()).getMethod();
    if (isStartMethod(method)) {
      // Cache the stop operation
      cacheStopOperation(classOperation);
      return true;
    }
    return false;
  }


  /**
   * Determines if the given method is a lifecycle {@code start()} method.
   *
   * @param method the method to check
   * @return {@code true} if it is a lifecycle start method, {@code false} otherwise
   */
  private boolean isStartMethod(Method method) {
    return method.getName().equals("start")
        && Modifier.isPublic(method.getModifiers())
        && !Modifier.isStatic(method.getModifiers())
        && method.getReturnType().equals(void.class)
        && method.getParameterCount() == 0;
  }

  /**
   * Determines if the given operation is a lifecycle {@code stop()} method.
   *
   * @param operation the operation to check
   * @return true if it is a lifecycle stop method, false otherwise
   */
  public boolean isLifecycleStopMethod(TypedOperation operation) {
    if (!(operation instanceof TypedClassOperation)) {
      return false;
    }

    TypedClassOperation classOperation = (TypedClassOperation) operation;
    if (!classOperation.isMethodCall() || classOperation.isStatic()) {
      return false;
    }

    Method method = ((MethodCall) classOperation.getOperation()).getMethod();
    boolean methodNamedStop = method.getName().equals("stop") || method.getName().equals("close");
    if (methodNamedStop && isValidStopMethod(method)) {
      return true;
    }
    return false;
  }

  /**
   * Caches the stop operation for the given start operation.
   *
   * @param startOperation the start operation to cache the stop operation for
   */
  private void cacheStopOperation(TypedClassOperation startOperation) {
    TypedOperation stopOperation = findMatchingStopMethod(startOperation);
    lifecycleMethods.put(startOperation, stopOperation);
    if (stopOperation != null) {
      // Cache the stop operation by type
      stopOperationsByType.put(startOperation.getDeclaringType(), stopOperation);
    }
  }

  /**
   * Finds a matching stop/close method for the given start operation.
   *
   * @param startOperation the start operation
   * @return the corresponding stop operation, or null if not found
   */
  public TypedOperation findMatchingStopMethod(TypedClassOperation startOperation) {
    ClassOrInterfaceType declaringType = startOperation.getDeclaringType();
    Class<?> clazz = declaringType.getRuntimeClass();

    String[] stopMethodNames = {"stop", "close"};
    for (String methodName : stopMethodNames) {
      try {
        Method method = clazz.getMethod(methodName);
        if (isValidStopMethod(method)) {
          // Create the TypedOperation for the stop method
          MethodCall methodCall = new MethodCall(method);
          TypedOperation stopOperation =
              new TypedClassOperation(
                  methodCall,
                  declaringType,
                  startOperation.getInputTypes(), // Receiver type
                  JavaTypes.VOID_TYPE);
          return stopOperation;
        }
      } catch (NoSuchMethodException e) {
        // Method not found, continue to next name
      }
    }
    return null;
  }

  /**
   * Checks if the method is a valid stop method.
   *
   * @param method the method to check
   */
  private boolean isValidStopMethod(Method method) {
    return Modifier.isPublic(method.getModifiers())
        && !Modifier.isStatic(method.getModifiers())
        && method.getReturnType().equals(void.class)
        && method.getParameterCount() == 0;
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
   * Appends stop methods to the given sequence for each lifecycle start statement.
   *
   * @param originalSequence the original sequence
   * @return the extended sequence with stop methods appended
   */
  public Sequence appendStopMethods(Sequence originalSequence) {
    Set<Integer> receiverVarIndices = new LinkedHashSet<>();
    for (int i = 0; i < originalSequence.size(); i++) {
      Statement stmt = originalSequence.getStatement(i);
      if (stmt.isLifecycleStart()) {
        Variable receiverVar = originalSequence.getInputs(i).get(0);
        int receiverVarIndex = receiverVar.getDeclIndex();
        receiverVarIndices.add(receiverVarIndex);
      }
      // If the stmt is a lifecycle stop method, remove the receiverVarIndex from the set
      if (stmt.isLifecycleStop()) {
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
