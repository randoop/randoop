package randoop.plugin.internal.ui;

import org.eclipse.core.expressions.PropertyTester;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.jdt.core.IJavaElement;

public class AdaptablePropertyTester extends PropertyTester {
  private static final String PROPERTY_IS_TESTABLE = "isTestable"; //$NON-NLS-1$

  /*
   * (non-Javadoc)
   * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
   */
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
      
      return isTestable(element);
    }

    throw new IllegalArgumentException("Unknown test property '" + property + "'"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private boolean isTestable(IJavaElement element) {
    switch (element.getElementType()) {
    case IJavaElement.PACKAGE_FRAGMENT_ROOT:
    case IJavaElement.PACKAGE_FRAGMENT:
    case IJavaElement.COMPILATION_UNIT:
    case IJavaElement.CLASS_FILE:
    case IJavaElement.TYPE:
    case IJavaElement.METHOD:
      return true;
    default:
      return false;
    }
  }

}
