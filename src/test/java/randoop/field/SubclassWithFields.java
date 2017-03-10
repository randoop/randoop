package randoop.field;

/**
 * SubclassWithFields is used for testing reflection over inherited fields in collecting Operations
 * by Reflection.getStatements.
 */
public class SubclassWithFields extends ClassWithFields {
  public int threeField = 33; //should hide superclass field
  public int tenField = 10;
}
