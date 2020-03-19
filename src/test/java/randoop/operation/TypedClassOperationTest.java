package randoop.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OmitMethodsPredicate;
import randoop.reflection.OperationExtractor;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.VisibilityPredicate;
import randoop.reflection.omitinputs.p.C;
import randoop.types.ClassOrInterfaceType;

public class TypedClassOperationTest {
  @Test
  public void testOperationForType() {
    ClassOrInterfaceType cType = ClassOrInterfaceType.forClass(C.class);
    Set<TypedOperation> operations = getOperations(cType);

    for (TypedOperation operation : operations) {
      TypedClassOperation classOperation = (TypedClassOperation) operation;
      final TypedClassOperation operationForType = classOperation.getOperationForType(cType);
      String expectedSignatureString = getExpectedSignature(classOperation, cType);
      assertEquals(classOperation.getOperation(), operationForType.getOperation());
      if (classOperation.isConstructorCall()) {
        assertTrue(operationForType.isConstructorCall());
        String signatureString = operationForType.getRawSignature().toString();
        assertEquals(expectedSignatureString, signatureString);
      }
      if (classOperation.isMethodCall()) {
        assertTrue(operationForType.isMethodCall());
        String signatureString = operationForType.getRawSignature().toString();
        assertEquals(expectedSignatureString, signatureString);
      }
    }
  }

  /**
   * Signature from {@link TypedClassOperation#getOperationForType(ClassOrInterfaceType)} should be
   * the same for constructors but should have type substituted for methods.
   *
   * @param operation the operation to get the expected signature for
   * @param type the substituted type
   * @return the String for the signature of the operation with declaring class substituted
   */
  private String getExpectedSignature(TypedClassOperation operation, ClassOrInterfaceType type) {
    String signature = operation.getRawSignature().toString();
    if (operation.isMethodCall() && !operation.getDeclaringType().equals(type)) {
      signature =
          signature.replace(operation.getDeclaringType().getBinaryName(), type.getBinaryName());
    }
    return signature;
  }

  private Set<TypedOperation> getOperations(ClassOrInterfaceType type) {
    OmitMethodsPredicate omitMethodsPredicate = OmitMethodsPredicate.NO_OMISSION;
    VisibilityPredicate visibility =
        new VisibilityPredicate.PackageVisibilityPredicate("randoop.reflection");
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();
    Collection<TypedOperation> oneClassOperations =
        OperationExtractor.operations(type, reflectionPredicate, omitMethodsPredicate, visibility);
    return new TreeSet<>(oneClassOperations);
  }
}
