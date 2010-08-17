package cov;

/**
 * This class contains strings used during source instrumentation. The
 * strings represent the names of coverage-realted fields inserted in
 * instrumented classes.
 *
 * The cov package implements a basic branch coverage instrumenter
 * that we use for the branch-directed test generation research.
 *
 * This tool is prototype-quality, not for production use. In
 * particular, it is missing a number of features including tracking
 * coverage for switch statements, and lack of support for
 * generics.
 */
public class Constants {

  public static String isInstrumentedField = "simplecov_instrumented";
  public static String MethodIdAnnotation = "SimpleCovMethodId";
  public static String trueBranches = "trueBranches";
  public static String falseBranches = "falseBranches";
  public static String branchLines = "branchLines";
  public static String methodIdToBranches = "methodIdToBranches";
  public static String sourceFileNameField = "sourceFileName";
  public static String methodLineSpansField = "methodLineSpans";

}
