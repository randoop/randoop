package randoop.plugin.internal.ui;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

public class ListPropertyTester extends PropertyTester {
  private static final String PROPERTY_IS_ALL_IN_SAME_PROJECT = "isAllInSameProject"; //$NON-NLS-1$
  
  @SuppressWarnings("unchecked")
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    if (!(receiver instanceof List<?>)) {
      throw new IllegalArgumentException(
          "Element must be of type 'ArrayList', is " + receiver == null ? "null" : receiver.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
    }
    List<?> receiverList = (List<?>) receiver;

    if (PROPERTY_IS_ALL_IN_SAME_PROJECT.equals(property)) {
      for (Object o : receiverList) {
        if (!(o instanceof IJavaElement)) {
          return false;
        }
      }
      return isAllInSameProject((List<IJavaElement>) receiverList);
    }
    
    throw new IllegalArgumentException("Unknown test property '" + property + "'"); //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  private static boolean isAllInSameProject(List<IJavaElement> elements) {
    if(elements.isEmpty()) {
      return false;
    }
    
    IJavaProject javaProject = elements.get(0).getJavaProject();
    
    for(IJavaElement element : elements) {
      if(element instanceof IJavaProject)
        return false;
      
      IJavaProject containingProject = element.getJavaProject();
      if(containingProject == null || !javaProject.equals(containingProject)) {
        return false;
      }
    }
    
    return true;
  }
  
}
