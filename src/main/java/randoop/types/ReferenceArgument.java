package randoop.types;

import java.util.List;
import java.util.Objects;

/**
 * Represents a reference type as a type argument to a parameterized type. (See <a
 * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.5.1">JLS Section
 * 4.5.1</a>.)
 */
public class ReferenceArgument extends TypeArgument {

  /** The reference type for this argument. */
  private final ReferenceType referenceType;

  /**
   * Creates a {@code ReferenceArgument} for the given {@link ReferenceType}.
   *
   * @param referenceType the {@link ReferenceType}
   */
  private ReferenceArgument(ReferenceType referenceType) {
    this.referenceType = referenceType;
  }

  /**
   * Creates a {@code ReferenceArgument} from the given type.
   *
   * @param type the type
   * @return a {@code ReferenceArgument} for the given type
   */
  public static ReferenceArgument forType(java.lang.reflect.Type type) {
    return forType(ReferenceType.forType(type));
  }

  public static ReferenceArgument forType(ReferenceType referenceType) {
    return new ReferenceArgument(referenceType);
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

  @Override
  public TypeArgument substitute(Substitution substitution) {
    return TypeArgument.forType(referenceType.substitute(substitution));
  }

  /**
   * {@inheritDoc}
   *
   * <p>Considers cases:
   *
   * <ul>
   *   <li>{@code T} contains {@code T}
   *   <li>{@code T} contains {@code ? extends T}
   *   <li>{@code T} contains {@code ? super T}
   * </ul>
   */
  @Override
  public boolean contains(TypeArgument otherArgument) {
    if (otherArgument.isWildcard()) {
      ParameterBound boundType = ((WildcardArgument) otherArgument).getTypeBound();
      return boundType.equals(new EagerReferenceBound(referenceType));
    } else {
      return referenceType.equals(((ReferenceArgument) otherArgument).getReferenceType());
    }
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
  public List<TypeVariable> getTypeParameters() {
    return referenceType.getTypeParameters();
  }

  @Override
  public boolean hasWildcard() {
    return referenceType.isParameterized() && ((ClassOrInterfaceType) referenceType).hasWildcard();
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

  @Override
  boolean isInstantiationOfTypeArgument(TypeArgument otherArgument) {
    if (!(otherArgument instanceof ReferenceArgument)) {
      return false;
    }

    ReferenceType otherReferenceType = ((ReferenceArgument) otherArgument).getReferenceType();

    return referenceType.isInstantiationOf(otherReferenceType);
  }

  @Override
  public Substitution getInstantiatingSubstitution(TypeArgument otherArgument) {
    if (!(otherArgument instanceof ReferenceArgument)) {
      return null;
    }
    ReferenceType otherReferenceType = ((ReferenceArgument) otherArgument).getReferenceType();
    return referenceType.getInstantiatingSubstitution(otherReferenceType);
  }

  @Override
  public boolean isVariable() {
    return referenceType.isVariable();
  }
}
