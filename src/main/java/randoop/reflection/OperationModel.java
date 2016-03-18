package randoop.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.CheckRep;
import randoop.CheckRepContract;
import randoop.ComponentManager;
import randoop.EqualsHashcode;
import randoop.EqualsReflexive;
import randoop.EqualsSymmetric;
import randoop.EqualsToNullRetFalse;
import randoop.LiteralFileReader;
import randoop.ObjectContract;
import randoop.TestValue;
import randoop.main.ClassNameErrorHandler;
import randoop.main.ThrowClassNameError;
import randoop.main.WarnOnBadClassName;
import randoop.operation.ConcreteOperation;
import randoop.operation.NonreceiverTerm;
import randoop.sequence.Sequence;
import randoop.types.ConcreteType;
import randoop.types.PrimitiveTypes;
import randoop.types.TypeFactory;
import randoop.util.ClassFileConstants;
import randoop.util.MultiMap;

import static randoop.main.GenInputsAbstract.ClassLiteralsMode;

/**
 * Created by bjkeller on 3/18/16.
 */
public class OperationModel {

// TODO make sure adding Object constructor
// TODO move seed sequence TestValue annotation scraping to here

    /**
     * Factory method to construct an operation model for a particular set of classes
     *
     * @param visibility
     *          the {@link randoop.reflection.VisibilityPredicate} to test
     *          accessibility of classes and class members.
     * @param reflectionPredicate
     * @param classnames
     * @param coveredClassnames
     * @return
     */
    public static OperationModel createModel(VisibilityPredicate visibility, DefaultReflectionPredicate reflectionPredicate, Set<String> classnames, Set<String> coveredClassnames, ClassNameErrorHandler errorHandler) {

        OperationModel model = new OperationModel();
        TypeFactory typeFactory = new TypeFactory(reflectionPredicate, model);

        for (String classname : classnames) {
            Class<?> c = null;
            try {
                c = typeFactory.getTypeForName(classname);
            } catch (ClassNotFoundException e) {
                errorHandler.handle(classname);
            }

            if (c != null) { // could be null if only warning on bad names
                // ignore interfaces and non-visible classes
                if (!visibility.isVisible(c)) {
                    System.out.println(
                            "Ignoring non-visible " + c + " specified via --classlist or --testclass.");
                } else if (c.isInterface()) {
                    System.out.println("Ignoring " + c + " specified via --classlist or --testclass.");
                } else {
                    if (Modifier.isAbstract(c.getModifiers()) && !c.isEnum()) {
                        System.out.println(
                                "Ignoring abstract " + c + " specified via --classlist or --testclass.");
                    } else {
                        classes.add(c);
                    }
                    if (coveredClassnames.contains(classname)) {
                        coveredClasses.add(c);
                    }
                }
            }
        }

        for (String classname : coveredClassnames) {
            if (!classnames.contains(classname)) {
                Class<?> c = null;
                try {
                    c = typeFactory.getTypeForName(classname);
                } catch (ClassNotFoundException e) {
                    errorHandler.handle(classname);
                }

                if (!visibility.isVisible(c)) {
                    System.out.println(
                            "Ignorning non-visible " + c + " specified as include-if-class-exercised target");
                } else if (c.isInterface()) {
                    System.out.println("Ignoring " + c + " specified as include-if-class-exercised target.");
                } else {
                    coveredClasses.add(c);
                }
            }
        }

        return model;
    }

    /**
     * @see randoop.SeedSequences#getSeedsFromAnnotatedFields(List)
     * @return list of fields annotated with TestValue
     */
    public Set<Object> getAnnotatedTestValues() {
        return new LinkedHashSet<>();
    }

