package randoop.reflection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses type signature strings used to identify methods and constructors in input. */
// TODO: This duplicates should be factored into a separate source set (aka, module) so that it
// can also be used in javagents. The patterns are duplicated from {@code ReplacementFileReader}
// from the mapcall agent.
public class SignatureParser {
  /** Regex for Java identifiers */
  private static final String ID_STRING =
      "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

  /**
   * Regex to match a sequence of identifiers (or {@code <init>}) separated by periods. Corresponds
   * to package names, fully-qualified classnames, or method names with fully-qualified classname.
   */
  private static final String DOT_DELIMITED_IDS =
      ID_STRING + "(?:\\." + ID_STRING + ")*" + "(?:\\.<init>)?";

  /**
   * Naive regex to match a method signature consisting of a fully-qualified method name followed by
   * anything in parentheses. The parentheses are expected to contain argument types, but the
   * pattern permits anything.
   *
   * <p>Capturing group 1 matches the fully-qualified method name, and capturing group 2 matches the
   * contents of the parentheses.
   */
  private static final Pattern SIGNATURE_PATTERN =
      Pattern.compile("(" + DOT_DELIMITED_IDS + ")\\(([^)]*)\\)");

  /**
   * Parses a fully-qualified signature and returns the corresponding {@code
   * java.lang.reflect.AccessibleObject}.
   *
   * <p>A signature is expected to have the form
   *
   * <ul>
   *   <li>{@code package-name.classname.method-name(argument-list)} for a method,
   *   <li>{@code package-name.classname.&lt;init&gt;(argument-list)} or {@code
   *       package-name.classname(argument-list)} for a constructor.
   * </ul>
   *
   * <p>where {@code package-name} is a period-separated list of identifiers, and <code>
   * argument-list</code> is a comma-separated (spaces-allowed) list of fully-qualified Java raw
   * types. Array types have the format <code>element-type[]</code>.
   *
   * @param signature the string to parse: a signature string for a method or constructor, in the
   *     above format
   * @param visibility the predicate for determining whether the method or constructor is visible
   * @param reflectionPredicate the predicate for checking reflection policy
   * @return the {@code AccessibleObject} for the method or constructor represented by the string
   * @throws IllegalArgumentException if the string does not have the format of a signature
   * @throws SignatureParseException if the signature is not fully-qualified, or the class, an
   *     argument type, or the method or constructor is not found using reflection
   */
  public static AccessibleObject parse(
      String signature, VisibilityPredicate visibility, ReflectionPredicate reflectionPredicate)
      throws SignatureParseException {
    Matcher signatureMatcher = SIGNATURE_PATTERN.matcher(signature);
    if (!signatureMatcher.matches()) {
      throw new IllegalArgumentException("Method signature expected: " + signature);
    }

    String qualifiedName = signatureMatcher.group(1);
    String argString = signatureMatcher.group(2);
    String[] arguments;
    if (argString.isEmpty()) {
      arguments = new String[0];
    } else {
      arguments = argString.split("\\s*,\\s*");
    }

    /*
     * The qualified name is one of
     *   package-name.class-name for a constructor
     *   package-name.class-name.<init> for a constructor (reflection notation)
     *   package-name.class-name.method-name for a method
     * Now, parse it.
     */
    String name;
    String qualifiedClassname;
    int dotPos = qualifiedName.lastIndexOf('.');
    if (dotPos > 0) {
      name = qualifiedName.substring(dotPos + 1);
      qualifiedClassname = qualifiedName.substring(0, dotPos);
    } else {
      throw new SignatureParseException("Fully-qualified name expected: " + qualifiedName);
    }

    // Check whether signature has constructor reflection format
    boolean isConstructor = name.equals("<init>");

    /*
     * The qualifiedClassname is either package-name.class-name, or package-name if the signature is
     * a constructor not represented as "<init>".
     */
    Class<?> classType;
    try {
      classType = Class.forName(qualifiedClassname);
    } catch (ClassNotFoundException first) {
      // could be that qualified name is package-name.class-name
      try {
        classType = Class.forName(qualifiedName);
        isConstructor = true;
      } catch (ClassNotFoundException e) {
        throw new SignatureParseException("Class not found for signature " + signature);
      }
    }

    // Can't use the method if the class is non-visible
    if (!visibility.isVisible(classType)) {
      System.out.println("Ignoring signature " + signature + " from non-visible " + classType);
      return null;
    }

    Class<?>[] argTypes = new Class<?>[arguments.length];
    for (int i = 0; i < arguments.length; i++) {
      try {
        argTypes[i] = TypeNames.getTypeForName(arguments[i]);
      } catch (ClassNotFoundException e) {
        throw new SignatureParseException(
            "Argument type \"" + arguments[i] + "\" not recognized in signature " + signature);
      }
    }

    if (isConstructor) {
      Constructor<?> constructor;
      try {
        constructor = classType.getConstructor(argTypes);
      } catch (NoSuchMethodException e) {
        throw new SignatureParseException("Constructor not found for signature " + signature);
      }
      if (reflectionPredicate.test(constructor)) {
        return constructor;
      }
    } else { // Otherwise, signature is a method
      Method method;
      try {
        method = classType.getMethod(name, argTypes);
      } catch (NoSuchMethodException e) {
        throw new SignatureParseException("Method not found for signature: " + signature);
      }
      if (reflectionPredicate.test(method)) {
        return method;
      }
    }
    return null;
  }
}
