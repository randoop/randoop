package randoop.input.toradocu;

/**
 * Modified class from Toradocu used to deserialize the JSON output of Toradocu.
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * DocumentedMethod represents the Javadoc documentation for a method in a class. It identifies the
 * method itself and key Javadoc information associated with it, such as throws tags, parameters,
 * and return type.
 */
public final class DocumentedMethod {

  /** Class in which the method is contained. */
  private final Type containingClass;
  /** Simple name of the method. */
  private final String name;
  /**
   * Return type of the method. {@code null} if this DocumentedMethod represents a constructor.
   * {@code Type.VOID} if the return type is void.
   */
  private final Type returnType;
  /** Method's parameters. */
  private final List<Parameter> parameters;
  /** Param tags specified in the method's Javadoc introduced in order. */
  private final Set<ParamTag> paramTags;
  /** Flag indicating whether this method takes a variable number of arguments. */
  private final boolean isVarArgs;
  /**
   * Throws tags specified in the method's Javadoc. Also, each throws tag can contain the
   * translation of the comment as Java boolean condition.
   */
  private final Set<ThrowsTag> throwsTags;
  /** Method signature in the format method_name(type1 arg1, type2 arg2, ...). */
  private final String signature;

  /** Target class passed to Toradocu with the option --target-class. */
  private final String targetClass;

  /**
   * Constructs a {@code DocumentedMethod} contained in a given {@code containingClass} with the
   * given {@code name}, {@code returnType}, {@code parameters}, and {@code throwsTags}.
   *
   * @param containingClass class containing the {@code DocumentedMethod}
   * @param name the simple name of the {@code DocumentedMethod}
   * @param returnType the fully qualified return type of the method or {@code null} if the {@code
   *     DocumentedMethod} is a constructor
   * @param parameters the parameters of the {@code DocumentedMethod}
   * @param paramTags the {@code @param tags} of the {@code DocumentedMethod}
   * @param isVarArgs true if the {@code DocumentedMethod} takes a variable number of arguments,
   *     false otherwise
   * @param throwsTags the {@code @throws tags} of the {@code DocumentedMethod}
   * @throws NullPointerException if {@code containingClass} or {@code name} is null
   */
  public DocumentedMethod(
      Type containingClass,
      String name,
      Type returnType,
      List<Parameter> parameters,
      Collection<ParamTag> paramTags,
      boolean isVarArgs,
      Collection<ThrowsTag> throwsTags,
      String signature,
      String targetClass) {

    this.containingClass = containingClass;
    this.name = name;
    this.returnType = returnType;
    this.parameters = parameters == null ? new ArrayList<Parameter>() : new ArrayList<>(parameters);
    this.paramTags =
        paramTags == null ? new LinkedHashSet<ParamTag>() : new LinkedHashSet<>(paramTags);
    this.isVarArgs = isVarArgs;
    this.throwsTags =
        throwsTags == null ? new LinkedHashSet<ThrowsTag>() : new LinkedHashSet<>(throwsTags);
    this.signature = signature;
    this.targetClass = targetClass;
  }

  /**
   * Returns an unmodifiable view of the throws tags in this method.
   *
   * @return an unmodifiable view of the throws tags in this method
   */
  public Set<ThrowsTag> throwsTags() {
    return Collections.unmodifiableSet(throwsTags);
  }

  /**
   * Returns an unmodifiable view of the param tags in this method.
   *
   * @return an unmodifiable view of the param tags in this method.
   */
  public Set<ParamTag> paramTags() {
    return Collections.unmodifiableSet(paramTags);
  }

  /**
   * Returns true if this method takes a variable number of arguments, false otherwise.
   *
   * @return {@code true} if this method takes a variable number of arguments, {@code false}
   *     otherwise
   */
  public boolean isVarArgs() {
    return isVarArgs;
  }

  /**
   * Returns the simple name of this method.
   *
   * @return the simple name of this method
   */
  public String getName() {
    return name;
  }

  /**
   * Returns true if this method is a constructor, false otherwise.
   *
   * @return {@code true} if this method is a constructor, {@code false} otherwise
   */
  public boolean isConstructor() {
    return returnType == null;
  }

  /**
   * Returns an unmodifiable list view of the parameters in this method.
   *
   * @return an unmodifiable list view of the parameters in this method
   */
  public List<Parameter> getParameters() {
    return Collections.unmodifiableList(parameters);
  }

  /**
   * Returns the signature of this method.
   *
   * @return the signature of this method
   */
  public String getSignature() {
    return signature;
  }

  /**
   * Returns the return type of this method or null if this is a constructor.
   *
   * @return the return type of this method or {@code null} if this is a constructor
   */
  public Type getReturnType() {
    return returnType;
  }

  /**
   * Returns the class in which this method is contained.
   *
   * @return the class in which this method is contained
   */
  public Type getContainingClass() {
    return containingClass;
  }

  /**
   * Returns true if this {@code DocumentedMethod} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof DocumentedMethod)) return false;

    DocumentedMethod that = (DocumentedMethod) obj;
    return this.containingClass.equals(that.containingClass)
        && this.name.equals(that.name)
        && Objects.equals(this.returnType, that.returnType)
        && this.parameters.equals(that.parameters)
        && this.isVarArgs == that.isVarArgs
        && this.throwsTags.equals(that.throwsTags)
        && this.paramTags.equals(that.paramTags)
        && this.signature.equals(that.signature)
        && this.targetClass.equals(that.targetClass);
  }

  /**
   * Returns the target class as specified with the command line option --target-class.
   *
   * @return the target class as specified with the command line option --target-class. Null if the
   *     command line options have not been parsed.
   */
  public String getTargetClass() {
    return targetClass;
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(containingClass, name, returnType, parameters, paramTags, throwsTags);
  }

  /**
   * Returns return type (when present), fully qualified class, and signature of this method in the
   * format "&lt;RETURN TYPE&gt; &lt;CLASS TYPE.METHOD SIGNATURE&gt;"
   *
   * @return return the string representation of this method
   */
  @Override
  public String toString() {
    StringBuilder methodAsString = new StringBuilder();
    if (returnType != null) {
      methodAsString.append(returnType + " ");
    }
    methodAsString.append(containingClass + Type.SEPARATOR + signature);
    return methodAsString.toString();
  }
}
