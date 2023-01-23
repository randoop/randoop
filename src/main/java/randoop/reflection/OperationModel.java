package randoop.reflection;

import static java.nio.charset.StandardCharsets.UTF_8;
import static randoop.main.GenInputsAbstract.ClassLiteralsMode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.plumelib.util.EntryReader;
import org.plumelib.util.UtilPlume;
import randoop.Globals;
import randoop.condition.SpecificationCollection;
import randoop.contract.CompareToAntiSymmetric;
import randoop.contract.CompareToEquals;
import randoop.contract.CompareToReflexive;
import randoop.contract.CompareToSubs;
import randoop.contract.CompareToTransitive;
import randoop.contract.EqualsHashcode;
import randoop.contract.EqualsReflexive;
import randoop.contract.EqualsReturnsNormally;
import randoop.contract.EqualsSymmetric;
import randoop.contract.EqualsToNullRetFalse;
import randoop.contract.EqualsTransitive;
import randoop.contract.ObjectContract;
import randoop.contract.SizeToArrayLength;
import randoop.generation.ComponentManager;
import randoop.main.ClassNameErrorHandler;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.main.RandoopClassNameError;
import randoop.main.RandoopUsageError;
import randoop.operation.OperationParseException;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.test.ContractSet;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;
import randoop.util.Log;
import randoop.util.MultiMap;

/**
 * {@code OperationModel} represents the information context from which tests are generated. The
 * model includes:
 *
 * <ul>
 *   <li>classes under test,
 *   <li>operations of all classes,
 *   <li>any atomic code sequences derived from command-line arguments, and
 *   <li>the contracts or oracles used to generate tests.
 * </ul>
 *
 * <p>This class manages all information about generic classes internally, and instantiates any type
 * variables in operations before returning them.
 */
public class OperationModel {

  /** The set of class declaration types for this model. */
  private Set<ClassOrInterfaceType> classTypes;

  /** The set of input types for this model. */
  private Set<Type> inputTypes;

  /** The set of classes used as goals in the covered-class test filter. */
  private final LinkedHashSet<Class<?>> coveredClassesGoal;

  /** Map for singleton sequences of literals extracted from classes. */
  private MultiMap<ClassOrInterfaceType, Sequence> classLiteralMap;

  /** Set of singleton sequences for values from TestValue annotated fields. */
  private Set<Sequence> annotatedTestValues;

  /** Set of object contracts used to generate tests. */
  private ContractSet contracts;

  /** Set of concrete operations extracted from classes. */
  private final Set<TypedOperation> operations;

  /** For debugging only. */
  private List<Pattern> omitMethods;

  /** User-supplied predicate for methods that should not be used during test generation. */
  private OmitMethodsPredicate omitMethodsPredicate;

  /** Create an empty model of test context. */
  private OperationModel() {
    // TreeSet here for deterministic coverage in the systemTest runNaiveCollectionsTest()
    classTypes = new TreeSet<>();
    inputTypes = new TreeSet<>();
    classLiteralMap = new MultiMap<>();
    annotatedTestValues = new LinkedHashSet<>();
    contracts = new ContractSet();
    contracts.add(EqualsReflexive.getInstance()); // arity=1
    contracts.add(EqualsSymmetric.getInstance()); // arity=2
    contracts.add(EqualsHashcode.getInstance()); // arity=2
    contracts.add(EqualsToNullRetFalse.getInstance()); // arity=1
    contracts.add(EqualsReturnsNormally.getInstance()); // arity=1
    contracts.add(EqualsTransitive.getInstance()); // arity=3
    contracts.add(CompareToReflexive.getInstance()); // arity=1
    contracts.add(CompareToAntiSymmetric.getInstance()); // arity=2
    contracts.add(CompareToEquals.getInstance()); // arity=2
    contracts.add(CompareToSubs.getInstance()); // arity=3
    contracts.add(CompareToTransitive.getInstance()); // arity=3
    contracts.add(SizeToArrayLength.getInstance()); // arity=1

    coveredClassesGoal = new LinkedHashSet<>();
    operations = new TreeSet<>();
  }

