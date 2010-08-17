package randoop.plugin.internal.core;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import randoop.plugin.RandoopPlugin;

/**
 * String mnemonic provider and parser for <code>IMethod</code>s. Method
 * mnemonics do not store their declaring type's mnemonic, and are not necessary
 * unique when compared to method mnemonics from other types. Method mnemonics
 * should be stored along with their declaring type in some fashion to support
 * recovery of the <code>IMathod</code> object.
 * <p>
 * Note that <code>MethodMnemonic</code> objects are constant; none of the
 * methods will mutate the object being operated on.
 * <p>
 * 
 * @author Peter Kalauskas
 */
public class MethodMnemonic {
  private static final char MNEMONIC_DELIMITER = '%';
  private final static int LENGTH = 3;

  private final IMethod fMethod;

  private final String fMethodName;
  private final boolean fIsConstructor;
  private final String fMethodSignature;

  /**
   * Constructs a <code>MethodMnemonic</code> from the given
   * <code>IMethod</code>. The new method mnemonic is guaranteed to exist.
   * 
   * @param m
   * @throws JavaModelException
   * @see MethodMnemonic#exists()
   */
  public MethodMnemonic(IMethod m) throws JavaModelException {
    Assert.isLegal(m != null, "Method is null"); //$NON-NLS-1$
    
    fMethod = m;

    fMethodName = fMethod.getElementName();
    fIsConstructor = fMethod.isConstructor();
    fMethodSignature = getStableSignature(m);
  }

  /**
   * 
   * @param method
   * @return
   * @throws JavaModelException
   */
  private static String getStableSignature(IMethod method) throws JavaModelException {
    StringBuilder methodSignature = new StringBuilder();
    methodSignature.append('(');
    String[] parameters = method.getParameterTypes();
    for (String parameter : parameters) {
      String sig = RandoopCoreUtil.getUnresolvedFullyQualifiedMethodSignature(method, parameter);
      methodSignature.append(sig);
    }
    methodSignature.append(')');
    String sig = RandoopCoreUtil.getUnresolvedFullyQualifiedMethodSignature(method, method.getReturnType());
    methodSignature.append(sig);

    return methodSignature.toString();
  }
  
  /**
   * Creates a new method mnemonic from the three pieces of information the
   * mnemonic string holds.
   * <p>
   * The created mnemonic will never exist according to
   * {@link MethodMnemonic#exists()}
   * <p>
   * <b>Note:</b> the arguments are not checked for validity. The given
   * signature may be incorrectly formatted
   * 
   * @throws IllegalArgumentException
   *           if the mnemonic is incorrectly formatted
   */
  public MethodMnemonic(String methodName, boolean isConstructor, String methodSignature) {
    fMethodName = methodName;
    fIsConstructor = isConstructor;
    fMethodSignature = methodSignature;

    fMethod = null;
  }

  /**
   * Creates a new method mnemonic from a mnemonic string previously returned by
   * {@link MethodMnemonic#toString()}
   * <p>
   * The created mnemonic will never exist according to
   * {@link MethodMnemonic#exists()}
   * 
   * @throws IllegalArgumentException
   *           if the mnemonic is incorrectly formatted
   */
  public MethodMnemonic(String mnemonic) {
    String[] s = mnemonic.split(new Character(MNEMONIC_DELIMITER).toString());
    Assert.isLegal(s.length == LENGTH, "MethodMnemonics must have 3 parts methodName|isConstructor|signature"); //$NON-NLS-1$

    fMethodName = s[0];
    fIsConstructor = Boolean.parseBoolean(s[1]);
    fMethodSignature = s[2];
    
    fMethod = null;
  }

  /**
   * Searches the given type for a method with the same exact mnemonic as this
   * one.
   * 
   * @param type
   * @return the method with an equivalent mnemonic, or <code>null</code> if
   *         none is found or if the type is <code>null</code> or does not exist
   */
  public IMethod findMethod(IType type) {
    if (type != null && type.exists()) {
      try {
        for (IMethod m : type.getMethods()) {
          if (m.isConstructor() == fIsConstructor) {
            if (m.getElementName().equals(fMethodName)) {
              if (getStableSignature(m).equals(fMethodSignature)) {
                return m;
              }
            }
          }
        }
      } catch (JavaModelException e) {
        IStatus s = RandoopStatus.JAVA_MODEL_EXCEPTION.getStatus(e);
        RandoopPlugin.log(s);
      }
    }
    return null;
  }

  /**
   * Returns the method this mnemonic has been created to represent.
   * <p>
   * <b>Note:</b> this will only return a non-<code>null</code> value when the
   * object is created with the {@link MethodMnemonic#MethodMnemonic(IMethod)}
   * constructor. Use {@link MethodMnemonic#findMethod(IType)} if a different
   * constructor is used.
   * 
   * @return the <code>IMethod</code> of this mnemonic, or <code>null</code>
   */
  public IMethod getMethod() {
    return fMethod;
  }

  /**
   * Returns the name of this method as given by
   * {@link IMethod#getElementName()}
   * 
   * @return the simple name of this method
   */
  public String getMethodName() {
    return fMethodName;
  }

  /**
   * Returns <code>true</code> if the method is a constructor
   * 
   * @return <code>true</code> if the method is a construct
   * @see IMethod#isConstructor()
   */
  public boolean isConstructor() {
    return fIsConstructor;
  }

  /**
   * Returns a method signature that is unqiue within the method's declaring
   * type.
   * <p>
   * <b>Note:</b> this is not equivalent to the signature returned by
   * {@link IMethod#getSignature()}
   * 
   * @return
   */
  public String getMethodSignature() {
    return fMethodSignature;
  }

  /**
   * Returns <code>true</code> the <code>IMethod</code> this mnemonic stores is
   * not-<code>null</code>.
   * 
   * @return <code>true</code> if the IMethod is not-<code>null</code>
   * @see org.eclipse.jdt.core.IJavaElement#exists()
   */
  public boolean exists() {
    return fMethod != null;
  }
  
  /**
   * Returns the string representation of this mnemonic. Information from the
   * mnemonic string may be retrieved using
   * {@link MethodMnemonic#MethodMnemonic(String)}, and subsequently the
   * <code>IMethod</code> may be retrieved with
   * {@link MethodMnemonic#findMethod(IType)}
   */
  @Override
  public String toString() {
    StringBuilder mnemonic = new StringBuilder();
    
    mnemonic.append(getMethodName());
    mnemonic.append(MNEMONIC_DELIMITER);
    mnemonic.append(isConstructor());
    mnemonic.append(MNEMONIC_DELIMITER);
    mnemonic.append(getMethodSignature());

    return mnemonic.toString();
  }
  
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * Returns true if this method mnemonic's string equals another one. Whether
   * or not the <code>IMethod</code> is present has no effect.
   * 
   * @return true if this method mnemonic equals another one
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MethodMnemonic) {
      return toString().equals(((MethodMnemonic) obj).toString());
    }
    return false;
  }
  
}
