package randoop.plugin.tests.ui.options;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Test;

public class ClasspathTest {
  @Test
  public void testMethod() throws CoreException {
    HashMap<String, IMethod> methodsByMnemonics = new HashMap<String, IMethod>();

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    for (IProject project : root.getProjects()) {
      IJavaProject javaproject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
      if (javaproject == null)
        continue;

      for (IClasspathEntry classpathEntry : javaproject.getRawClasspath()) {
        System.out.println(classpathEntry.getPath());
      }
    }
    
  }
}
