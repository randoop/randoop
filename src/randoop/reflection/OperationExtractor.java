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
import randoop.operation.FinalInstanceField;
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
   * Creates a visitor object that collects Operation objects corresponding
   * to class members visited by {@link ReflectionManager}. 
   * Stores {@link Operation} objects in an ordered collection to ensure they
   * are strictly ordered once flattened to a list. This is needed to guarantee 
   * determinism between Randoop runs with the same classes and parameters. 
   */
  public OperationExtractor() {
    this.operations = new TreeSet<>();
  }
  
  public List<Operation> getOperations() {
    return new ArrayList<Operation>(operations);
  }
  
  /**
   * Collects the members of a collection of classes.
   * Returns a filtered list of {@code Operation} objects.
   * 
   * @param classListing  the collection of class objects from which to extract
   * @param predicate  whether to include class members in results
   * @return list of {@code Operation} objects satisfying the predicate
   */
  public static List<Operation> getOperations(Collection<Class<?>> classListing, 
      ReflectionPredicate predicate) {
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
   * Creates a {@link ConstructorCall} object for the {@link Constructor}.
   * 
   * @param c  a {@link Constructor} object to be represented as an {@link Operation}.
   */
  @Override
  public void visit(Constructor<?> c) {
    operations.add(new ConstructorCall(c));
  }

  /**
   * Creates a {@link MethodCall} object for the {@link Method}.
   * 
   * @param method  a {@link Method} object to be represented as an {@link Operation}.
   */
  @Override
  public void visit(Method method) {
    operations.add(new MethodCall(method));
  }

  /**
   * Adds the {@link Operation} objects corresponding to 
   * getters and setters appropriate to the kind of field.
   * 
   * @param field  a {@link Field} object to be represented as an {@link Operation}.
   */
  @Override
  public void visit(Field field) {
    int mods = field.getModifiers();
    
    if (Modifier.isStatic(mods)) {
      if (Modifier.isFinal(mods)) {
        StaticFinalField s = new StaticFinalField(field);
        operations.add(new FieldGetter(s));
      } else {
        StaticField s = new StaticField(field);
        operations.add(new FieldGetter(s));
        operations.add(new FieldSetter(s));
      }
    } else {
      if (Modifier.isFinal(mods)) {
        FinalInstanceField i = new FinalInstanceField(field);
        operations.add(new FieldGetter(i));
      } else {
        InstanceField i = new InstanceField(field);
        operations.add(new FieldGetter(i));
        operations.add(new FieldSetter(i));
      }
    }
  }

  /**
   * Creates a {@link EnumConstant} object for the {@link Enum}.
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
