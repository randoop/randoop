package randoop.plugin.tests.ui.options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.BeforeClass;
import org.junit.Test;

import randoop.plugin.internal.ui.options.MethodMnemonics;
import randoop.plugin.tests.ui.launching.ProjectCreator;

@SuppressWarnings("nls")
public class MethodMnemonicsTest {
  private static IJavaProject fJavaProject;

  @BeforeClass
  public static void beforeClass() throws Exception {
    fJavaProject = ProjectCreator.createStandardDemoProject();
    assertNotNull(fJavaProject);
  }

  @Test
  public void testMethod() throws JavaModelException {
    IPath path = fJavaProject.getPath().append(new Path("src"));
    IPackageFragmentRoot src = fJavaProject.findPackageFragmentRoot(path);

    // Find each IMethod in each IType in the src folder and compute its
    // mnemonic. Then store each mnemonic-IMethod pair in a HashMap
    HashMap<String, IMethod> methodsByMnemonics = new HashMap<String, IMethod>();
    for (IJavaElement e : src.getChildren()) {
      assertTrue(e instanceof IPackageFragment);
      IPackageFragment pf = (IPackageFragment) e;
      pf.open(null);

      for (ICompilationUnit cu : pf.getCompilationUnits()) {
        for (IType type : cu.getTypes()) {
          for (IMethod m : type.getMethods()) {
            String mnemonic = MethodMnemonics.getMnemonic(m);

            methodsByMnemonics.put(mnemonic, m);
          }
        }
      }
    }

    // Check that MethodMnemonics.getMethod reconstructs the methods correctly
    for (String mnemonic : methodsByMnemonics.keySet()) {
      IMethod m = MethodMnemonics.getMethod(fJavaProject, mnemonic);
      assertNotNull(m);
      assertEquals(m, methodsByMnemonics.get(mnemonic));
    }
  }

}
