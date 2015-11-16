package randoop;

/**
 * AbstractOperation is an abstract implementation of the Operation interface
 * to provide default implementations of Operation predicates.
 * 
 * @author bjkeller
 *
 */
public abstract class AbstractOperation implements Operation {

  @Override
  public boolean isStatic() {
    return false;
  }

  @Override
  public boolean isMessage() {
    return false;
  }

  @Override
  public boolean isConstructorCall() {
    return false;
  }

  @Override
  public boolean isNonreceivingValue() {
    return false;
  }
  
  @Override
  public Object getValue() {
    throw new IllegalArgumentException("No value for this kind of operation.");
  }

}
