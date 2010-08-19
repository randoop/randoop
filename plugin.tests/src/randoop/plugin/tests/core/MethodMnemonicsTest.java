package randoop.plugin.tests.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import org.junit.BeforeClass;
import org.junit.Test;

import randoop.plugin.internal.core.MethodMnemonic;
import randoop.plugin.internal.core.TypeMnemonic;
import randoop.plugin.tests.WorkspaceManager;

@SuppressWarnings("nls")
public class MethodMnemonicsTest {
  private static IJavaProject boundaryProject;
  private static IJavaProject pathplannerProject;
  private static IJavaProject kenkenProject;
  
  @BeforeClass
  public static void beforeClass() throws IOException, CoreException {
    boundaryProject = WorkspaceManager.getJavaProject(WorkspaceManager.BOUNDARY);
    kenkenProject = WorkspaceManager.getJavaProject(WorkspaceManager.KENKEN);
    pathplannerProject = WorkspaceManager.getJavaProject(WorkspaceManager.PATH_PLANNER);
  }
  
  @Test
  public void testMethod() throws CoreException {
    List<IType> types = new ArrayList<IType>();
    types.add(pathplannerProject.findType("demo.pathplanning.algorithms.AStar"));
    types.add(pathplannerProject.findType("demo.pathplanning.algorithms.PathPlanningContext"));
    types.add(pathplannerProject.findType("demo.pathplanning.model.Location"));
    types.add(pathplannerProject.findType("demo.pathplanning.model.Node"));
    types.add(pathplannerProject.findType("demo.pathplanning.model.Direction"));
    
    types.add(kenkenProject.findType("lpf.model.core.Cell"));
    types.add(kenkenProject.findType("lpf.model.core.CellsIterator"));
    types.add(kenkenProject.findType("lpf.model.core.Grid"));
    types.add(kenkenProject.findType("lpf.model.core.Location"));
    types.add(kenkenProject.findType("lpf.model.core.Puzzle"));
    types.add(kenkenProject.findType("lpf.model.core.Value"));
    
    types.add(boundaryProject.findType("jar.ClassInAnExportedJar"));
    types.add(boundaryProject.findType("jar.ClassInAJar"));
    
    types.add(boundaryProject.findType("demo.pathplanning.algorithms.AStar"));
    types.add(boundaryProject.findType("demo.pathplanning.algorithms.PathPlanningContext"));
    types.add(boundaryProject.findType("demo.pathplanning.model.Location"));
    
    types.add(boundaryProject.findType("lpf.model.core.Cell"));
    types.add(boundaryProject.findType("lpf.model.core.CellsIterator"));
    types.add(boundaryProject.findType("lpf.model.core.Grid"));
    types.add(boundaryProject.findType("lpf.model.core.Location"));
    types.add(boundaryProject.findType("lpf.model.core.Puzzle"));
    types.add(boundaryProject.findType("lpf.model.core.Value"));

    HashMap<IType, List<String>> mnemonicsByType = new HashMap<IType, List<String>>();
    HashMap<String, IMethod> methodsByMnemonics = new HashMap<String, IMethod>();
    HashMap<String, MethodMnemonic> mnemonicByStringMnemonics = new HashMap<String, MethodMnemonic>();

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    for (IType type : types) {
      assertNotNull(type);
      assertTrue(type.exists());

      if (!type.isAnonymous()) {
        int flags = type.getFlags();
        if (!(Flags.isInterface(flags) || Flags.isAbstract(flags))) {
          
          List<String> mnemonicList = mnemonicsByType.get(type);
          if (mnemonicList == null) {
            mnemonicList = new ArrayList<String>();
            mnemonicsByType.put(type, mnemonicList);
          }
          
          for (IMethod m : type.getMethods()) {
            MethodMnemonic mnemonic = new MethodMnemonic(m);
            assertTrue(mnemonic.exists());
            assertNotNull(mnemonic.getMethod());
            
            String mnemonicString = mnemonic.toString();
            
            mnemonicByStringMnemonics.put(mnemonicString, mnemonic);
            mnemonicList.add(mnemonicString);
            methodsByMnemonics.put(mnemonicString, m);
          }
        }
      }
    }

    // Test MethodMnemonic's other constructors and findMethod
    for (String mnemonic : methodsByMnemonics.keySet()) {
      MethodMnemonic originalMnemonic = mnemonicByStringMnemonics.get(mnemonic);
      assertTrue(originalMnemonic.exists());
      assertNotNull(originalMnemonic.getMethod());
      
      MethodMnemonic recontructedMnemonic1 = new MethodMnemonic(mnemonic);
      assertFalse(recontructedMnemonic1.exists());
      assertNull(recontructedMnemonic1.getMethod());
      assertEquals(originalMnemonic, recontructedMnemonic1);
      
      MethodMnemonic recontructedMnemonic2 = new MethodMnemonic(recontructedMnemonic1.getMethodName(),
          recontructedMnemonic1.isConstructor(), recontructedMnemonic1.getMethodSignature());
      assertEquals(originalMnemonic, recontructedMnemonic2);
      assertFalse(recontructedMnemonic2.exists());
      assertNull(recontructedMnemonic2.getMethod());
      
      for (IType type : mnemonicsByType.keySet()) {
        List<String> mnemonics = mnemonicsByType.get(type);
        if (mnemonics.contains(mnemonic)) {
          assertNotNull(recontructedMnemonic1.findMethod(type));
        } else {
          assertNull(recontructedMnemonic1.findMethod(type));
        }
      }
    }
  }

}