  /**
   * Factory method to construct an operation model for a particular set of classes.
   *
   * @param accessibility the {@link AccessibilityPredicate} to test accessibility of classes and
   *     class members
   * @param reflectionPredicate the reflection predicate to determine which classes and class
   *     members are used
   * @param omitMethods the patterns for operations that should be omitted
   * @param classnames the names of classes under test
   * @param coveredClassesGoalNames the coverage goal: the names of classes to be tested by the
   *     covered class heuristic
   * @param errorHandler the handler for bad file name errors
   * @param literalsFileList the list of literals file names
   * @param operationSpecifications the collection of operation specifications
   * @return the {@link OperationModel} constructed with the given arguments
   * @throws SignatureParseException if a method signature is ill-formed
   * @throws NoSuchMethodException if an attempt is made to load a non-existent method
   */
  public static OperationModel createModel(
      AccessibilityPredicate accessibility,
      ReflectionPredicate reflectionPredicate,
      List<Pattern> omitMethods,
      Set<@ClassGetName String> classnames,
      Set<@ClassGetName String> coveredClassesGoalNames,
      ClassNameErrorHandler errorHandler,
      List<String> literalsFileList,
      SpecificationCollection operationSpecifications)
      throws SignatureParseException, NoSuchMethodException {

    OperationModel model = new OperationModel();

    // for debugging only
    model.omitMethods = omitMethods;

    model.addClassTypes(
        accessibility,
        reflectionPredicate,
        classnames,
        coveredClassesGoalNames,
        errorHandler,
        literalsFileList);

    model.omitMethodsPredicate = new OmitMethodsPredicate(omitMethods);

    model.addOperationsFromClasses(accessibility, reflectionPredicate, operationSpecifications);
    model.operations.addAll(
        model.getOperationsFromFile(
            GenInputsAbstract.methodlist, accessibility, reflectionPredicate));
    model.addObjectConstructor();

    return model;
  }

  /**
   * Factory method to construct an operation model for a particular set of classes without an
   * omit-methods list or behavior specifications.
   *
   * @param accessibility the {@link randoop.reflection.AccessibilityPredicate} to test
   *     accessibility of classes and class members
   * @param reflectionPredicate the reflection predicate to determine which classes and class
   *     members are used
   * @param classnames the names of classes under test
   * @param coveredClassnames the names of classes to be tested by exercised heuristic
   * @param errorHandler the handler for bad file name errors
   * @param literalsFileList the list of literals file names
   * @return the operation model for the parameters
   * @throws SignatureParseException if a method signature is ill-formed
   * @throws NoSuchMethodException if an attempt is made to load a non-existent method
   */
  static OperationModel createModel(
      AccessibilityPredicate accessibility,
      ReflectionPredicate reflectionPredicate,
      Set<@ClassGetName String> classnames,
      Set<@ClassGetName String> coveredClassnames,
      ClassNameErrorHandler errorHandler,
      List<String> literalsFileList)
      throws NoSuchMethodException, SignatureParseException {
    return createModel(
        accessibility,
        reflectionPredicate,
        new ArrayList<Pattern>(0),
        classnames,
        coveredClassnames,
        errorHandler,
        literalsFileList,
        null);
  }

  /**
   * Factory method to construct an operation model for a particular set of classes without behavior
   * specifications.
   *
   * @param accessibility the {@link AccessibilityPredicate} to test accessibility of classes and
   *     class members
   * @param reflectionPredicate the reflection predicate to determine which classes and class
   *     members are used
   * @param omitMethods the patterns for operations that should be omitted
   * @param classnames the names of classes under test
   * @param coveredClassnames the names of classes to be tested by covered class heuristic
   * @param errorHandler the handler for bad file name errors
   * @param literalsFileList the list of literals file names
   * @return the {@link OperationModel} constructed with the given arguments
   * @throws SignatureParseException if a method signature is ill-formed
   * @throws NoSuchMethodException if an attempt is made to load a non-existent method
   */
  public static OperationModel createModel(
      AccessibilityPredicate accessibility,
      ReflectionPredicate reflectionPredicate,
      List<Pattern> omitMethods,
      Set<@ClassGetName String> classnames,
      Set<@ClassGetName String> coveredClassnames,
      ClassNameErrorHandler errorHandler,
      List<String> literalsFileList)
      throws NoSuchMethodException, SignatureParseException {
    return createModel(
        accessibility,
        reflectionPredicate,
        omitMethods,
        classnames,
        coveredClassnames,
        errorHandler,
        literalsFileList,
        null);
  }

