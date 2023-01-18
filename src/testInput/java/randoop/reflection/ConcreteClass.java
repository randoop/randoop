package randoop.reflection;

import randoop.TestValue;

/** An example class to test harvesting of types and operations. */
public class ConcreteClass {

  public String thePublicField;

  public final int thePublicFinalField;

  public int[] thePublicArray;

  @TestValue public static String thePublicStaticField = "thevalue";

  private int thePrivateField;

  private final int thePrivateFinalField;

  public ConcreteClass(
      String thePublicField,
      int thePrivateField,
      int thePrivateFinalField,
      int thePublicFinalField) {
    this.thePublicField = thePublicField;
    this.thePrivateField = thePrivateField;
    this.thePrivateFinalField = thePrivateFinalField;
    this.thePublicFinalField = thePublicFinalField;
    this.thePublicArray = new int[5];
  }

  @SuppressWarnings("EqualsHashCode")
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ConcreteClass)) {
      return false;
    }
    return true;
  }

  public int getThePrivateField() {
    return thePrivateField;
  }

  public <T> void setThePublicField(T value) {
    thePublicField = value.toString();
  }

  public void setThePrivateField(int i) {
    thePrivateField = i;
  }

  public void setTheArrayField(int[] a) {
    thePublicArray = a;
  }
}
