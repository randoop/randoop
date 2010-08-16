package randoop.plugin.internal.ui.options;

/**
 * 
 * @author Peter Kalauskas
 */
public interface IOptionChangeEvent {

  /**
   * The value this option has just been changed to
   */
  public String getAttributeName();

  /**
   * The attribute corresponding to how this option is stored in a launch
   * configurtion
   */
  public Object getValue();

}
