package randoop.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import plume.UtilMDE;

import randoop.BugInRandoopException;

/**
 * Represents a parameterized type as a generic class instantiated with
 * concrete type arguments.
 * <p>
 * Note that {@link java.lang.reflect.ParameterizedType} is an interface that
 * can represent either a parameterized type in the sense meant here, or a
 * generic class.
 * Conversion to this type from this and other {@link java.lang.reflect.Type}
 * interfaces is handled by
 * {@link randoop.types.GeneralType#forType(java.lang.reflect.Type)}.
 */
public class ParameterizedType extends ConcreteType {

  private final List<TypeArgument> argumentList;

  /** The generic class for this type */
  private GenericClassType instantiatedType;

  /** The instantiating type substitution */
  private Substitution substitution;

  /**
   * Create a parameterized type from the generic class type.
   *
   * @param instantiatedType  the generic class type
   * @param substitution  the substitution for type variables
   * @param argumentList  the list of argument types
   * @throws IllegalArgumentException if either argument is null
   */
  ParameterizedType(GenericClassType instantiatedType, Substitution substitution, List<TypeArgument> argumentList) {
    if (instantiatedType == null) {
      throw new IllegalArgumentException("instantiated type must be non-null");
    }
    if (substitution == null) {
      throw new IllegalArgumentException("substitution must be non-null");
    }

    this.instantiatedType = instantiatedType;
    this.substitution = substitution;
    this.argumentList = argumentList;
  }

  /**
   * {@inheritDoc}
   * Test if the given object is equal to this parameterized type.
   * Two parameterized types are equal if they have the same raw type and
   * the same type arguments.
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ParameterizedType)) {
      return false;
    }
    ParameterizedType t = (ParameterizedType) obj;
    if (!instantiatedType.equals(t.instantiatedType)) {
      return false;
    }

    // Cannot guarantee that instantiated types have same type variables, or
    // that if they do the substitutions will work,
    // so check that parameters are mapped to the samesame concrete types
    List<TypeParameter> typeParameters = instantiatedType.getTypeParameters();
    List<TypeParameter> otherParameters = t.instantiatedType.getTypeParameters();
    for (int i = 0; i < typeParameters.size(); i++) {
      if (!(substitution
          .get(typeParameters.get(i).getParameter())
          .equals(t.substitution.get(otherParameters.get(i).getParameter())))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(instantiatedType, substitution, argumentList);
  }

  /**
   * {@inheritDoc}
   * Tests for assignability to a parameterized type: can the given concrete
   * type be assigned to this parameterized type.
   * <p>
   * A {@code ConcreteType} is assignable to a parameterized type via
   * identity, widening reference conversion, or unchecked conversion from raw
   * type.
   * The method {@link Class#isAssignableFrom(Class)} checks widening reference
   * conversion, but with parameterized types it is also necessary to check type
   * arguments.  Since a widening reference conversion (JLS, section 5.1.5)
   * exists from type S to type T if S is a subtype of T, this method calls
   * {@link ParameterizedType#isSubtypeOf(ConcreteType)} to test the subtype
   * relation.
   */
  @Override
  public boolean isAssignableFrom(ConcreteType sourceType) {
    if (sourceType == null) {
      throw new IllegalArgumentException("source type must be non-null");
    }

    // do a couple of quick checks for things that don't work
    // first, can't assign an array to a parameterized type
    if (sourceType.isArray()) {
      return false;
    }

    // second, if underlying Class objects not assignable then there is
    // definitely no widening conversion
    if (!this.getRuntimeClass().isAssignableFrom(sourceType.getRuntimeClass())) {
      return false;
    }

    // Now, check subtype relation
    try {
      if (sourceType.isSubtypeOf(this)) {
        return true;
      }
    } catch (RandoopTypeException e) {
      // this is a bit dangerous, but I'm doing it anyway
      return false;
    }

    // otherwise, test unchecked
    return sourceType.isRawtype() && sourceType.hasRuntimeClass(this.getRuntimeClass());

  }

