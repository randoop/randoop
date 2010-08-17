package randoop.plugin.tests.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.junit.Test;

import randoop.plugin.internal.core.TypeMnemonic;
import randoop.plugin.tests.WorkspaceManager;

@SuppressWarnings("nls")
public class TypeMnemonicTest {
  private static IJavaProject boundaryProject;
  private static IJavaProject pathplannerProject;
  private static IJavaProject kenkenProject;
  
  @BeforeClass
  public static void beforeClass() throws IOException, CoreException {
    WorkspaceManager.setupDemoWorkspace();
    
    boundaryProject = WorkspaceManager.getJavaProject(WorkspaceManager.BOUNDARY);
    kenkenProject = WorkspaceManager.getJavaProject(WorkspaceManager.KENKEN);
    pathplannerProject = WorkspaceManager.getJavaProject(WorkspaceManager.PATH_PLANNER);
  }
  
  @Test
  public void testReassignment() throws CoreException, IOException {
    List<IType> pathplannerTypes = new ArrayList<IType>();
    pathplannerTypes.add(pathplannerProject.findType("demo.pathplanning.algorithms.AStar"));
    pathplannerTypes.add(pathplannerProject.findType("demo.pathplanning.algorithms.PathPlanningContext"));
    pathplannerTypes.add(pathplannerProject.findType("demo.pathplanning.model.Location"));
    pathplannerTypes.add(pathplannerProject.findType("demo.pathplanning.model.Node"));
    pathplannerTypes.add(pathplannerProject.findType("demo.pathplanning.model.Direction"));
    
    for(IType pathplannerType : pathplannerTypes) {
      assertNotNull(pathplannerType);
      
      TypeMnemonic typeMnemonic = new TypeMnemonic(pathplannerType);
      
      assertTrue(typeMnemonic.exists());
      // Both the boundary project and kenken project reference the pathplanner
      // project in someway. Therefore, we should be able to reassign this type mnemonic to
      // each of these projects.
      assertNotNull(typeMnemonic.reassign(boundaryProject));
      assertNotNull(typeMnemonic.reassign(kenkenProject));
    }
    
    List<IType> kenkenTypes = new ArrayList<IType>();
    kenkenTypes.add(kenkenProject.findType("lpf.model.core.Cell"));
    kenkenTypes.add(kenkenProject.findType("lpf.model.core.CellsIterator"));
    kenkenTypes.add(kenkenProject.findType("lpf.model.core.Grid"));
    kenkenTypes.add(kenkenProject.findType("lpf.model.core.Location"));
    kenkenTypes.add(kenkenProject.findType("lpf.model.core.Puzzle"));
    kenkenTypes.add(kenkenProject.findType("lpf.model.core.Value"));
    
    for(IType kenkenType : kenkenTypes) {
      assertNotNull(kenkenType);
      
      TypeMnemonic typeMnemonic = new TypeMnemonic(kenkenType);
      assertTrue(typeMnemonic.exists());

      // This type should be accessible from the boundary project since it
      // references the kenken projects class folder, 'KenKen/bin'
      assertNotNull(typeMnemonic.reassign(boundaryProject));
    }
    
    // Make a new list with types from jar files
    List<IType> jarTypes = new ArrayList<IType>();
    jarTypes.add(boundaryProject.findType("jar.ClassInAnExportedJar"));
    jarTypes.add(boundaryProject.findType("jar.ClassInAJar"));
    
    for(IType type : jarTypes) {
      assertNotNull(type);
      
      TypeMnemonic typeMnemonic = new TypeMnemonic(type);
      assertTrue(typeMnemonic.exists());
      
      // Each of these types should be accessible from the kenken project since
      // one of them is referenced as an external library, and the other is
      // exported and kenken references the boundary project in its classpath
      assertNotNull(typeMnemonic.reassign(kenkenProject));
    }
    
    List<IType> pathplannerTypesViaLink = new ArrayList<IType>();
    pathplannerTypesViaLink.add(boundaryProject.findType("demo.pathplanning.algorithms.AStar"));
    pathplannerTypesViaLink.add(boundaryProject.findType("demo.pathplanning.algorithms.PathPlanningContext"));
    pathplannerTypesViaLink.add(boundaryProject.findType("demo.pathplanning.model.Location"));
    
    for(IType type : pathplannerTypesViaLink) {
      assertNotNull(type);
      
      TypeMnemonic typeMnemonic = new TypeMnemonic(type);
      assertTrue(typeMnemonic.exists());

      // Each of type should be accessible from pathplanner since it holds the
      // original source folder to which the boundary project links
      assertNotNull(typeMnemonic.reassign(pathplannerProject));
    }
    
    List<IType> kenkenTypesViaClassFolder = new ArrayList<IType>();
    kenkenTypesViaClassFolder.add(boundaryProject.findType("lpf.model.core.Cell"));
    kenkenTypesViaClassFolder.add(boundaryProject.findType("lpf.model.core.CellsIterator"));
    kenkenTypesViaClassFolder.add(boundaryProject.findType("lpf.model.core.Grid"));
    kenkenTypesViaClassFolder.add(boundaryProject.findType("lpf.model.core.Location"));
    kenkenTypesViaClassFolder.add(boundaryProject.findType("lpf.model.core.Puzzle"));
    kenkenTypesViaClassFolder.add(boundaryProject.findType("lpf.model.core.Value"));
    
    for(IType kenkenType : kenkenTypesViaClassFolder) {
      assertNotNull(kenkenType);
      
      TypeMnemonic typeMnemonic = new TypeMnemonic(kenkenType);
      assertTrue(typeMnemonic.exists());

      // This type should be accessible from the kenken project since it
      // generates the binary folder, 'KenKen/bin', that the boundary project
      // references.
      assertNotNull(typeMnemonic.reassign(kenkenProject));
    }
  }

}
