package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.ComponentManager;
import randoop.LiteralFileReader;
import randoop.contract.EqualsHashcode;
import randoop.contract.EqualsReflexive;
import randoop.contract.EqualsSymmetric;
import randoop.contract.EqualsToNullRetFalse;
import randoop.contract.ObjectContract;
import randoop.main.ClassNameErrorHandler;
import randoop.operation.ConcreteOperation;
import randoop.sequence.Sequence;
import randoop.types.ConcreteType;
import randoop.types.TypeNames;
import randoop.util.MultiMap;

import static randoop.main.GenInputsAbstract.ClassLiteralsMode;

/**
 * {@code OperationModel} represents the context in which tests are to be generated.
 * The model includes:
 * <ul>
 *   <li>Classes under test.</li>
 *   <li>Operations of all classes.</li>
 *   <li>Any atomic code sequences derived from command-line arguments.</li>
 *   <li>The contracts or oracles used to generate tests.</li>
 * </ul>
 * <p>
 * This class manages all information about generic classes internally, and instantiates any
 * type variables in operations before returning them.
 */
public class OperationModel {

  private final LinkedHashSet<Class<?>> coveredClasses;
  /** Map for singleton sequences of literals extracted from classes. */
  private MultiMap<Class<?>, Sequence> classLiteralMap;

  /** Set of singleton sequences for values from TestValue annotated fields. */
  private Set<Sequence> annotatedTestValues; // instantiated using TestValueExtractor

  /** Set of object contracts ("oracles") used to generate tests. */
  private Set<ObjectContract> contracts;

  /**
   * Create an empty model of test context.
   */
  private OperationModel() {
    classLiteralMap = new MultiMap<>();
    annotatedTestValues = new LinkedHashSet<>();
    contracts = new LinkedHashSet<>();
    coveredClasses = new LinkedHashSet<>();
  }

  /**
   * Factory method to construct an operation model for a particular set of classes
   *
   * @param visibility
   *          the {@link randoop.reflection.VisibilityPredicate} to test
   *          accessibility of classes and class members.
   * @param reflectionPredicate  the reflection predicate to determine which classes and
   *                             class members are used
   * @param classnames  the names of classes under test
   * @param exercisedClassnames  the names of classes to be tested by exercised heuristic
   * @return the operation model for the parameters
   */
  public static OperationModel createModel(
      VisibilityPredicate visibility,
      DefaultReflectionPredicate reflectionPredicate,
      Set<String> classnames,
      Set<String> exercisedClassnames,
      ClassNameErrorHandler errorHandler,
      List<String> literalsFileList) {

    // TODO make sure adding Object constructor

    //when see new class:
    // - get type
    // - get all operations
    // - if type is
    OperationModel model = new OperationModel();
    ReflectionManager mgr = new ReflectionManager(reflectionPredicate);
    mgr.add(new TestValueExtractor(model.annotatedTestValues));
    mgr.add(new CheckRepExtractor(model.contracts));
    if (literalsFileList.contains("CLASSES")) {
      mgr.add(new ClassLiteralExtractor(model.classLiteralMap));
    }

    // Collect classes under test
    for (String classname : classnames) {
      Class<?> c = null;
      try {
        c = TypeNames.getTypeForName(classname);
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
            mgr.apply(c);
          }
          if (exercisedClassnames.contains(classname)) {
            model.addCoveredClass(c);
          }
        }
      }
    }

    // Collect exercised classes
    for (String classname : exercisedClassnames) {
      if (!classnames.contains(classname)) {
        Class<?> c = null;
        try {
          c = TypeNames.getTypeForName(classname);
        } catch (ClassNotFoundException e) {
          errorHandler.handle(classname);
        }

        if (!visibility.isVisible(c)) {
          System.out.println(
              "Ignorning non-visible " + c + " specified as include-if-class-exercised target");
        } else if (c.isInterface()) {
          System.out.println("Ignoring " + c + " specified as include-if-class-exercised target.");
        } else {
          model.addCoveredClass(c);
        }
      }
    }

    model.addDefaultContracts();

    return model;
  }

  private void addCoveredClass(Class<?> c) {
    coveredClasses.add(c);
  }

  /**
   * Adds literals to the component manager, by parsing any literals files
   * specified by the user.
   */
  public void addClassLiterals(
      ComponentManager compMgr, List<String> literalsFile, ClassLiteralsMode literalsLevel) {

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
        Package pkg = (literalsLevel == ClassLiteralsMode.PACKAGE ? cls.getPackage() : null);
        for (Sequence seq : literalmap.getValues(cls)) {
          switch (literalsLevel) {
            case CLASS:
              compMgr.addClassLevelLiteral(ConcreteType.forClass(cls), seq);
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
    for (String sig : methodSignatures) {
      //parse sig to operation
    }
  }

  /**
   * Add Randoop's default contracts for test generation.
   * <p>
   *   Note: if you add to this list, also update the Javadoc for check_object_contracts.
   */
  private void addDefaultContracts() {
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
  public Set<ObjectContract> getContracts() {
    return contracts;
  }

  public ConcreteOperation getOperation(Constructor<Object> constructor) {
    return null;
  }

  public Set<Sequence> getAnnotatedTestValues() {
    return annotatedTestValues;
  }
}
