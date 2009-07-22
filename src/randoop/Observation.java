package randoop;


/**
 * An observation represents some observed aspect of the execution of a
 * statement sequence.
 */
public interface Observation {

  String toCodeStringPreStatement();

  String toCodeStringPostStatement();

  /** Returns the fixed value that is tested for in the observation **/
  String get_value();
}
