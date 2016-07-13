package randoop.types;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a type argument of a parameterized type as described in
 * JLS (Section 4.5.1).
 * A type argument may be either a reference type or a wildcard.
 */
public abstract class TypeArgument {

  /**
   * Applies the type substitution to this type argument.
   *
   * @param substitution  the substitution
   * @return a version of this type argument with type variables replaced by the substitution
   */
  public abstract TypeArgument apply(Substitution<ReferenceType> substitution);

  /**
   * Checks whether this type argument contains another argument, using relationship defined in
   * section 4.5.1 of JLS.
   *
   * @param otherArgument  the other {@code TypeArgument}
   * @return true if this argument contains the other argument
   */
  public abstract boolean contains(TypeArgument otherArgument);

  /**
   * Indicates whether this type argument is generic.
   *
   * @return true if this type argument is generic, false otherwise
   */
  public abstract boolean isGeneric();

  /**
   * Converts a {@code java.lang.reflect.Type} to a {@code TypeArgument}
   * object.
   *
   * @param type  the type of a type argument
   * @return the {@code TypeArgument} for the given type
   */
  public static TypeArgument forType(Type type) {
    if (type instanceof WildcardType) {
      return WildcardArgument.forType(type);
    } else {
      return ReferenceArgument.forType(type);
    }
  }

  /**
   * Indicates whether this type argument can be instantiated by the given
   * {@link GeneralType}.
   *
   * @param generalType  the type to test
   * @param substitution the substitution for this type (? do we need this?)
   * @return true if the type can instantiate this argument, false otherwise
   */
  public abstract boolean canBeInstantiatedAs(
      GeneralType generalType, Substitution<ReferenceType> substitution);

  /**
   * Indicate whether this type argument is a wildcard argument.
   *
   * @return true if this is a wildcard argument, false otherwise
   */
  public boolean isWildcard() {
    return false;
  }

  /**
   * Indicates whether this type argument is a capture variable as the result of a capture
   * conversion constructed by {@link InstantiatedType#applyCaptureConversion()}.
   *
   * @return true if this argument is a capture variable, or false otherwise
   */
  boolean isCaptureVariable() {
    return false;
  }

  /**
   * The type parameters for this type argument.
   *
   * @return the list of type parameters for this argument
   */
  public List<TypeVariable> getTypeParameters() {
    return new ArrayList<>();
  }
}
