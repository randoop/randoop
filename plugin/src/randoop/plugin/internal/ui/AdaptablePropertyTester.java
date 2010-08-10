package randoop.plugin.internal.ui;

import org.eclipse.core.expressions.PropertyTester;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class AdaptablePropertyTester extends PropertyTester {
  private static final String PROPERTY_IS_TESTABLE = "isTestable"; //$NON-NLS-1$

  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    if (!(receiver instanceof IAdaptable)) {
      throw new IllegalArgumentException(
          "Element must be of type 'IAdaptable', is " + receiver == null ? "null" : receiver.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    if (PROPERTY_IS_TESTABLE.equals(property)) {
      if (!(receiver instanceof IJavaElement)) {
        return false;
      }
      IJavaElement element = (IJavaElement) receiver;
      
      try {
        return isTestable(element);
      } catch (JavaModelException e) {
        return false;
      }
    }

    throw new IllegalArgumentException("Unknown test property '" + property + "'"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public static boolean isTestable(IJavaElement element) throws JavaModelException {
    switch (element.getElementType()) {
    case IJavaElement.PACKAGE_FRAGMENT_ROOT:
      IPackageFragmentRoot pfr = (IPackageFragmentRoot) element;
      for (IJavaElement e : pfr.getChildren()) {
        if (isTestable(e)) {
          return true;
        }
      }
      return false;
    case IJavaElement.PACKAGE_FRAGMENT:
      IPackageFragment pf = (IPackageFragment) element;
      for (IJavaElement e : pf.getChildren()) {
        if (isTestable(e)) {
          return true;
        }
      }
      return false;
    case IJavaElement.COMPILATION_UNIT:
      ICompilationUnit cu = (ICompilationUnit) element;
      for (IType t : cu.getTypes()) {
        if (isTestable(t)) {
          return true;
        }
      }
      return false;
    case IJavaElement.CLASS_FILE:
      IClassFile cf = (IClassFile) element;
      return isTestable(cf.getType());
    case IJavaElement.METHOD:
      IMethod m = (IMethod) element;
      int flags = m.getFlags();
      if (Flags.isPublic(flags) && !Flags.isSynthetic(flags) && !Flags.isBridge(flags)) {
        return isTestable(m.getDeclaringType());
      }
      return false;
    case IJavaElement.TYPE:
      IType t = (IType) element;
      flags = t.getFlags();
      return !Flags.isAbstract(flags) && !Flags.isInterface(flags);
    default:
      return false;
    }
  }

}
