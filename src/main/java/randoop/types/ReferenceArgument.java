package randoop.types;

import java.lang.reflect.*;
import java.util.List;
import java.util.Objects;

/**
 * Represents a reference type as a type argument to a parameterized type.
 * (See JLS, 4.5.1)
 */
public class ReferenceArgument extends TypeArgument {

  /** The reference type for this argument */
  private final ReferenceType referenceType;

  /**
   * Creates a {@code ReferenceArgument} for the given {@link ReferenceType}.
   *
   * @param referenceType  the {@link ReferenceType}
   */
  ReferenceArgument(ReferenceType referenceType) {
    this.referenceType = referenceType;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ReferenceArgument)) {
      return false;
    }
    ReferenceArgument referenceArgument = (ReferenceArgument) obj;
    return this.referenceType.equals(referenceArgument.referenceType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(referenceType);
  }

  @Override
  public String toString() {
    return referenceType.toString();
  }

  /**
   * Get the reference type for this type argument.
   *
   * @return the reference type of this type argument
   */
  public ReferenceType getReferenceType() {
    return referenceType;
  }

  @Override
  public ReferenceArgument apply(Substitution<ReferenceType> substitution) {
    return new ReferenceArgument(referenceType.apply(substitution));
  }

  /**
   * {@inheritDoc}
   * Considers cases:
   * <ul>
   *   <li>{@code T} contains {@code T}</li>
   *   <li>{@code T} contains {@code ? extends T}</li>
   *   <li>{@code T} contains {@code ? super T}</li>
   * </ul>
   */
  @Override
  public boolean contains(TypeArgument otherArgument) {
    if (otherArgument.isWildcard()) {
      return referenceType.equals(((WildcardArgument) otherArgument).getBoundType());
    } else {
      return referenceType.equals(((ReferenceArgument) otherArgument).getReferenceType());
    }
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    return referenceType.getTypeParameters();
  }

  public boolean isCaptureVariable() {
    return referenceType.isCaptureVariable();
  }
  /**
   * Indicates whether a {@code ReferenceArgument} is generic.
   *
   * @return true if the {@link ReferenceType} is generic, false otherwise
   */
  @Override
  public boolean isGeneric() {
    return referenceType.isGeneric();
  }

  /**
   * Creates a {@code ReferenceArgument} from the given type.
   *
   * @param type  the type
   * @return a {@code ReferenceArgument} for the given type
   */
  public static ReferenceArgument forType(Type type) {
    return new ReferenceArgument(ReferenceType.forType(type));
  }

  /**
   * {@inheritDoc}
   * Reference types must unify, meaning, e.g., if {@code referenceType} is an
   * {@link ArrayType}, then {@code generalType} must be an {@link ArrayType}.
   * If {@code referenceType} is a {@link TypeVariable}, then {@code generalType}
   * has to satisfy the variables bounds.
   */
  @Override
  public boolean canBeInstantiatedAs(
      GeneralType generalType, Substitution<ReferenceType> substitution) {
    if (referenceType.equals(generalType)) {
      return true;
    }

    if (referenceType.isGeneric()) {
      //  - array type has to be instantiation
      if (referenceType instanceof ArrayType) {
        if (!generalType.isArray()) {
          return false;
        }
        // generalType.getElementType() has to instantiate referenceType.getElementType()
        return false;
      }

      //  - class or interface has to be instantiation
      if (referenceType instanceof ParameterizedType) {
        if (!(generalType instanceof ParameterizedType)) {
          return false;
        }
        // type arguments must instantiate type arguments
        return false;
      }

      //  - type variable bounds have to be satisfied
      if (referenceType instanceof TypeVariable) {
        return ((TypeVariable) referenceType)
            .getUpperTypeBound()
            .isSatisfiedBy(generalType, substitution);
      }
    }

    return false;
  }
}
