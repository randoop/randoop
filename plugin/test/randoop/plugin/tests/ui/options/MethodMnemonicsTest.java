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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.junit.BeforeClass;
import org.junit.Test;

import randoop.plugin.internal.core.MethodMnemonic;
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
  public void testJRENaming() throws CoreException {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = root.getProject("Demo Project"/* Java project name */);
    IJavaProject javaproject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
    if (javaproject == null)
      fail();

    IClasspathEntry classpathEntry = JavaRuntime.getDefaultJREContainerEntry();
    IPackageFragmentRoot[] jrePackageFragmentRoots = javaproject.findPackageFragmentRoots(classpathEntry);

    assertNotNull(jrePackageFragmentRoots);
    assertTrue(jrePackageFragmentRoots.length != 0);

    for (IPackageFragmentRoot packageFragmentRoot : jrePackageFragmentRoots) {
      for (IJavaElement e : packageFragmentRoot.getChildren()) {
        assertTrue(e instanceof IPackageFragment);
        IPackageFragment packageFragment = (IPackageFragment) e;
        packageFragment.open(null);

        assertEquals(IPackageFragmentRoot.K_BINARY, packageFragment.getKind());
        for (IClassFile classFile : packageFragment.getClassFiles()) {
          IType type = classFile.getType();

          String fqname = type.getFullyQualifiedName();

          int lastDelimiter = fqname.lastIndexOf('.');
          String typeName = fqname.substring(lastDelimiter + 1);

          if (typeName.contains("$")) { //$NON-NLS-1$
            typeName = typeName.substring(typeName.lastIndexOf('$') + 1);
          }

          // Make some assertions about how this type is named
          if (type.isAnonymous()) {
            // Ensure the secondary type's name is a number
            assertTrue(typeName.matches("\\p{Digit}+"));
          } else {
            // This is where the test failed for
            // com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility$1NullInputStream
            assertEquals(typeName, type.getElementName());
          }
        }
      }
    }
  }

  @Test
  public void testMethod() throws CoreException {
    HashMap<String, IMethod> methodsByMnemonics = new HashMap<String, IMethod>();

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    for (IProject project : root.getProjects()) {
      IJavaProject javaproject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
      if (javaproject == null)
        continue;

      for (IClasspathEntry classpathEntry : javaproject.getRawClasspath()) {
        for (IPackageFragmentRoot packageFragmentRoot : javaproject.findPackageFragmentRoots(classpathEntry)) {
          // Find each IMethod in each IType in the src folder and compute its
          // mnemonic. Then store each mnemonic-IMethod pair in a HashMap
          for (IJavaElement e : packageFragmentRoot.getChildren()) {
            assertTrue(e instanceof IPackageFragment);
            IPackageFragment pf = (IPackageFragment) e;
            pf.open(null);

            if (pf.getKind() == IPackageFragmentRoot.K_SOURCE) {
              for (ICompilationUnit cu : pf.getCompilationUnits()) {
                for (IType type : cu.getTypes()) {
                  if (!type.isAnonymous()) {
                    int flags = type.getFlags();
                    if (!(Flags.isInterface(flags) || Flags.isAbstract(flags))) {
                      for (IMethod m : type.getMethods()) {
                        String mnemonic = new MethodMnemonic(m).toString();

                        methodsByMnemonics.put(mnemonic, m);
                      }
                    }
                  }
                }
              }
            } else if (pf.getKind() == IPackageFragmentRoot.K_BINARY) {
              for (IClassFile cf : pf.getClassFiles()) {
                IType type = cf.getType();
                if (!type.isAnonymous()) {
                  int flags = type.getFlags();
                  if (!(Flags.isInterface(flags) || Flags.isAbstract(flags))) {
                    for (IMethod m : type.getMethods()) {
                      String mnemonic = new MethodMnemonic(m).toString();

                      methodsByMnemonics.put(mnemonic, m);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    System.out.println("Verifying");
    verify(root, methodsByMnemonics);
  }

  private void verify(IWorkspaceRoot root, HashMap<String, IMethod> methodsByMnemonics) throws CoreException {
    // Check that MethodMnemonics.getMethod reconstructs the methods correctly
    for (String mnemonic : methodsByMnemonics.keySet()) {
      IMethod m = new MethodMnemonic(root, mnemonic).getMethod();
      assertNotNull(m);
      methodsByMnemonics.get(mnemonic);

      IMethod m2 = methodsByMnemonics.get(mnemonic);

      assertEquals(m, m2);
    }
  }

}
