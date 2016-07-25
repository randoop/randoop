package randoop.types;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a parameterized type as a generic class instantiated with
 * type arguments.
 * <p>
 * Note that {@link java.lang.reflect.ParameterizedType} is an interface that
 * can represent either a parameterized type in the sense meant here, or a
 * generic class.
 * Conversion to this type from this and other {@link java.lang.reflect.Type}
 * interfaces is handled by
 * {@link randoop.types.GeneralType#forType(java.lang.reflect.Type)}.
 */
public class InstantiatedType extends ParameterizedType {

  private final List<TypeArgument> argumentList;

  /** The generic class for this type */
  private final GenericClassType instantiatedType;

  /**
   * Create a parameterized type from the generic class type.
   *
   * @param instantiatedType  the generic class type
   * @param argumentList  the list of argument types
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
   * Test if the given object is equal to this parameterized type.
   * Two parameterized types are equal if they have the same raw type and
   * the same type arguments.
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof InstantiatedType)) {
      return false;
    }
    InstantiatedType t = (InstantiatedType) obj;
    return instantiatedType.equals(t.instantiatedType) && argumentList.equals(t.argumentList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(instantiatedType, argumentList);
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
  public boolean isMemberClass() {
    return instantiatedType.isMemberClass();
  }

  @Override
  public boolean isAssignableFrom(GeneralType otherType) {
    if (super.isAssignableFrom(otherType)) {
      return true;
    }

    // unchecked conversion
    return otherType.isRawtype() && otherType.hasRuntimeClass(this.getRuntimeClass());
  }

  @Override
  public boolean isGeneric() {
    for (TypeArgument argument : argumentList) {
      if (argument.isGeneric()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isInterface() {
    return instantiatedType.isInterface();
  }

  /**
   * {@inheritDoc}
   * Handles specific cases of supertypes of a parameterized type
   *  <code>C&lt;T<sub>1</sub>,&hellip;,T<sub>n</sub>&gt;</code>
   * instantiating the generic type
   *  <code>C&lt;F<sub>1</sub>,&hellip;,F<sub>n</sub>&gt;</code>
   * by substitution
   *  <code>&#952; =[F<sub>1</sub>:=T<sub>1</sub>,&hellip;,F<sub>n</sub>:=T<sub>n</sub>]</code>
   * for which direct supertypes are:
   * <ol>
   *   <li> <code>D&lt;U<sub>1</sub>&#952;,&hellip;,U<sub>k</sub>&#952;&gt;</code>
   *        where <code>D&lt;U<sub>1</sub>,&hellip;,U<sub>k</sub>&gt;</code> is a
   *        supertype of <code>C&lt;F<sub>1</sub>,&hellip;,F<sub>n</sub>&gt;</code>.
   *   <li> <code>C&lt;S<sub>1</sub>,&hellip;,S<sub>n</sub>&gt;</code> where
   *        S<sub>i</sub> <i>contains</i> T<sub>i</sub> (JLS section 4.5.1).
   *
   *   <li> The rawtype <code>C</code>.
   *   <li> <code>Object</code> if generic form is interface with no
   *   interfaces as supertypes.
   * </ol>
   */
  @Override
  public boolean isSubtypeOf(GeneralType otherType) {
    if (!otherType.isParameterized()) {

      if (super.isSubtypeOf(otherType)) {
        return true;
      }

      // rawtype is a direct supertype (see JLS section 4.10.2)
      if (otherType.isRawtype()) {
        if (otherType.hasRuntimeClass(this.getRuntimeClass())) {
          return true;
        }

        SimpleClassOrInterfaceType rawtype = new SimpleClassOrInterfaceType(this.getRuntimeClass());
        return rawtype.isSubtypeOf(otherType);
      }

      return false;
    }

    // second clause: rawtype same and parameters S_i of otherType contains T_i of this
    if (otherType.hasRuntimeClass(this.getRuntimeClass())) {
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
    InstantiatedType pt = (InstantiatedType) otherType;
    InstantiatedType superType = (InstantiatedType) this.getMatchingSupertype(pt.instantiatedType);

    return superType != null && pt.equals(superType);
  }

  /**
   * Creates the type substitution of the type arguments of this type for the type variables of the
   * instantiated class, if the type arguments are reference types.
   * If any type argument is a wildcard, then null is returned.
   *
   * @return the type substitution of the type arguments of this class for the type variables of the instantiated type
   */
  public Substitution<ReferenceType> getTypeSubstitution() {
    List<ReferenceType> arguments = new ArrayList<>();
    for (TypeArgument arg : this.getTypeArguments()) {
      if (!arg.isWildcard()) {
        arguments.add(((ReferenceArgument) arg).getReferenceType());
      }
    }
    Substitution<ReferenceType> substitution = null;
    if (arguments.size() == this.getTypeArguments().size()) {
      substitution = Substitution.forArgs(instantiatedType.getTypeParameters(), arguments);
    }
    return substitution;
  }

  /**
   * {@inheritDoc}
   * @return true, since this is a parameterized type
   */
  @Override
  public boolean isParameterized() {
    return true;
  }

  @Override
  public InstantiatedType apply(Substitution<ReferenceType> substitution) {
    List<TypeArgument> argumentList = new ArrayList<>();
    for (TypeArgument argument : this.argumentList) {
      argumentList.add(argument.apply(substitution));
    }
    return new InstantiatedType(instantiatedType, argumentList);
  }

  /**
   * {@inheritDoc}
   * @return the name of this type
   */
  @Override
  public String toString() {
    return this.getName();
  }

  @Override
  public GenericClassType getGenericClassType() {
    return instantiatedType.getGenericClassType();
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
  public List<TypeArgument> getTypeArguments() {
    return argumentList;
  }

  /**
   * Checks whether this parameterized type is an instantiation of the given
   * generic class type.
   *
   * @param genericClassType  the generic class type
   * @return true if this type is an instantiation of the generic class, false otherwise
   */
  public boolean isInstantiationOf(GenericClassType genericClassType) {
    return instantiatedType.isInstantiationOf(genericClassType);
  }

  /**
   * Checks whether this type is an instantiation of the given instantiated type.
   * This is only possible if this type is <code>A&lt;T<sub>1</sub>,&hellip;,T<sub>k</sub>&gt;</code>
   * where all <code>T<sub>i</sub></code> are instantiated by ground types (e.g., does not have type variables),
   * the other type is <code>A&lt;S<sub>1</sub>,&hellip;,S<sub>k</sub>&gt;</code>, and
   * each <code>T<sub>i</sub></code> matches <code>S<sub>i</sub></code> for <code>i = 1,&hellip;,k</code>
   * as follows:
   * <ol>
   *   <li>
   *     If <code>S<sub>i</sub></code> is the variable <code>X</code> with lower bound <code>L</code>
   *     and upper bound <code>U</code>, then <code>T<sub>i</sub></code>
   *     is a supertype of <code>L</code> and a subtype of <code>U</code>
   *   </li>
   *   <li>
   *     <code>S<sub>i</sub></code> is identical to <code>T<sub>i</sub></code>
   *   </li>
   * </ol>
   * @see ReferenceType#isInstantiationOf(ReferenceType)
   *
   * @param otherType  the other {@link InstantiatedType}
   * @return true if this type is an instantiation of the other type, false otherwise
   */
  @Override
  boolean isInstantiationOf(ReferenceType otherType) {
    if (super.isInstantiationOf(otherType)) {
      return true;
    }
    if (otherType instanceof InstantiatedType) {
      InstantiatedType otherInstType = (InstantiatedType) otherType;
      if (this.instantiatedType.equals(otherInstType.instantiatedType)) {
        for (int i = 0; i < this.argumentList.size(); i++) {
          if (!this.argumentList.get(i).isInstantiationOf(otherInstType.argumentList.get(i))) {
            return false;
          }
        }
        return true;
      }
      return false; // instantiated generic class types are not same
    }

    return false;
  }

  @Override
  public boolean isStatic() {
    return instantiatedType.isStatic();
  }

  /**
   * Constructs the superclass type for this parameterized type.
   * <p>
   * Implementation note: we can think of an {@link InstantiatedType}
   * <code>A&lt;T<sub>1</sub>,&hellip;,T<sub>k</sub>&gt;</code>
   * as being represented as a generic class
   * <code>A&lt;F<sub>1</sub>,&hellip;,F<sub>k</sub>&gt;</code>
   * with a substitution <code>[ F<sub>i</sub> := T<sub>i</sub>]</code>
   * for all of the type parameters <code>F<sub>i</sub></code>.
   * So, when we compute a superclass, we first find the supertype of the generic class
   * <code>B&lt;F<sub>1</sub>,&hellip;,F<sub>k</sub>&gt;</code>,
   * and then apply the substitution <code>[ F<sub>i</sub> := T<sub>i</sub>]</code>
   * using the method {@link GenericClassType#getSuperclass(Substitution)}.
   *
   * @return the superclass type for this parameterized type
   */
  @Override
  public ClassOrInterfaceType getSuperclass() {
    Substitution<ReferenceType> substitution =
        Substitution.forArgs(instantiatedType.getTypeParameters(), getReferenceArguments());
    return this.instantiatedType.getSuperclass(substitution);
  }

  /**
   * Constructs the list of interface supertypes for this parameterized type.
   * <p>
   * See the implementation note for {@link #getSuperclass()}.
   *
   * @return list of interface supertypes for this parameterized type
   */
  @Override
  public List<ClassOrInterfaceType> getInterfaces() {
    List<ClassOrInterfaceType> interfaces = new ArrayList<>();
    Substitution<ReferenceType> substitution =
        Substitution.forArgs(instantiatedType.getTypeParameters(), getReferenceArguments());
    for (ClassOrInterfaceType type : instantiatedType.getInterfaces(substitution)) {
      interfaces.add(type);
    }

    return interfaces;
  }

  /**
   * Constructs a capture conversion for this type.
   * If this type has wildcard type arguments, then introduces {@link CaptureTypeVariable} for each
   * wildcard as described in the JLS, section 5.1.10,
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.10">Capture Conversion</a>.
   *
   * <p>
   * Based on algorithm in
   * Mads Torgerson <i>et al.</i>
   * "<a href="http://www.jot.fm/issues/issue_2004_12/article5.pdf">Adding Wildcards to the Java Programming Language</a>",
   * Journal of Object Technology, 3 (December 2004) 11, 97-116. Special Issue: OOPS track at SAC 2004.
   * </p>
   *
   * <p>
   * If this type has no wildcards, then returns this type.
   *</p>
   *
   * @return the capture conversion type for this type
   */
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

    Substitution<ReferenceType> substitution =
        Substitution.forArgs(instantiatedType.getTypeParameters(), convertedTypeList);
    for (int i = 0; i < convertedTypeList.size(); i++) {
      if (convertedTypeList.get(i).isCaptureVariable()) {
        CaptureTypeVariable captureVariable = (CaptureTypeVariable) convertedTypeList.get(i);
        captureVariable.convert(instantiatedType.getTypeParameters().get(i), substitution);
      }
    }

    List<TypeArgument> convertedArgumentList = new ArrayList<>();
    for (ReferenceType type : convertedTypeList) {
      convertedArgumentList.add(new ReferenceArgument(type));
    }

    return new InstantiatedType(instantiatedType, convertedArgumentList);
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    Set<TypeVariable> paramSet = new LinkedHashSet<>();
    for (TypeArgument argument : argumentList) {
      paramSet.addAll(argument.getTypeParameters());
    }
    return new ArrayList<>(paramSet);
  }

  /**
   * Returns the list of reference type arguments of this type if there are no wildcards.
   *
   * @return the list of reference types that are arguments to this type
   */
  private List<ReferenceType> getReferenceArguments() {
    List<ReferenceType> referenceArgList = new ArrayList<>();
    for (TypeArgument argument : argumentList) {
      if (!argument.isWildcard()) {
        referenceArgList.add(((ReferenceArgument) argument).getReferenceType());
      } else {
        throw new IllegalArgumentException("cannot convert a wildcard to a reference type");
      }
    }
    return referenceArgList;
  }
}
