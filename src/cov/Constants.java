package cov;

/**
 * This class contains strings used during source instrumentation. The strings
 * represent the names of coverage-related fields inserted in instrumented
 * classes.
 *
 * The cov package implements a basic branch coverage instrumenter that we use
 * for the branch-directed test generation research.
 *
 * This tool is prototype-quality, not for production use. In particular, it is
 * missing a number of features including tracking coverage for switch
 * statements, and lack of support for generics.
 * 
 * These strings are also used to control whether Randoop manipulates fields
 * with these names in {@link randoop.GenTests}.
 */
public class Constants {

  public static final String IS_INSTRUMENTED_FIELD = "simplecov_instrumented";
  public static final String METHOD_ID_ANNOTATION = "SimpleCovMethodId";
  public static final String TRUE_BRANCHES = "trueBranches";
  public static final String FALSE_BRANCHES = "falseBranches";
  public static final String BRANCHLINES = "branchLines";
  public static final String METHOD_ID_TO_BRANCHES = "methodIdToBranches";
  public static final String SOURCE_FILE_NAME = "sourceFileName";
  public static final String METHOD_LINE_SPANS_FIELD = "methodLineSpans";

}
