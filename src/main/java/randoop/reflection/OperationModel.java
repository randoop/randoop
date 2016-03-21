package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.CheckRep;
import randoop.ComponentManager;
import randoop.LiteralFileReader;
import randoop.contract.CheckRepContract;
import randoop.contract.EqualsHashcode;
import randoop.contract.EqualsReflexive;
import randoop.contract.EqualsSymmetric;
import randoop.contract.EqualsToNullRetFalse;
import randoop.contract.ObjectContract;
import randoop.main.ClassNameErrorHandler;
import randoop.operation.ConcreteOperation;
import randoop.operation.NonreceiverTerm;
import randoop.sequence.Sequence;
import randoop.types.ConcreteType;
import randoop.types.TypeFactory;
import randoop.util.ClassFileConstants;
import randoop.util.MultiMap;

import static randoop.main.GenInputsAbstract.ClassLiteralsMode;

/**
 * {@code OperationModel} represents the class and operation model of a set of input classes for
 * which tests are to be generated.
 */
public class OperationModel {
    private MultiMap<Class<?>, Sequence> classLiteralMap; // instantiated using ClassLiteralExtractor
    private Set<Sequence> annotatedTestValues; // instantiated using TestValueExtractor
    private List<ObjectContract> contracts;


// TODO make sure adding Object constructor
// TODO move seed sequence TestValue annotation scraping to here --- do annotation scraping as visitor

    // TODO should typefactory really just be reflectionmanager?
// TODO and do annotation scraping as visitor?

    //TODO Add use of TestValueExtractor for scraping TestValue annotation
    //TODO Add use of CheckRepExtractor for scraping CheckRep annotation
    //TODO Add use of ClassLiteralExtractor 

    private OperationModel() {
        classLiteralMap = new MultiMap<>();
        annotatedTestValues = new LinkedHashSet<>();
        List<ObjectContract> = new ArrayList<>();
    }

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

        //when see new class:
        // - get type
        // - get all operations
        // - if type is
        OperationModel model = new OperationModel();
        TypeFactory typeFactory = new TypeFactory(reflectionPredicate, model);

        Set<Class<?>> coveredClasses = new LinkedHashSet<>();

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
     * Adds literals to the component manager, by parsing any literals files
     * specified by the user.
     */
    public void addClassLiterals(ComponentManager compMgr, List<String> literalsFile, ClassLiteralsMode literalsLevel) {

        // Add a (1-element) sequence corresponding to each literal to the component
        // manager.

        for (String filename : literalsFile) {
            MultiMap<Class<?>, Sequence> literalmap;
            if (filename.equals("CLASSES")) {
                literalmap = classLiteralMap;
            } else {
                literalmap = LiteralFileReader.parse(filename);
            }

            for (Class<?> cls : literalmap.keySet()) {
                Package pkg =
                        (literalsLevel == ClassLiteralsMode.PACKAGE
                                ? cls.getPackage()
                                : null);
                for (Sequence seq : literalmap.getValues(cls)) {
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

    private void addContracts(Set<ObjectContract> checkRepContracts) {
        // Add CheckRep contracts collected by CheckRepExtractor
        contracts.addAll(checkRepContracts);

        // Add all of Randoop's default contracts.
        // Note: if you add to this list, also update the Javadoc for
        // check_object_contracts.
        contracts.add(new EqualsReflexive());
        contracts.add(new EqualsSymmetric());
        contracts.add(new EqualsHashcode());
        contracts.add(new EqualsToNullRetFalse());
    }

  /**
   * Returns all {@link ObjectContract} objects for this run of Randoop.
   * Includes Randoop defaults and {@link randoop.CheckRep} annotated methods.
   *
   * @return the list of contracts
   */
    public List<ObjectContract> getContracts() {
        return contracts;
    }

    public ConcreteOperation getOperation(Constructor<Object> constructor) {
        return null;
    }

    public Set<Sequence> getAnnotatedTestValues() {
        return annotatedTestValues;
    }
}
