package randoop.api;

import org.checkerframework.checker.signature.qual.BinaryName;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.plumelib.util.FileWriterWithName;
import randoop.main.GenInputsAbstract;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GenTestsOptions {

  public static final GenTestsOptions DEFAULT = new GenTestsOptions();

  private final List<Path> testjar;
  private final List<String> testPackage;
  private final Path classlist;
  private final List<@BinaryName String> testclass;
  private final Path methodlist;
  private final List<Pattern> omitClasses;
  private final List<Path> omitClassesFile;
  private final List<Pattern> omitMethods;
  private final List<Path> omitMethodsFile;
  private final List<String> omitField;
  private final Path omitFieldFile;
  private final boolean onlyTestPublicMembers;
  private final boolean silentlyIgnoreBadClassNames;
  private final GenInputsAbstract.FlakyTestAction flakyTestBehavior;
  private final int nondeterministicMethodsToOutput;
  private final boolean noErrorRevealingTests;
  private final boolean noRegressionTests;
  private final boolean noRegressionAssertions;
  private final boolean checkCompilable;
  private final Pattern requireClassnameInTest;
  private final Path requireCoveredClasses;
  private final boolean minimizeErrorTest;
  private final GenInputsAbstract.BehaviorType checkedException;
  private final GenInputsAbstract.BehaviorType uncheckedException;
  private final GenInputsAbstract.BehaviorType cmException;
  private final GenInputsAbstract.BehaviorType ncdfException;
  private final GenInputsAbstract.BehaviorType npeOnNullInput;
  private final GenInputsAbstract.BehaviorType npeOnNonNullInput;
  private final GenInputsAbstract.BehaviorType oomException;
  private final GenInputsAbstract.BehaviorType sofException;
  private final List<Path> specifications;
  private final boolean useJdkSpecifications;
  private final boolean ignoreConditionCompilationError;
  private final boolean ignoreConditionException;
  private final Path sideEffectFreeMethods;
  private final int timeLimit;
  private final int attemptedLimit;
  private final int generatedLimit;
  private final int outputLimit;
  private final int maxsize;
  private final boolean stopOnErrorTest;
  private final double nullRatio;
  private final boolean forbidNull;
  private final List<String> literalsFile;
  private final GenInputsAbstract.ClassLiteralsMode literalsLevel;
  private final GenInputsAbstract.MethodSelectionMode methodSelection;
  private final int stringMaxlen;
  private final double aliasRatio;
  private final GenInputsAbstract.InputSelectionMode inputSelection;
  private final int clear;
  private final long clearMemory; // default: 4G
  private final int testsPerFile;
  private final String errorTestBasename;
  private final String regressionTestBasename;
  private final String junitPackageName;
  private final String junitBeforeEach;
  private final String junitAfterEach;
  private final String junitBeforeAll;
  private final String junitAfterAll;
  private final String junitOutputDir;
  private final boolean dontOutputTests;
  private final boolean junitReflectionAllowed;
  private final List<String> systemProps;
  private final String jvmMaxMemory;
  private final int randomseed;
  private final boolean deterministic;
  private final boolean progressDisplay;
  private final long progressIntervalMillis;
  private final long progressIntervalSteps;
  private final boolean debugChecks;
  private final FileWriterWithName log;
  private final FileWriterWithName selectionLog;
  private final FileWriterWithName operationHistoryLog;
  private final boolean printNonCompilingFile;
  private final List<@ClassGetName String> visitor;

  private GenTestsOptions() {
    this(new ArrayList<>(), // testjar
        new ArrayList<>(), // testPackage
        GenInputsAbstract.classlist,
        new ArrayList<>(), // testclass
        GenInputsAbstract.methodlist,
        new ArrayList<>(), // omitClasses
        new ArrayList<>(), // omitClassesFile
        new ArrayList<>(), // omitMethods
        new ArrayList<>(), // omitMethodsFile
        new ArrayList<>(), // omitField
        GenInputsAbstract.omit_field_file,
        GenInputsAbstract.only_test_public_members,
        GenInputsAbstract.silently_ignore_bad_class_names,
        GenInputsAbstract.flaky_test_behavior,
        GenInputsAbstract.nondeterministic_methods_to_output,
        GenInputsAbstract.no_error_revealing_tests,
        GenInputsAbstract.no_regression_tests,
        GenInputsAbstract.no_regression_assertions,
        GenInputsAbstract.check_compilable,
        GenInputsAbstract.require_classname_in_test,
        GenInputsAbstract.require_covered_classes,
        GenInputsAbstract.minimize_error_test,
        GenInputsAbstract.checked_exception,
        GenInputsAbstract.unchecked_exception,
        GenInputsAbstract.cm_exception,
        GenInputsAbstract.ncdf_exception,
        GenInputsAbstract.npe_on_null_input,
        GenInputsAbstract.npe_on_non_null_input,
        GenInputsAbstract.oom_exception,
        GenInputsAbstract.sof_exception,
        new ArrayList<>(), // specifications
        GenInputsAbstract.use_jdk_specifications,
        GenInputsAbstract.ignore_condition_compilation_error,
        GenInputsAbstract.ignore_condition_exception,
        GenInputsAbstract.side_effect_free_methods,
        GenInputsAbstract.time_limit,
        GenInputsAbstract.attempted_limit,
        GenInputsAbstract.generated_limit,
        GenInputsAbstract.output_limit,
        GenInputsAbstract.maxsize,
        GenInputsAbstract.stop_on_error_test,
        GenInputsAbstract.null_ratio,
        GenInputsAbstract.forbid_null,
        new ArrayList<>(), // literalsFile
        GenInputsAbstract.literals_level,
        GenInputsAbstract.method_selection,
        GenInputsAbstract.string_maxlen,
        GenInputsAbstract.alias_ratio,
        GenInputsAbstract.input_selection,
        GenInputsAbstract.clear,
        GenInputsAbstract.clear_memory,
        GenInputsAbstract.testsperfile,
        GenInputsAbstract.error_test_basename,
        GenInputsAbstract.regression_test_basename,
        GenInputsAbstract.junit_package_name,
        GenInputsAbstract.junit_before_each,
        GenInputsAbstract.junit_after_each,
        GenInputsAbstract.junit_before_all,
        GenInputsAbstract.junit_after_all,
        GenInputsAbstract.junit_output_dir,
        GenInputsAbstract.dont_output_tests,
        GenInputsAbstract.junit_reflection_allowed,
        new ArrayList<>(), // systemProps
        GenInputsAbstract.jvm_max_memory,
        GenInputsAbstract.randomseed,
        GenInputsAbstract.deterministic,
        GenInputsAbstract.progressdisplay,
        GenInputsAbstract.progressintervalmillis,
        GenInputsAbstract.progressintervalsteps,
        GenInputsAbstract.debug_checks,
        GenInputsAbstract.log,
        GenInputsAbstract.selection_log,
        GenInputsAbstract.operation_history_log,
        GenInputsAbstract.print_non_compiling_file,
        new ArrayList<>() // visitor
        );
  }

  public GenTestsOptions(List<Path> testjar,
                         List<String> testPackage,
                         Path classlist,
                         List<@BinaryName String> testclass,
                         Path methodlist,
                         List<Pattern> omitClasses,
                         List<Path> omitClassesFile,
                         List<Pattern> omitMethods,
                         List<Path> omitMethodsFile,
                         List<String> omitField,
                         Path omitFieldFile,
                         boolean onlyTestPublicMembers,
                         boolean silentlyIgnoreBadClassNames,
                         GenInputsAbstract.FlakyTestAction flakyTestBehavior,
                         int nondeterministicMethodsToOutput,
                         boolean noErrorRevealingTests,
                         boolean noRegressionTests,
                         boolean noRegressionAssertions,
                         boolean checkCompilable,
                         Pattern requireClassnameInTest,
                         Path requireCoveredClasses,
                         boolean minimizeErrorTest,
                         GenInputsAbstract.BehaviorType checkedException,
                         GenInputsAbstract.BehaviorType uncheckedException,
                         GenInputsAbstract.BehaviorType cmException,
                         GenInputsAbstract.BehaviorType ncdfException,
                         GenInputsAbstract.BehaviorType npeOnNullInput,
                         GenInputsAbstract.BehaviorType npeOnNonNullInput,
                         GenInputsAbstract.BehaviorType oomException,
                         GenInputsAbstract.BehaviorType sofException,
                         List<Path> specifications,
                         boolean useJdkSpecifications,
                         boolean ignoreConditionCompilationError,
                         boolean ignoreConditionException,
                         Path sideEffectFreeMethods,
                         int timeLimit,
                         int attemptedLimit,
                         int generatedLimit,
                         int outputLimit,
                         int maxsize,
                         boolean stopOnErrorTest,
                         double nullRatio,
                         boolean forbidNull,
                         List<String> literalsFile,
                         GenInputsAbstract.ClassLiteralsMode literalsLevel,
                         GenInputsAbstract.MethodSelectionMode methodSelection,
                         int stringMaxlen,
                         double aliasRatio,
                         GenInputsAbstract.InputSelectionMode inputSelection,
                         int clear,
                         long clearMemory,
                         int testsPerFile,
                         String errorTestBasename,
                         String regressionTestBasename,
                         String junitPackageName,
                         String junitBeforeEach,
                         String junitAfterEach,
                         String junitBeforeAll,
                         String junitAfterAll,
                         String junitOutputDir,
                         boolean dontOutputTests,
                         boolean junitReflectionAllowed,
                         List<String> systemProps,
                         String jvmMaxMemory,
                         int randomseed,
                         boolean deterministic,
                         boolean progressDisplay,
                         long progressIntervalMillis,
                         long progressIntervalSteps,
                         boolean debugChecks,
                         FileWriterWithName log,
                         FileWriterWithName selectionLog,
                         FileWriterWithName operationHistoryLog,
                         boolean printNonCompilingFile,
                         List<@ClassGetName String> visitor) {
    this.testjar = testjar;
    this.testPackage = testPackage;
    this.classlist = classlist;
    this.testclass = testclass;
    this.methodlist = methodlist;
    this.omitClasses = omitClasses;
    this.omitClassesFile = omitClassesFile;
    this.omitMethods = omitMethods;
    this.omitMethodsFile = omitMethodsFile;
    this.omitField = omitField;
    this.omitFieldFile = omitFieldFile;
    this.onlyTestPublicMembers = onlyTestPublicMembers;
    this.silentlyIgnoreBadClassNames = silentlyIgnoreBadClassNames;
    this.flakyTestBehavior = flakyTestBehavior;
    this.nondeterministicMethodsToOutput = nondeterministicMethodsToOutput;
    this.noErrorRevealingTests = noErrorRevealingTests;
    this.noRegressionTests = noRegressionTests;
    this.noRegressionAssertions = noRegressionAssertions;
    this.checkCompilable = checkCompilable;
    this.requireClassnameInTest = requireClassnameInTest;
    this.requireCoveredClasses = requireCoveredClasses;
    this.minimizeErrorTest = minimizeErrorTest;
    this.checkedException = checkedException;
    this.uncheckedException = uncheckedException;
    this.cmException = cmException;
    this.ncdfException = ncdfException;
    this.npeOnNullInput = npeOnNullInput;
    this.npeOnNonNullInput = npeOnNonNullInput;
    this.oomException = oomException;
    this.sofException = sofException;
    this.specifications = specifications;
    this.useJdkSpecifications = useJdkSpecifications;
    this.ignoreConditionCompilationError = ignoreConditionCompilationError;
    this.ignoreConditionException = ignoreConditionException;
    this.sideEffectFreeMethods = sideEffectFreeMethods;
    this.timeLimit = timeLimit;
    this.attemptedLimit = attemptedLimit;
    this.generatedLimit = generatedLimit;
    this.outputLimit = outputLimit;
    this.maxsize = maxsize;
    this.stopOnErrorTest = stopOnErrorTest;
    this.nullRatio = nullRatio;
    this.forbidNull = forbidNull;
    this.literalsFile = literalsFile;
    this.literalsLevel = literalsLevel;
    this.methodSelection = methodSelection;
    this.stringMaxlen = stringMaxlen;
    this.aliasRatio = aliasRatio;
    this.inputSelection = inputSelection;
    this.clear = clear;
    this.clearMemory = clearMemory;
    this.testsPerFile = testsPerFile;
    this.errorTestBasename = errorTestBasename;
    this.regressionTestBasename = regressionTestBasename;
    this.junitPackageName = junitPackageName;
    this.junitBeforeEach = junitBeforeEach;
    this.junitAfterEach = junitAfterEach;
    this.junitBeforeAll = junitBeforeAll;
    this.junitAfterAll = junitAfterAll;
    this.junitOutputDir = junitOutputDir;
    this.dontOutputTests = dontOutputTests;
    this.junitReflectionAllowed = junitReflectionAllowed;
    this.systemProps = systemProps;
    this.jvmMaxMemory = jvmMaxMemory;
    this.randomseed = randomseed;
    this.deterministic = deterministic;
    this.progressDisplay = progressDisplay;
    this.progressIntervalMillis = progressIntervalMillis;
    this.progressIntervalSteps = progressIntervalSteps;
    this.debugChecks = debugChecks;
    this.log = log;
    this.selectionLog = selectionLog;
    this.operationHistoryLog = operationHistoryLog;
    this.printNonCompilingFile = printNonCompilingFile;
    this.visitor = visitor;
  }

  public void configure() {
    GenInputsAbstract.testjar = testjar;
    GenInputsAbstract.test_package = testPackage;
    GenInputsAbstract.classlist = classlist;
    GenInputsAbstract.testclass = testclass;
    GenInputsAbstract.methodlist = methodlist;
    GenInputsAbstract.omit_classes = omitClasses;
    GenInputsAbstract.omit_classes_file = omitClassesFile;
    GenInputsAbstract.omit_methods = omitMethods;
    GenInputsAbstract.omit_methods_file = omitMethodsFile;
    GenInputsAbstract.omit_field = omitField;
    GenInputsAbstract.omit_field_file = omitFieldFile;
    GenInputsAbstract.only_test_public_members = onlyTestPublicMembers;
    GenInputsAbstract.silently_ignore_bad_class_names = silentlyIgnoreBadClassNames;
    GenInputsAbstract.flaky_test_behavior = flakyTestBehavior;
    GenInputsAbstract.nondeterministic_methods_to_output = nondeterministicMethodsToOutput;
    GenInputsAbstract.no_error_revealing_tests = noErrorRevealingTests;
    GenInputsAbstract.no_regression_tests = noRegressionTests;
    GenInputsAbstract.no_regression_assertions = noRegressionAssertions;
    GenInputsAbstract.check_compilable = checkCompilable;
    GenInputsAbstract.require_classname_in_test = requireClassnameInTest;
    GenInputsAbstract.require_covered_classes = requireCoveredClasses;
    GenInputsAbstract.minimize_error_test = minimizeErrorTest;
    GenInputsAbstract.checked_exception = checkedException;
    GenInputsAbstract.unchecked_exception = uncheckedException;
    GenInputsAbstract.cm_exception = cmException;
    GenInputsAbstract.ncdf_exception = ncdfException;
    GenInputsAbstract.npe_on_null_input = npeOnNullInput;
    GenInputsAbstract.npe_on_non_null_input = npeOnNonNullInput;
    GenInputsAbstract.oom_exception = oomException;
    GenInputsAbstract.sof_exception = sofException;
    GenInputsAbstract.specifications = specifications;
    GenInputsAbstract.use_jdk_specifications = useJdkSpecifications;
    GenInputsAbstract.ignore_condition_compilation_error = ignoreConditionCompilationError;
    GenInputsAbstract.ignore_condition_exception = ignoreConditionException;
    GenInputsAbstract.side_effect_free_methods = sideEffectFreeMethods;
    GenInputsAbstract.time_limit = timeLimit;
    GenInputsAbstract.attempted_limit = attemptedLimit;
    GenInputsAbstract.generated_limit = generatedLimit;
    GenInputsAbstract.output_limit = outputLimit;
    GenInputsAbstract.maxsize = maxsize;
    GenInputsAbstract.stop_on_error_test = stopOnErrorTest;
    GenInputsAbstract.null_ratio = nullRatio;
    GenInputsAbstract.forbid_null = forbidNull;
    GenInputsAbstract.literals_file = literalsFile;
    GenInputsAbstract.literals_level = literalsLevel;
    GenInputsAbstract.method_selection = methodSelection;
    GenInputsAbstract.string_maxlen = stringMaxlen;
    GenInputsAbstract.alias_ratio = aliasRatio;
    GenInputsAbstract.input_selection = inputSelection;
    GenInputsAbstract.clear = clear;
    GenInputsAbstract.clear_memory = clearMemory;
    GenInputsAbstract.testsperfile = testsPerFile;
    GenInputsAbstract.error_test_basename = errorTestBasename;
    GenInputsAbstract.regression_test_basename = regressionTestBasename;
    GenInputsAbstract.junit_package_name = junitPackageName;
    GenInputsAbstract.junit_before_each = junitBeforeEach;
    GenInputsAbstract.junit_after_each = junitAfterEach;
    GenInputsAbstract.junit_before_all = junitBeforeAll;
    GenInputsAbstract.junit_after_all = junitAfterAll;
    GenInputsAbstract.junit_output_dir = junitOutputDir;
    GenInputsAbstract.dont_output_tests = dontOutputTests;
    GenInputsAbstract.junit_reflection_allowed = junitReflectionAllowed;
    GenInputsAbstract.system_props = systemProps;
    GenInputsAbstract.jvm_max_memory = jvmMaxMemory;
    GenInputsAbstract.randomseed = randomseed;
    GenInputsAbstract.deterministic = deterministic;
    GenInputsAbstract.progressdisplay = progressDisplay;
    GenInputsAbstract.progressintervalmillis = progressIntervalMillis;
    GenInputsAbstract.progressintervalsteps = progressIntervalSteps;
    GenInputsAbstract.debug_checks = debugChecks;
    GenInputsAbstract.log = log;
    GenInputsAbstract.selection_log = selectionLog;
    GenInputsAbstract.operation_history_log = operationHistoryLog;
    GenInputsAbstract.print_non_compiling_file = printNonCompilingFile;
    GenInputsAbstract.visitor = visitor;
  }
}
