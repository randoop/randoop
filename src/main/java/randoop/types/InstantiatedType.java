package randoop.types;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.CollectionsPlume;

/**
 * Represents a parameterized type as a generic class instantiated with type arguments (possibly
 * wildcard type arguments).
 *
 * <p>Note that {@link java.lang.reflect.ParameterizedType} is an interface that can represent
 * either a parameterized type in the sense meant here, or a generic class. Conversion to this type
 * from {@link java.lang.reflect.Type} interfaces is handled by {@link
 * Type#forType(java.lang.reflect.Type)}.
 */
public class InstantiatedType extends ParameterizedType {

  /** The generic class for this type. Its enclosing type is instantiated (or is not generic). */
  private final GenericClassType genericType;

  /** The type arguments for this class. */
  private final List<TypeArgument> argumentList;

  /**
   * Create a parameterized type from the generic class type.
   *
   * @param genericType the generic class type
   * @param argumentList the list of type arguments
   * @throws IllegalArgumentException if either argument is null
   */
  /*package*/ InstantiatedType(GenericClassType genericType, List<TypeArgument> argumentList) {
    super();
    if (genericType == null) {
      throw new IllegalArgumentException("instantiated type must be non-null");
    }

    this.genericType = genericType;
    this.argumentList = argumentList;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Test if the given object is equal to this parameterized type. Two parameterized types are
   * equal if they have the same raw type, the same type arguments, and the same enclosing type.
   */
  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InstantiatedType)) {
      return false;
    }
    InstantiatedType other = (InstantiatedType) obj;
    // super.equals compares the enclosing types, as for Outer<String>.Inner<Integer>.
    return genericType.equals(other.getGenericClassType())
        && argumentList.equals(other.argumentList)
        && super.equals(obj);
  }

  @Override
  public int hashCode() {
    return Objects.hash(genericType, argumentList, super.hashCode());
  }

  @Override
  public InstantiatedType substitute(Substitution substitution) {
    List<TypeArgument> argumentList =
        CollectionsPlume.mapList(
            (TypeArgument argument) -> argument.substitute(substitution), this.argumentList);
    ClassOrInterfaceType enclosing = substituteEnclosingType(substitution);
    if (sameType(enclosing, enclosingType) && sameElements(argumentList, this.argumentList)) {
      return this;
    }
    InstantiatedType result = new InstantiatedType(genericType, argumentList);
    if (isMemberClass()) {
      result.enclosingType = enclosing;
    }
    return result;
  }

  /**
   * Constructs a capture conversion for this type. If this type has wildcard type arguments, then
   * introduces {@link CaptureTypeVariable} for each wildcard as described in the JLS, section
   * 5.1.10, <a
   * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-5.html#jls-5.1.10">Capture
   * Conversion</a>. A type argument that is not a wildcard is unchanged, even if it contains a
   * wildcard: for example, the capture conversion of {@code List<List<?>>} is {@code
   * List<List<?>>}.
   *
   * <p>Based on algorithm in Mads Torgerson <i>et al.</i> "<a
   * href="https://www.jot.fm/issues/issue_2004_12/article5.pdf">Adding Wildcards to the Java
   * Programming Language</a>", Journal of Object Technology, 3 (December 2004) 11, 97-116. Special
   * Issue: OOPS track at SAC 2004.
   *
   * <p>Converts only the wildcards that are type arguments of this type or of an enclosing type,
   * not those nested within type arguments or within wildcard bounds. If {@link
   * #hasWildcardArgument()} is false, then returns this type.
   *
   * @return the capture conversion type for this type
   */
  @Override
  public InstantiatedType applyCaptureConversion() {

    if (!this.hasWildcardArgument()) {
      return this;
    }

    List<ReferenceType> convertedTypeList = new ArrayList<>(argumentList.size());
    for (TypeArgument argument : argumentList) {
      if (argument.isWildcard()) {
        convertedTypeList.add(new CaptureTypeVariable((WildcardArgument) argument));
      } else {
        convertedTypeList.add(((ReferenceArgument) argument).getReferenceType());
      }
    }

    Substitution substitution =
        new Substitution(genericType.getDeclaredTypeParameters(), convertedTypeList);
    // A bound of a type parameter can mention a type parameter of an enclosing class.
    ClassOrInterfaceType convertedEnclosingType = null;
    if (isMemberClass()) {
      convertedEnclosingType = enclosingType.applyCaptureConversion();
      substitution = convertedEnclosingType.getReferenceArgumentSubstitution().extend(substitution);
    }
    for (int i = 0; i < convertedTypeList.size(); i++) {
      if (convertedTypeList.get(i).isCaptureVariable()) {
        CaptureTypeVariable captureVariable = (CaptureTypeVariable) convertedTypeList.get(i);
        captureVariable.convert(genericType.getDeclaredTypeParameters().get(i), substitution);
      }
    }

    List<TypeArgument> convertedArgumentList =
        CollectionsPlume.mapList(TypeArgument::forType, convertedTypeList);

    InstantiatedType result = new InstantiatedType(genericType, convertedArgumentList);
    result.enclosingType = convertedEnclosingType;
    return result;
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
    Substitution substitution = getSupertypeSubstitution();
    return genericType.getInterfaces(substitution);
  }

  @Override
  public GenericClassType getGenericClassType() {
    return genericType.getGenericClassType();
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
    // isInstantiationOf can be true for a member class of another class, if the enclosing types
    // match, so also check the class.
    if (this.runtimeClassIs(goalType.getRuntimeClass()) && this.isInstantiationOf(goalType)) {
      return this;
    }
    return super.getMatchingSupertype(goalType);
  }

  /**
   * Returns the list of reference type arguments of this type if there are no wildcards.
   *
   * @return the list of reference types that are arguments to this type
   */
  /*package*/ List<ReferenceType> getReferenceArguments() {
    return CollectionsPlume.mapList(
        (TypeArgument argument) ->
            argument.isWildcard()
                ? ((WildcardArgument) argument).getWildcardType()
                : ((ReferenceArgument) argument).getReferenceType(),
        argumentList);
  }

  @Override
  public Class<?> getRuntimeClass() {
    return genericType.getRuntimeClass();
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
    Substitution substitution = getSupertypeSubstitution();
    return this.getGenericClassType().getSuperclass(substitution);
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

  @Override
  /*package*/ Substitution getReferenceArgumentSubstitution() {
    return super.getReferenceArgumentSubstitution()
        .extend(genericType.getDeclaredTypeParameters(), getReferenceArguments());
  }

  @Override
  /*package*/ Substitution getSupertypeSubstitution() {
    return super.getSupertypeSubstitution()
        .extend(genericType.getDeclaredTypeParameters(), getReferenceArguments());
  }

  /**
   * Returns the type substitution of the type arguments of this type for the type variables of the
   * instantiated class and of the classes that enclose it as a member class, if the type arguments
   * are reference types. For example, for {@code Outer<String>.Inner<Integer>}, where {@code
   * Inner<U>} is a member class of {@code Outer<T>}, the result maps {@code T} to {@code String}
   * and {@code U} to {@code Integer}. If any of those type arguments is a wildcard, then null is
   * returned.
   *
   * @return the type substitution of the type arguments of this type for the type variables of the
   *     instantiated class and its enclosing classes, or null if any of the type arguments is a
   *     wildcard
   */
  public @Nullable Substitution getTypeSubstitution() {
    if (hasWildcardArgument()) {
      return null;
    }
    return getReferenceArgumentSubstitution();
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
  public boolean hasWildcardArgument() {
    if (super.hasWildcardArgument()) {
      return true;
    }
    for (TypeArgument argument : argumentList) {
      if (argument.isWildcard()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean hasCaptureVariable() {
    if (super.hasCaptureVariable()) { // an enclosing type has a capture variable
      return true;
    }
    for (TypeArgument argument : argumentList) {
      if (argument.hasCaptureVariable()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isAbstract() {
    return genericType.isAbstract();
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
  public boolean isGenericForm() {
    return argumentList.equals(genericType.getTypeArguments()) && super.isGenericForm();
  }

  @Override
  public boolean isGeneric(boolean ignoreWildcards) {
    if (super.isGeneric(ignoreWildcards)) { // enclosing type is generic
      return true;
    }
    for (TypeArgument argument : argumentList) {
      if (argument.isGeneric(ignoreWildcards)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if this type is an instantiation of the given instantiated type. This is only
   * possible if this type is {@code A<T1,...,Tk>} where all {@code Ti} are instantiated by ground
   * types (i.e., does not have type variables), the other type is {@code A<S1,...,Sk>}, and each
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
    // 3 cases:  otherType is InstantiatedType, GenericClassType, or something else.
    // Recall that ParameterizedType's two subtypes are InstantiatedType and GenericClassType.

    if (otherType instanceof InstantiatedType) {
      InstantiatedType otherInstType = (InstantiatedType) otherType;
      if (!this.getGenericClassType().equals(otherInstType.getGenericClassType())) {
        return false;
      }
      // For a member class, such as Outer<String>.Inner<Integer>, the enclosing type must also be
      // an instantiation.
      if (this.isMemberClass()
          && otherInstType.isMemberClass()
          && !this.enclosingType.isInstantiationOf(otherInstType.enclosingType)) {
        return false;
      }
      for (int i = 0; i < this.argumentList.size(); i++) {
        TypeArgument thisTypeArg = this.argumentList.get(i);
        TypeArgument otherTypeArg = otherInstType.argumentList.get(i);
        if (!thisTypeArg.isInstantiationOfTypeArgument(otherTypeArg)) {
          return false;
        }
      }
      return true;
    } else if (otherType instanceof GenericClassType) {
      GenericClassType otherGenClass = (GenericClassType) otherType;
      if (!this.getGenericClassType().equals(otherGenClass)) {
        return false;
      }
      // TODO: Ensure that a substitution exists.
      // This implementation incorrectly returns true for
      // isInstantiationOf("foo(Integer, String)", "foo(T,T)").
      return true;
    } else {
      // otherType is not a ParameterizedType (not an InstantiatedType or GenericClassType)
      if (super.isInstantiationOf(otherType)) {
        return true;
      }
      return false;
    }
  }

  @Override
  public @Nullable Substitution getInstantiatingSubstitution(ReferenceType goalType) {
    Substitution superResult = getInstantiatingSubstitutionforTypeVariable(this, goalType);
    if (superResult != null) {
      return superResult;
    }

    assert goalType.isGeneric();
    Substitution substitution = super.getInstantiatingSubstitution(goalType);
    if (substitution == null) {
      // The enclosing type of this member class does not instantiate that of the goal type.
      return null;
    }
    if (goalType instanceof InstantiatedType) {
      InstantiatedType otherInstType = (InstantiatedType) goalType;
      if (this.getGenericClassType().equals(otherInstType.getGenericClassType())) {
        for (int i = 0; i < this.argumentList.size(); i++) {
          TypeArgument thisTArg = this.argumentList.get(i);
          TypeArgument otherTArg = otherInstType.argumentList.get(i);
          if (!otherTArg.isGeneric()) {
            // A type argument with no type variables binds none, so it must match exactly.
            if (!thisTArg.equals(otherTArg)) {
              return null;
            }
            continue;
          }
          Substitution subst = thisTArg.getInstantiatingSubstitution(otherTArg);
          // The substitution from the enclosing type, or for another argument, can map a type
          // variable to a different type, as for Map<String, Integer> and Map<T, T>, or for
          // Outer<String>.Inner<Integer> and Outer<E>.Inner<E>.  Then this type does not
          // instantiate goalType.
          if (subst == null || !substitution.isConsistentWith(subst)) {
            return null;
          }
          try {
            substitution = substitution.extend(subst);
          } catch (IllegalArgumentException e) {
            // The bindings map a type variable to distinct types, as for
            // Outer<String>.GenericInner<Integer> and Outer<X>.GenericInner<X>.
            return null;
          }
        }
        return substitution;
      }
      return null;
    }
    // TODO: Why is this test so late, after expensive computation?
    if (goalType instanceof GenericClassType) {
      return substitution;
    }
    return null;
  }

  @Override
  public boolean isInterface() {
    return genericType.isInterface();
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
    return argType.isSubtypeOfOrEqualTo(this);
  }

  @Override
  public boolean isStatic() {
    return genericType.isStatic();
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
  public boolean isSubtypeOfOrEqualTo(Type otherType) {
    // A member class with a parameterized enclosing type, such as Outer<String>.Inner, is
    // parameterized even if it is not an InstantiatedType.  The clauses below need an
    // InstantiatedType; the supertype search below (super.isSubtypeOfOrEqualTo) handles other
    // types.
    if (otherType instanceof InstantiatedType) {
      InstantiatedType otherInstantiatedType = (InstantiatedType) otherType;

      // second clause: rawtype same and parameters S_i of otherType contains T_i of this
      if (otherType.runtimeClassIs(this.getRuntimeClass())) {
        // The enclosing type is part of the parameterization:  for example,
        // `Outer<String>.Inner<Integer>` is not a subtype of `Outer<Number>.Inner<Integer>`.
        if (!this.enclosingTypeIsSubtypeOf(otherInstantiatedType)) {
          return otherInstantiatedType.isGenericForm();
        }
        if (this.typeArgumentsAreContainedBy(otherInstantiatedType)) {
          return true;
        }
        // JLS 4.10.2:  a type with wildcard type arguments is a subtype of the types whose type
        // arguments contain the type arguments of its capture conversion.  Capture conversion
        // incorporates the bounds of the type parameters into the capture variables.
        if (this.hasWildcardArgument()
            && this.applyCaptureConversion().typeArgumentsAreContainedBy(otherInstantiatedType)) {
          return true;
        }
        // As for the rawtype (see NonParameterizedType.isSubtypeOfOrEqualTo), a parameterized type
        // is also a subtype of the generic form of its class.
        return otherInstantiatedType.isGenericForm();
      }

      // first clause, combined with the second:  the supertype with the same rawtype as otherType
      // is a subtype of otherType if the type arguments of otherType contain its type arguments,
      // as List<String> is a subtype of Collection<?> because Collection<String> is.
      // As in Subtyping.isSubtypeOrEqual, a type with wildcard arguments is captured before its
      // supertype is found:  if C<T> implements Supplier<List<T>>, then the supertype of C<?> is
      // Supplier<List<CAP>>, not Supplier<List<?>>, which substituting the wildcard would give.
      InstantiatedType source = this.hasWildcardArgument() ? this.applyCaptureConversion() : this;
      InstantiatedType superType =
          source.getMatchingSupertype(otherInstantiatedType.getGenericClassType());
      if (superType != null) {
        // getMatchingSupertype returns an instantiation of otherType's generic class, so this
        // applies type argument containment, as for Collection<Integer> and
        // Collection<? extends Number>, and the recursive call uses the second clause above and
        // does not recurse further.
        assert superType.runtimeClassIs(otherType.getRuntimeClass())
            : "superType " + superType + " does not match " + otherType;
        if (superType.isSubtypeOfOrEqualTo(otherType)) {
          return true;
        }
      }
    }

    if (super.isSubtypeOfOrEqualTo(otherType)) {
      return true;
    }

    // wildcard clause
    if (this.hasWildcardArgument()) { // JLS 4.10.2
      // old note says this has to be tested first
      return this.applyCaptureConversion().isSubtypeOfOrEqualTo(otherType);
    }

    // The supertypes of a rawtype are erasures, so this is true for a parameterized `otherType`
    // only if `otherType` is the generic form of this type's class.
    return this.getRawtype().isSubtypeOfOrEqualTo(otherType);
  }

  /**
   * Returns true if the enclosing type of this type is a subtype of the enclosing type of the given
   * type. The given type must be an instantiation of the same generic class as this type. Returns
   * true if either type has no enclosing type.
   *
   * @param otherType an instantiation of the same generic class as this type
   * @return true if the enclosing type of this type is a subtype of that of {@code otherType}
   */
  private boolean enclosingTypeIsSubtypeOf(InstantiatedType otherType) {
    if (this.enclosingType == null || otherType.enclosingType == null) {
      return true;
    }
    return this.enclosingType.isSubtypeOfOrEqualTo(otherType.enclosingType);
  }

  /**
   * Returns true if each type argument of the given type contains the corresponding type argument
   * of this type, including the type arguments of the enclosing types of member classes, if both
   * are recorded. The given type must be an instantiation of the same generic class as this type.
   *
   * @param otherType an instantiation of the same generic class as this type
   * @return true if each type argument of {@code otherType} contains that of this type
   */
  private boolean typeArgumentsAreContainedBy(InstantiatedType otherType) {
    List<TypeArgument> otherTypeArguments = otherType.getTypeArguments();
    assert otherTypeArguments.size() == argumentList.size();
    for (int i = 0; i < argumentList.size(); i++) {
      if (!otherTypeArguments.get(i).contains(argumentList.get(i))) {
        return false;
      }
    }
    // For a member class of a generic class, Outer<String>.Inner<Integer> is not a subtype of
    // Outer<Number>.Inner<Integer>.  An enclosing class that declares no type parameters, as
    // Middle in Outer<String>.Middle.Inner<Integer>, is skipped, as in
    // Subtyping.argumentsContained.
    ClassOrInterfaceType enclosing = this;
    ClassOrInterfaceType otherEnclosing = otherType;
    while (enclosing.isMemberClass() && otherEnclosing.isMemberClass()) {
      enclosing = enclosing.enclosingType;
      otherEnclosing = otherEnclosing.enclosingType;
      if (enclosing instanceof InstantiatedType && otherEnclosing instanceof InstantiatedType) {
        return ((InstantiatedType) enclosing)
            .typeArgumentsAreContainedBy((InstantiatedType) otherEnclosing);
      }
    }
    return true;
  }

  @Override
  public NonParameterizedType getRawtype() {
    return genericType.getRawtype();
  }
}
