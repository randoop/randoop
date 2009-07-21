package cov;

/**
 * The basic unit of coverage, for example, the true branch of
 * an if-statement, or a specific case in a switch statement.
 */
public interface CoverageAtom {

  String getClassName();

  int getLineNumber();
}