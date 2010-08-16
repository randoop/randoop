package randoop.plugin.internal.core;

public enum TestKinds {
  all("all", "Pass or Fail"), //$NON-NLS-1$
  pass("pass", "Pass"), //$NON-NLS-1$
  fail("fail", "Fail"); //$NON-NLS-1$

  private String fArgumentName;
  private String fTranslatableName;

  TestKinds(String argumentName, String translatableName) {
    fArgumentName = argumentName;
    fTranslatableName = translatableName;
  }

  /**
   * Returns a unique identifier for this <code>TestKinds</code> that may be
   * used to retrieve and store it in other environments. Command identifiers
   * correspond to a <code>TestKinds</code> ordinal. It is recommended that
   * <code>TestKinds</code> are arranged by their <code>commandId</code> if they
   * are displayed in a list.
   * 
   * @return
   */
  public int getCommandId() {
    return ordinal();
  }

  /**
   * Returns the argument that can be interpreted by Randoop. In other words,
   * the argument used for the option --output-tests when calling Randoop. This
   * is a unique identifier that may be used to retrieve and store it in other
   * environments.
   * 
   * @return the argument to pass to Randoop
   */
  public String getArgumentName() {
    return fArgumentName;
  }

  /**
   * Returns the human-readable name of this <code>TestKind</code>. This name is
   * externalized for translation purposes, and it is meant to be used in user
   * interfaces
   * 
   * @return the human-readable translation of this <code>TestKind</code>
   */
  public String getTranslatableName() {
    return fTranslatableName;
  }

  /**
   * Returns the <code>TestKind</code> associated with this
   * <code>commandId</code> or <code>null</code> if none exists.
   * 
   * @param commandId
   *          the <code>TestKind</code> associated with this command identifier
   * @return the TestKind corresponding to the <code>commandId</code>, or
   *         <code>null</code>
   */
  public static TestKinds getTestKind(int commandId) {
    try {
      return values()[commandId];
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  /**
   * Returns an ordered list of translatable names for each
   * <code>TestKinds</code>
   */
  public static String[] getTranslatableNames() {
    String[] texts = new String[TestKinds.values().length];

    for (int i = -0; i < texts.length; i++) {
      texts[i] = TestKinds.values()[i].getTranslatableName();
    }

    return texts;
  }
  
}
