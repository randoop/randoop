package randoop.reflection;

import randoop.TestValue;

/**
 * Created by bjkeller on 3/24/16.
 */
public class ConcreteClass {

  public String thePublicField;

  @TestValue
  public static String thePublicStaticField = "thevalue";

  private int thePrivateField;

  private final int thePrivateFinalField;

  public ConcreteClass(String thePublicField, int thePrivateField, int thePrivateFinalField) {
    this.thePublicField = thePublicField;
    this.thePrivateField = thePrivateField;
    this.thePrivateFinalField = thePrivateFinalField;
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof ConcreteClass)) {
      return false;
    }
    return true;
  }

  public int getThePrivateField() { return thePrivateField; }

  public <T> void setThePublicField(T value) {
    thePublicField = value.toString();
  }

  public void setThePrivateField(int i) { thePrivateField = i; }
}
