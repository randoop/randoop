package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import randoop.field.FinalInstanceField;
import randoop.field.InstanceField;
import randoop.field.StaticField;
import randoop.field.StaticFinalField;
import randoop.types.GeneralType;
import randoop.util.Log;

public class TypeHarvester {

  private ReflectionPredicate predicate;
  private Set<GeneralType> cutTypes;
  private Set<GeneralType> inputTypes;

  public TypeHarvester(ReflectionPredicate predicate) {
    this.predicate = predicate;
    this.cutTypes = new LinkedHashSet<>();
  }

  public void apply(List<Class<?>> classes) {
    for (Class<?> c : classes) {
      if (predicate.test(c)) {
        GeneralType t = apply(c);
        cutTypes.add(t);
        getOperationTypes(t);
      }
    }
  }

  private void getOperationTypes(GeneralType t) {
    // TODO Auto-generated method stub
    /*
    for (Operation op : t.getOperations()) {
      inputTypes.add(op.getOutputType());
      inputTypes.addAll(op.getInputTypes().asSet());
    }
    */
  }

 public GeneralType apply(Class<?> typeClass) {
   assert (predicate.test(typeClass));
   assert ! (typeClass.isArray() || typeClass.isPrimitive());

   GeneralType result = null;

   if (Log.isLoggingOn()) Log.logLine("visiting class " + typeClass.getName());

   if (typeClass.isEnum()) { //treat enum classes differently
     return applyEnum(typeClass);
   }

   applyMethods(typeClass);

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

   applyFields(typeClass);

   return result;
 }

 private void applyFields(Class<?> c) {
   //The set of fields declared in class c is needed to ensure we don't collect
   //inherited fields that are hidden by local declaration
   Set<String> declaredNames = new TreeSet<>();
   for (Field f : c.getDeclaredFields()) { // for fields declared by c
     declaredNames.add(f.getName());
     if (predicate.test(f)) {
       visit(f);
     }
   }
   for (Field f : c.getFields()) { // for all public fields of c
     // keep a field that satisfies filter, and is not inherited and hidden by local declaration
     if (predicate.test(f) && (! declaredNames.contains(f.getName()))) {
       visit(f);
     }
   }
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

 private void applyMethods(Class<?> c) {
   Set<Method> methods = new HashSet<>();
   for (Method m : c.getMethods()) { // for all public methods
     methods.add(m); // remember to avoid duplicates
     if (predicate.test(m)) { // if satisfies predicate then visit
      // visitMethod(m);
     }
   }
   for (Method m : c.getDeclaredMethods()) { // for all methods declared by c
     // if not duplicate and satisfies predicate
     if ((! methods.contains(m)) && predicate.test(m)) {
      // visitMethod(m);
     }
   }


 }
}
