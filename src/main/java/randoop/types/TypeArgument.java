package randoop.types;

import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a type argument of a parameterized type as described in <a
 * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.5.1">JLS Section
 * 4.5.1</a>.
 *
 * <pre>
 *   TypeArgument:
 *     ReferenceType
 *     Wildcard
 * </pre>
 *
 * @see ReferenceArgument
 * @see WildcardArgument
 */
public abstract class TypeArgument {

  /**
   * Converts a {@code java.lang.reflect.Type} to a {@code TypeArgument} object.
   *
   * @param type the type of a type argument
   * @return the {@code TypeArgument} for the given type
   */
  public static TypeArgument forType(java.lang.reflect.Type type) {
    if (type instanceof WildcardType) {
      return WildcardArgument.forType(type);
    } else {
      return ReferenceArgument.forType(type);
    }
  }

  public static TypeArgument forType(ReferenceType referenceType) {
    if (referenceType instanceof randoop.types.WildcardType) {
      return WildcardArgument.forType(referenceType);
    }
    return ReferenceArgument.forType(referenceType);
  }

  /**
   * Applies the type substitution to this type argument.
   *
   * @param substitution the substitution
   * @return a version of this type argument with type variables replaced by the substitution
   */
  public abstract TypeArgument substitute(Substitution substitution);

  /**
   * Returns true if this type argument contains another argument, using relationship defined in <a
   * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.5.1">JLS Section
   * 4.5.1</a>.
   *
   * @param otherArgument the other {@code TypeArgument}
   * @return true if this argument contains the other argument
   */
  public abstract boolean contains(TypeArgument otherArgument);

  /**
   * The type parameters for this type argument.
   *
   * @return the list of type parameters for this argument
   */
  public List<TypeVariable> getTypeParameters() {
    return new ArrayList<>();
  }

  /**
   * Returns true if this type argument has a wildcard.
   *
   * @return true if this argument has a wildcard argument
   */
  public boolean hasWildcard() {
    return false;
  }

  /**
   * Returns true if this type argument has a capture variable.
   *
   * @return true if this argument has a capture variable
   */
  public boolean hasCaptureVariable() {
    return false;
  }

  /**
   * Returns true if this type argument is generic.
   *
   * @return true if this type argument is generic, false otherwise
   */
  public final boolean isGeneric() {
    return isGeneric(false);
  }

  /**
   * Returns true if this type argument is generic.
   *
   * @param ignoreWildcards if true, ignore wildcards; that is, treat wildcards as not making the
   *     operation generic
   * @return true if this type argument is generic, false otherwise
   */
  public abstract boolean isGeneric(boolean ignoreWildcards);

  /**
   * Returns true if this type argument is an instantiation of the other argument.
   *
   * @param otherArgument the other argument
   * @return true if this type is an instantiation of the other argument, false otherwise
   * @see InstantiatedType#isInstantiationOf(ReferenceType)
   */
  boolean isInstantiationOfTypeArgument(TypeArgument otherArgument) {
    return false;
  }

  /**
   * Returns true if this type argument is a wildcard argument.
   *
   * @return true if this is a wildcard argument, false otherwise
   */
  public boolean isWildcard() {
    return false;
  }

  /**
   * Returns a unifying substitution. Returns null if unification failed.
   *
   * @param goalType the generic type for which a substitution is needed
   * @return a substitution unifying this type or a supertype of this type with the goal type, or
   *     null if unification failed
   */
  public @Nullable Substitution getInstantiatingSubstitution(TypeArgument goalType) {
    // This implementation indicates failure.  It is overridden by subclasses.
    return null;
  }

  /**
   * Returns true if this type argument is a type variable.
   *
   * @return true if this argument is a type variable, false otherwise
   */
  public abstract boolean isVariable();

  /**
   * Returns the fully-qualified name.
   *
   * @return the fully-qualified name
   */
  public abstract String getFqName();

  /**
   * Returns the binary name.
   *
   * @return the binary name
   */
  public abstract String getBinaryName();
}
