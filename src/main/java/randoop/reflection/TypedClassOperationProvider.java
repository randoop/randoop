package randoop.reflection;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.plumelib.util.EntryReader;
import org.plumelib.util.UtilPlume;
import randoop.condition.SpecificationCollection;
import randoop.main.RandoopUsageError;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;

/**
 * {@code TypedClassOperationProvider} encapsulates the logic of transformation
 * of {@code ClassOrInterfaceType}s and {@code Path}s with method signatures
 * in binary form into {@code TypedClassOperation} objects
 */
public class TypedClassOperationProvider {

  private final OmitMethodsPredicate omitMethodsPredicate;

  public TypedClassOperationProvider(OmitMethodsPredicate omitMethodsPredicate) {
    this.omitMethodsPredicate = omitMethodsPredicate;
  }

  /**
   * Returns operations from all of the classes of {@code classTypes}.
   *
   * @param classTypes class types
   * @param accessibility the accessibility predicate
   * @param reflectionPredicate the reflection predicate
   * @param operationSpecifications the collection of {@link
   *     randoop.condition.specification.OperationSpecification}
   * @return operations from classTypes
   */
  public List<TypedOperation> getOperationsFromClasses(
      Set<ClassOrInterfaceType> classTypes,
      AccessibilityPredicate accessibility,
      ReflectionPredicate reflectionPredicate,
      SpecificationCollection operationSpecifications) {
    List<TypedOperation> operations = new ArrayList<>();
    Iterator<ClassOrInterfaceType> itor = classTypes.iterator();
    while (itor.hasNext()) {
      ClassOrInterfaceType classType = itor.next();
      try {
        Collection<TypedOperation> oneClassOperations =
            OperationExtractor.operations(
                classType,
                reflectionPredicate,
                omitMethodsPredicate,
                accessibility,
                operationSpecifications);
        operations.addAll(oneClassOperations);
      } catch (Throwable e) {
        // TODO: What is an example of this?  Should an error be raised, rather than this
        // easy-to-overlook output?
        System.out.printf(
            "Removing %s from the classes under test due to problem extracting operations:%n%s%n",
            classType, UtilPlume.stackTraceToString(e));
        itor.remove();
      }
    }
    return operations;
  }

  /**
   * Constructs an operation from every method signature in the given file.
   *
   * @param methodSignaturesFile the file containing the signatures; if null, return the empty list
   * @param accessibility the accessibility predicate
   * @param reflectionPredicate the reflection predicate
   * @return operations read from the file
   * @throws SignatureParseException if any signature is syntactically invalid
   */
  public List<TypedClassOperation> getOperationsFromFile(
      Path methodSignaturesFile,
      AccessibilityPredicate accessibility,
      ReflectionPredicate reflectionPredicate)
      throws SignatureParseException {
    List<TypedClassOperation> result = new ArrayList<>();
    if (methodSignaturesFile == null) {
      return result;
    }
    try (EntryReader reader = new EntryReader(methodSignaturesFile, "(//|#).*$", null)) {
      for (String line : reader) {
        String sig = line.trim();
        if (!sig.isEmpty()) {
          try {
            TypedClassOperation operation =
                signatureToOperation(sig, accessibility, reflectionPredicate);
            if (!omitMethodsPredicate.shouldOmit(operation)) {
              result.add(operation);
            }
          } catch (FailedPredicateException e) {
            System.out.printf("Ignoring %s that failed predicate: %s%n", sig, e.getMessage());
          }
        }
      }
    } catch (IOException e) {
      throw new RandoopUsageError("Problem reading file " + methodSignaturesFile, e);
    }
    return result;
  }

  /**
   * Given a signature, returns the method or constructor it represents.
   *
   * @param signature the operation's signature, in Randoop's format
   * @param accessibility the accessibility predicate
   * @param reflectionPredicate the reflection predicate
   * @return the method or constructor that the signature represents
   * @throws FailedPredicateException if the accessibility or reflection predicate returns false on
   *     the class or the method or constructor
   * @throws SignatureParseException if the signature cannot be parsed
   */
  public static TypedClassOperation signatureToOperation(
      String signature,
      AccessibilityPredicate accessibility,
      ReflectionPredicate reflectionPredicate)
      throws SignatureParseException, FailedPredicateException {
    AccessibleObject accessibleObject;
    accessibleObject = SignatureParser.parse(signature, accessibility, reflectionPredicate);
    if (accessibleObject == null) {
      throw new FailedPredicateException(
          String.format(
              "accessibleObject is null for %s, typically due to predicates: %s, %s",
              signature, accessibility, reflectionPredicate));
    }
    if (accessibleObject instanceof Constructor) {
      return TypedOperation.forConstructor((Constructor) accessibleObject);
    } else {
      return TypedOperation.forMethod((Method) accessibleObject);
    }
  }
}
