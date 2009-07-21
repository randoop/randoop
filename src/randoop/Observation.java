package randoop;


/**
 * An observation represents some observed aspect of the execution of a
 * statement sequence.
 */
public interface Observation {

  String toCodeStringPreStatement();

  String toCodeStringPostStatement();
}
