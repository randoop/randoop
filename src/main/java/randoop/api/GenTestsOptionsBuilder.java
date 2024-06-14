package randoop.api;

import org.checkerframework.checker.signature.qual.BinaryName;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.plumelib.util.FileWriterWithName;
import randoop.main.GenInputsAbstract;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GenTestsOptionsBuilder {

  private List<Path> testjar = new ArrayList<>();
  private List<String> testPackage = new ArrayList<>();
  private Path classlist = GenInputsAbstract.classlist;
  private List<@BinaryName String> testclass = new ArrayList<>();
  private Path methodlist = GenInputsAbstract.methodlist;
  private List<Pattern> omitClasses = new ArrayList<>();
  private List<Path> omitClassesFile = new ArrayList<>();
  private List<Pattern> omitMethods = new ArrayList<>();
  private List<Path> omitMethodsFile = new ArrayList<>();
  private List<String> omitField = new ArrayList<>();
  private Path omitFieldFile = GenInputsAbstract.omit_field_file;
  private boolean onlyTestPublicMembers = GenInputsAbstract.only_test_public_members;
  private boolean silentlyIgnoreBadClassNames = GenInputsAbstract.silently_ignore_bad_class_names;
  private GenInputsAbstract.FlakyTestAction flakyTestBehavior = GenInputsAbstract.flaky_test_behavior;
  private int nondeterministicMethodsToOutput = GenInputsAbstract.nondeterministic_methods_to_output;
  private boolean noErrorRevealingTests = GenInputsAbstract.no_error_revealing_tests;
  private boolean noRegressionTests = GenInputsAbstract.no_regression_tests;
  private boolean noRegressionAssertions = GenInputsAbstract.no_regression_assertions;
  private boolean checkCompilable = GenInputsAbstract.check_compilable;
  private Pattern requireClassnameInTest = GenInputsAbstract.require_classname_in_test;
  private Path requireCoveredClasses = GenInputsAbstract.require_covered_classes;
  private boolean minimizeErrorTest = GenInputsAbstract.minimize_error_test;
  private GenInputsAbstract.BehaviorType checkedException = GenInputsAbstract.checked_exception;
  private GenInputsAbstract.BehaviorType uncheckedException = GenInputsAbstract.unchecked_exception;
  private GenInputsAbstract.BehaviorType cmException = GenInputsAbstract.cm_exception;
  private GenInputsAbstract.BehaviorType ncdfException = GenInputsAbstract.ncdf_exception;
  private GenInputsAbstract.BehaviorType npeOnNullInput = GenInputsAbstract.npe_on_null_input;
  private GenInputsAbstract.BehaviorType npeOnNonNullInput = GenInputsAbstract.npe_on_non_null_input;
  private GenInputsAbstract.BehaviorType oomException = GenInputsAbstract.oom_exception;
  private GenInputsAbstract.BehaviorType sofException = GenInputsAbstract.sof_exception;
  private List<Path> specifications = GenInputsAbstract.specifications;
  private boolean useJdkSpecifications = GenInputsAbstract.use_jdk_specifications;
  private boolean ignoreConditionCompilationError = GenInputsAbstract.ignore_condition_compilation_error;
  private boolean ignoreConditionException = GenInputsAbstract.ignore_condition_exception;
  private Path sideEffectFreeMethods = GenInputsAbstract.side_effect_free_methods;
  private int timeLimit = GenInputsAbstract.time_limit;
  private int attemptedLimit = GenInputsAbstract.attempted_limit;
  private int generatedLimit = GenInputsAbstract.generated_limit;
  private int outputLimit = GenInputsAbstract.output_limit;
  private int maxsize = GenInputsAbstract.maxsize;
  private boolean stopOnErrorTest = GenInputsAbstract.stop_on_error_test;
  private double nullRatio = GenInputsAbstract.null_ratio;
  private boolean forbidNull = GenInputsAbstract.forbid_null;
  private List<String> literalsFile = GenInputsAbstract.literals_file;
  private GenInputsAbstract.ClassLiteralsMode literalsLevel = GenInputsAbstract.literals_level;
  private GenInputsAbstract.MethodSelectionMode methodSelection = GenInputsAbstract.method_selection;
  private int stringMaxlen = GenInputsAbstract.string_maxlen;
  private double aliasRatio = GenInputsAbstract.alias_ratio;
  private GenInputsAbstract.InputSelectionMode inputSelection = GenInputsAbstract.input_selection;
  private int clear = GenInputsAbstract.clear;
  private long clearMemory = GenInputsAbstract.clear_memory;
  private int testsPerFile = GenInputsAbstract.testsperfile;
  private String errorTestBasename = GenInputsAbstract.error_test_basename;
  private String regressionTestBasename = GenInputsAbstract.regression_test_basename;
  private String junitPackageName = GenInputsAbstract.junit_package_name;
  private String junitBeforeEach = GenInputsAbstract.junit_before_each;
  private String junitAfterEach = GenInputsAbstract.junit_after_each;
  private String junitBeforeAll = GenInputsAbstract.junit_before_all;
  private String junitAfterAll = GenInputsAbstract.junit_after_all;
  private String junitOutputDir = GenInputsAbstract.junit_output_dir;
  private boolean dontOutputTests = GenInputsAbstract.dont_output_tests;
  private boolean junitReflectionAllowed = GenInputsAbstract.junit_reflection_allowed;
  private List<String> systemProps = new ArrayList<>();
  private String jvmMaxMemory = GenInputsAbstract.jvm_max_memory;
  private int randomseed = GenInputsAbstract.randomseed;
  private boolean deterministic = GenInputsAbstract.deterministic;
  private boolean progressDisplay = GenInputsAbstract.progressdisplay;
  private long progressIntervalMillis = GenInputsAbstract.progressintervalmillis;
  private long progressIntervalSteps = GenInputsAbstract.progressintervalsteps;
  private boolean debugChecks = GenInputsAbstract.debug_checks;
  private FileWriterWithName log = GenInputsAbstract.log;
  private FileWriterWithName selectionLog = GenInputsAbstract.selection_log;
  private FileWriterWithName operationHistoryLog = GenInputsAbstract.operation_history_log;
  private boolean printNonCompilingFile = GenInputsAbstract.print_non_compiling_file;
  private List<@ClassGetName String> visitor = new ArrayList<>();

  public GenTestsOptionsBuilder testjar(List<Path> testjars) {
    testjar.addAll(testjars);
    return this;
  }

  public GenTestsOptionsBuilder testjar(Path testjar) {
    this.testjar.add(testjar);
    return this;
  }

  public GenTestsOptionsBuilder testPackage(List<String> testPackages) {
    testPackage.addAll(testPackages);
    return this;
  }

  public GenTestsOptionsBuilder testPackage(String testPackage) {
    this.testPackage.add(testPackage);
    return this;
  }

  public GenTestsOptionsBuilder classlist(Path classlist) {
    this.classlist = classlist;
    return this;
  }

  public GenTestsOptionsBuilder testclass(List<@BinaryName String> testclasses) {
    testclass.addAll(testclasses);
    return this;
  }

  public GenTestsOptionsBuilder testclass(@BinaryName String testclass) {
    this.testclass.add(testclass);
    return this;
  }

  public GenTestsOptionsBuilder methodlist(Path methodlist) {
    this.methodlist = methodlist;
    return this;
  }

  public GenTestsOptionsBuilder omitClasses(List<Pattern> omitClasses) {
    this.omitClasses.addAll(omitClasses);
    return this;
  }

  public GenTestsOptionsBuilder omitClasses(Pattern omitClassesPattern) {
    omitClasses.add(omitClassesPattern);
    return this;
  }

  public GenTestsOptionsBuilder omitClassesFile(List<Path> omitClassesFiles) {
    omitClassesFile.addAll(omitClassesFiles);
    return this;
  }

  public GenTestsOptionsBuilder omitClassesFIle(Path omitClassesFile){
    this.omitClassesFile.add(omitClassesFile);
    return this;
  }

  public GenTestsOptionsBuilder omitMethods(List<Pattern> omitMethodsPatterns) {
    omitMethods.addAll(omitMethodsPatterns);
    return this;
  }

  public GenTestsOptionsBuilder omitMethods(Pattern omitMethodsPattern) {
    omitMethods.add(omitMethodsPattern);
    return this;
  }

  public GenTestsOptionsBuilder omitMethodsFile(List<Path> omitMethodsFiles) {
    omitMethodsFile.addAll(omitMethodsFiles);
    return this;
  }

  public GenTestsOptionsBuilder omitMethodsFile(Path omitMethodsFile) {
    this.omitMethodsFile.add(omitMethodsFile);
    return this;
  }

  public GenTestsOptionsBuilder omitField(List<String> omitFields) {
    omitField.addAll(omitFields);
    return this;
  }

  public GenTestsOptionsBuilder omitField(String omitField) {
    this.omitField.add(omitField);
    return this;
  }

  public GenTestsOptionsBuilder omitFieldFile(Path omitFieldFile) {
    this.omitFieldFile = omitFieldFile;
    return this;
  }

  public GenTestsOptionsBuilder onlyTestPublicMembers(boolean onlyTestPublicMembers) {
    this.onlyTestPublicMembers = onlyTestPublicMembers;
    return this;
  }

  public GenTestsOptionsBuilder silentlyIgnoreBadClassNames(boolean silentlyIgnoreBadClassNames) {
    this.silentlyIgnoreBadClassNames = silentlyIgnoreBadClassNames;
    return this;
  }

  public GenTestsOptionsBuilder flakyTestBehavior(GenInputsAbstract.FlakyTestAction flakyTestBehavior) {
    this.flakyTestBehavior = flakyTestBehavior;
    return this;
  }

  public GenTestsOptionsBuilder nondeterministicMethodsToOutput(int nondeterministicMethodsToOutput) {
    this.nondeterministicMethodsToOutput = nondeterministicMethodsToOutput;
    return this;
  }

  public GenTestsOptionsBuilder noErrorRevealingTests(boolean noErrorRevealingTests) {
    this.noErrorRevealingTests = noErrorRevealingTests;
    return this;
  }

  public GenTestsOptionsBuilder noRegressionTests(boolean noRegressionTests) {
    this.noRegressionTests = noRegressionTests;
    return this;
  }

  public GenTestsOptionsBuilder noRegressionAssertions(boolean noRegressionAssertions) {
    this.noRegressionAssertions = noRegressionAssertions;
    return this;
  }

  public GenTestsOptionsBuilder checkCompilable(boolean checkCompilable) {
    this.checkCompilable = checkCompilable;
    return this;
  }

  public GenTestsOptionsBuilder requireClassnameInTest(Pattern requireClassnameInTest) {
    this.requireClassnameInTest = requireClassnameInTest;
    return this;
  }

  public GenTestsOptionsBuilder requireCoveredClasses(Path requireCoveredClasses) {
    this.requireCoveredClasses = requireCoveredClasses;
    return this;
  }

  public GenTestsOptionsBuilder minimizeErrorTest(boolean minimizeErrorTest) {
    this.minimizeErrorTest = minimizeErrorTest;
    return this;
  }

  public GenTestsOptionsBuilder checkedException(GenInputsAbstract.BehaviorType checkedException) {
    this.checkedException = checkedException;
    return this;
  }

  public GenTestsOptionsBuilder uncheckedException(GenInputsAbstract.BehaviorType uncheckedException) {
    this.uncheckedException = uncheckedException;
    return this;
  }

  public GenTestsOptionsBuilder cmException(GenInputsAbstract.BehaviorType cmException) {
    this.cmException = cmException;
    return this;
  }

  public GenTestsOptionsBuilder ncdfException(GenInputsAbstract.BehaviorType ncdfException) {
    this.ncdfException = ncdfException;
    return this;
  }

  public GenTestsOptionsBuilder npeOnNullInput(GenInputsAbstract.BehaviorType npeOnNullInput) {
    this.npeOnNullInput = npeOnNullInput;
    return this;
  }

  public GenTestsOptionsBuilder npeOnNonNullInput(GenInputsAbstract.BehaviorType npeOnNonNullInput) {
    this.npeOnNonNullInput = npeOnNonNullInput;
    return this;
  }

  public GenTestsOptionsBuilder oomException(GenInputsAbstract.BehaviorType oomException) {
    this.oomException = oomException;
    return this;
  }

  public GenTestsOptionsBuilder sofException(GenInputsAbstract.BehaviorType sofException) {
    this.sofException = sofException;
    return this;
  }

  public GenTestsOptionsBuilder specifications(List<Path> specifications) {
    this.specifications.addAll(specifications);
    return this;
  }

  public GenTestsOptionsBuilder specifications(Path specification) {
    specifications.add(specification);
    return this;
  }

  public GenTestsOptionsBuilder useJdkSpecifications(boolean useJdkSpecifications) {
    this.useJdkSpecifications = useJdkSpecifications;
    return this;
  }

  public GenTestsOptionsBuilder ignoreConditionCompilationError(boolean ignoreConditionCompilationError) {
    this.ignoreConditionCompilationError = ignoreConditionCompilationError;
    return this;
  }

  public GenTestsOptionsBuilder ignoreConditionException(boolean ignoreConditionException) {
    this.ignoreConditionException = ignoreConditionException;
    return this;
  }

  public GenTestsOptionsBuilder sideEffectFreeMethods(Path sideEffectFreeMethods) {
    this.sideEffectFreeMethods = sideEffectFreeMethods;
    return this;
  }

  public GenTestsOptionsBuilder timeLimit(int timeLimit) {
    this.timeLimit = timeLimit;
    return this;
  }

  public GenTestsOptionsBuilder attemptedLimit(int attemptedLimit) {
    this.attemptedLimit = attemptedLimit;
    return this;
  }

  public GenTestsOptionsBuilder generatedLimit(int generatedLimit) {
    this.generatedLimit = generatedLimit;
    return this;
  }

  public GenTestsOptionsBuilder outputLimit(int outputLimit) {
    this.outputLimit = outputLimit;
    return this;
  }

  public GenTestsOptionsBuilder maxsize(int maxsize) {
    this.maxsize = maxsize;
    return this;
  }

  public GenTestsOptionsBuilder stopOnErrorTest(boolean stopOnErrorTest) {
    this.stopOnErrorTest = stopOnErrorTest;
    return this;
  }

  public GenTestsOptionsBuilder nullRatio(double nullRatio) {
    this.nullRatio = nullRatio;
    return this;
  }

  public GenTestsOptionsBuilder forbidNull(boolean forbidNull) {
    this.forbidNull = forbidNull;
    return this;
  }

  public GenTestsOptionsBuilder literalsFile(List<String> literalsFiles) {
    literalsFile.addAll(literalsFiles);
    return this;
  }

  public GenTestsOptionsBuilder literalsFile(String literalsFile) {
    this.literalsFile.add(literalsFile);
    return this;
  }

  public GenTestsOptionsBuilder literalsLevel(GenInputsAbstract.ClassLiteralsMode literalsLevel) {
    this.literalsLevel = literalsLevel;
    return this;
  }

  public GenTestsOptionsBuilder methodSelection(GenInputsAbstract.MethodSelectionMode methodSelection) {
    this.methodSelection = methodSelection;
    return this;
  }

  public GenTestsOptionsBuilder stringMaxlen(int stringMaxlen) {
    this.stringMaxlen = stringMaxlen;
    return this;
  }

  public GenTestsOptionsBuilder aliasRatio(double aliasRatio) {
    this.aliasRatio = aliasRatio;
    return this;
  }

  public GenTestsOptionsBuilder inputSelection(GenInputsAbstract.InputSelectionMode inputSelection) {
    this.inputSelection = inputSelection;
    return this;
  }

  public GenTestsOptionsBuilder clear(int clear) {
    this.clear = clear;
    return this;
  }

  public GenTestsOptionsBuilder clearMemory(long clearMemory) {
    this.clearMemory = clearMemory;
    return this;
  }

  public GenTestsOptionsBuilder testsPerFile(int testsPerFile) {
    this.testsPerFile = testsPerFile;
    return this;
  }

  public GenTestsOptionsBuilder errorTestBasename(String errorTestBasename) {
    this.errorTestBasename = errorTestBasename;
    return this;
  }

  public GenTestsOptionsBuilder regressionTestBasename(String regressionTestBasename) {
    this.regressionTestBasename = regressionTestBasename;
    return this;
  }

  public GenTestsOptionsBuilder junitPackageName(String junitPackageName) {
    this.junitPackageName = junitPackageName;
    return this;
  }

  public GenTestsOptionsBuilder junitBeforeEach(String junitBeforeEach) {
    this.junitBeforeEach = junitBeforeEach;
    return this;
  }

  public GenTestsOptionsBuilder junitAfterEach(String junitAfterEach) {
    this.junitAfterEach = junitAfterEach;
    return this;
  }

  public GenTestsOptionsBuilder junitBeforeAll(String junitBeforeAll) {
    this.junitBeforeAll = junitBeforeAll;
    return this;
  }

  public GenTestsOptionsBuilder junitAfterAll(String junitAfterAll) {
    this.junitAfterAll = junitAfterAll;
    return this;
  }

  public GenTestsOptionsBuilder junitOutputDir(String junitOutputDir) {
    this.junitOutputDir = junitOutputDir;
    return this;
  }

  public GenTestsOptionsBuilder dontOutputTests(boolean dontOutputTests) {
    this.dontOutputTests = dontOutputTests;
    return this;
  }

  public GenTestsOptionsBuilder junitReflectionAllowed(boolean junitReflectionAllowed) {
    this.junitReflectionAllowed = junitReflectionAllowed;
    return this;
  }

  public GenTestsOptionsBuilder systemProps(List<String> systemProps) {
    this.systemProps.addAll(systemProps);
    return this;
  }

  public GenTestsOptionsBuilder systemProps(String systemProp) {
    systemProps.add(systemProp);
    return this;
  }

  public GenTestsOptionsBuilder jvmMaxMemory(String jvmMaxMemory) {
    this.jvmMaxMemory = jvmMaxMemory;
    return this;
  }

  public GenTestsOptionsBuilder randomseed(int randomseed) {
    this.randomseed = randomseed;
    return this;
  }

  public GenTestsOptionsBuilder deterministic(boolean deterministic) {
    this.deterministic = deterministic;
    return this;
  }

  public GenTestsOptionsBuilder progressDisplay(boolean progressDisplay) {
    this.progressDisplay = progressDisplay;
    return this;
  }

  public GenTestsOptionsBuilder progressIntervalMillis(long progressIntervalMillis) {
    this.progressIntervalMillis = progressIntervalMillis;
    return this;
  }

  public GenTestsOptionsBuilder progressIntervalSteps(long progressIntervalSteps) {
    this.progressIntervalSteps = progressIntervalSteps;
    return this;
  }

  public GenTestsOptionsBuilder debugChecks(boolean debugChecks) {
    this.debugChecks = debugChecks;
    return this;
  }

  public GenTestsOptionsBuilder log(FileWriterWithName log) {
    this.log = log;
    return this;
  }

  public GenTestsOptionsBuilder selectionLog(FileWriterWithName selectionLog) {
    this.selectionLog = selectionLog;
    return this;
  }

  public GenTestsOptionsBuilder operationHistoryLog(FileWriterWithName operationHistoryLog) {
    this.operationHistoryLog = operationHistoryLog;
    return this;
  }

  public GenTestsOptionsBuilder printNonCompilingFile(boolean printNonCompilingFile) {
    this.printNonCompilingFile = printNonCompilingFile;
    return this;
  }

  public GenTestsOptionsBuilder visitor(List<@ClassGetName String> visitors) {
    visitor.addAll(visitors);
    return this;
  }

  public GenTestsOptionsBuilder visitor(@ClassGetName String visitor) {
    this.visitor.add(visitor);
    return this;
  }

  public GenTestsOptions build() {
    return new GenTestsOptions(
        testjar,
        testPackage,
        classlist,
        testclass,
        methodlist,
        omitClasses,
        omitClassesFile,
        omitMethods,
        omitMethodsFile,
        omitField,
        omitFieldFile,
        onlyTestPublicMembers,
        silentlyIgnoreBadClassNames,
        flakyTestBehavior,
        nondeterministicMethodsToOutput,
        noErrorRevealingTests,
        noRegressionTests,
        noRegressionAssertions,
        checkCompilable,
        requireClassnameInTest,
        requireCoveredClasses,
        minimizeErrorTest,
        checkedException,
        uncheckedException,
        cmException,
        ncdfException,
        npeOnNullInput,
        npeOnNonNullInput,
        oomException,
        sofException,
        specifications,
        useJdkSpecifications,
        ignoreConditionCompilationError,
        ignoreConditionException,
        sideEffectFreeMethods,
        timeLimit,
        attemptedLimit,
        generatedLimit,
        outputLimit,
        maxsize,
        stopOnErrorTest,
        nullRatio,
        forbidNull,
        literalsFile,
        literalsLevel,
        methodSelection,
        stringMaxlen,
        aliasRatio,
        inputSelection,
        clear,
        clearMemory,
        testsPerFile,
        errorTestBasename,
        regressionTestBasename,
        junitPackageName,
        junitBeforeEach,
        junitAfterEach,
        junitBeforeAll,
        junitAfterAll,
        junitOutputDir,
        dontOutputTests,
        junitReflectionAllowed,
        systemProps,
        jvmMaxMemory,
        randomseed,
        deterministic,
        progressDisplay,
        progressIntervalMillis,
        progressIntervalSteps,
        debugChecks,
        log,
        selectionLog,
        operationHistoryLog,
        printNonCompilingFile,
        visitor);
  }
}
