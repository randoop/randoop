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
   * Creates a {@link PropertyExpression} that calls the given expression method.
   *
   * @param expressionMethod the reflection object for the expression method
   * @param comment the comment describing this expression
   * @param contractText the text for this expression
   */
  PropertyExpression(Method expressionMethod, String comment, String contractText) {
    super(expressionMethod, comment, contractText);
  }

  /**
   * Creates a {@link PropertyExpression} for evaluating the property (see {@link
   * randoop.condition.specification.Property}) of a {@link
   * randoop.condition.specification.PostSpecification}.
   *
   * @param signature the signature for the expression method to be created. The class name of the
   *     expression method signature is ignored and a new name is generated using {@link
   *     #nameGenerator}.
   * @param declarations the parameter declaration string for the expression method to be created,
   *     including parameter names and wrapped in parentheses
   * @param expressionSource the source code for a Java expression to be used as the body of the
   *     expression method
   * @param contractSource a Java expression that is the source code for the expression, in the
   *     format of {@link BooleanExpression#getContractSource()}
   * @param comment the comment describing the expression
   * @param compiler the compiler to used to compile the expression method
   * @return the {@link BooleanExpression} that evaluates the given expression source on parameters
   *     described by the declaration string
   */
  static PropertyExpression createPropertyExpression(
      RawSignature signature,
      String declarations,
      String expressionSource,
      String contractSource,
      String comment,
      SequenceCompiler compiler) {
    Method expressionMethod =
        BooleanExpression.createMethod(signature, declarations, expressionSource, compiler);
    return new PropertyExpression(expressionMethod, comment, contractSource);
  }

  /**
   * Returns the {@link PropertyExpression} that checks the expression with the given argument
   * values as the pre-state.
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
   * @param returnType the return type for the method, or the declaring class for a constructor
   * @return the constructed property expression method signature
   */
  static RawSignature getRawSignature(
      String packageName, Class<?> receiverType, Class<?>[] parameterTypes, Class<?> returnType) {
    int shift = (receiverType != null) ? 1 : 0;
    Class<?>[] expressionParameterTypes =
        new Class<?>[parameterTypes.length + shift + (returnType != null ? 1 : 0)];
    if (receiverType != null) {
      expressionParameterTypes[0] = receiverType;
    }
    System.arraycopy(parameterTypes, 0, expressionParameterTypes, shift, parameterTypes.length);
    if (returnType != null) {
      expressionParameterTypes[expressionParameterTypes.length - 1] = returnType;
    }
    return new RawSignature(
        packageName, EXPRESSION_CLASS_NAME, "ClassNameWillBeReplaced", expressionParameterTypes);
  }
}
