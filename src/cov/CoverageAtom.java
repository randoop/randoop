package cov;

/**
 * A CoverageAtom repreasents a basic unit of coverage, for example,
 * the true branch of an if-statement, or a specific case in a switch
 * statement.
 *
 * The cov package implements a basic branch coverage instrumenter
 * that we use for the branch-directed test generation research.
 *
 * This tool is prototype-quality, not for production use. In
 * particular, it is missing a number of features including tracking
 * coverage for switch statements, and lack of support for
 * generics.
 */
public interface CoverageAtom {

  String getClassName();

  int getLineNumber();
}