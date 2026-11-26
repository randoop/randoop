package randoop.types;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.plumelib.util.StringsPlume;

/**
 * Represents a class or interface type as defined in JLS Section 4.3.
 *
 * <p>This abstract class corresponds to this grammar production in the JLS:
 *
 * <pre>
 *   ClassOrInterfaceType:
 *     ClassType
 *     InterfaceType
 * </pre>
 *
 * InterfaceType is syntactically the same as ClassType. Therefore, {@code ClassType} and {@code
 * InterfaceType} do not exist as subclasses of this class. Rather, the subclasses of this type
 * distinguish between types with parameters ({@link ParameterizedType}), and types without ({@link
 * NonParameterizedType}).
 */
public abstract class ClassOrInterfaceType extends ReferenceType {

  /** Set to true to enable debug output to standard out. */
  private static boolean debug = false;

  /**
   * The enclosing type. Non-null only if this is a nested type (either a member type or a nested
   * static type).
   */
  protected @Nullable ClassOrInterfaceType enclosingType = null;

  /** Creates a ClassOrInterfaceType. */
  public ClassOrInterfaceType() {
    super();
  }

  /**
   * Translates a {@code Class} object that represents a class or interface into a {@code
   * ClassOrInterfaceType} object. If the object has parameters, then delegates to {@link
   * ParameterizedType#forClass(Class)}. Otherwise, creates a {@link NonParameterizedType} object
   * from the given object.
   *
   * @param classType the class type to translate
   * @return the {@code ClassOrInterfaceType} object created from the given class type
   */
  public static ClassOrInterfaceType forClass(Class<?> classType) {
    if (classType.isArray() || classType.isPrimitive()) {
      throw new IllegalArgumentException("type must be a class or interface, got " + classType);
    }

    ClassOrInterfaceType type;
    if (classType.getTypeParameters().length > 0) {
      type = ParameterizedType.forClass(classType);
    } else {
      type = NonParameterizedType.forClass(classType);
    }
    Class<?> enclosingClass = classType.getEnclosingClass();
    if (enclosingClass != null) {
      type.enclosingType = forClass(enclosingClass);
    }
    return type;
  }

  /**
   * Creates a {@code ClassOrInterfaceType} object for a given {@code java.lang.reflect.Type}
   * reference. If type is a {@code java.lang.reflect.ParameterizedType}, then calls {@link
   * ParameterizedType#forType(java.lang.reflect.Type)}. Otherwise, if type is a {@code Class}
   * object, calls {@link #forClass(Class)}.
   *
   * @param type the type reference
   * @return the {@code ClassOrInterfaceType} object for the given type
   */
  public static ClassOrInterfaceType forType(java.lang.reflect.Type type) {

    if (type instanceof java.lang.reflect.ParameterizedType) {
      java.lang.reflect.ParameterizedType t = (java.lang.reflect.ParameterizedType) type;
      Class<?> rawType = (Class<?>) t.getRawType();
      // If the type arguments of the owner type, and of its own owner types, are the type
      // parameters of the declarations of the enclosing classes, then the enclosing type from the
      // declaration of the member class is correct, and getPreservedOwnerType returns null.
      // Otherwise, the owner type is preserved.
      java.lang.reflect.ParameterizedType ownerType = getPreservedOwnerType(t);
      // A member class with no type parameters of its own, such as Outer<E>.Inner, shows up as a
      // ParameterizedType whose owner type supplies the type arguments.
      if (rawType.getTypeParameters().length == 0) {
        if (ownerType != null) {
          // Use the owner type rather than the enclosing class, because the owner type's type
          // arguments may differ from the enclosing class's type parameters.  For example, in
          // `<E> void m(Outer<E>.Inner inner)`, the owner type is `Outer<E>` where `E` is the
          // method's type parameter, not the type parameter declared by `Outer`.
          // Do not use NonParameterizedType.forClass, which returns a shared cached object.
          NonParameterizedType result = new NonParameterizedType(rawType);
          result.enclosingType = forType(ownerType);
          return result;
        }
        return forClass(rawType);
      }
      // For a generic member class, such as Middle in Outer<String>.Middle<Integer>,
      // ParameterizedType.forType keeps the owner type, whose type arguments its own member
      // classes and supertypes may use.  It also sets the enclosing type of a member of a
      // non-generic class, such as NonGenericOuter.Base<String>, and supplies an enclosing type
      // for an owner type in declaration form, as in Outer<T>.Middle<Integer>.
      return ParameterizedType.forType(t);
    }

    if (type instanceof Class<?>) {
      Class<?> classType = (Class<?>) type;
      // if the type is generic, forcing the type to be a parameterized type can result in errors
      // because type parameters will be from the class declaration rather than the context of
      // the type in the code.  In this case, it is possible to have two distinct
      // java.lang.reflect.TypeVariables that represent the same type parameter.
      //
      return new NonParameterizedType(classType);
    }

    throw new IllegalArgumentException("Unable to create class type from type " + type);
  }