  /**
   * {@inheritDoc}
   * Handles specific cases of supertypes of a parameterized type
   *  <code>C&lt;T<sub>1</sub>,...,T<sub>n</sub>&gt;</code>
   * instantiating the generic type
   *  <code>C&lt;F<sub>1</sub>,...,F<sub>n</sub>&gt;</code>
   * by substitution
   *  <code>&#952; =[F<sub>1</sub>/T<sub>1</sub>,...,F<sub>n</sub>]</code>
   * for which direct supertypes are:
   * <ol>
   *   <li> <code>D&lt;U<sub>1</sub>&#952;,...,U<sub>k</sub>&#952;&gt;</code>
   *        where <code>D&lt;U<sub>1</sub>,...,U<sub>k</sub>&gt;</code> is a
   *        supertype of <code>C&lt;F<sub>1</sub>,...,F<sub>n</sub>&gt;</code>.
   *   <li> <code>C&lt;S<sub>1</sub>,...,S<sub>n</sub>&gt;</code> where
   *        S<sub>i</sub> <i>contains</i> T<sub>i</sub> (JLS section 4.5.1).
   *
   *   <li> The rawtype <code>C</code>.
   *   <li> <code>Object</code> if generic form is interface with no
   *   interfaces as supertypes.
   * </ol>
   * Note that the full definition of the <i>contains</i> relation in the second
   * clause involves wildcards, which are not currently implemented, and so the
   * relation reduces to equality on the parameter bounds.
   * (Tested using {@link GenericClassType#equals(Object)}.)
   */
  @Override
  public boolean isSubtypeOf(ConcreteType type) throws RandoopTypeException {

    if (type == null) {
      throw new IllegalArgumentException("type must be non-null");
    }

    // Object is *the* supertype
    if (type.isObject()) {
      return true;
    }

    // rawtype is a direct supertype (see JLS section 4.10.2)
    if (type.isRawtype()) {
      return type.hasRuntimeClass(this.getRuntimeClass());
    }

    // the next two checks are short-circuiting to avoid recursive calls

    // minimally, underlying Class should be assignable
    Class<?> otherRuntimeType = type.getRuntimeClass();
    Class<?> thisRuntimeType = this.getRuntimeClass();
    if (!otherRuntimeType.isAssignableFrom(thisRuntimeType)) {
      return false;
    }

    // if would-be supertype not parameterized, check supertype of generic form
    // this is the "direct superclass" clause in definition for generic type
    if (!type.isParameterized()) {
      return instantiatedType.isSubtypeOf(type);
    }

    // otherwise, check more complicated cases of definition

    // second clause.
    // Not yet handling wildcards, so "contains" reduces to equality
    if (thisRuntimeType.equals(otherRuntimeType)) {
      return this.equals(type);
    }

    // first clause.
    // Extra fragile because this.substitution only applies to the type
    // variables of this.instantiatedType, which are shared by supertype objects
    // created by Class.getGenericInterfaces() and Class.getGenericSuperClass().
    // This is how GenericClassType.getMatchingSupertype(GenericClassType) works.
    // If we get GenericClassType supertype via other means, the type variables
    // will be distinct and the substitution will return null values even if the
    // variable names and type bounds are the same.

    ParameterizedType pt = (ParameterizedType) type;
    GenericClassType genericSuperType;
    genericSuperType = this.instantiatedType.getMatchingSupertype(pt.instantiatedType);
    if (genericSuperType == null) { // no matching supertype
      return false;
    }
    ConcreteType superType = (ConcreteType)genericSuperType.apply(this.substitution);
    if (pt.equals(superType)) {
      return true; // found type
    }

    // non-null superType is potentially on transitive chain to type
    return superType.isSubtypeOf(type);
  }

  /**
   * {@inheritDoc}
   * @return true, since this is a parameterized type
   */
  @Override
  public boolean isParameterized() {
    return true;
  }

  /**
   * {@inheritDoc}
   * @return the name of this type
   */
  @Override
  public String toString() {
    return this.getName();
  }

  /**
   * {@inheritDoc}
   * @return the fully qualified name of this type with fully qualified type
   * arguments
   */
  @Override
  public String getName() {
    return getRuntimeClass().getCanonicalName()
        + "<"
        + UtilMDE.join(this.getTypeArguments(), ",")
        + ">";
  }

  /**
   * {@inheritDoc}
   * @return the rawtype of the generic type that this type instantiates
   */
  @Override
  public Class<?> getRuntimeClass() {
    return instantiatedType.getRuntimeClass();
  }

  /**
   * Returns the type arguments for this type.
   *
   * @return the list of type arguments
   */
  public List<ConcreteType> getTypeArguments() {
    List<ConcreteType> arguments = new ArrayList<>();
    for (TypeParameter parameter : instantiatedType.getTypeParameters()) {
      arguments.add(substitution.get(parameter.getParameter()));
    }
    return arguments;
  }

  /**
   * Checks whether this parameterized type is an instantiation of the given
   * generic class type.
   *
   * @param genericClassType  the generic class type
   * @return true if this type is an instantiation of the generic class, false otherwise
   */
  public boolean isInstantiationOf(GenericClassType genericClassType) {
    return instantiatedType.equals(genericClassType);
  }

  public boolean isInstantiatedSubTypeOf(GenericClassType genericClassType) {
    try {
      return instantiatedType.equals(genericClassType)
          || instantiatedType.getMatchingSupertype(genericClassType) != null;
    } catch (RandoopTypeException e) {
      throw new BugInRandoopException("type error when testing subtype: " + e.getMessage());
    }
  }

  /**
   * Constructs the superclass type for this parameterized type.
   *
   * @return the superclass type for this parameterized type
   */
  @Override
  public ConcreteType getSuperclass() throws RandoopTypeException {
    GeneralType superclass = this.instantiatedType.getSuperclass();
    if (superclass == null) {
      return null;
    }

    assert (superclass instanceof GenericClassType) || (superclass instanceof ConcreteType) : "unexpected type: " + superclass;

    if (superclass instanceof GenericClassType) {
      return new ParameterizedType((GenericClassType)superclass, this.substitution, argumentList);
    }

    return (ConcreteType)superclass;
  }
}
