package randoop.types;

import java.util.ArrayDeque;
import java.util.ArrayList;
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
      // A member class with no type parameters of its own, such as Outer<E>.Inner, shows up as a
      // ParameterizedType whose owner type supplies the type arguments.
      Class<?> rawType = (Class<?>) t.getRawType();
      if (rawType.getTypeParameters().length > 0) {
        // ParameterizedType.forType converts the owner type.  Converting it here as well would
        // convert each owner in a chain of generic member classes repeatedly.
        return ParameterizedType.forType(t);
      }
      // The enclosing type of the result, converted from the owner type, such as Outer<E> in
      // Outer<E>.Inner or Outer<E>.Middle<String> in Outer<E>.Middle<String>.Inner.  It is null if
      // the owner type supplies no type arguments or is the generic form of its class, which
      // requires each owner type in the chain, such as A<U> in A<U>.B.C, to be the generic form of
      // its class.  Use the owner type rather than the enclosing class, because the owner type's
      // type arguments may differ from the enclosing class's type parameters.  For example, in
      // `<E> void m(Outer<E>.Inner inner)`, the owner type is `Outer<E>` where `E` is the method's
      // type parameter, not the type parameter declared by `Outer`.
      ClassOrInterfaceType ownerClassType = null;
      java.lang.reflect.Type ownerType = t.getOwnerType();
      if (ownerType instanceof java.lang.reflect.ParameterizedType
          && rawType.getEnclosingClass() != null
          && !java.lang.reflect.Modifier.isStatic(rawType.getModifiers())) {
        // Convert the complete owner chain, because the type arguments can be on an owner that is
        // several levels out, as in Outer<String>.Mid.Inner.
        ownerClassType = forType(ownerType);
        if (ownerClassType.isGenericForm()) {
          ownerClassType = null;
        }
      }
      if (ownerClassType == null) {
        // The result is the generic form of the member class.
        return forClass(rawType);
      }
      // Do not use NonParameterizedType.forClass, which returns a shared cached object.
      NonParameterizedType result = new NonParameterizedType(rawType);
      result.enclosingType = ownerClassType;
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
    // True if this type or one of its supertypes matches the goal type.  If neither does, then no
    // substitution unifies them.
    boolean matched =
        goalType instanceof ClassOrInterfaceType && goalType.runtimeClassIs(this.getRuntimeClass());
    // For a member class such as Outer<String>.Inner and a member class goal type such as
    // Outer<E>.Inner, the enclosing type of this type instantiates the enclosing type of the goal
    // type.  The enclosing type of this type is not matched against a goal type that is not a
    // member class, such as a type variable.
    @Nullable ClassOrInterfaceType goalClassType =
        goalType instanceof ClassOrInterfaceType ? (ClassOrInterfaceType) goalType : null;
    if (goalClassType != null && goalClassType.isMemberClass()) {
      ClassOrInterfaceType enclosingGoalType = goalClassType.enclosingType;
      // Matching enclosing types do not make unrelated member classes, such as
      // Outer<String>.Inner and Outer<E>.Other, unify.
      if (!goalClassType.getRuntimeClass().isAssignableFrom(this.getRuntimeClass())) {
        return null;
      }
      if (!goalClassType.runtimeClassIs(this.getRuntimeClass())) {
        // This type's class is a strict subclass of the goal type's class, possibly a member of a
        // different class, as for B.Sub that extends A<String>.Base.  Then the enclosing types of
        // this type and the goal type are unrelated.  Match the superclass of this type whose
        // class is the goal type's class, such as A<String>.Base, so that the type arguments of
        // its enclosing type bind the type variables of the goal type's enclosing type.  (A member
        // class is never an interface, so a superclass is the only possible match.)
        for (ClassOrInterfaceType superclass = this.getSuperclass();
            superclass != null;
            superclass = superclass.getSuperclass()) {
          if (superclass.runtimeClassIs(goalClassType.getRuntimeClass())) {
            return superclass.getInstantiatingSubstitution(goalType);
          }
        }
        return null;
      }
      if (this.isMemberClass()) {
        ClassOrInterfaceType thisEnclosingType = enclosingType;
        if (enclosingGoalType.isGeneric()) {
          substitution = thisEnclosingType.getInstantiatingSubstitution(enclosingGoalType);
          if (substitution == null) {
            return null;
          }
        } else if (!thisEnclosingType.isSubtypeOfOrEqualTo(enclosingGoalType)) {
          // A non-generic enclosing goal type has no type variables to bind, so the enclosing type
          // of this type must be a subtype of it.  For example, Outer<Integer>.Inner2<Long> does
          // not instantiate Outer<String>.Inner2<U>.
          return null;
        }
      }
    }

    if (goalType instanceof GenericClassType) {
      InstantiatedType supertype = this.getMatchingSupertype((GenericClassType) goalType);
      if (supertype != null) {
        matched = true;
        Substitution supertypeSubstitution = supertype.getTypeSubstitution();
        if (supertypeSubstitution == null
            || !substitution.isConsistentWith(supertypeSubstitution)) {
          return null;
        }
        substitution = substitution.extend(supertypeSubstitution);
      }
    }
    return matched ? substitution : null;
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
   * member class, then the enclosing type of this type must instantiate the enclosing type of
   * {@code otherType}.
   */
  @Override
  public boolean isInstantiationOf(ReferenceType otherType) {
    if (super.isInstantiationOf(otherType)) {
      return true;
    }
    if (this.isNestedClass() && (otherType instanceof ClassOrInterfaceType)) {
      ClassOrInterfaceType thisEnclosingType = this.enclosingType;
      ClassOrInterfaceType otherClassType = (ClassOrInterfaceType) otherType;
      ClassOrInterfaceType otherEnclosingType = otherClassType.enclosingType;
      // Both must be the same nested class, such as Outer<String>.A and Outer<T>.A, not
      // Outer<String>.A and Outer<T>.B.
      return otherEnclosingType != null
          && otherClassType.runtimeClassIs(this.getRuntimeClass())
          && thisEnclosingType.isInstantiationOf(otherEnclosingType);
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

    for (ClassOrInterfaceType current = this; current != null; current = current.getSuperclass()) {
      if (current.equals(otherClass)) {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * <p>A member class has a wildcard if its enclosing type does, as {@code Outer<?>.Inner} does.
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
   * Returns the substitution that maps the type parameters of the classes that enclose this type's
   * class to the type arguments of the enclosing types of this type. For example, for {@code
   * Outer<String>.Inner}, it maps the type parameter of {@code Outer} to {@code String}. Returns
   * the empty substitution if this type is not a member class.
   *
   * @return the substitution for the type parameters of the enclosing classes
   */
  /*package*/ Substitution getEnclosingTypeSubstitution() {
    if (!isMemberClass()) {
      return new Substitution();
    }
    return getOwnTypeArgumentSubstitution(enclosingType);
  }

  /**
   * Returns the substitution that maps the type parameters of the class of the given type, and of
   * the classes that enclose it, to the type arguments of the given type and of its enclosing
   * types. For example, for {@code Outer<String>.Holder<Integer>}, it maps the type parameter of
   * {@code Outer} to {@code String} and the type parameter of {@code Holder} to {@code Integer}.
   *
   * @param type a class or interface type
   * @return the substitution for the type parameters of the class of {@code type} and of the
   *     enclosing classes
   */
  /*package*/ static Substitution getOwnTypeArgumentSubstitution(ClassOrInterfaceType type) {
    Substitution result = type.getEnclosingTypeSubstitution();
    if (type instanceof InstantiatedType) {
      InstantiatedType instantiatedType = (InstantiatedType) type;
      result =
          result.extend(
              new Substitution(
                  instantiatedType.getGenericClassType().getDeclaredTypeParameters(),
                  instantiatedType.getReferenceArguments()));
    }
    return result;
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

  @Override
  public boolean isClassOrInterfaceType() {
    return true;
  }
}
