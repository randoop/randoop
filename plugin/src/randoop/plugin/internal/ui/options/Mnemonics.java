package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;

public class Mnemonics {
  final static char TYPE_NAME_END = '#';
  final static char METHOD_NAME_END = '%';
  
  // expects use of $
  public static String[] splitFullyQualifiedName(String fqname) {
    int seperator = fqname.lastIndexOf('.');
    if (seperator == -1) {
      String[] packageAndName = { IConstants.EMPTY_STRING, fqname };
      return packageAndName;
    } else {
      String[] packageAndName = { fqname.substring(0, seperator),
          fqname.substring(seperator + 1) };
      return packageAndName;
    }
  }
  
  public static String getFullyQualifiedName(String[] packageAndName) {
    Assert.isLegal(packageAndName.length == 2);
    
    if (packageAndName[0].equals(IConstants.EMPTY_STRING)) {
      return packageAndName[1];
    } else {
      return packageAndName[0] + '.' + packageAndName[1];
    }
  }
  
  public static IMethod getMethod(IJavaProject javaProject, String mnemonic) {
    String[] methodInfo = splitMethodMnemonic(mnemonic);
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
  public static String getMethodMnemonic(IMethod method) {
    Assert.isLegal(method != null);
    Assert.isLegal(method.exists());
    IType type = method.getDeclaringType();
    Assert.isNotNull(type);
    
    try {
      StringBuilder mnemonic = new StringBuilder();
      mnemonic.append(getTypeMnemonic(type));
      mnemonic.append(TYPE_NAME_END);
      mnemonic.append(method.getElementName());
      mnemonic.append(METHOD_NAME_END);
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
   * <code>[0]</code> - <code>getTypeMnemonic(IMethod.getDeclaringType())</code>
   * - the fully qualified name of this method's declaring type
   * <br>
   * <code>[1]</code> - <code>IMethod.getElementName()</code> - the name of this method
   * <br>
   * <code>[2]</code> - <code>IMethod.getSignature()</code> - the signature of
   * this method <br>
   * 
   * @param mnemonic
   * @throws IllegalArgumentException
   *           if the mnemonic is incorrectly formatted
   * @return
   */
  public static String[] splitMethodMnemonic(String mnemonic) {
    int typenameEnd = mnemonic.indexOf(TYPE_NAME_END);
    int methodNameEnd = mnemonic.indexOf(METHOD_NAME_END);
    
    // Check that there is one, and only one, occurrence of TYPE_NAME_END and METHOD_NAME_END
    Assert.isLegal(typenameEnd != -1);
    Assert.isLegal(typenameEnd == mnemonic.lastIndexOf(TYPE_NAME_END));
    Assert.isLegal(methodNameEnd != -1);
    Assert.isLegal(methodNameEnd == mnemonic.lastIndexOf(METHOD_NAME_END));
    
    String[] methodInfo = new String[3];
    methodInfo[0] = mnemonic.substring(0, typenameEnd);
    methodInfo[1] = mnemonic.substring(typenameEnd + 1, methodNameEnd);
    methodInfo[2] = mnemonic.substring(methodNameEnd + 1);

    return methodInfo;
  }

  public static String getMethodMnemonic(String[] methodInfo) {
    Assert.isLegal(methodInfo.length == 3);
    StringBuilder mnemonic = new StringBuilder();
    mnemonic.append(methodInfo[0]);
    mnemonic.append(TYPE_NAME_END);
    mnemonic.append(methodInfo[1]);
    mnemonic.append(METHOD_NAME_END);
    mnemonic.append(methodInfo[2]);
    return mnemonic.toString();
  }

  public static Object getTypeMnemonic(IType type) {
    return type.getFullyQualifiedName();
  }

  public static IType getType(IJavaProject javaProject, String mnemonic) {
    IProgressMonitor pm = new NullProgressMonitor();

    Assert.isLegal(javaProject != null);
    Assert.isLegal(javaProject.exists());
    Assert.isLegal(mnemonic != null);

    try {
      return javaProject.findType(mnemonic.replace('$', '.'), pm);
    } catch (JavaModelException e) {
      RandoopPlugin.log(e);
    }
    
    return null;
  }

  public static String getFullyQualifiedName(String typeMnemonic) {
    return typeMnemonic;
  }
}
