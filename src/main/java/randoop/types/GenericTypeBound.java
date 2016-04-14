package randoop.types;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Objects;

import plume.UtilMDE;

/**
 * Represents a type bound in which a type variable occurs.
 * To evaluate this kind of bound, a substitution is needed to instantiate the
 * bound to a concrete type bound.
 * @see GenericClassType
 * @see Substitution
 */
public class GenericTypeBound extends TypeBound {

  /** the rawtype for this generic bound */
  private final Class<?> rawType;

  /** the type parameters for this bound */
  private final Type[] parameters;

  private final TypeOrdering typeOrdering;
  /**
   * Creates a {@code GenericTypeBound} from the given rawtype and type parameters.
   *
   * @param rawType  the rawtype for the type bound
   * @param parameters  the type parameters for the type bound
   */
  public GenericTypeBound(Class<?> rawType, Type[] parameters, TypeOrdering typeOrdering) {
    this.rawType = rawType;
    this.parameters = parameters;
    this.typeOrdering = typeOrdering;
  }

  /**
   * {@inheritDoc}
   * @return true if argument is a {@code GenericTypeBound}, and the rawtype
   *         and parameters are identical, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof GenericTypeBound)) {
      return false;
    }
    GenericTypeBound b = (GenericTypeBound) obj;
    if (!this.rawType.equals(b.rawType)) {
      return false;
    }
    if (this.parameters.length != b.parameters.length) {
      return false;
    }
    for (int i = 0; i < this.parameters.length; i++) {
      if (!this.parameters[i].equals(b.parameters[i])) {
        return false;
      }
    }
    return this.typeOrdering.equals(b.typeOrdering);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rawType, parameters, typeOrdering);
  }

  /**
   * {@inheritDoc}
   * @return the name of the type bound
   */
  @Override
  public String toString() {
    return rawType.toString() + "<" + UtilMDE.join(parameters, ",") + ">";
  }

  /**
   * {@inheritDoc}
   * This generic type bound is satisfied by a concrete type if the concrete type
   * formed by applying the substitution to this generic bound is satisfied by
   * the concrete type.
   */
  @Override
  public boolean isSatisfiedBy(ConcreteType argType, Substitution substitution) throws RandoopTypeException {
    ConcreteTypeBound b = instantiate(substitution);
    return b.isSatisfiedBy(argType, substitution);
  }

  /**
   * Creates concrete type bound from this bound by instantiating the type
   * variables of this bound with the given substitution. The substitution must
   * be for the generic class to which the bound belongs.
   *
   * @param substitution  the substitution for instantiating type variables
   * @return the concrete type bound formed by substituting type variables with
   * the concrete types using the substitution
   * @throws IllegalArgumentException if either an argument is not a type variable,
   * or a type variable has no instantiation in the substitution
   */
  public ConcreteTypeBound instantiate(Substitution substitution) throws RandoopTypeException {
    ConcreteType[] concreteArgs = new ConcreteType[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      if (!(parameters[i] instanceof TypeVariable)) {
        throw new IllegalArgumentException("unable to instantiate type parameter " + parameters[i]);
      }
      ConcreteType t = substitution.get(parameters[i]);
      if (t == null) {
        throw new IllegalArgumentException("unable to instantiate type parameter " + parameters[i]);
      }
      concreteArgs[i] = t;
    }
    return new ConcreteTypeBound(ConcreteType.forClass(rawType, concreteArgs), typeOrdering);
  }

  /**
   * {@inheritDoc}
   * As a hack to return something usable, returns {@code Object}, but needs to
   * be bound based on parameter.
   */
  @Override
  public Class<?> getRuntimeClass() {
    return rawType;
  }

  @Override
  public TypeBound apply(Substitution substitution) {
    return this;
  }
}
