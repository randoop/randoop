package randoop.reflection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import randoop.types.Type;

/** Parses type signature strings used to identify methods and constructors in input. */
public class SignatureParser {

  // TODO: The duplicated regular expressions should be factored into a separate source set (aka,
  // module) so that it can also be used in javagents. The patterns are duplicated from {@code
  // ReplacementFileReader} from the replacecall agent.

  /** Regex for Java identifiers. */
  public static final String ID_STRING = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

  /**
   * Regex to match a sequence of identifiers (or {@code <init>}) separated by periods. Corresponds
   * to package names, fully-qualified classnames, or method names with fully-qualified classname.
   */
  public static final String DOT_DELIMITED_IDS =
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
   *   <li>{@code package-name.classname.<init>(argument-list)} or {@code
   *       package-name.classname(argument-list)} for a constructor.
   * </ul>
   *
   * <p>where {@code package-name} is a period-separated list of identifiers, and {@code
   * argument-list} is a comma-separated (spaces-allowed) list of fully-qualified Java raw types.
   * Array types have the format {@code element-type[]}.
   *
   * @param signature the string to parse: a signature string for a method or constructor, in the
   *     above format
   * @param visibility the predicate for determining whether the method or constructor is visible
   * @param reflectionPredicate the predicate for checking reflection policy
   * @return the {@code AccessibleObject} for the method or constructor represented by the string;
   *     null if the visibility or reflection predicate returns false on the class or the method or
   *     constructor
   * @throws IllegalArgumentException if the string does not have the format of a signature
   * @throws SignatureParseException if the signature is not fully-qualified, or the class, an
   *     argument type, or the method or constructor is not found using reflection
   */
  @SuppressWarnings("signature") // parsing
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
     * The qualified name should be one of
     *   package-name.class-name for a constructor
     *   package-name.class-name.<init> for a constructor (reflection notation)
     *   package-name.class-name.method-name for a method
     * Now, parse it.
     */
    int dotPos = qualifiedName.lastIndexOf('.');
    if (dotPos <= 0) {
      throw new SignatureParseException("Fully-qualified name expected: " + qualifiedName);
    }
    String name = qualifiedName.substring(dotPos + 1);
    String qualifiedClassname = qualifiedName.substring(0, dotPos);

    // Check whether signature has constructor reflection format
    boolean isConstructor = name.equals("<init>");

    /*
     * The qualifiedClassname is either package-name.class-name, or package-name if the signature is
     * a constructor not represented as "<init>".
     */
    Class<?> clazz;
    try {
      clazz = Type.forFullyQualifiedName(qualifiedClassname);
    } catch (ClassNotFoundException first) {
      // could be that qualified name is package-name.class-name
      try {
        clazz = Type.forFullyQualifiedName(qualifiedName);
        isConstructor = true;
      } catch (ClassNotFoundException e) {
        throw new SignatureParseException(
            "Class not found for method or constructor "
                + qualifiedName
                + " in signature "
                + signature,
            e);
      }
    }

    // Can't use the method if the class is non-visible
    if (!visibility.isVisible(clazz)) {
      System.out.println("Ignoring signature " + signature + " from non-visible " + clazz);
      return null;
    }

    Class<?>[] argTypes = new Class<?>[arguments.length];
    for (int i = 0; i < arguments.length; i++) {
      try {
        argTypes[i] = Type.forFullyQualifiedName(arguments[i]);
      } catch (ClassNotFoundException e) {
        throw new SignatureParseException(
            "Argument type \"" + arguments[i] + "\" not recognized in signature " + signature, e);
      }
    }

    if (isConstructor) {
      Constructor<?> constructor;
      try {
        constructor = clazz.getConstructor(argTypes);
      } catch (NoSuchMethodException e) {
        throw new SignatureParseException(
            "Class " + clazz + " found, but constructor not found for signature " + signature, e);
      }
      if (reflectionPredicate.test(constructor) && visibility.isVisible(constructor)) {
        return constructor;
      }
    } else { // Otherwise, signature is a method
      Method method;
      try {
        method = clazz.getMethod(name, argTypes);
      } catch (NoSuchMethodException e) {
        StringBuilder b = new StringBuilder();
        b.append(
            String.format(
                "Class %s found, but method %s(%s) not found for signature %s%n",
                clazz, name, Arrays.toString(argTypes), signature));
        b.append("Here are the declared methods:%n");
        for (Method m : clazz.getDeclaredMethods()) {
          b.append(String.format("  %s%n", m));
        }
        throw new SignatureParseException(b.toString(), e);
      }
      if (reflectionPredicate.test(method) && visibility.isVisible(method)) {
        return method;
      }
    }
    return null;
  }
}
