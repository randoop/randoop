package randoop.plugin.internal.ui;

import java.util.ArrayList;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

public class ArrayListPropertyTester extends PropertyTester {
  private static final String PROPERTY_IS_ALL_IN_SAME_PROJECT = "isAllInSameProject"; //$NON-NLS-1$
  
  @SuppressWarnings("unchecked")
  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    if (!(receiver instanceof ArrayList<?>)) {
      throw new IllegalArgumentException(
          "Element must be of type 'ArrayList', is " + receiver == null ? "null" : receiver.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
    }
    ArrayList<?> receiverList = (ArrayList<?>) receiver;

    if (PROPERTY_IS_ALL_IN_SAME_PROJECT.equals(property)) {
      for (Object o : receiverList) {
        if (!(o instanceof IJavaElement)) {
          return false;
        }
      }
      return isAllInSameProject((ArrayList<IJavaElement>) receiverList);
    }
    
    throw new IllegalArgumentException("Unknown test property '" + property + "'"); //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  private boolean isAllInSameProject(ArrayList<IJavaElement> elements) {
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
