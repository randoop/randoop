package randoop.condition;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import plume.UtilMDE;
import randoop.condition.specification.Identifiers;
import randoop.sequence.Variable;

/** Represents the signature of a condition method for a particular {@code AccessibleObject}. */
public class ConditionSignature {

  /** The name of dummy variables used by {@link randoop.contract.ObjectContract}. */
  private static final String DUMMY_VARIABLE_NAME = "x";

  /** The map of variable name replacements */
  private final Map<String, String> replacements;

  /** The signature string for a precondition */
  private String preconditionSignature;

  /** The signature string for a post-condition */
  private String postconditionSignature;

  /** The package name for the condition method */
  private String packageName;

  /**
   * Creates a {@link ConditionSignature} object in the given package with the signature strings and
   * variable replacements.
   *
   * @param packageName the package name for the condition
   * @param preconditionSignature the signature string for a precondition
   * @param postconditionSignature the signature
   * @param replacements the map of variable replacements
   */
  private ConditionSignature(
      String packageName,
      String preconditionSignature,
      String postconditionSignature,
      Map<String, String> replacements) {
    this.packageName = packageName;
    this.preconditionSignature = preconditionSignature;
    this.postconditionSignature = postconditionSignature;
    this.replacements = replacements;
  }

  /**
   * Creates an object representing the condition method signature(s) for the given {@code
   * AccessibleObject} using the identifiers from a {@link
   * randoop.condition.specification.OperationSpecification}.
   *
   * @param object the method or constructor as an {@code AccessibleObject}
   * @param identifiers the {@link Identifiers} for for the signature created
   * @return the {@link ConditionSignature} object for the method using the identifiers
   */
  public static ConditionSignature create(AccessibleObject object, Identifiers identifiers) {
    int varIndex = 0;
    String packageName = "";
    Class<?>[] parameterTypes;
    Class<?> returnType;
    List<String> paramList = new ArrayList<>();
    Map<String, String> replacements = new HashMap<>();
    if (object instanceof Method) {
      Method method = (Method) object;
      packageName = method.getDeclaringClass().getPackage().getName();
      paramList.add(
          method.getDeclaringClass().getCanonicalName() + " " + identifiers.getReceiverName());
      if (!Modifier.isStatic(method.getModifiers() & Modifier.methodModifiers())) {
        replacements.put(identifiers.getReceiverName(), DUMMY_VARIABLE_NAME + varIndex++);
      }
      parameterTypes = method.getParameterTypes();
      returnType = method.getReturnType();
    } else if (object instanceof Constructor) {
      Constructor<?> constructor = (Constructor<?>) object;
      packageName = constructor.getDeclaringClass().getPackage().getName();
      parameterTypes = constructor.getParameterTypes();
      returnType = constructor.getDeclaringClass();
    } else {
      return null;
    }
    if (packageName.startsWith("java.")) {
      packageName = "randoop." + packageName;
    }
    for (int i = 0; i < parameterTypes.length; i++) {
      paramList.add(
          parameterTypes[i].getCanonicalName() + " " + identifiers.getParameterNames().get(i));
      replacements.put(identifiers.getParameterNames().get(i), DUMMY_VARIABLE_NAME + varIndex++);
    }
    String preconditionSignature = "(" + UtilMDE.join(paramList, ", ") + ")"; //receiver, params
    paramList.add(returnType.getCanonicalName() + " " + identifiers.getReturnName());
    replacements.put(identifiers.getReturnName(), DUMMY_VARIABLE_NAME + varIndex);
    String postconditionSignature =
        "(" + UtilMDE.join(paramList, ", ") + ")"; //receiver, params, result
    return new ConditionSignature(
        packageName, preconditionSignature, postconditionSignature, replacements);
  }

  /**
   * Returns the package name for the condition methods.
   *
   * @return the package name of the condition methods
   */
  String getPackageName() {
    return packageName;
  }

  /**
   * Returns the pre-condition method signature from this object. If this object is a method, this
   * signature includes the receiver type, and parameter types. If this object is a constructor,
   * this signature includes the parameter types.
   *
   * @return the pre-condition signature for this object
   */
  String getPreConditionSignature() {
    return preconditionSignature;
  }

  /**
   * Returns the post-condition method signature from this object. This signature includes the
   * parameters of the pre-condition signature, plus the return type.
   *
   * @return the post-condition signature for this object
   */
  String getPostConditionSignature() {
    return postconditionSignature;
  }

  /**
   * Replaces occurrences of the identifiers from this signature in the given condition text with
   * dummy identifiers corresponding the the argument order.
   *
   * @see randoop.contract.ObjectContractUtils#localizeContractCode(String, Variable...)
   * @param conditionText the Java Boolean expression text over the variables of this signature
   * @return the conditionText with
   */
  String replaceWithDummyVariables(String conditionText) {
    // make sure that we are replacing from longer to shorter strings to avoid mangled replacement
    Set<String> names = new TreeSet<>(new LengthComparator());
    names.addAll(replacements.keySet());
    for (String name : names) {
      String namePattern = "\\b" + name + "\\b";
      conditionText = conditionText.replaceAll(namePattern, replacements.get(name));
    }
    return conditionText;
  }

  /** Comparator to order strings by decreasing length. */
  private static class LengthComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
      if (o1.length() < o2.length()) {
        return 1; // shorter last
      } else if (o1.length() > o2.length()) {
        return -1; // longer first
      }
      return o1.compareTo(o2);
    }
  }
}