  /**
   * Adds literals to the component manager, by parsing any literals files specified by the user.
   * Includes literals at different levels indicated by {@link ClassLiteralsMode}.
   *
   * @param compMgr the component manager
   * @param literalsFile the list of literals file names
   * @param literalsLevel the level of literals to add
   */
  public void addClassLiterals(
      ComponentManager compMgr, List<String> literalsFile, ClassLiteralsMode literalsLevel) {

    // Add a (1-element) sequence corresponding to each literal to the component
    // manager.

    for (String filename : literalsFile) {
      MultiMap<ClassOrInterfaceType, Sequence> literalmap;
      if (filename.equals("CLASSES")) {
        literalmap = classLiteralMap;
      } else {
        literalmap = LiteralFileReader.parse(filename);
      }

      for (ClassOrInterfaceType type : literalmap.keySet()) {
        Package pkg = (literalsLevel == ClassLiteralsMode.PACKAGE ? type.getPackage() : null);
        for (Sequence seq : literalmap.getValues(type)) {
          switch (literalsLevel) {
            case CLASS:
              compMgr.addClassLevelLiteral(type, seq);
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
                  "Unexpected error in GenTests.  Please report at"
                      + " https://github.com/randoop/randoop/issues , providing the information"
                      + " requested at"
                      + " https://randoop.github.io/randoop/manual/index.html#bug-reporting .");
          }
        }
      }
    }
  }

  /**
   * Given a file containing fully-qualified method signatures, returns the operations for them.
   *
   * @param file a file that contains method or constructor signatures, one per line. If null, this
   *     method returns an empty map.
   * @return a map from each class type to its methods and constructors that were read from the file
   * @throws OperationParseException if a method signature cannot be parsed
   */
  public static MultiMap<Type, TypedClassOperation> readOperations(@Nullable Path file)
      throws OperationParseException {
    return readOperations(file, false);
  }

  /**
   * Given a file containing fully-qualified method signatures, returns the operations for them.
   *
   * @param file a file that contains method or constructor signatures, one per line. If null, this
   *     method returns an empty map.
   * @param ignoreParseError if true, ignore parse errors (skip malformed signatures)
   * @return a map from each class type to its methods and constructors that were read from the file
   * @throws OperationParseException if a method signature cannot be parsed
   */
  public static MultiMap<Type, TypedClassOperation> readOperations(
      @Nullable Path file, boolean ignoreParseError) throws OperationParseException {
    if (file != null) {
      try (EntryReader er = new EntryReader(file, "(//|#).*$", null)) {
        return OperationModel.readOperations(er, ignoreParseError);
      } catch (IOException e) {
        String message = String.format("Error while reading file %s: %s%n", file, e.getMessage());
        throw new RandoopUsageError(message, e);
      }
    }
    return new MultiMap<>(0);
  }

  /**
   * Returns operations read from the given EntryReader, which contains fully-qualified method
   * signatures.
   *
   * @param er the EntryReader to read from
   * @param ignoreParseError if true, ignore parse errors (skip malformed signatures)
   * @return contents of the file, as a map from classes to operations
   */
  private static MultiMap<Type, TypedClassOperation> readOperations(
      EntryReader er, boolean ignoreParseError) {
    MultiMap<Type, TypedClassOperation> operationsMap = new MultiMap<>();
    for (String line : er) {
      String sig = line.trim();
      TypedClassOperation operation;
      try {
        operation =
            signatureToOperation(
                sig, AccessibilityPredicate.IS_ANY, new EverythingAllowedPredicate());
      } catch (SignatureParseException e) {
        if (ignoreParseError) {
          continue;
        } else {
          throw new RandoopUsageError(
              String.format("%s:%d: %s", er.getFileName(), er.getLineNumber(), e));
        }
      } catch (FailedPredicateException e) {
        throw new RandoopBug("This can't happen", e);
      }
      if (operation.getInputTypes().size() > 0) {
        operationsMap.add(operation.getInputTypes().get(0), operation);
      }
    }
    return operationsMap;
  }

  /**
   * Returns operations read from the given stream, which contains fully-qualified method
   * signatures.
   *
   * @param is the stream from which to read
   * @param filename the file name to use in diagnostic messages
   * @return contents of the file, as a map from classes to operations
   */
  public static MultiMap<Type, TypedClassOperation> readOperations(
      InputStream is, String filename) {
    return readOperations(is, filename, false);
  }

  /**
   * Returns operations read from the given stream, which contains fully-qualified method
   * signatures.
   *
   * @param is the stream from which to read
   * @param filename the file name to use in diagnostic messages
   * @param ignoreParseError if true, ignore parse errors (skip malformed signatures)
   * @return contents of the file, as a map from classes to operations
   */
  public static MultiMap<Type, TypedClassOperation> readOperations(
      InputStream is, String filename, boolean ignoreParseError) {
    if (is == null) {
      throw new RandoopBug("input stream is null for file " + filename);
    }
    // Read method omissions from user-provided file
    try (EntryReader er = new EntryReader(is, filename, "^#.*", null)) {
      return OperationModel.readOperations(er, ignoreParseError);
    } catch (IOException e) {
      String message = String.format("Error while reading file %s: %s%n", filename, e.getMessage());
      throw new RandoopUsageError(message, e);
    }
  }

  /**
   * Returns the set of types for classes under test.
   *
   * @return the set of class types
   */
  public Set<ClassOrInterfaceType> getClassTypes() {
    return classTypes;
  }

  /**
   * Returns the set of {@code Class<?>} objects that are the goals for the covered class heuristic.
   *
   * @return the set of covered classes
   */
  public Set<Class<?>> getCoveredClassesGoal() {
    return coveredClassesGoal;
  }

  /**
   * Returns the set of input types that occur as parameters in classes under test.
   *
   * @return the set of input types that occur in classes under test
   * @see TypeExtractor
   */
  public Set<Type> getInputTypes() {
    // TODO this is not used, should it be? or should it even be here?
    return inputTypes;
  }

  /**
   * Return the operations of this model as a list.
   *
   * @return the operations of this model
   */
  public List<TypedOperation> getOperations() {
    return new ArrayList<>(operations);
  }

  /**
   * Returns all {@link ObjectContract} objects for this run of Randoop. Includes Randoop defaults
   * and {@link randoop.CheckRep} annotated methods.
   *
   * @return the list of contracts
   */
  public ContractSet getContracts() {
    return contracts;
  }

  /**
   * Returns the user-specified predicate for methods that should not be called.
   *
   * @return the user-specified predicate for methods that should not be called
   */
  public OmitMethodsPredicate getOmitMethodsPredicate() {
    return omitMethodsPredicate;
  }

  /**
   * Returns the set of singleton sequences for values from {@code @TestValue} annotated fields.
   *
   * @return sequences that get fields annotated with {@code @TestValue}
   */
  public Set<Sequence> getAnnotatedTestValues() {
    return annotatedTestValues;
  }

  public void log() {
    if (Log.isLoggingOn()) {
      logOperations(GenInputsAbstract.log);
    }
  }

  /**
   * Output the operations of this model to {@code out}, if logging is enabled.
   *
   * @param out the PrintStream on which to produce output
   */
  public void logOperations(PrintStream out) {
    logOperations(new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, UTF_8))));
  }

  /**
   * Output the operations of this model, if logging is enabled.
   *
   * @param out the Writer on which to produce output
   */
  public void logOperations(Writer out) {
    try {
      out.write("Operations: (" + operations.size() + ")" + Globals.lineSep);
      for (TypedOperation t : operations) {
        out.write("  " + t.toString());
        out.write(Globals.lineSep);
        out.flush();
      }
    } catch (IOException e) {
      throw new RandoopBug("Error while logging operations", e);
    }
  }

  /** Print a verbose representation of the model, if logging is enabled. */
  public void dumpModel() {
    if (Log.isLoggingOn()) {
      dumpModel(GenInputsAbstract.log);
    }
  }

  /**
   * Print a verbose representation of the model to {@code out}.
   *
   * @param out the PrintStream on which to produce output
   */
  public void dumpModel(PrintStream out) {
    dumpModel(new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, UTF_8))));
  }

  /**
   * Print a verbose representation of the model to {@code out}.
   *
   * @param out the Writer on which to produce output
   */
  public void dumpModel(Writer out) {
    try {
      out.write(String.format("Model with hashcode %s:%n", hashCode()));
      out.write(String.format("  classTypes = %s%n", classTypes));
      out.write(String.format("  inputTypes = %s%n", inputTypes));
      out.write(String.format("  coveredClassesGoal = %s%n", coveredClassesGoal));
      out.write(String.format("  classLiteralMap = %s%n", classLiteralMap));
      out.write(String.format("  annotatedTestValues = %s%n", annotatedTestValues));
      out.write(String.format("  contracts = %s%n", contracts));
      out.write(String.format("  omitMethods = [%n"));
      for (Pattern p : omitMethods) {
        out.write(String.format("    %s%n", p));
      }
      out.write(String.format("  ]%n"));
      // Use logOperations instead: out.write(String.format("  operations = %s%n", operations));
      logOperations(out);
    } catch (IOException ioe) {
      throw new Error(ioe);
    }
  }

  /**
   * Gathers class types to be used in a run of Randoop and adds them to this {@code
   * OperationModel}. Specifically, collects types for classes-under-test, objects for covered-class
   * heuristic, concrete input types, annotated test values, and literal values. It operates by
   * converting from strings to {@code Class} objects. Also collects annotated test values, and
   * class literal values used in test generation.
   *
   * @param accessibility the accessibility predicate
   * @param reflectionPredicate the predicate to determine which reflection objects are used
   * @param classnames the names of classes-under-test
   * @param coveredClassesGoalNames the names of classes used as goals in the covered-class
   *     heuristic
   * @param errorHandler the handler for bad class names
   * @param literalsFileList the list of literals file names
   */
  private void addClassTypes(
      AccessibilityPredicate accessibility,
      ReflectionPredicate reflectionPredicate,
      Set<@ClassGetName String> classnames,
      Set<@ClassGetName String> coveredClassesGoalNames,
      ClassNameErrorHandler errorHandler,
      List<String> literalsFileList) {
    ReflectionManager mgr = new ReflectionManager(accessibility);
    mgr.add(new DeclarationExtractor(this.classTypes, reflectionPredicate));
    mgr.add(new TypeExtractor(this.inputTypes, accessibility));
    mgr.add(new TestValueExtractor(this.annotatedTestValues));
    mgr.add(new CheckRepExtractor(this.contracts));
    if (literalsFileList.contains("CLASSES")) {
      mgr.add(new ClassLiteralExtractor(this.classLiteralMap));
    }

    // Collect classes under test
    int succeeded = 0;
    for (String classname : classnames) {
      Class<?> c;
      try {
        c = getClass(classname, errorHandler);
      } catch (RandoopClassNameError e) {
        // System.out.println();
        // System.out.println(e.getMessage());
        // System.out.println();
        // continue;
        throw new RandoopUsageError("Could not load class " + classname + ": " + e.getMessage());
      }
      // Note that c could be null if errorHandler just warns on bad names
      if (c != null) {
        // Don't exclude abstract classes and interfaces.  They cannot be instantiated, but they can
        // be a return type, so Randoop can obtain variables of those declared types.
        boolean classIsAccessible = accessibility.isAccessible(c);
        boolean hasAccessibleStaticMethod = false;
        if (!classIsAccessible) {
          for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers()) && accessibility.isAccessible(m)) {
              hasAccessibleStaticMethod = true;
              break;
            }
          }
          System.out.printf(
              "Cannot instantiate non-accessible %s specified via --testclass or --classlist%s.%n",
              c, hasAccessibleStaticMethod ? "; will use its static methods" : "");
        }
        if (classIsAccessible || hasAccessibleStaticMethod) {
          try {
            mgr.apply(c);
            succeeded++;
          } catch (Throwable e) {
            System.out.printf(
                "Cannot get methods for %s specified via --testclass or --classlist due to"
                    + " exception:%n%s%n",
                c.getName(), UtilPlume.stackTraceToString(e));
          }
        }
      }
    }
    if (GenInputsAbstract.progressdisplay) {
      if (succeeded == classnames.size()) {
        System.out.printf("%nWill try to generate tests for %d classes.%n", succeeded);
      } else {
        System.out.printf(
            "%nWill try to generate tests for %d out of %d classes.%n",
            succeeded, classnames.size());
      }
    }

    // Collect covered classes
    for (String classname : coveredClassesGoalNames) {
      Class<?> c = getClass(classname, errorHandler);
      if (c != null && !c.isInterface()) {
        coveredClassesGoal.add(c);
      }
    }
  }

  /**
   * Returns the class whose name is {@code classname}. A wrapper around Class.forName.
   *
   * @param classname the name of a class or primitive type
   * @param errorHandler is called if no such class exists
   * @return the Class whose name is {@code classname}. May return null if {@code errorHandler} just
   *     warns on bad names.
   */
  private static @Nullable Class<?> getClass(
      @ClassGetName String classname, ClassNameErrorHandler errorHandler) {
    try {
      return TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      errorHandler.handle(classname, e);
    } catch (Throwable e) {
      if (e.getCause() != null) {
        e = e.getCause();
      }
      errorHandler.handle(classname, e);
    }
    return null;
  }

  /**
   * Adds operations to this {@link OperationModel} from all of the classes of {@link #classTypes}.
   *
   * @param accessibility the accessibility predicate
   * @param reflectionPredicate the reflection predicate
   * @param operationSpecifications the collection of {@link
   *     randoop.condition.specification.OperationSpecification}
   */
  private void addOperationsFromClasses(
      AccessibilityPredicate accessibility,
      ReflectionPredicate reflectionPredicate,
      SpecificationCollection operationSpecifications) {
    Iterator<ClassOrInterfaceType> itor = classTypes.iterator();
    while (itor.hasNext()) {
      ClassOrInterfaceType classType = itor.next();
      try {
        Collection<TypedOperation> oneClassOperations =
            OperationExtractor.operations(
                classType,
                reflectionPredicate,
                omitMethodsPredicate,
                accessibility,
                operationSpecifications);
        operations.addAll(oneClassOperations);
      } catch (Throwable e) {
        // TODO: What is an example of this?  Should an error be raised, rather than this
        // easy-to-overlook output?
        System.out.printf(
            "Removing %s from the classes under test due to problem extracting operations:%n%s%n",
            classType, UtilPlume.stackTraceToString(e));
        itor.remove();
      }
    }
  }

  /**
   * Constructs an operation from every method signature in the given file.
   *
   * @param methodSignatures_file the file containing the signatures; if null, return the emply list
   * @param accessibility the accessibility predicate
   * @param reflectionPredicate the reflection predicate
   * @return operations read from the file
   * @throws SignatureParseException if any signature is syntactically invalid
   */
  private List<TypedClassOperation> getOperationsFromFile(
      Path methodSignatures_file,
      AccessibilityPredicate accessibility,
      ReflectionPredicate reflectionPredicate)
      throws SignatureParseException {
    List<TypedClassOperation> result = new ArrayList<>();
    if (methodSignatures_file == null) {
      return result;
    }
    try (EntryReader reader = new EntryReader(methodSignatures_file, "(//|#).*$", null)) {
      for (String line : reader) {
        String sig = line.trim();
        if (!sig.isEmpty()) {
          try {
            TypedClassOperation operation =
                signatureToOperation(sig, accessibility, reflectionPredicate);
            if (!omitMethodsPredicate.shouldOmit(operation)) {
              result.add(operation);
            }
          } catch (FailedPredicateException e) {
            System.out.printf("Ignoring %s that failed predicate: %s%n", sig, e.getMessage());
          }
        }
      }
    } catch (IOException e) {
      throw new RandoopUsageError("Problem reading file " + methodSignatures_file, e);
    }
    return result;
  }

  /**
   * Given a signature, returns the method or constructor it represents.
   *
   * @param signature the operation's signature, in Randoop's format
   * @param accessibility the accessibility predicate
   * @param reflectionPredicate the reflection predicate
   * @return the method or constructor that the signature represents
   * @throws FailedPredicateException if the accessibility or reflection predicate returns false on
   *     the class or the method or constructor
   * @throws SignatureParseException if the signature cannot be parsed
   */
  public static TypedClassOperation signatureToOperation(
      String signature,
      AccessibilityPredicate accessibility,
      ReflectionPredicate reflectionPredicate)
      throws SignatureParseException, FailedPredicateException {
    AccessibleObject accessibleObject;
    accessibleObject = SignatureParser.parse(signature, accessibility, reflectionPredicate);
    if (accessibleObject == null) {
      throw new FailedPredicateException(
          String.format(
              "accessibleObject is null for %s, typically due to predicates: %s, %s",
              signature, accessibility, reflectionPredicate));
    }
    if (accessibleObject instanceof Constructor) {
      return TypedOperation.forConstructor((Constructor) accessibleObject);
    } else {
      return TypedOperation.forMethod((Method) accessibleObject);
    }
  }

  /** Creates and adds the Object class default constructor call to the concrete operations. */
  private void addObjectConstructor() {
    Constructor<?> objectConstructor;
    try {
      objectConstructor = Object.class.getConstructor();
    } catch (NoSuchMethodException e) {
      throw new RandoopBug("unable to load java.lang.Object() constructor", e);
    }
    TypedClassOperation operation = TypedOperation.forConstructor(objectConstructor);
    classTypes.add(operation.getDeclaringType());
    operations.add(operation);
  }
}
