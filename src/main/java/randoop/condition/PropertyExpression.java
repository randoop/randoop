package randoop.condition;

import java.lang.reflect.Method;
import randoop.compile.SequenceCompiler;
import randoop.reflection.RawSignature;

/**
 * The executable form of a {@link randoop.condition.specification.Property}.
 *
 * <p>Though a property may refer to pre-state, at the moment this just behaves as a {@link
 * BooleanExpression} that is applied to the post-state.
 */
public class PropertyExpression extends BooleanExpression {

  /**
   * Creates a {@link PropertyExpression} that calls the given condition method.
   *
   * @param conditionMethod the reflection object for the condition method
   * @param comment the comment describing this condition
   * @param conditionText the text for this condition
   */
  PropertyExpression(Method conditionMethod, String comment, String conditionText) {
    super(conditionMethod, comment, conditionText);
  }

  public PropertyExpression createPropertyExpression(
      RawSignature signature,
      String declarations,
      String expressionSource,
      String contractSource,
      String comment,
      SequenceCompiler compiler) {
    Method conditionMethod =
        BooleanExpression.createMethod(signature, declarations, expressionSource, compiler);

    return new PropertyExpression(conditionMethod, comment, contractSource);
  }

  /**
   * Returns the {@link PropertyExpression} that checks the condition with the given argument values
   * as the pre-state.
   *
   * <p>Since pre-state is not yet implemented, this method just returns this object.
   *
   * @param args the pre-state values to the arguments
   * @return the {@link PropertyExpression} with the pre-state set
   */
  PropertyExpression addPrestate(Object[] args) {
    return this;
  }

  /**
   * Creates a {@link RawSignature} for the expression method of the the {@link PropertyExpression}
   * for a {@link randoop.condition.specification.Property}.
   *
   * <p>The parameter types for the expression have the receiver type first, followed by the
   * parameter types. method parameter types are as given.
   *
   * <p>Note that these signatures may be used more than once for different expression methods, and
   * so {@link #createMethod(RawSignature, String, String, SequenceCompiler)} replaces the classname
   * to ensure a unique name.
   *
   * @param packageName the package name for the expression class
   * @param receiverType the declaring class of the method or constructor, used as receiver type if
   *     {@code firstArgumentIsReceiver} is true
   * @param parameterTypes the parameter types for the original method or constructor
   * @param returnType the return type for the method, or the declaring class for a constructor, or
   *     null if constructing a {@code RawSignature} for a precondition
   * @return the constructed post-expression method signature
   */
  static RawSignature getRawSignature(
      String packageName, Class<?> receiverType, Class<?>[] parameterTypes, Class<?> returnType) {
    int shift = 1;
    Class<?>[] expressionParameterTypes =
        new Class<?>[parameterTypes.length + shift + (returnType != null ? 1 : 0)];
    expressionParameterTypes[0] = receiverType;
    if (returnType != null) {
      expressionParameterTypes[expressionParameterTypes.length - 1] = returnType;
    }
    System.arraycopy(parameterTypes, 0, expressionParameterTypes, shift, parameterTypes.length);
    return new RawSignature(
        packageName, EXPRESSION_CLASS_NAME, "ClassNameWillBeReplaced", expressionParameterTypes);
  }
}