    /**
     * Inspects the declared fields of the given classes. If it finds fields with
     * a @TestValue annotation, ensures that the fields are static, public, and
     * declare a primitive type (or String), or an array of such types. It returns
     * a set of statement sequences corresponding to the values collected from the
     * annotated fields.
     *
     * @param fields
     *          A list of static fields with @TestValue annotation
     * @return A set of Sequences representing primitive values collected
     *         frome @TestValue-annotated fields in the given classes
     */
    public static Set<Sequence> getSeedsFromAnnotatedFields(List<Field> fields) {

        // This list will store the primitive values (or Strings) obtained from
        // @TestValue fields.
        List<Object> primitives = new ArrayList<>();

        // Find primitive values from static class fields specified by the user
        // as @TestValue fields.
        for (Field f : fields) {
            if (f.getAnnotation(TestValue.class) != null) {

                // Check that @TestValue field is static.
                if (!Modifier.isStatic(f.getModifiers())) {
                    String msg =
                            "RANDOOP ANNOTATION ERROR: Expected @TestValue-annotated field "
                                    + f.getName()
                                    + " in class "
                                    + f.getDeclaringClass()
                                    + " to be declared static but it was not.";
                    throw new RuntimeException(msg);
                }

                // Check that @TestValue field is public.
                if (!Modifier.isPublic(f.getModifiers())) {
                    String msg =
                            "RANDOOP ANNOTATION ERROR: Expected @TestValue-annotated field "
                                    + f.getName()
                                    + " in class "
                                    + f.getDeclaringClass()
                                    + " to be declared public but it was not.";
                    throw new RuntimeException(msg);
                }

                // Check that @TestValue field is accessible.
                if (!Modifier.isPublic(f.getModifiers())) {
                    String msg =
                            "RANDOOP ANNOTATION ERROR: Expected @TestValue-annotated field "
                                    + f.getName()
                                    + " in class "
                                    + f.getDeclaringClass()
                                    + " to be accessible at runtime but it was not.";
                    throw new RuntimeException(msg);
                }

                // Get the value(s) stored in the field.
                Class<?> ftype = f.getType();
                try {

                    // Case 1: f is a primitive type
                    if (PrimitiveTypes.isPrimitiveOrStringType(ftype)) {
                        printDetectedAnnotatedFieldMsg(f);
                        primitives.add(f.get(null));

                        // Case 2: f is a primitive array type
                    } else if (ftype.isArray()
                            && PrimitiveTypes.isPrimitiveOrStringType(ftype.getComponentType())) {
                        printDetectedAnnotatedFieldMsg(f);
                        Object array = f.get(null);
                        int length = Array.getLength(array);
                        for (int i = 0; i < length; i++) {
                            primitives.add(Array.get(array, i));
                        }

                    } else {
                        String msg =
                                "RANDOOP ANNOTATION ERROR: Expected @TestValue-annotated field "
                                        + f.getName()
                                        + " in class "
                                        + f.getDeclaringClass()
                                        + " to declare a primitive type, String, or an array of primitives of Strings, "
                                        + "but the field's type is "
                                        + f.getType()
                                        + ".";
                        throw new RuntimeException(msg);
                    }

                } catch (IllegalArgumentException e) {
                    String msg =
                            "RANDOOP ANNOTATION ERROR: IllegalArgumentException when processing @TestValue-annotated field "
                                    + f.getName()
                                    + " in class "
                                    + f.getDeclaringClass()
                                    + ".";
                    throw new RuntimeException(msg);
                } catch (IllegalAccessException e) {
                    String msg =
                            "RANDOOP ANNOTATION ERROR: IllegalAccessException when processing @TestValue-annotated field "
                                    + f.getName()
                                    + " in class "
                                    + f.getDeclaringClass()
                                    + ". (Is the class declaring this field publicly-visible?)";
                    throw new RuntimeException(msg);
                }
            }
        }

        // Now we convert the values collected to sequences. We do this by calling
        // the objectsToSeeds(List<Object>) method.
        //
        // There is a small wrinkle left: method objectsToSeeds(List<Object>)
        // doesn't admit null values.
        // Note that if there was a null value in the values we collected, it must
        // have comes from a
        // String field. In this case, we remove the null value, and it afterwards.
        boolean nullString = primitives.remove(null);
        Set<Sequence> retval = objectsToSeeds(primitives);
        if (nullString) {
            // Add "String x = null" statement.
            retval.add(Sequence.create(NonreceiverTerm.createNullOrZeroTerm(ConcreteType.forClass(String.class))));
        }
        return retval;
    }

    private static void printDetectedAnnotatedFieldMsg(Field f) {
        String msg =
                "ANNOTATION: Detected @TestValue-annotated field "
                        + f.getType().getCanonicalName()
                        + " \""
                        + f.getName()
                        + "\" in class "
                        + f.getDeclaringClass().getCanonicalName()
                        + ". Will collect its primtive values to use in generation.";
        System.out.println(msg);
    }