  /**
   * Returns the owner type of the given parameterized type, if the conversion of the type to a
   * {@link ClassOrInterfaceType} uses that owner type as the enclosing type. This is so if the type
   * is a member class (not a static nested class) and the owner type is a parameterized type that
   * is not the type that the declaration of the enclosing class denotes. For example, the result
   * for {@code Outer<String>.Inner} is {@code Outer<String>}. Otherwise, returns null, and the
   * conversion uses the enclosing type of the class declaration.
   *
   * @param type a parameterized type
   * @return the owner type to use as the enclosing type, or null
   */
  /*package*/ static java.lang.reflect.@Nullable ParameterizedType getPreservedOwnerType(
      java.lang.reflect.ParameterizedType type) {
    java.lang.reflect.Type ownerType = type.getOwnerType();
    // If the type arguments of the owner type, and of its own owner types, are the type parameters
    // of the declarations of the enclosing classes, the class declaration gives the enclosing type.
    if (ownerType instanceof java.lang.reflect.ParameterizedType
        && !java.lang.reflect.Modifier.isStatic(((Class<?>) type.getRawType()).getModifiers())
        && !isDeclarationForm((java.lang.reflect.ParameterizedType) ownerType)) {
      return (java.lang.reflect.ParameterizedType) ownerType;
    }
    return null;
  }

  /**
   * Returns true if the given parameterized type is the type that its class declaration denotes:
   * its type arguments are the type parameters of its class, and, if it is a member class, the same
   * holds for its owner type. For example, this returns false for {@code Outer<String>.Middle},
   * where {@code Middle} declares no type parameters, though its own type arguments are the (empty)
   * type parameters of {@code Middle}.
   *
   * @param type a parameterized type
   * @return true if {@code type} is the type that its class declaration denotes
   */
  /*package*/ static boolean isDeclarationForm(java.lang.reflect.ParameterizedType type) {
    Class<?> rawType = (Class<?>) type.getRawType();
    if (!Arrays.equals(type.getActualTypeArguments(), rawType.getTypeParameters())) {
      return false;
    }
    java.lang.reflect.Type ownerType = type.getOwnerType();
    return !(ownerType instanceof java.lang.reflect.ParameterizedType)
        || java.lang.reflect.Modifier.isStatic(rawType.getModifiers())
        || isDeclarationForm((java.lang.reflect.ParameterizedType) ownerType);
  }

