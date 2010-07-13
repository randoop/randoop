package randoop.plugin.internal.core;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import randoop.plugin.RandoopPlugin;

/**
 * Returns a mnemonic for this method. The mnemonic can be used to reconstruct
 * information about the <code>IMethod</code>
 */
public class MethodMnemonic {
  private final static String DELIMITER = "%"; //$NON-NLS-1$

  private final TypeMnemonic fDeclaringTypeMnemonic;
  private final IMethod fMethod;

  private final String fMethodName;
  private final boolean fIsConstructor;
  private final String fMethodSignature;
  private final boolean fExists;

  public MethodMnemonic(IMethod m) throws JavaModelException {
    Assert.isLegal(m != null);
    
    fMethod = m;

    IType t = fMethod.getDeclaringType();
    Assert.isNotNull(t);

    fDeclaringTypeMnemonic = new TypeMnemonic(t);

    fMethodName = fMethod.getElementName();
    fIsConstructor = fMethod.isConstructor();
    fMethodSignature = fMethod.getSignature();

    fExists = fMethod != null;
  }

  /**
   * 
   * @param mnemonic
   * @throws CoreException
   * @throws IllegalArgumentException
   *           if the mnemonic is incorrectly formatted
   */
  public MethodMnemonic(IWorkspaceRoot root, String mnemonic) {
    String[] s = mnemonic.split(DELIMITER);
    Assert.isLegal(s.length == 4);

    String declaringTypeMnemonicString = s[0];
    fMethodName = s[1];
    fIsConstructor = Boolean.parseBoolean(s[2]);
    fMethodSignature = s[3];

    TypeMnemonic declaringTypeMnemonic = null;
    IMethod method = null;
    declaringTypeMnemonic = new TypeMnemonic(root, declaringTypeMnemonicString);
    
    method = findMethod(declaringTypeMnemonic.getType(), fMethodName, fIsConstructor, fMethodSignature);
    fExists = method != null;

    fDeclaringTypeMnemonic = declaringTypeMnemonic;
    fMethod = method;
  }

  private static IMethod findMethod(IType type, String name, boolean isConstructor, String signature) {
    if (type != null && type.exists()) {
      try {
        for (IMethod m : type.getMethods()) {
          if (m.isConstructor() == isConstructor) {
            if (m.getElementName().equals(name)) {
              if (m.getSignature().equals(signature)) {
                return m;
              }
            }
          }
        }
      } catch (JavaModelException e) {
        RandoopPlugin.log(e);
      }
    }
    return null;
  }

  public TypeMnemonic getDeclaringTypeMnemonic() {
    return fDeclaringTypeMnemonic;
  }

  public IMethod getMethod() {
    return fMethod;
  }

  public String getMethodName() {
    return fMethodName;
  }

  public boolean isConstructor() {
    return fIsConstructor;
  }

  public String getMethodSignature() {
    return fMethodSignature;
  }

  public boolean exists() {
    return fExists;
  }

  @Override
  public String toString() {
    StringBuilder mnemonic = new StringBuilder();
    
    mnemonic.append(fDeclaringTypeMnemonic.toString());
    mnemonic.append(DELIMITER);
    mnemonic.append(getMethodName());
    mnemonic.append(DELIMITER);
    mnemonic.append(isConstructor());
    mnemonic.append(DELIMITER);
    mnemonic.append(getMethodSignature());

    return mnemonic.toString();
  }

}
