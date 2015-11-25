package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import randoop.operation.ConstructorCall;
import randoop.operation.EnumConstant;
import randoop.operation.FieldGetter;
import randoop.operation.FieldSetter;
import randoop.operation.InstanceField;
import randoop.operation.MethodCall;
import randoop.operation.Operation;
import randoop.operation.StaticField;
import randoop.operation.StaticFinalField;

/**
 * OperationExtractor is a {@link ClassVisitor} that creates a collection of {@link Operation}
 * objects through its visit methods as called by {@link ReflectionManager#apply(Class)}.
 * 
 * @see ReflectionManager
 * @see ClassVisitor
 *
 */
public class OperationExtractor implements ClassVisitor {

  private Set<Operation> operations;
 
  /**
   * OperationExtractor() creates a visitor object that collects Operation objects corresponding
   * to class members visited by {@link ReflectionManager}. Stores {@link Operation} objects in
   * ordered set to ensure strict order once flattened to list --- needed to guarantee determinism
   * between Randoop runs with same classes and parameters. 
   */
  public OperationExtractor() {
    this.operations = new TreeSet<>();
  }
  
  public List<Operation> getOperations() {
    return new ArrayList<Operation>(operations);
  }
  
  /**
   * getStatements collects the methods, constructor and enum constants for a collection of classes.
   * Returns a filtered list of StatementKind objects.
   * 
   * @param classListing collection of class objects from which to extract.
   * @param predicate filter object determines whether method/constructor/enum constant can be used.
   * @return list of StatementKind objects representing filtered set.
   */
  public static List<Operation> getOperations(Collection<Class<?>> classListing, ReflectionPredicate predicate) {
    if (predicate == null) predicate = new DefaultReflectionPredicate();
    ReflectionManager mgr = new ReflectionManager(predicate);
    OperationExtractor op = new OperationExtractor();
    mgr.add(op);
    for (Class<?> c : classListing) {
      mgr.apply(c);
    }
    return op.getOperations();
  }

  /**
   * visit(Constructor) creates a {@link ConstructorCall} object for the {@link Constructor}.
   * 
   * @param c a {@link Constructor} object to be represented as an {@link Operation}.
   */
  @Override
  public void visit(Constructor<?> c) {
    operations.add(new ConstructorCall(c));
  }

  /**
   * visit(Method) creates a {@link MethodCall} object for the {@link Method}.
   * 
   * @param m a {@link Method} object to be represented as an {@link Operation}.
   */
  @Override
  public void visit(Method m) {
    operations.add(new MethodCall(m));
  }

  /**
   * visit(Field) adds the {@link Operation} objects corresponding to 
   * getters and setters appropriate to the kind of field.
   * 
   * @param f a {@link Field} object to be represented as an {@link Operation}.
   */
  @Override
  public void visit(Field f) {
    int mods = f.getModifiers();
    
    if (Modifier.isStatic(mods)) {
      if (Modifier.isFinal(mods)) {
        StaticFinalField s = new StaticFinalField(f);
        operations.add(new FieldGetter(s));
      } else {
        StaticField s = new StaticField(f);
        operations.add(new FieldGetter(s));
        operations.add(new FieldSetter(s));
      }
    } else {
      InstanceField i = new InstanceField(f);
      operations.add(new FieldGetter(i));
      operations.add(new FieldSetter(i));
    }
  }

  /**
   * visit(Enum) creates a {@link EnumConstant} object for the {@link Enum}.
   * 
   * @param e an {@link Enum} object to be represented as an {@link Operation}.
   */
  @Override
  public void visit(Enum<?> e) {
    operations.add(new EnumConstant(e));
  }

  @Override
  public void visitBefore(Class<?> c) {
    //nothing to do here
  }

  @Override
  public void visitAfter(Class<?> c) {
   //nothing to do here
  }

}