  // This implementation is for an abstract class; subclasses do the real work.
  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ClassOrInterfaceType)) {
      return false;
    }
    ClassOrInterfaceType otherType = (ClassOrInterfaceType) obj;
    return Objects.equals(this.constrainingEnclosingType(), otherType.constrainingEnclosingType());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(constrainingEnclosingType());
  }

  /**
   * Returns the enclosing type of this type, or null if this type is not a member class (an inner
   * class, which is not static), has no enclosing type, or has an enclosing type that is the
   * generic form of its class. Equality and hashing use this rather than the enclosing type itself.
   * The enclosing type of a static nested class places no constraint on the type, and neither does
   * a generic-form enclosing type, such as {@code NonGenericOuter} in {@code
   * NonGenericOuter.Inner<Integer>} or {@code Outer<T>} in {@code Outer<T>.Inner<String>}, where
   * {@code T} is the type parameter of {@code Outer}; such an enclosing type is equivalent to no
   * enclosing type. Different construction paths yield either representation: some omit a
   * generic-form enclosing type, but others keep it, such as {@link
   * #forType(java.lang.reflect.Type)} for an owner type in declaration form and {@link
   * GenericClassType#instantiate(ReferenceType...)}.
   *
   * @return the enclosing type of this type, or null if it does not constrain this type
   */
  private @Nullable ClassOrInterfaceType constrainingEnclosingType() {
    if (!isMemberClass() || enclosingType == null || enclosingType.isGenericForm()) {
      return null;
    }
    return enclosingType;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This abstract method allows substitutions to be applied to {@link ClassOrInterfaceType}
   * objects without casting.
   */
  @Override
  public abstract ClassOrInterfaceType substitute(Substitution substitution);

  /**
   * Applies the substitution to the enclosing type of this type and adds the result as the
   * enclosing class of the given type.
   *
   * @param substitution the substitution to apply to the enclosing type
   * @param type the type to which resulting enclosing type is to be added; will be side-effected
   * @return the type with enclosing type added if needed
   */
  /*package*/ final ClassOrInterfaceType substitute(
      Substitution substitution, ClassOrInterfaceType type) {
    if (this.isMemberClass()) {
      type.enclosingType = enclosingType.substitute(substitution);
    }
    return type;
  }

  /**
   * Returns the result of applying the given substitution to the enclosing type of this type, if
   * this type is a member class. Otherwise, returns the enclosing type of this type, which is null
   * if this type is not nested. The result is the enclosing type itself if the substitution changes
   * nothing in it.
   *
   * <p>In particular, if the enclosing type is a {@link GenericClassType}, as for a reflected
   * {@code Outer<T>.Inner<String>}, and the substitution maps none of its type parameters to a
   * different type, then the result is that {@link GenericClassType}, rather than the equivalent
   * {@link InstantiatedType} {@code Outer<T>} that {@link GenericClassType#substitute} would
   * create. The two are not equal, so the substituted member type would not equal this one.
   *
   * @param substitution a substitution
   * @return the enclosing type of this type, with {@code substitution} applied if this type is a
   *     member class
   */
  /*package*/ final @Nullable ClassOrInterfaceType substituteEnclosingType(
      Substitution substitution) {
    if (!isMemberClass()) {
      return enclosingType;
    }
    ClassOrInterfaceType enclosing = enclosingType;
    if (enclosing instanceof GenericClassType
        && !mapsToOtherType(enclosing.getTypeParameters(), substitution)) {
      return enclosing;
    }
    return enclosing.substitute(substitution);
  }

  /**
   * Returns true if the given substitution maps some of the given type variables to a type other
   * than that type variable object itself. A copy of a type variable with other bounds is another
   * type.
   *
   * @param variables type variables
   * @param substitution a substitution
   * @return true if {@code substitution} maps some element of {@code variables} to another type
   */
  /*package*/ static boolean mapsToOtherType(
      List<TypeVariable> variables, Substitution substitution) {
    for (TypeVariable variable : variables) {
      ReferenceType type = substitution.get(variable);
      if (type != null && !sameType(type, variable)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public abstract ClassOrInterfaceType applyCaptureConversion();

  /**
   * Applies capture conversion to the enclosing type of this type and adds the result as the
   * enclosing class of the given type.
   *
   * @param type this type with capture conversion applied; will be side-effected
   * @return the type with converted enclosing type
   */
  /*package*/ final ClassOrInterfaceType applyCaptureConversion(ClassOrInterfaceType type) {
    if (this.isMemberClass()) {
      type.enclosingType = enclosingType.applyCaptureConversion();
    }
    return type;
  }

  /**
   * Returns the name of this class type. Does not include package, enclosing classes, or type
   * arguments.
   *
   * @return the name of this class
   */
  @Override
  public String getSimpleName() {
    return getRuntimeClass().getSimpleName();
  }

  @Override
  public @Nullable String getCanonicalName() {
    return getRuntimeClass().getCanonicalName();
  }

  @Override
  public @Nullable String getFqName() {
    if (this.isNestedClass()) {
      if (this.isStatic()) {
        return enclosingType.getCanonicalName() + "." + this.getSimpleName();
      }
      return enclosingType.getFqName() + "." + this.getSimpleName();
    }
    return this.getCanonicalName();
  }

  @Override
  public String getBinaryName() {
    if (this.isNestedClass()) {
      if (this.isStatic()) {
        // HACK
        return enclosingType.getBinaryName().replaceAll("<[^<]*>$", "")
            + "$"
            + this.getSimpleName();
      }
      return enclosingType.getBinaryName() + "$" + this.getSimpleName();
    }
    return getRuntimeClass().getName();
  }

  @Override
  public String getUnqualifiedBinaryName() {
    String prefix = "";
    if (this.isNestedClass()) {
      prefix = enclosingType.getUnqualifiedBinaryName() + "$";
    }
    return prefix + this.getSimpleName();
  }

  /**
   * Returns the interface types directly implemented by this class or interface type. Preserves the
   * order in the reflection method {@link Class#getGenericInterfaces()}. If no interfaces are
   * implemented, then returns the empty list.
   *
   * @return the list of interfaces directly implemented by this type
   */
  public abstract List<ClassOrInterfaceType> getInterfaces();

  /**
   * Returns the package of the runtime class of this type.
   *
   * @return the package of the runtime class of this type, or null if there is none
   */
  public @Nullable Package getPackage() {
    Class<?> c = getRuntimeClass();
    if (c == null) {
      throw new IllegalArgumentException("Class " + this.toString() + " has no runtime class");
    }
    return c.getPackage();
  }

  /**
   * Returns the package part of a type name, including the final period. Returns the empty string
   * for a type in the unnamed package.
   *
   * @return the package part of a type name, or ""
   */
  /*package*/ String getPackagePrefix() {
    Package pkg = getPackage();
    if (pkg == null) {
      return "";
    } else {
      return pkg.getName() + ".";
    }
  }

  /**
   * Returns the non-parameterized form of this class type.
   *
   * @return the non-parameterized form of this class type
   */
  @Override
  public abstract NonParameterizedType getRawtype();

  /**
   * Finds the parameterized type that is a supertype of this class that also matches the given
   * generic class. For example, if {@code class C<T> implements Comparator<T>} and {@code class A
   * extends C<String>}, then when applied to {@code A}, this method would return {@code C<String>}
   * when given {@code C<T>}, and {@code Comparator<String>} when given {@code Comparator<E>}.
   * Returns null if there is no such type.
   *
   * <p>Performs a depth-first search of the supertype relation for this type. If the goal type is
   * an interface, then searches the interfaces of this type first.
   *
   * @param goalType the generic class type
   * @return the instantiated type matching the goal type, or null
   */
  public @Nullable InstantiatedType getMatchingSupertype(GenericClassType goalType) {
    if (goalType.isInterface()) {
      for (ClassOrInterfaceType interfaceType : this.getInterfaces()) {
        if (goalType.getRuntimeClass().isAssignableFrom(interfaceType.getRuntimeClass())) {
          // A member interface that declares no type parameters, of a parameterized type, is
          // parameterized but is not an InstantiatedType.
          if (interfaceType instanceof InstantiatedType) {
            InstantiatedType type = (InstantiatedType) interfaceType;
            if (type.runtimeClassIs(goalType.getRuntimeClass())
                && type.isInstantiationOf(goalType)) {
              return (InstantiatedType) interfaceType;
            }
            InstantiatedType result = type.getMatchingSupertype(goalType);
            if (result != null) {
              return result;
            }
          } else {
            return interfaceType.getMatchingSupertype(goalType);
          }
        }
      }
    }

    ClassOrInterfaceType superclass = this.getSuperclass();
    if (superclass != null
        && !superclass.isObject()
        && goalType.getRuntimeClass().isAssignableFrom(superclass.getRuntimeClass())) {

      // isInstantiationOf can be true for a member class of another class, if the enclosing types
      // match, so also check the class.
      if (superclass.runtimeClassIs(goalType.getRuntimeClass())) {
        // A raw superclass, such as Box for `class RawChild extends Box`, is not an
        // InstantiatedType and does not instantiate the goal type.  Neither do its supertypes,
        // which are erased.
        return superclass instanceof InstantiatedType && superclass.isInstantiationOf(goalType)
            ? (InstantiatedType) superclass
            : null;
      }

      return superclass.getMatchingSupertype(goalType);
    }

    return null;
  }

  @Override
  public @Nullable Substitution getInstantiatingSubstitution(ReferenceType goalType) {
    Substitution superResult = getInstantiatingSubstitutionforTypeVariable(this, goalType);
    if (superResult != null) {
      return superResult;
    }
    if (goalType.isVariable()) {
      // The bounds of the type variable reject this type.
      return null;
    }

    assert goalType.isGeneric() : "goal type must be generic";

    // For a generic class goal type, the supertype of this type that instantiates it.  It is
    // computed once, because each computation of a supertype of a type with a wildcard argument
    // captures the wildcard anew:  the enclosing type matched below and the type substitution of
    // the supertype must come from the same capture.
    InstantiatedType supertype = null;
    if (goalType instanceof GenericClassType) {
      supertype = this.getMatchingSupertype((GenericClassType) goalType);
      // No supertype instantiates the goal type, as for a class that extends the raw type of the
      // goal type's class, so no substitution binds the goal type's type parameters.
      if (supertype == null) {
        return null;
      }
    }

    Substitution substitution = new Substitution();
    // For a member class such as Outer<String>.Inner and a member class goal type such as
    // Outer<E>.Inner, the enclosing type of this type instantiates the enclosing type of the goal
    // type.  This type need not itself be a member class:  for a top-level class that extends
    // Outer<String>.Inner, the enclosing type of that supertype instantiates the goal's.  No
    // enclosing type is matched against a goal type that is not a member class, such as a type
    // variable.
    if (goalType instanceof ClassOrInterfaceType) {
      ClassOrInterfaceType goalClassType = (ClassOrInterfaceType) goalType;
      if (goalClassType.isMemberClass()) {
        ClassOrInterfaceType enclosingGoalType = goalClassType.enclosingType;
        // Matching enclosing types do not make unrelated member classes, such as
        // Outer<String>.Inner and Outer<E>.Other, unify.
        if (!goalClassType.getRuntimeClass().isAssignableFrom(this.getRuntimeClass())) {
          return null;
        }
        ClassOrInterfaceType thisEnclosingType =
            supertype == null
                ? getEnclosingTypeFor(goalClassType)
                : (supertype.isMemberClass() ? supertype.enclosingType : enclosingType);
        if (thisEnclosingType instanceof GenericClassType
            && thisEnclosingType.equals(enclosingGoalType)) {
          // The enclosing type is the generic form of the goal's enclosing class, as for
          // Outer<T>.Inner<String> and the goal Outer<T>.Inner<U>.  A generic type matches itself
          // and binds no type variable.  getInstantiatingSubstitution would fail, because
          // getMatchingSupertype searches only the strict supertypes of a GenericClassType.
          // InstantiatedType#getInstantiatingSubstitution rejects a binding, by a type argument,
          // of a type variable that this enclosing type fixes; see
          // getFixedEnclosingTypeParameters.
        } else if (enclosingGoalType.isGeneric() && thisEnclosingType != null) {
          substitution = thisEnclosingType.getInstantiatingSubstitution(enclosingGoalType);
          if (substitution == null) {
            return null;
          }
        } else if (enclosingGoalType.isParameterized()
            && thisEnclosingType != null
            && !thisEnclosingType.isSubtypeOfOrEqualTo(enclosingGoalType)) {
          // No substitution instantiates a non-generic enclosing type:  the type arguments of the
          // enclosing goal type are fixed, as in Outer<String>.Inner<X>, and the enclosing type of
          // this type's view does not match them, as in Outer<Integer>.Inner<String>.  The
          // enclosing types of the enclosing types are compared too:
          // Outer<String>.Middle<Integer> is not Outer<Number>.Middle<Integer>.
          return null;
        }
      }
    }

    if (supertype != null) {
      // The type substitution of the supertype maps the type parameters of the goal type,
      // including those of its enclosing classes.  The enclosing type of the supertype can differ
      // from that of this type, as for Outer<String>.Sub that extends Outer<Integer>.Inner; the
      // substitution above is from the enclosing type of the supertype, so it is consistent with
      // the supertype's substitution.
      Substitution supertypeSubstitution = supertype.getTypeSubstitution();
      if (supertypeSubstitution == null || !substitution.isConsistentWith(supertypeSubstitution)) {
        return null;
      }
      substitution = substitution.extend(supertypeSubstitution);
    } else if (goalType instanceof InstantiatedType && this instanceof NonParameterizedType) {
      // The goal type has type arguments of its own, which this type, such as a class Owner.Child
      // that extends Owner.Base<String>, does not bind directly.  Its supertype of the goal type's
      // class binds them.  A raw type, such as Owner.Base, has no such supertype.
      // InstantiatedType#getInstantiatingSubstitution handles the type arguments of an
      // InstantiatedType receiver.
      InstantiatedType goalInstantiatedType = (InstantiatedType) goalType;
      InstantiatedType matchingSupertype =
          this.getMatchingSupertype(goalInstantiatedType.getGenericClassType());
      if (matchingSupertype == null) {
        return null;
      }
      Substitution supertypeSubstitution =
          matchingSupertype.getInstantiatingSubstitution(goalInstantiatedType);
      if (supertypeSubstitution == null || !substitution.isConsistentWith(supertypeSubstitution)) {
        return null;
      }
      substitution = substitution.extend(supertypeSubstitution);
    }
    return substitution;
  }

  /**
   * Returns the enclosing type of this type's view of the class of the given member class type. It
   * differs from this type's own enclosing type if a supertype is a member of another instantiation
   * of the enclosing class: if {@code Outer<T>.Derived<V>} extends {@code Outer<String>.Base<V>},
   * then for {@code Outer<Integer>.Derived<Long>} and the class of {@code Outer<T>.Base<U>}, the
   * result is {@code Outer<String>}. A supertype whose enclosing type is not given explicitly, as
   * when Derived extends {@code Base<V>}, has the enclosing type of this type.
   *
   * @param goalClassType a member class type whose class is that of this type or of a supertype
   * @return the enclosing type of this type's view of the class of {@code goalClassType}, or null
   *     if no supertype of this type is of that class
   */
  private @Nullable ClassOrInterfaceType getEnclosingTypeFor(ClassOrInterfaceType goalClassType) {
    if (this.runtimeClassIs(goalClassType.getRuntimeClass())) {
      return enclosingType;
    }
    for (ClassOrInterfaceType supertype : this.getSupertypesStrict()) {
      if (supertype.runtimeClassIs(goalClassType.getRuntimeClass())) {
        return supertype.isMemberClass() ? supertype.enclosingType : enclosingType;
      }
    }
    return null;
  }

  /**
   * Returns the type variables that a match of this type against the given goal type must leave
   * unchanged. These are the type parameters of the enclosing type of {@code goalType}, if that
   * enclosing type is the generic form of its class and this type's view of the goal type's class
   * has the same enclosing type, as for {@code Outer<T>.Inner<String>} and the goal {@code
   * Outer<T>.Inner<U>}. {@link #getInstantiatingSubstitution(ReferenceType)} matches such an
   * enclosing type to itself, so a type argument may not bind its type variables to another type:
   * {@code Outer<T>.Inner<String>} does not instantiate {@code Outer<T>.Inner<T>}. Otherwise,
   * returns the empty list.
   *
   * @param goalType the goal type of a match
   * @return the type variables that the match must not map to another type
   */
  /*package*/ final List<TypeVariable> getFixedEnclosingTypeParameters(ReferenceType goalType) {
    if (!(goalType instanceof ClassOrInterfaceType)) {
      return Collections.emptyList();
    }
    ClassOrInterfaceType goalClassType = (ClassOrInterfaceType) goalType;
    if (!goalClassType.isMemberClass()
        || !(goalClassType.enclosingType instanceof GenericClassType)
        || !goalClassType.getRuntimeClass().isAssignableFrom(this.getRuntimeClass())) {
      return Collections.emptyList();
    }
    ClassOrInterfaceType enclosingGoalType = goalClassType.enclosingType;
    if (!enclosingGoalType.equals(getEnclosingTypeFor(goalClassType))) {
      return Collections.emptyList();
    }
    return enclosingGoalType.getTypeParameters();
  }

  /**
   * Returns the type for the superclass for this class. Returns null if this type has no superclass
   * (it is the Object type or an interface type).
   *
   * @return the superclass of this type, or null
   */
  public abstract @Nullable ClassOrInterfaceType getSuperclass();

  /**
   * Returns the immediate supertypes of this type.
   *
   * @return the immediate supertypes of this type
   */
  @SuppressWarnings("MixedMutabilityReturnType")
  public List<ClassOrInterfaceType> getImmediateSupertypes() {
    if (this.isObject()) {
      return Collections.emptyList();
    }
    ClassOrInterfaceType superclass = this.getSuperclass();
    List<ClassOrInterfaceType> interfaces = this.getInterfaces();
    List<ClassOrInterfaceType> supertypes = new ArrayList<>(interfaces.size() + 1);
    if (superclass != null) {
      supertypes.add(superclass);
    }
    supertypes.addAll(interfaces);
    return supertypes;
  }

  /**
   * Returns a set of all of the strict supertypes of this type. The result contains no duplicates
   * and does not contain this type itself.
   *
   * @return the set of all supertypes of this type
   */
  public Set<ClassOrInterfaceType> getSupertypesStrict() {
    return getSupertypes(false);
  }

  /**
   * Returns a set containing this type and all its supertypes. The result contains no duplicates.
   *
   * @return this type and all its supertypes
   */
  public Set<ClassOrInterfaceType> getSupertypesInclusive() {
    return getSupertypes(true);
  }

  /**
   * Returns a set containing the supertypes of this type. If {@code includeSelf} is true, the set
   * also contains this type itself; otherwise it contains only the strict supertypes. The set
   * contains no duplicates.
   *
   * @param includeSelf if true, the result contains this type as well as all supertypes
   * @return the set of all supertypes of this type
   */
  public Set<ClassOrInterfaceType> getSupertypes(boolean includeSelf) {
    Set<ClassOrInterfaceType> result = new LinkedHashSet<>();
    Queue<ClassOrInterfaceType> worklist = new ArrayDeque<>();
    worklist.add(this);
    while (!worklist.isEmpty()) {
      ClassOrInterfaceType t = worklist.remove();
      if (result.add(t)) {
        // An interface may be added to the worklist multiple times, but it will only appear
        // once in the result.  It doesn't seem worthwhile to test, here, whether the interface
        // has already been seen, since adding it to the result set does that same test.
        // Processing interfaces before classes does reduce duplication within the worklist.
        worklist.addAll(t.getInterfaces());
        ClassOrInterfaceType superclass = t.getSuperclass();
        if (superclass != null) {
          worklist.add(superclass);
        }
      }
    }
    if (!includeSelf) {
      result.remove(this);
    }
    return result;
  }

  /**
   * Returns true if this class is abstract.
   *
   * @return true if this class is abstract, false otherwise
   */
  public abstract boolean isAbstract();

  @Override
  public boolean isGeneric(boolean ignoreWildcards) {
    return this.isMemberClass() && enclosingType.isGeneric(ignoreWildcards);
  }

  /**
   * {@inheritDoc}
   *
   * <p>For a {@link ClassOrInterfaceType} that is a member class, if {@code otherType} is also a
   * member class, then the enclosing type of this type must instantiate the enclosing type of
   * {@code otherType}.
   */
  @Override
  public boolean isInstantiationOf(ReferenceType otherType) {
    if (super.isInstantiationOf(otherType)) {
      return true;
    }
    if (this.isNestedClass() && (otherType instanceof ClassOrInterfaceType)) {
      ClassOrInterfaceType otherClassType = (ClassOrInterfaceType) otherType;
      // TODO: This checks that both are member classes, but they should be named the same and with
      // the same type parameters too.
      return otherClassType.isNestedClass()
          && this.enclosingType.isInstantiationOf(otherClassType.enclosingType);
    }
    return false;
  }

  /**
   * Returns true if this class is a nested class.
   *
   * @return true iff this class is a nested class
   */
  @Pure
  @EnsuresNonNullIf(expression = "enclosingType", result = true)
  public final boolean isNestedClass() {
    return enclosingType != null;
  }

  /**
   * Returns true if this class is a member of another class. (See <a
   * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html#jls-8.5">JLS section
   * 8.5</a>).
   *
   * @return true if this class is a member class, false otherwise
   */
  @Pure
  @EnsuresNonNullIf(expression = "enclosingType", result = true)
  public final boolean isMemberClass() {
    return !isStatic() && isNestedClass();
  }

  /**
   * Returns true if this type is the generic form of its class: the type that the class declaration
   * denotes, in which the type arguments of the class and of its enclosing types are their own type
   * parameters. Examples are {@code List<E>}, {@code Outer<T>.Inner}, and {@code String}. Raw
   * {@code List} and {@code List<T>} for a type variable {@code T} other than the type parameter of
   * {@code List} are not generic forms.
   *
   * @return true if this type is the generic form of its class
   */
  public boolean isGenericForm() {
    return !isMemberClass() || enclosingType.isGenericForm();
  }

  @Override
  @EnsuresNonNullIf(expression = "enclosingType", result = true)
  public boolean isParameterized() {
    return this.isMemberClass() && enclosingType.isParameterized();
  }

  /**
   * Returns true if this class is static.
   *
   * @return true if this class is static, false otherwise
   */
  public abstract boolean isStatic();

  /**
   * Returns true if this type is a subtype of the given type according to transitive closure of
   * definition of the <i>direct supertype</i> relation in <a
   * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.10.2">section 4.10.2
   * of JLS for JavaSE 8</a>.
   *
   * <p>Returns true if {@code otherType} is the same type as this type.
   *
   * @param otherType the possible supertype
   * @return true if this type is a subtype of the given type, false otherwise
   * @see #isAssignableFrom(Type)
   * @see ParameterizedType#isSubtypeOfOrEqualTo(Type)
   */
  @Override
  public boolean isSubtypeOfOrEqualTo(Type otherType) {
    // TODO: Cache and look up results, for efficiency?
    if (debug) {
      System.out.printf(
          "isSubtypeOfOrEqualTo(%s, %s) [%s, %s]%n",
          this, otherType, this.getClass(), otherType.getClass());
    }

    // This handles Object, this type itself, and a type variable whose lower bound is a supertype
    // of this type.
    if (super.isSubtypeOfOrEqualTo(otherType)) {
      return true;
    }

    if (!(otherType instanceof ClassOrInterfaceType)) {
      return false;
    }
    ClassOrInterfaceType otherRefType = (ClassOrInterfaceType) otherType;
    // For a member class that declares no type parameters, of a parameterized type, the type
    // arguments of the enclosing types are compared by containment, not equality:
    // O<String>.Plain is a subtype of O<?>.Plain.  Subtyping compares each enclosing type.
    if (otherRefType instanceof NonParameterizedType
        && otherRefType.isParameterized()
        && Subtyping.isSubtypeOrEqual(this, otherRefType, new Substitution())) {
      return true;
    }
    if (otherRefType.isInterface()) {
      return this.isSubinterfaceOf(otherRefType);
    } else {
      return this.isSubclassOf(otherRefType);
    }
  }

  /**
   * Returns true if this type is a subtype of the given interface.
   *
   * <p>The receiver type may be a class or an interface.
   *
   * @param otherInterface an interface that might be a supertype of this
   * @return true if this type implements the given interface, false otherwise
   */
  public boolean isSubinterfaceOf(ClassOrInterfaceType otherInterface) {
    if (debug) {
      System.out.printf(
          "isSubinterfaceOf(%s, %s) [%s, %s]%n",
          this, otherInterface, this.getClass(), otherInterface.getClass());
    }

    assert otherInterface.isInterface();

    ClassOrInterfaceType superclass = this.getSuperclass();
    if (superclass != null) {
      if (superclass.isSubinterfaceOf(otherInterface)) {
        return true;
      }
    }

    for (ClassOrInterfaceType iface : getInterfaces()) { // directly implemented interfaces
      if (debug) {
        System.out.printf("  iface: %s%n", StringsPlume.toStringAndClass(iface));
      }

      if (iface.equals(otherInterface)) {
        return true;
      }
      // TODO: Use iteration rather than recursion, for efficiency?
      if (iface.isSubinterfaceOf(otherInterface)) {
        if (debug) {
          System.out.printf(
              "isSubinterfaceOf(%s, %s) [%s, %s] => true%n",
              this, otherInterface, this.getClass(), otherInterface.getClass());
        }
        return true;
      }
    }

    if (debug) {
      System.out.printf(
          "isSubinterfaceOf(%s, %s) [%s, %s] => false%n",
          this, otherInterface, this.getClass(), otherInterface.getClass());
    }
    return false;
  }

  /**
   * Returns true if this type is a strict subclass of the given class.
   *
   * @param otherClass the possible supertype
   * @return true if this type is a subclass of the given type, false otherwise
   */
  public boolean isSubclassOf(ClassOrInterfaceType otherClass) {
    if (debug) {
      System.out.printf(
          "isSubclassOf(%s, %s) [%s, %s]%n",
          this, otherClass, this.getClass(), otherClass.getClass());
    }

    assert !otherClass.isInterface();
    if (this.isInterface()) {
      return false;
    }

    if (otherClass.isObject()) {
      return true;
    }

    // InstantiatedType.equals ignores enclosing types, so compare them too:  a class that
    // extends Outer<String>.Inner<Integer> is not a subclass of Outer<Number>.Inner<Integer>.
    if (isSameWithEnclosingTypes(this, otherClass)) {
      return true;
    }
    for (ClassOrInterfaceType current = this.getSuperclass();
        current != null;
        current = current.getSuperclass()) {
      if (isSameWithEnclosingTypes(current, otherClass)) {
        return true;
      }
      // A strict superclass of the same class as otherClass can be a subtype of otherClass without
      // being equal to it, as Outer<String>.Inner is a subtype of Outer<?>.Inner.  The recursive
      // call does not recurse further, because the strict superclasses of current are of other
      // classes.
      if (current.runtimeClassIs(otherClass.getRuntimeClass())) {
        return current.isSubtypeOfOrEqualTo(otherClass);
      }
    }
    return false;
  }

  /**
   * Returns true if the given types are equal, and so are their enclosing types at each level at
   * which both are member classes. {@link InstantiatedType#equals(Object)} does not compare
   * enclosing types, so it treats {@code Outer<String>.Inner<Integer>} and {@code
   * Outer<Number>.Inner<Integer>} as equal.
   *
   * <p>A type that is not a member class matches any enclosing type. A conversion of a reflection
   * type whose owner type is in declaration form, such as {@code Outer<T>.Inner<Integer>}, records
   * no enclosing type, but stands for the same class as one that does.
   *
   * @param type a type
   * @param otherType another type
   * @return true if the types are equal, including their enclosing types where both have one
   */
  /*package*/ static boolean isSameWithEnclosingTypes(
      ClassOrInterfaceType type, ClassOrInterfaceType otherType) {
    ClassOrInterfaceType current = type;
    ClassOrInterfaceType otherCurrent = otherType;
    if (!current.equals(otherCurrent)) {
      return false;
    }
    while (current.isMemberClass() && otherCurrent.isMemberClass()) {
      current = current.enclosingType;
      otherCurrent = otherCurrent.enclosingType;
      if (!current.equals(otherCurrent)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if, at each level at which both of the given types are member classes, the
   * enclosing type of {@code type} instantiates the enclosing type of {@code goalType}, as
   * determined by {@link #isInstantiationOf(ReferenceType)}. {@link
   * InstantiatedType#isInstantiationOf(ReferenceType)} compares only the type arguments of the
   * types themselves, not those of their enclosing types.
   *
   * @param type a type
   * @param goalType a type, which may be generic
   * @return true if each enclosing type of {@code type} instantiates the corresponding enclosing
   *     type of {@code goalType}
   */
  /*package*/ static boolean enclosingTypesInstantiate(
      ClassOrInterfaceType type, ClassOrInterfaceType goalType) {
    ClassOrInterfaceType current = type;
    ClassOrInterfaceType goalCurrent = goalType;
    while (current.isMemberClass() && goalCurrent.isMemberClass()) {
      current = current.enclosingType;
      goalCurrent = goalCurrent.enclosingType;
      if (!current.isInstantiationOf(goalCurrent)) {
        return false;
      }
    }
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * <p>For a member class, this includes wildcards in the enclosing type, as in {@code
   * Outer<?>.Inner}.
   */
  @Override
  public boolean hasWildcard() {
    return isMemberClass() && enclosingType.hasWildcard();
  }

  /**
   * Returns true if some type argument of this type, or of an enclosing type, is a wildcard. These
   * are the wildcards that {@link #applyCaptureConversion()} converts. Unlike {@link
   * #hasWildcard()}, does not look within type arguments.
   *
   * @return true if some type argument of this type or of an enclosing type is a wildcard
   */
  public boolean hasWildcardArgument() {
    return isMemberClass() && enclosingType.hasWildcardArgument();
  }

  @Override
  public boolean hasCaptureVariable() {
    return isMemberClass() && enclosingType.hasCaptureVariable();
  }

  /**
   * Returns the type arguments for this type.
   *
   * @return the list of type arguments
   */
  public List<TypeArgument> getTypeArguments() {
    return new ArrayList<>(0);
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    if (this.isMemberClass()) {
      return enclosingType.getTypeParameters();
    }
    return new ArrayList<>(0);
  }

  /**
   * Returns the substitution that maps the type parameters of this type's class, and of the classes
   * that enclose it as a member class, to the corresponding type arguments of this type. For
   * example, for {@code Outer<String>.Inner<Integer>}, where {@code Inner<U>} is a member class of
   * {@code Outer<T>}, the result maps {@code T} to {@code String} and {@code U} to {@code Integer}.
   * The result omits the type parameters of a generic form, such as {@code Outer<T>}. A wildcard
   * type argument is mapped as in {@link InstantiatedType#getReferenceArguments()}.
   *
   * @return the substitution that maps the type parameters of this type's class and of its
   *     enclosing classes to the type arguments of this type
   */
  /*package*/ Substitution getReferenceArgumentSubstitution() {
    if (this.isMemberClass()) {
      return enclosingType.getReferenceArgumentSubstitution();
    }
    return new Substitution();
  }

  /**
   * Returns the substitution to apply to the declared supertypes of this type's class. It is like
   * {@link #getReferenceArgumentSubstitution()}, but it maps the type parameters of the enclosing
   * classes to the type arguments of the capture conversion of the enclosing type, because the
   * supertypes of a type with wildcard arguments are those of its capture conversion. For example,
   * for {@code Outer<? extends Number>.Inner}, where {@code Inner} is a member class of {@code
   * Outer<T>}, the result maps {@code T} to a capture variable whose upper bound is {@code Number}.
   *
   * @return the substitution to apply to the declared supertypes of this type's class
   */
  /*package*/ Substitution getSupertypeSubstitution() {
    if (this.isMemberClass()) {
      return enclosingType.applyCaptureConversion().getReferenceArgumentSubstitution();
    }
    return new Substitution();
  }

  @Override
  public boolean isClassOrInterfaceType() {
    return true;
  }
}
