package randoop.types;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import randoop.field.FinalInstanceField;
import randoop.field.InstanceField;
import randoop.field.StaticField;
import randoop.field.StaticFinalField;
import randoop.reflection.OperationModel;
import randoop.operation.EnumConstant;
import randoop.operation.MethodCall;
import randoop.reflection.ReflectionPredicate;

public class TypeFactory {

  private OperationModel model;
  private ReflectionPredicate predicate;

  public TypeFactory(ReflectionPredicate predicate, OperationModel model) {
    this.predicate = predicate;
    this.model = model;
  }

  // Note to self:  this should be pushing types and operations into bookkeeping class
  // rather than attempting to shove values into each type object.
  // have a lot of common code between concrete and generic classes

  /**
   * Returns a {@code GeneralType} object for the given class object.
   * <p>
   * A {@link Class} object is a runtime representation of a type, and could
   * represent either a concrete or generic type.
   * In Randoop, only expect to see {@link Class} objects for classes under
   * test, which means this method does not handle array, primitive or
   * parameterized types.
   *
   * @param typeClass  the type to be converted to a {@code GeneralType}
   * @return a {@link GenericClassType} if {@code typeClass} has type
   *         parameters, and a {@link ConcreteSimpleType} otherwise
   */
  public GeneralType forClass(Class<?> typeClass) {
    assert predicate.test(typeClass);
    assert !(typeClass.isArray() || typeClass.isPrimitive());

    if (typeClass.getTypeParameters().length > 0) { // if is generic

      GenericType classType = new GenericClassType(typeClass);

      // methods
      Set<Method> methods = new HashSet<>();
      for (Method m : typeClass.getMethods()) { // for all public methods
        methods.add(m); // remember to avoid duplicates
        if (predicate.test(m)) { // if satisfies predicate then visit
          classType.add(buildGenericMethodCall(m, classType));
        }
      }
      for (Method m : typeClass.getDeclaredMethods()) { // for all methods declared by c
        // if not duplicate and satisfies predicate
        if ((!methods.contains(m)) && predicate.test(m)) {
          classType.add(buildGenericMethodCall(m, classType));
        }
      }

      //fields
      Set<String> declaredNames = new TreeSet<>();
      for (Field f : typeClass.getDeclaredFields()) { // for fields declared by c
        declaredNames.add(f.getName());
        if (predicate.test(f)) {
          visit(f);
        }
      }
      for (Field f : typeClass.getFields()) { // for all public fields of c
        // keep a field that satisfies filter, and is not inherited and hidden by local declaration
        if (predicate.test(f) && (!declaredNames.contains(f.getName()))) {
          visit(f);
        }
      }

      // constructors
      for (Constructor<?> co : typeClass.getDeclaredConstructors()) {
        if (predicate.test(co)) {
          //visitConstructor(co);
        }
      }

      for (Class<?> ic : typeClass.getDeclaredClasses()) { //look for inner enums
        if (ic.isEnum() && predicate.test(ic)) {
          applyEnum(ic);
        }
      }

      return classType;
    }

    if (typeClass.isEnum()) {
      return forEnum(typeClass);
    }

    return new ConcreteSimpleType(typeClass);
  }

  private ConcreteType forEnum(Class<?> typeClass) {
    assert typeClass.isEnum();
    ConcreteType enumType = new ConcreteSimpleType(typeClass);
    Set<String> overrideMethods = new HashSet<String>();
    for (Object obj : typeClass.getEnumConstants()) {
      Enum<?> e = (Enum<?>) obj;
      enumType.add(new EnumConstant(e, new ConcreteTypeTuple(), enumType));
      if (!e.getClass().equals(typeClass)) { //does constant have an anonymous class?
        for (Method m : e.getClass().getDeclaredMethods()) {
          overrideMethods.add(m.getName()); //collect any potential overrides
        }
      }
    }

    //get methods that are explicitly declared in the enum
    for (Method m : typeClass.getDeclaredMethods()) {
      if (predicate.test(m)) {
        if (!m.getName().equals("values") && !m.getName().equals("valueOf")) {
          enumType.add(buildMethodCall(m, enumType));
        }
      }
    }
    //get any inherited methods also declared in anonymous class of some constant
    for (Method m : typeClass.getMethods()) {
      if (predicate.test(m) && overrideMethods.contains(m.getName())) {
        enumType.add(buildMethodCall(m, enumType));
      }
    }
    return enumType;
  }

  private MethodCall buildMethodCall(Method m, ConcreteType classType) {
    ConcreteType retType = ConcreteType.forType(m.getGenericReturnType());
    List<ConcreteType> paramTypes = new ArrayList<>();
    if (!Modifier.isStatic(m.getModifiers() & Modifier.methodModifiers())) {
      paramTypes.add(classType);
    }
    for (Type t : m.getGenericParameterTypes()) {
      paramTypes.add(ConcreteType.forType(t));
    }
    return new MethodCall(m, new ConcreteTypeTuple(paramTypes), retType);
  }

  private void visit(Field field) {
    int mods = field.getModifiers() & Modifier.fieldModifiers();

    if (Modifier.isStatic(mods)) {
      if (Modifier.isFinal(mods)) {
        StaticFinalField s = new StaticFinalField(field);
        // operations.add(new FieldGet(s));
      } else {
        StaticField s = new StaticField(field);
        //   operations.add(new FieldGet(s));
        //   operations.add(new FieldSet(s));
      }
    } else {
      if (Modifier.isFinal(mods)) {
        FinalInstanceField i = new FinalInstanceField(field);
        //   operations.add(new FieldGet(i));
      } else {
        InstanceField i = new InstanceField(field);
        //   operations.add(new FieldGet(i));
        //   operations.add(new FieldSet(i));
      }
    }
  }
}
