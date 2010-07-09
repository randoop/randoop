package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import randoop.plugin.RandoopPlugin;

public class MethodMnemonics {
  public static IMethod getMethod(IJavaProject javaProject, String mnemonic) {
    String[] methodInfo = getMethodInfo(mnemonic);
    IProgressMonitor pm = new NullProgressMonitor();
    
    try {
      IType type = javaProject.findType(methodInfo[0], pm);
      if (type != null && type.exists()) {
        return getMethod(type, methodInfo[1], methodInfo[2]);
      }
    } catch (JavaModelException e) {
      RandoopPlugin.log(e);
    }
    return null;
  }
  
  public static IMethod getMethod(IType type, String name, String signature) {
    try {
      for (IMethod m : type.getMethods()) {
        if (m.getElementName().equals(name)) {
          if (m.getSignature().equals(signature)) {
            return m;
          }
        }
      }
    } catch (JavaModelException e) {
      RandoopPlugin.log(e);
    }
    return null;
  }

  /**
   * Returns a mnemonic for this method. The mnemonic can be used to reconstruct
   * information about the <code>IMethod</code> using
   * <code>getMethodInfo(String mnemonic)</code>
   * 
   * @param m
   * @return
   */
  public static String getMnemonic(IMethod method) {
    Assert.isLegal(method != null);
    Assert.isLegal(method.exists());
    IType type = method.getDeclaringType();
    Assert.isNotNull(type);
    
    try {
      StringBuilder mnemonic = new StringBuilder();
      mnemonic.append(type.getFullyQualifiedName('.'));
      mnemonic.append('#');
      mnemonic.append(method.getElementName());
      mnemonic.append('%');
      mnemonic.append(method.getSignature());
      return mnemonic.toString();
    } catch (JavaModelException e) {
      return null;
    }
  }

  /**
   * Returns information about an <code>IMethod</code> given the mnemonic, as
   * returned from <code>getMnemonic(IMethod m)</code>. The returned
   * <code>String[]</code> will have three indices with information as follows.
   * <p>
   * <code>[0]</code> -
   * <code>IMethod.getDeclaringType().getFullyQualifiedName()</code> - the fully
   * qualified name of this method's declaring type <code>[1]</code> - the name
   * of this method <code>IMethod.getElementName()</code> <br>
   * <br>
   * <code>[2]</code> - <code>IMethod.getSignature()</code> - the signature of
   * this method <br>
   * 
   * @param mnemonic
   * @throws IllegalArgumentException
   *           if the mnemonic is incorrectly formatted
   * @return
   */
  public static String[] getMethodInfo(String mnemonic) {
    int typenameEnd = mnemonic.indexOf('#');
    int methodNameEnd = mnemonic.indexOf('%');
    
    // Check that there is one, and only one, occurrence of '#' and '%'
    Assert.isLegal(typenameEnd != -1);
    Assert.isLegal(typenameEnd == mnemonic.lastIndexOf('#'));
    Assert.isLegal(methodNameEnd != -1);
    Assert.isLegal(methodNameEnd == mnemonic.lastIndexOf('%'));
    
    String[] methodInfo = new String[3];
    methodInfo[0] = mnemonic.substring(0, typenameEnd);
    methodInfo[1] = mnemonic.substring(typenameEnd + 1, methodNameEnd);
    methodInfo[2] = mnemonic.substring(methodNameEnd + 1);

    return methodInfo;
  }
}
