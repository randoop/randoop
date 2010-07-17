package randoop.plugin.tests.ui.options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.junit.BeforeClass;
import org.junit.Test;

import randoop.plugin.internal.core.MethodMnemonic;
import randoop.plugin.tests.WorkspaceManager;

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