    /**
     * Adds literals to the component manager, by parsing any literals files
     * specified by the user.
     */
    public void addClassLiterals(ComponentManager compMgr, List<String> literalsFile, ClassLiteralsMode literalsLevel) {

        // Add a (1-element) sequence corresponding to each literal to the component
        // manager.
        for (String filename : literalsFile) {
            MultiMap<Class<?>, NonreceiverTerm> literalmap;
            if (filename.equals("CLASSES")) {
                Collection<ClassFileConstants.ConstantSet> css = new ArrayList<>(classes.size());
                for (Class<?> clazz : classes) {
                    css.add(ClassFileConstants.getConstants(clazz.getName()));
                }
                literalmap = ClassFileConstants.toMap(css);
            } else {
                literalmap = LiteralFileReader.parse(filename);
            }

            for (Class<?> cls : literalmap.keySet()) {
                Package pkg =
                        (literalsLevel == ClassLiteralsMode.PACKAGE
                                ? cls.getPackage()
                                : null);
                for (NonreceiverTerm p : literalmap.getValues(cls)) {
                    Sequence seq = Sequence.create(p);
                    switch (literalsLevel) {
                        case CLASS:
                            compMgr.addClassLevelLiteral(cls, seq);
                            break;
                        case PACKAGE:
                            assert pkg != null;
                            compMgr.addPackageLevelLiteral(pkg, seq);
                            break;
                        case ALL:
                            compMgr.addGeneratedSequence(seq);
                            break;
                        default:
                            throw new Error(
                                    "Unexpected error in GenTests -- please report at https://github.com/randoop/randoop/issues");
                    }
                }
            }
        }
    }

    public Set<Class<?>> getCoveredClasses() {
        return null;
    }

    public Set<ConcreteType> getClasses() {
        return new LinkedHashSet<>();
    }

    public boolean hasClasses() {
        return false;
    }

    public List<ConcreteOperation> getConcreteOperations() {
        return new ArrayList<>();
    }

    public void addOperations(Set<String> methodSignatures) {
        for (String sig: methodSignatures) {
            //parse sig to operation
        }
    }

    public List<ObjectContract> getContracts() {
        List<ObjectContract> contracts = new ArrayList<>();

        // Add any @CheckRep-annotated methods
        List<ObjectContract> checkRepContracts = getContractsFromAnnotations(classes);
        contracts.addAll(checkRepContracts);

        // Now add all of Randoop's default contracts.
        // Note: if you add to this list, also update the Javadoc for
        // check_object_contracts.
        contracts.add(new EqualsReflexive());
        contracts.add(new EqualsSymmetric());
        contracts.add(new EqualsHashcode());
        contracts.add(new EqualsToNullRetFalse());

        return contracts;
    }

    public static List<ObjectContract> getContractsFromAnnotations(Set<ConcreteType> types) {

        List<ObjectContract> contractsFound = new ArrayList<>();

        for (ConcreteType type : types) {
            Class<?> c = type.getRuntimeClass();
            for (Method m : c.getDeclaredMethods()) {
                if (m.getAnnotation(CheckRep.class) != null) {

                    // Check that method is an instance (not a static) method.
                    if (Modifier.isStatic(m.getModifiers())) {
                        String msg =
                                "RANDOOP ANNOTATION ERROR: Expected @CheckRep-annotated method "
                                        + m.getName()
                                        + " in class "
                                        + m.getDeclaringClass()
                                        + " to be an instance method, but it is declared static.";
                        throw new RuntimeException(msg);
                    }

                    // Check that method is public.
                    if (!Modifier.isPublic(m.getModifiers())) {
                        String msg =
                                "RANDOOP ANNOTATION ERROR: Expected @CheckRep-annotated method "
                                        + m.getName()
                                        + " in class "
                                        + m.getDeclaringClass()
                                        + " to be declared public but it is not.";
                        throw new RuntimeException(msg);
                    }

                    // Check that method takes no arguments.
                    if (m.getParameterTypes().length > 0) {
                        String msg =
                                "RANDOOP ANNOTATION ERROR: Expected @CheckRep-annotated method "
                                        + m.getName()
                                        + " in class "
                                        + m.getDeclaringClass()
                                        + " to declare no parameters but it does (method signature:"
                                        + m.toString()
                                        + ").";
                        throw new RuntimeException(msg);
                    }

                    // Check that method's return type is void.
                    if (!(m.getReturnType().equals(boolean.class) || m.getReturnType().equals(void.class))) {
                        String msg =
                                "RANDOOP ANNOTATION ERROR: Expected @CheckRep-annotated method "
                                        + m.getName()
                                        + " in class "
                                        + m.getDeclaringClass()
                                        + " to have void or boolean return type but it does not (method signature:"
                                        + m.toString()
                                        + ").";
                        throw new RuntimeException(msg);
                    }

                    printDetectedAnnotatedCheckRepMethod(m);
                    contractsFound.add(new CheckRepContract(m));
                }
            }
        }
        return contractsFound;
    }

    private static void printDetectedAnnotatedCheckRepMethod(Method m) {
        String msg =
                "ANNOTATION: Detected @CheckRep-annotated method \""
                        + m.toString()
                        + "\". Will use it to check rep invariant of class "
                        + m.getDeclaringClass().getCanonicalName()
                        + " during generation.";
        System.out.println(msg);
    }

    public ConcreteOperation getOperation(Constructor<Object> constructor) {
        return null;
    }
}
