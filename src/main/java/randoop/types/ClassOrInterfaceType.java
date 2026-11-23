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
      // A member class with no type parameters of its own, such as Outer<E>.Inner, shows up as a
      // ParameterizedType whose owner type supplies the type arguments.
      Class<?> rawType = (Class<?>) t.getRawType();
      // The enclosing type of the result, converted from the owner type, such as Outer<E> in
      // Outer<E>.Inner or Outer<E>.Middle<String> in Outer<E>.Middle<String>.Inner.  It is null if
      // the owner type supplies no type arguments or is the generic form of its class.  Use the
      // owner type rather than the enclosing class, because the owner type's type arguments may
      // differ from the enclosing class's type parameters.  For example, in
      // `<E> void m(Outer<E>.Inner inner)`, the owner type is `Outer<E>` where `E` is the method's
      // type parameter, not the type parameter declared by `Outer`.
      ClassOrInterfaceType ownerClassType = null;
      java.lang.reflect.ParameterizedType ownerType = convertedOwnerType(t);
      if (ownerType != null) {
        ownerClassType = forType(ownerType);
      }
      if (rawType.getTypeParameters().length == 0) {
        if (ownerClassType == null) {
          // The result is the generic form of the member class.
          return forClass(rawType);
        }
        return forMemberClass(rawType, ownerClassType);
      }
      ParameterizedType result = ParameterizedType.forType(t);
      if (ownerClassType != null) {
        result.enclosingType = ownerClassType;
      } else if (t.getOwnerType() instanceof Class<?>
          && !java.lang.reflect.Modifier.isStatic(rawType.getModifiers())) {
        // The owner type supplies no type arguments, as for NonGenericOuter.Base<String>.  Set the
        // enclosing type as GenericClassType#substitute does for a member class, so that the two
        // representations of the type are equal.
        result.enclosingType = forClass((Class<?>) t.getOwnerType());
      }
      return result;
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
   * Returns true if the given type is the form of its class that the class declaration denotes: the
   * type arguments of the type and of each of its owner types are the type parameters declared by
   * the corresponding class. For example, {@code Outer<T>.Middle} is, where {@code T} is the type
   * parameter declared by {@code Outer}, but {@code Outer<U>.Middle} and {@code Outer<String>} are
   * not.
   *
   * @param type a parameterized type
   * @return true if {@code type} and its owner types have their declared type parameters as type
   *     arguments
   */
  private static boolean isDeclarationForm(java.lang.reflect.ParameterizedType type) {
    if (!Arrays.equals(
        type.getActualTypeArguments(), ((Class<?>) type.getRawType()).getTypeParameters())) {
      return false;
    }
    java.lang.reflect.Type ownerType = type.getOwnerType();
    return !(ownerType instanceof java.lang.reflect.ParameterizedType)
        || isDeclarationForm((java.lang.reflect.ParameterizedType) ownerType);
  }

  /**
   * Returns the owner type of the given parameterized type that {@link #forType} converts to the
   * enclosing type of its result. That is the owner type of a member class (not a static nested
   * class) if the owner type is parameterized and is not in declaration form (see {@link
   * #isDeclarationForm}), as {@code Outer<String>} is for {@code Outer<String>.Inner} and for
   * {@code Outer<String>.GenericInner<Integer>}.
   *
   * <p>The owner types must be checked transitively: in {@code Outer<U>.Middle.Inner}, the owner
   * type {@code Outer<U>.Middle} has no type arguments of its own, but {@code Outer<U>} does.
   *
   * @param type a parameterized type
   * @return the owner type that {@link #forType} converts for {@code type}, or null if there is
   *     none
   */
  /*package*/ static java.lang.reflect.@Nullable ParameterizedType convertedOwnerType(
      java.lang.reflect.ParameterizedType type) {
    Class<?> rawType = (Class<?>) type.getRawType();
    java.lang.reflect.Type ownerType = type.getOwnerType();
    if (ownerType instanceof java.lang.reflect.ParameterizedType
        && !java.lang.reflect.Modifier.isStatic(rawType.getModifiers())
        && !isDeclarationForm((java.lang.reflect.ParameterizedType) ownerType)) {
      return (java.lang.reflect.ParameterizedType) ownerType;
    }
    return null;
  }

  /**
   * Returns the owner type of the given parameterized type that determines the enclosing type of
   * the result of {@link #forType}. It is the owner type returned by {@link #convertedOwnerType} or
   * by {@link #memberClassOwnerType}. The latter includes an owner type in declaration form, such
   * as {@code Outer<T>} for {@code Outer<T>.Inner}, which is the enclosing type of the generic form
   * of {@code Inner}.
   *
   * @param type a parameterized type
   * @return the owner type that determines the enclosing type of the conversion of {@code type}, or
   *     null if there is none
   */
  /*package*/ static java.lang.reflect.@Nullable ParameterizedType enclosingOwnerType(
      java.lang.reflect.ParameterizedType type) {
    java.lang.reflect.ParameterizedType ownerType = memberClassOwnerType(type);
    return ownerType != null ? ownerType : convertedOwnerType(type);
  }

  /**
   * Returns the owner type of the given parameterized type, if the parameterized type is a member
   * class that has no type parameters of its own and whose owner type is parameterized, as {@code
   * Outer<String>} is for {@code Outer<String>.Inner}. Such a parameterized type has no type
   * arguments; its owner type supplies them.
   *
   * @param type a parameterized type
   * @return the owner type of {@code type} if {@code type} is a member class with no type
   *     parameters of its own and a parameterized owner type; otherwise null
   */
  /*package*/ static java.lang.reflect.@Nullable ParameterizedType memberClassOwnerType(
      java.lang.reflect.ParameterizedType type) {
    Class<?> rawType = (Class<?>) type.getRawType();
    java.lang.reflect.Type ownerType = type.getOwnerType();
    if (rawType.getTypeParameters().length == 0
        && ownerType instanceof java.lang.reflect.ParameterizedType
        && !java.lang.reflect.Modifier.isStatic(rawType.getModifiers())) {
      return (java.lang.reflect.ParameterizedType) ownerType;
    }
    return null;
  }

  /**
   * Returns the type of the given member class, which has no type parameters of its own, with the
   * given enclosing type, as {@code Outer<String>.Inner} for {@code Inner} and {@code
   * Outer<String>}.
   *
   * @param memberClass a member class that has no type parameters
   * @param enclosingType the enclosing type of the result
   * @return the type of {@code memberClass} whose enclosing type is {@code enclosingType}
   */
  /*package*/ static NonParameterizedType forMemberClass(
      Class<?> memberClass, ClassOrInterfaceType enclosingType) {
    // Do not use NonParameterizedType.forClass, which returns a shared cached object.
    NonParameterizedType result = new NonParameterizedType(memberClass);
    result.enclosingType = enclosingType;
    return result;
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
    ClassOrInterfaceType enclosingThis = this.enclosingType;
    ClassOrInterfaceType enclosingOther = otherType.enclosingType;
    return Objects.equals(enclosingThis, enclosingOther);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(enclosingType);
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
   * @param substitution a substitution
   * @return the enclosing type of this type, with {@code substitution} applied if this type is a
   *     member class
   */
  /*package*/ final @Nullable ClassOrInterfaceType substituteEnclosingType(
      Substitution substitution) {
    return isMemberClass() ? enclosingType.substitute(substitution) : enclosingType;
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
          if (interfaceType.isParameterized()) {
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
      if (superclass.runtimeClassIs(goalType.getRuntimeClass())
          && superclass.isInstantiationOf(goalType)) {
        return (InstantiatedType) superclass;
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

    assert goalType.isGeneric() : "goal type must be generic";

    Substitution substitution = new Substitution();
    // For a member class such as Outer<String>.Inner and a member class goal type such as
    // Outer<E>.Inner, the enclosing type of this type instantiates the enclosing type of the goal
    // type.  The enclosing type of this type is not matched against a goal type that is not a
    // member class, such as a type variable.
    if (this.isMemberClass() && goalType instanceof ClassOrInterfaceType) {
      ClassOrInterfaceType thisEnclosingType = enclosingType;
      ClassOrInterfaceType goalClassType = (ClassOrInterfaceType) goalType;
      if (goalClassType.isMemberClass()) {
        ClassOrInterfaceType enclosingGoalType = goalClassType.enclosingType;
        // Matching enclosing types do not make unrelated member classes, such as
        // Outer<String>.Inner and Outer<E>.Other, unify.
        if (!goalClassType.getRuntimeClass().isAssignableFrom(this.getRuntimeClass())) {
          return null;
        }
        // If this type's class is a strict subclass of the goal type's class, possibly a member of
        // a different class, as for B.Sub that extends A.Base<String>, then the enclosing types of
        // this type and the goal type are unrelated, so do not match them.  The supertype
        // matching below handles that case.
        if (goalClassType.runtimeClassIs(this.getRuntimeClass())) {
          if (enclosingGoalType.isGeneric()) {
            substitution = thisEnclosingType.getInstantiatingSubstitution(enclosingGoalType);
            if (substitution == null) {
              return null;
            }
          } else if (!thisEnclosingType.isSubtypeOfOrEqualTo(enclosingGoalType)) {
            // For example, Outer<Integer>.Inner<String> does not instantiate
            // Outer<String>.Inner<U>.
            return null;
          }
        }
      }
    }

    if (goalType instanceof GenericClassType) {
      InstantiatedType supertype = this.getMatchingSupertype((GenericClassType) goalType);
      if (supertype != null) {
        // The type substitution of the supertype maps the type parameters of the goal type,
        // including those of its enclosing classes.  The enclosing type of the supertype can differ
        // from that of this type, as for Outer<String>.Sub that extends Outer<Integer>.Inner, so
        // the substitution from the enclosing type of this type does not apply.
        return supertype.getTypeSubstitution();
      }
    }
    return substitution;
  }

  /**
   * Returns the type for the superclass for this class. Returns null if this type has no superclass
   * (it is the Object type or an interface type).
   *
   * @return the superclass of this type, or null
   */
  public abstract ClassOrInterfaceType getSuperclass();

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
   * member class, then this type and {@code otherType} must have the same runtime class, and the
   * enclosing type of this type must instantiate the enclosing type of {@code otherType}.
   */
  @Override
  public boolean isInstantiationOf(ReferenceType otherType) {
    if (super.isInstantiationOf(otherType)) {
      return true;
    }
    if (this.isNestedClass() && (otherType instanceof ClassOrInterfaceType)) {
      ClassOrInterfaceType otherClassType = (ClassOrInterfaceType) otherType;
      // Matching enclosing types do not make unrelated member classes, such as Container.Other and
      // Container.Generic<T>, instantiations of one another.
      // TODO: For a member class with type parameters of its own, this does not check them.
      return otherClassType.isNestedClass()
          && this.getRuntimeClass().equals(otherClassType.getRuntimeClass())
          && this.enclosingType.isInstantiationOf(otherClassType.enclosingType);
    }
    return false;
  }

  /**
   * Returns true if this class is a nested class.
   *
   * @return true iff this class is a nested class
   */
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
  @EnsuresNonNullIf(expression = "enclosingType", result = true)
  public final boolean isMemberClass() {
    return isNestedClass() && !isStatic();
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
    if (this.isMemberSubtypeByEnclosingType(otherRefType)) {
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

      if (iface.equals(otherInterface) || iface.isMemberSubtypeByEnclosingType(otherInterface)) {
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

    for (ClassOrInterfaceType current = this; current != null; current = current.getSuperclass()) {
      if (current.equals(otherClass) || current.isMemberSubtypeByEnclosingType(otherClass)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if this type and {@code otherType} are the same non-generic member class, and the
   * enclosing type of this type is a subtype of that of {@code otherType}. For example, {@code
   * Outer<Integer>.Inner} is a subtype of {@code Outer<? extends Number>.Inner}, but not of {@code
   * Outer<String>.Inner}. Such a member class has no type arguments of its own, so it is the
   * enclosing types that must be compared, by subtyping rather than by equality.
   *
   * <p>Returns false if this type is the generic form of its class, such as {@code Outer<T>.Inner}.
   * The generic form represents the supertype of a rawtype, which is not a subtype of a
   * parameterized type other than the generic form (see {@link
   * NonParameterizedType#isSubtypeOfOrEqualTo}).
   *
   * @param otherType the possible supertype
   * @return true if this type and {@code otherType} are the same non-generic member class and the
   *     enclosing type of this type is a subtype of that of {@code otherType}
   */
  private boolean isMemberSubtypeByEnclosingType(ClassOrInterfaceType otherType) {
    return this instanceof NonParameterizedType
        && otherType instanceof NonParameterizedType
        && !this.isRawtype()
        && !otherType.isRawtype()
        && this.isMemberClass()
        && otherType.isMemberClass()
        && !this.isGenericForm()
        && this.runtimeClassIs(otherType.getRuntimeClass())
        && this.enclosingType.isSubtypeOfOrEqualTo(otherType.enclosingType);
  }

  @Override
  public boolean hasWildcard() {
    return false;
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
