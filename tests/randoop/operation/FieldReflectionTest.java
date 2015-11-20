package randoop.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationExtractor;
import randoop.reflection.ReflectionPredicate;
import randoop.util.Reflection;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * FieldReflectionTest consists of tests of {@link Reflection#getStatements}
 * method to verify that field statements are collected as expected.
 * 
 * @author bjkeller
 *
 */
public class FieldReflectionTest {

  /**
   * basicFields tests that all of the expected fields are collected for the 
   * ClassWithFields class. 
   */
  @Test
  public void basicFields() {
    ArrayList<Class<?>> classes = new ArrayList<>();
    Class<?> c = ClassWithFields.class;
    classes.add(c);
    
    @SuppressWarnings("unchecked")
    List<Field> fields = Arrays.asList(c.getFields());
    List<Operation> actual = OperationExtractor.getOperations(classes, null);
    
    //number of statements is twice number of fields plus constructor and getter minus one for each constant
    //in this case, 11
    assertEquals("number of statements twice number of fields", 2 * fields.size() + 2 - 1, actual.size());
    
    //exclude private or protected fields
    List<Field> exclude = new ArrayList<>();
    for (Field f : c.getDeclaredFields()) {
      int mods = f.getModifiers();
      if (Modifier.isPrivate(mods) || Modifier.isProtected(mods)) {
        exclude.add(f);
      }
    }
    
    for (Field f : fields) {
      assertTrue("field " + f.toGenericString() + " should occur", actual.containsAll(getStatementKinds(f)));
    }
    
    for (Field f : exclude) {
      assertFalse("field " + f.toGenericString() + " should not occur", actual.containsAll(getStatementKinds(f)));
    }
    
  }
  
  /**
   * inheritedFields looks for statements built for inherited fields.
   * Avoid hidden fields, because we cannot get to them without reflection.
   */
  @Test
  public void inheritedFields() {
    ArrayList<Class<?>> classes = new ArrayList<>();
    Class<?> c = SubclassWithFields.class;
    classes.add(c);
    
    List<Field> expected = new ArrayList<>();
    List<Field> exclude = new ArrayList<>();
    List<String> declared = new ArrayList<>();
    for (Field f : c.getDeclaredFields()) {
      declared.add(f.getName());
    }
    
    for (Field f : c.getFields()) {
      if (declared.contains(f.getName())) { 
        if (c.equals(f.getDeclaringClass())) { 
          expected.add(f);
        } else { //hidden
          exclude.add(f);
        }
      } else {
        expected.add(f);
      }
    }
    List<Operation> actual = OperationExtractor.getOperations(classes, null);
    
    assertEquals("number of statements", 2*expected.size() - 1 + 2, actual.size());
    
    for (Field f : expected) {
      assertTrue("field " + f.toGenericString() + " should occur", actual.containsAll(getStatementKinds(f)));
    }
    
    for (Field f : exclude) {
      assertFalse("field " + f.toGenericString() + " should not occur", actual.containsAll(getStatementKinds(f)));
    }
  }
  
  /**
   * filteredFields checks to ensure we don't get any fields that should be removed
   * 
   */
  @Test
  public void filteredFields() {
    ArrayList<Class<?>> classes = new ArrayList<>();
    Class<?> c = ClassWithFields.class;
    classes.add(c);
    
    //let's exclude every field
    List<Field> exclude = new ArrayList<>();
    Set<String> excludeNames = new TreeSet<String>();
    for (Field f : c.getFields()) {
      excludeNames.add(f.getDeclaringClass().getName()+"."+f.getName());
      exclude.add(f);
    }
    
    ReflectionPredicate filter = new DefaultReflectionPredicate(null, excludeNames);
    List<Operation> actual = OperationExtractor.getOperations(classes, filter);
    
    assertEquals("number of statements", 2, actual.size());
    
    for (Field f : exclude) {
      assertFalse("field " + f.toGenericString() + " should not occur", actual.containsAll(getStatementKinds(f)));
    }
    
  }
  
  /**
   * getStatementKinds maps a field into possible statements.
   * Looks at modifiers to decide which kind of field wrapper
   * to create and then builds list with getter and setter.
   * 
   * @param f - reflective Field object
   * @return List of getter/setter statements for the field
   */
  private Collection<?> getStatementKinds(Field f) {
    List<Operation> statements = new ArrayList<>();
    int mods = f.getModifiers();
    if (Modifier.isStatic(mods)) {
      if (Modifier.isFinal(mods)) {
        statements.add(new FieldGetter(new StaticFinalField(f)));
      } else {
        statements.add(new FieldGetter(new StaticField(f)));
        statements.add(new FieldSetter(new StaticField(f)));
      }
    } else {
      statements.add(new FieldGetter(new InstanceField(f)));
      statements.add(new FieldSetter(new InstanceField(f)));
    }
    return statements;
  }

}
