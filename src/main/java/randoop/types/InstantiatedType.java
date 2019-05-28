package randoop.types;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a parameterized type as a generic class instantiated with type arguments.
 *
 * <p>Note that {@link java.lang.reflect.ParameterizedType} is an interface that can represent
 * either a parameterized type in the sense meant here, or a generic class. Conversion to this type
 * from {@link java.lang.reflect.Type} interfaces is handled by {@link
 * Type#forType(java.lang.reflect.Type)}.
 */
public class InstantiatedType extends ParameterizedType {

  /** The generic class for this type. */
  private final GenericClassType instantiatedType;

  /** The type arguments for this class. */
  private final List<TypeArgument> argumentList;

  /**
   * Create a parameterized type from the generic class type.
   *
   * @param instantiatedType the generic class type
   * @param argumentList the list of type arguments
   * @throws IllegalArgumentException if either argument is null
   */
  InstantiatedType(GenericClassType instantiatedType, List<TypeArgument> argumentList) {
    if (instantiatedType == null) {
      throw new IllegalArgumentException("instantiated type must be non-null");
    }

    this.instantiatedType = instantiatedType;
    this.argumentList = argumentList;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Test if the given object is equal to this parameterized type. Two parameterized types are
   * equal if they have the same raw type and the same type arguments.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InstantiatedType)) {
      return false;
    }
    InstantiatedType other = (InstantiatedType) obj;
    return instantiatedType.equals(other.instantiatedType)
        && argumentList.equals(other.argumentList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(instantiatedType, argumentList);
  }

  @Override
  public String toString() {
    return this.getName();
  }

  @Override
  public InstantiatedType substitute(Substitution substitution) {
    List<TypeArgument> argumentList = new ArrayList<>();
    for (TypeArgument argument : this.argumentList) {
      argumentList.add(argument.substitute(substitution));
    }
    return (InstantiatedType)
        substitute(substitution, new InstantiatedType(instantiatedType, argumentList));
  }

  /**
   * Constructs a capture conversion for this type. If this type has wildcard type arguments, then
   * introduces {@link CaptureTypeVariable} for each wildcard as described in the JLS, section
   * 5.1.10, <a
   * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.10">Capture
   * Conversion</a>.
   *
   * <p>Based on algorithm in Mads Torgerson <i>et al.</i> "<a
   * href="http://www.jot.fm/issues/issue_2004_12/article5.pdf">Adding Wildcards to the Java
   * Programming Language</a>", Journal of Object Technology, 3 (December 2004) 11, 97-116. Special
   * Issue: OOPS track at SAC 2004.
   *
   * <p>If this type has no wildcards, then returns this type.
   *
   * @return the capture conversion type for this type
   */
  @Override
  public InstantiatedType applyCaptureConversion() {

    if (!this.hasWildcard()) {
      return this;
    }

    List<ReferenceType> convertedTypeList = new ArrayList<>();
    for (TypeArgument argument : argumentList) {
      if (argument.isWildcard()) {
        WildcardArgument convertedArgument = ((WildcardArgument) argument).applyCaptureConversion();
        convertedTypeList.add(new CaptureTypeVariable(convertedArgument));
      } else {
        ReferenceType convertedArgument =
            ((ReferenceArgument) argument).getReferenceType().applyCaptureConversion();
        convertedTypeList.add(convertedArgument);
      }
    }

    Substitution substitution =
        new Substitution(instantiatedType.getTypeParameters(), convertedTypeList);
    for (int i = 0; i < convertedTypeList.size(); i++) {
      if (convertedTypeList.get(i).isCaptureVariable()) {
        CaptureTypeVariable captureVariable = (CaptureTypeVariable) convertedTypeList.get(i);
        captureVariable.convert(instantiatedType.getTypeParameters().get(i), substitution);
      }
    }

    List<TypeArgument> convertedArgumentList = new ArrayList<>();
    for (ReferenceType type : convertedTypeList) {
      convertedArgumentList.add(TypeArgument.forType(type));
    }

    return (InstantiatedType)
        applyCaptureConversion(new InstantiatedType(instantiatedType, convertedArgumentList));
  }

  /**
   * Constructs the list of interfaces for this parameterized type.
   *
   * <p>See the implementation note for {@link #getSuperclass()}.
   *
   * @return list of directly-implemented interfaces for this parameterized type
   */
  @Override
  public List<ClassOrInterfaceType> getInterfaces() {
    List<ClassOrInterfaceType> interfaces = new ArrayList<>();
    Substitution substitution =
        new Substitution(instantiatedType.getTypeParameters(), getReferenceArguments());
    for (ClassOrInterfaceType type : instantiatedType.getInterfaces(substitution)) {
      interfaces.add(type);
    }

    return interfaces;
  }

  @Override
  public GenericClassType getGenericClassType() {
    return instantiatedType.getGenericClassType();
  }

  /**
   * {@inheritDoc}
   *
   * <p>An instantiated type may have a wildcard, and so must perform capture conversion before
   * doing supertype search.
   */
  @Override
  public InstantiatedType getMatchingSupertype(GenericClassType goalType) {
    /*
    if (this.hasWildcard()) {
      return this.applyCaptureConversion().getMatchingSupertype(goalType);
    }
    */
    if (this.isInstantiationOf(goalType)) {
      return this;
    }
    return super.getMatchingSupertype(goalType);
  }

  /**
   * Returns the list of reference type arguments of this type if there are no wildcards.
   *
   * @return the list of reference types that are arguments to this type
   */
  List<ReferenceType> getReferenceArguments() {
    List<ReferenceType> referenceArgList = new ArrayList<>();
    for (TypeArgument argument : argumentList) {
      if (!argument.isWildcard()) {
        referenceArgList.add(((ReferenceArgument) argument).getReferenceType());
      } else {
        referenceArgList.add(((WildcardArgument) argument).getWildcardType());
      }
    }
    return referenceArgList;
  }

  @Override
  public Class<?> getRuntimeClass() {
    return instantiatedType.getRuntimeClass();
  }

  /**
   * Constructs the superclass type for this parameterized type.
   *
   * <p>Implementation note: we can think of an {@link InstantiatedType} {@code A<T1,...,Tk>} as
   * being represented as a generic class {@code A<F1,...,Fk>} with a substitution {@code [ Fi :=
   * Ti]} for all of the type parameters {@code Fi}. So, when we compute a superclass, we first find
   * the supertype of the generic class {@code B<F1,...,Fk>}, and then apply the substitution {@code
   * [ Fi := Ti]} using the method {@link GenericClassType#getSuperclass(Substitution)}.
   *
   * @return the superclass type for this parameterized type
   */
  @Override
  public ClassOrInterfaceType getSuperclass() {
    Substitution substitution =
        new Substitution(instantiatedType.getTypeParameters(), getReferenceArguments());
    return this.instantiatedType.getSuperclass(substitution);
  }

  /**
   * Returns the type arguments for this type.
   *
   * @return the list of type arguments
   */
  @Override
  public List<TypeArgument> getTypeArguments() {
    return argumentList;
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    Set<TypeVariable> paramSet = new LinkedHashSet<>(super.getTypeParameters());
    for (TypeArgument argument : argumentList) {
      List<TypeVariable> params = argument.getTypeParameters();
      paramSet.addAll(params);
    }
    return new ArrayList<>(paramSet);
  }

  /**
   * Creates the type substitution of the type arguments of this type for the type variables of the
   * instantiated class, if the type arguments are reference types. If any type argument is a
   * wildcard, then null is returned.
   *
   * @return the type substitution of the type arguments of this class for the type variables of the
   *     instantiated type
   */
  public Substitution getTypeSubstitution() {
    List<ReferenceType> arguments = new ArrayList<>();
    for (TypeArgument arg : this.getTypeArguments()) {
      if (!arg.isWildcard()) {
        arguments.add(((ReferenceArgument) arg).getReferenceType());
      }
    }
    Substitution substitution = null;
    if (arguments.size() == this.getTypeArguments().size()) {
      substitution = new Substitution(instantiatedType.getTypeParameters(), arguments);
    }
    return substitution;
  }

  @Override
  public boolean hasWildcard() {
    for (TypeArgument argument : argumentList) {
      if (argument.hasWildcard()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isAbstract() {
    return instantiatedType.isAbstract();
  }

  @Override
  public boolean isAssignableFrom(Type otherType) {
    if (super.isAssignableFrom(otherType)) {
      return true;
    }

    // unchecked conversion
    return otherType.isRawtype() && otherType.runtimeClassIs(this.getRuntimeClass());
  }

  @Override
  public boolean isGeneric() {
    if (super.isGeneric()) { // enclosing type is generic
      return true;
    }
    for (TypeArgument argument : argumentList) {
      if (argument.isGeneric()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether this type is an instantiation of the given instantiated type. This is only
   * possible if this type is {@code A<T1,...,Tk>} where all {@code Ti} are instantiated by ground
   * types (e.g., does not have type variables), the other type is {@code A<S1,...,Sk>}, and each
   * {@code Ti} matches {@code Si} for {@code i = 1,...,k} as follows:
   *
   * <ol>
   *   <li>If {@code Si} is the variable {@code X} with lower bound {@code L} and upper bound {@code
   *       U}, then {@code Ti} is a supertype of {@code L} and a subtype of {@code U}
   *   <li>{@code Si} is identical to {@code Ti}
   * </ol>
   *
   * @param otherType the other {@link InstantiatedType}
   * @return true if this type is an instantiation of the other type, false otherwise
   * @see ReferenceType#isInstantiationOf(ReferenceType)
   */
  @Override
  public boolean isInstantiationOf(ReferenceType otherType) {
    if (super.isInstantiationOf(otherType) && !(otherType instanceof InstantiatedType)) {
      return true;
    }
    if (otherType instanceof InstantiatedType) {
      InstantiatedType otherInstType = (InstantiatedType) otherType;
      if (this.instantiatedType.equals(otherInstType.instantiatedType)) {
        for (int i = 0; i < this.argumentList.size(); i++) {
          TypeArgument thisTypeArg = this.argumentList.get(i);
          TypeArgument otherTypeArg = otherInstType.argumentList.get(i);
          if (!thisTypeArg.isInstantiationOfTypeArgument(otherTypeArg)) {
            return false;
          }
        }
        return true;
      }
      return false; // instantiated generic class types are not same
    }
    return (otherType instanceof GenericClassType)
        && this.instantiatedType.isInstantiationOf(otherType);
  }

  @Override
  public Substitution getInstantiatingSubstitution(ReferenceType goalType) {
    Substitution superResult =
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(this, goalType);
    if (superResult != null) {
      return superResult;
    }

    assert goalType.isGeneric();
    Substitution substitution = super.getInstantiatingSubstitution(goalType);
    if (goalType instanceof InstantiatedType) {
      InstantiatedType otherInstType = (InstantiatedType) goalType;
      if (this.instantiatedType.equals(otherInstType.instantiatedType)) {
        for (int i = 0; i < this.argumentList.size(); i++) {
          TypeArgument thisTArg = this.argumentList.get(i);
          TypeArgument otherTArg = otherInstType.argumentList.get(i);
          Substitution subst = thisTArg.getInstantiatingSubstitution(otherTArg);
          if (subst == null) {
            return null;
          }
          substitution = substitution.extend(subst);
        }
        return substitution;
      }
      return null;
    }
    if (goalType instanceof GenericClassType) {
      return substitution;
    }
    return null;
  }

  @Override
  public boolean isInterface() {
    return instantiatedType.isInterface();
  }

  @Override
  public boolean isParameterized() {
    return true;
  }

  /**
   * Determines if this type is recursive in the sense that the type is the bound of its type
   * argument. So, should have a single type argument that is a subtype of this type.
   *
   * @return true if the type argument is a subtype of this type, false otherwise
   */
  public boolean isRecursiveType() {
    if (this.argumentList.size() > 1 || this.argumentList.get(0).hasWildcard()) {
      return false;
    }
    ReferenceType argType = ((ReferenceArgument) this.argumentList.get(0)).getReferenceType();
    return argType.isSubtypeOf(this);
  }

  @Override
  public boolean isStatic() {
    return instantiatedType.isStatic();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Handles specific cases of supertypes of a parameterized type {@code C<T1,...,Tn>}
   * instantiating the generic type {@code C<F1,...,Fn>} by substitution &theta;{@code
   * =[F1:=T1,...,Fn:=Tn]} for which direct supertypes are:
   *
   * <ol>
   *   <li>{@code D<U1}&theta;{@code ,...,Uk}&theta;{@code >} where {@code D<U1,...,Uk>} is a
   *       supertype of {@code C<F1,...,Fn>}.
   *   <li>{@code C<S1,...,Sn>} where Si <i>contains</i> Ti (JLS section 4.5.1).
   *   <li>The rawtype {@code C}.
   *   <li>{@code Object} if generic form is interface with no interfaces as supertypes.
   * </ol>
   */
  @Override
  public boolean isSubtypeOf(Type otherType) {
    if (otherType.isParameterized()) {

      // second clause: rawtype same and parameters S_i of otherType contains T_i of this
      if (otherType.runtimeClassIs(this.getRuntimeClass())) {
        ParameterizedType otherParameterizedType = (ParameterizedType) otherType;
        List<TypeArgument> otherTypeArguments = otherParameterizedType.getTypeArguments();
        List<TypeArgument> thisTypeArguments = this.getTypeArguments();
        assert otherTypeArguments.size() == thisTypeArguments.size();
        int i = 0;
        while (i < thisTypeArguments.size()
            && otherTypeArguments.get(i).contains(thisTypeArguments.get(i))) {
          i++;
        }
        if (i == thisTypeArguments.size()) {
          return true;
        }
      }

      // first clause.
      InstantiatedType otherInstandiatedType = (InstantiatedType) otherType;
      InstantiatedType superType =
          this.getMatchingSupertype(otherInstandiatedType.instantiatedType);
      if (superType != null && superType.equals(otherType)) {
        return true;
      }
    }

    if (super.isSubtypeOf(otherType)) {
      return true;
    }

    // wildcard clause
    if (this.hasWildcard()) { // JLS 4.10.2
      // old note says this has to be tested first
      return this.applyCaptureConversion().isSubtypeOf(otherType);
    }

    return this.getRawtype().isSubtypeOf(otherType);
  }

  @Override
  public NonParameterizedType getRawtype() {
    return instantiatedType.getRawtype();
  }
}
