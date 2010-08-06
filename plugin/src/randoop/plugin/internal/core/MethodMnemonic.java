package randoop.plugin.internal.core;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;

/**
 * Returns a mnemonic for this method. The mnemonic can be used to reconstruct
 * information about the <code>IMethod</code>
 */
public class MethodMnemonic {
  public final static int LENGTH = 3;

  private final IMethod fMethod;

  private final TypeMnemonic fDeclaringTypeMnemonic;
  private final String fMethodName;
  private final boolean fIsConstructor;
  private final String fMethodSignature;

  public MethodMnemonic(IMethod m) throws JavaModelException {
    Assert.isLegal(m != null);
    
    fMethod = m;

    IType t = fMethod.getDeclaringType();
    Assert.isNotNull(t);

    fDeclaringTypeMnemonic = new TypeMnemonic(t);

    fMethodName = fMethod.getElementName();
    fIsConstructor = fMethod.isConstructor();
    
    IType type = m.getDeclaringType();
    fMethodSignature = getStableSignature(m);
  }

  public static String getStableSignature(IMethod method) throws JavaModelException {
    IType type = method.getDeclaringType();
    
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
  
  public MethodMnemonic(String typeMnemonic, String methodName, boolean isConstructor, String methodSignature) {
    fDeclaringTypeMnemonic = new TypeMnemonic(typeMnemonic);
    fMethodName = methodName;
    fIsConstructor = isConstructor;
    fMethodSignature = methodSignature;

    fMethod = null;
  }
  
  public MethodMnemonic(String mnemonic) {
    this(mnemonic, null);
  }
  
  /**
   * 
   * @param mnemonic
   * @throws CoreException
   * @throws IllegalArgumentException
   *           if the mnemonic is incorrectly formatted
   */
  public MethodMnemonic(String mnemonic, IWorkspaceRoot root) {
    String[] s = mnemonic.split(IConstants.MNEMONIC_DELIMITER);
    Assert.isLegal(s.length == TypeMnemonic.LENGTH + LENGTH);

    int typeMnemonicEnd = 0;
    for (int i=0;i<TypeMnemonic.LENGTH;i++) {
      typeMnemonicEnd = mnemonic.indexOf(IConstants.MNEMONIC_DELIMITER, typeMnemonicEnd + 1);
    }
    TypeMnemonic typeMnemonic = new TypeMnemonic(mnemonic.substring(0, typeMnemonicEnd));
    fMethodName = s[TypeMnemonic.LENGTH];
    fIsConstructor = Boolean.parseBoolean(s[TypeMnemonic.LENGTH + 1]);
    fMethodSignature = s[TypeMnemonic.LENGTH + 2];

    fDeclaringTypeMnemonic = new TypeMnemonic(typeMnemonic.toString(), root);
    fMethod = findMethod(fDeclaringTypeMnemonic.getType(), fMethodName, fIsConstructor, fMethodSignature);
  }

  private static IMethod findMethod(IType type, String name, boolean isConstructor, String signature) {
    if (type != null && type.exists()) {
      try {
        for (IMethod m : type.getMethods()) {
          if (m.isConstructor() == isConstructor) {
            if (m.getElementName().equals(name)) {
              if (getStableSignature(m).equals(signature)) {
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
    return fMethod != null;
  }

  @Override
  public String toString() {
    StringBuilder mnemonic = new StringBuilder();
    
    mnemonic.append(fDeclaringTypeMnemonic.toString());
    mnemonic.append(IConstants.MNEMONIC_DELIMITER);
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
