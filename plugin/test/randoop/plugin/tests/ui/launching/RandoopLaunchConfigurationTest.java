package randoop.plugin.tests.ui.launching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.BeforeClass;
import org.junit.Test;

import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.ui.launching.RandoopLaunchConfiguration;

@SuppressWarnings("nls")
public class RandoopLaunchConfigurationTest {
  private static IJavaProject javaProject;

  @BeforeClass
  public static void beforeClass() {
    javaProject = ProjectCreator.createStandardDemoProject();
  }

  @Test
  public void create() throws CoreException, IOException {
    ILaunchConfigurationWorkingCopy config = ProjectCreator.createNewAllTypeConfig(javaProject);
    
    RandoopLaunchConfiguration rlc = new RandoopLaunchConfiguration(config);
    
    assertEquals(new RandoopArgumentCollector(config), rlc.getArguments());
    
    List<String> testFiles = new ArrayList<String>();

    String testFolder = IPath.SEPARATOR + ProjectCreator.demoProjectName
        + IPath.SEPARATOR + ProjectCreator.testFolderName + IPath.SEPARATOR;

    testFiles.add(testFolder + "demo/pathplanning/tests/AllTest.java");
    testFiles.add(testFolder + "demo/pathplanning/tests/AllTest0.java");
    testFiles.add(testFolder + "demo/pathplanning/tests/AllTest1.java");
    rlc.setGeneratedTests(testFiles);
    
    List<ICompilationUnit> compUnits = rlc.getCompilationUnits();
    assertEquals(3, compUnits.size());
    for(ICompilationUnit cu : compUnits) {
      assertTrue(cu.exists());
      assertTrue(testFiles.contains(cu.getPath().toString()));
    }
  }
}
