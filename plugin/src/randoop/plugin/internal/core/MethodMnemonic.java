package randoop.plugin.internal.core;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;

/**
 * Returns a mnemonic for this method. The mnemonic can be used to reconstruct
 * information about the <code>IMethod</code>
 */
public class MethodMnemonic {
  private final static int LENGTH = 3;

  private final IMethod fMethod;

  private final String fMethodName;
  private final boolean fIsConstructor;
  private final String fMethodSignature;

  public MethodMnemonic(IMethod m) throws JavaModelException {
    Assert.isLegal(m != null, "Method cannot be null"); //$NON-NLS-1$
    
    fMethod = m;

    fMethodName = fMethod.getElementName();
    fIsConstructor = fMethod.isConstructor();
    fMethodSignature = getStableSignature(m);
  }

  public static String getStableSignature(IMethod method) throws JavaModelException {
    StringBuilder methodSignature = new StringBuilder();
    methodSignature.append('(');
    String[] parameters = method.getParameterTypes();
    for (String parameter : parameters) {
      String sig = RandoopCoreUtil.getFullyQualifiedUnresolvedSignature(method, parameter);
      methodSignature.append(sig);
    }
    methodSignature.append(')');
    String sig = RandoopCoreUtil.getFullyQualifiedUnresolvedSignature(method, method.getReturnType());
    methodSignature.append(sig);

    return methodSignature.toString();
  }
  
  public MethodMnemonic(String methodName, boolean isConstructor, String methodSignature) {
    fMethodName = methodName;
    fIsConstructor = isConstructor;
    fMethodSignature = methodSignature;

    fMethod = null;
  }
  
  public MethodMnemonic(String mnemonic) {
    String[] s = mnemonic.split(IConstants.MNEMONIC_DELIMITER);
    Assert.isLegal(s.length == LENGTH, "MethodMnemonics must have 3 parts methodName|isConstructor|signature"); //$NON-NLS-1$

    fMethodName = s[0];
    fIsConstructor = Boolean.parseBoolean(s[1]);
    fMethodSignature = s[2];
    
    fMethod = null;
  }

  /**
   * 
   * @param mnemonic
   * @throws CoreException
   * @throws IllegalArgumentException
   *           if the mnemonic is incorrectly formatted
   */
//  public MethodMnemonic(String mnemonic, IWorkspaceRoot root) {
//    String[] s = mnemonic.split(IConstants.MNEMONIC_DELIMITER);
//    Assert.isLegal(s.length == TypeMnemonic.LENGTH + LENGTH);
//
//    int typeMnemonicEnd = 0;
//    for (int i=0;i<TypeMnemonic.LENGTH;i++) {
//      typeMnemonicEnd = mnemonic.indexOf(IConstants.MNEMONIC_DELIMITER, typeMnemonicEnd + 1);
//    }
//    TypeMnemonic typeMnemonic = new TypeMnemonic(mnemonic.substring(0, typeMnemonicEnd));
//    fMethodName = s[TypeMnemonic.LENGTH];
//    fIsConstructor = Boolean.parseBoolean(s[TypeMnemonic.LENGTH + 1]);
//    fMethodSignature = s[TypeMnemonic.LENGTH + 2];
//
//    fDeclaringTypeMnemonic = new TypeMnemonic(typeMnemonic.toString(), root);
//    fMethod = findMethod(fDeclaringTypeMnemonic.getType(), fMethodName, fIsConstructor, fMethodSignature);
//  }

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
        RandoopPlugin.log(e);
      }
    }
    return null;
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
    return fMethod != null;
  }

  @Override
  public String toString() {
    StringBuilder mnemonic = new StringBuilder();
    
    mnemonic.append(getMethodName());
    mnemonic.append(IConstants.MNEMONIC_DELIMITER);
    mnemonic.append(isConstructor());
    mnemonic.append(IConstants.MNEMONIC_DELIMITER);
    mnemonic.append(getMethodSignature());

    return mnemonic.toString();
  }
  
  @Override
  public int hashCode() {
    return toString().hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MethodMnemonic) {
      return toString().equals(((MethodMnemonic) obj).toString());
    }
    return false;
  }
  
}
