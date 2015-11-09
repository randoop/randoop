package randoop;

/**
 * SubclassWithFields is used for testing reflection over
 * inherited fields in collecting StatementKinds by
 * Reflection.getStatements.
 * 
 * @author bjkeller
 *
 */
public class SubclassWithFields extends ClassWithFields {
  public int threeField = 33; //should hide superclass field
  public int tenField = 10;
}
