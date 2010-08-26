package randoop.plugin.tests.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.junit.BeforeClass;
import org.junit.Test;

import randoop.plugin.internal.core.TypeMnemonic;
import randoop.plugin.tests.WorkspaceManager;

@SuppressWarnings("nls")
public class TypeMnemonicTest {
  
  private static final String ARRAY_LIST_CONTENTS = "package java.util;\n" + "\n"
      + "public class ArrayList {\n" + "\n" + "}\n" + "\n";
  
  private static Map<String, TypeMnemonic> originalMnemonicsByString;
  private static IJavaProject boundaryProject;
  private static IJavaProject pathplannerProject;
  private static IJavaProject kenkenProject;
  private static IWorkspaceRoot root;

  @BeforeClass
  public static void beforeClass() throws IOException, CoreException {
    boundaryProject = WorkspaceManager.getJavaProject(WorkspaceManager.BOUNDARY);
    kenkenProject = WorkspaceManager.getJavaProject(WorkspaceManager.KENKEN);
    pathplannerProject = WorkspaceManager.getJavaProject(WorkspaceManager.PATH_PLANNER);
    originalMnemonicsByString = new HashMap<String, TypeMnemonic>();
    root = ResourcesPlugin.getWorkspace().getRoot();
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
      originalMnemonicsByString.put(typeMnemonic.toString(), typeMnemonic);
      
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
      originalMnemonicsByString.put(typeMnemonic.toString(), typeMnemonic);

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
      originalMnemonicsByString.put(typeMnemonic.toString(), typeMnemonic);
      
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
      
      TypeMnemonic typeMnemonic = new TypeMnemonic(boundaryProject.getElementName(), IClasspathEntry.CPE_SOURCE, boundaryProject.getPath().append("srcLinked"), type.getFullyQualifiedName());
      typeMnemonic = typeMnemonic.resolve(root);
      assertTrue(typeMnemonic.exists());
      originalMnemonicsByString.put(typeMnemonic.toString(), typeMnemonic);

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
      
      TypeMnemonic typeMnemonic = new TypeMnemonic(boundaryProject.getElementName(), IClasspathEntry.CPE_LIBRARY, kenkenProject.getOutputLocation(), kenkenType.getFullyQualifiedName());
      typeMnemonic = typeMnemonic.resolve(root);
      assertTrue(typeMnemonic.exists());
      originalMnemonicsByString.put(typeMnemonic.toString(), typeMnemonic);

      // This type should be accessible from the kenken project since it
      // generates the binary folder, 'KenKen/bin', that the boundary project
      // references.
      assertNotNull(typeMnemonic.reassign(kenkenProject));
    }
    
    List<IType> boundaryTypesViaKenKen = new ArrayList<IType>();
    boundaryTypesViaKenKen.add(kenkenProject.findType("ClassInDefaultPackage"));
    boundaryTypesViaKenKen.add(kenkenProject.findType("boundary.InnerTypes"));
    boundaryTypesViaKenKen.add(kenkenProject.findType("boundary.InnerTypes.NamedStaticInnerType"));
    boundaryTypesViaKenKen.add(kenkenProject.findType("boundary.InnerTypes.NamedStaticInnerType.NamedStaticInnerType2"));
    boundaryTypesViaKenKen.add(kenkenProject.findType("boundary.InnerTypes.NamedStaticInnerType.NamedNonStaticInnerType2"));
    boundaryTypesViaKenKen.add(kenkenProject.findType("boundary.InnerTypes.NamedNonStaticInnerType"));
    boundaryTypesViaKenKen.add(kenkenProject.findType("boundary.InnerTypes.NamedNonStaticInnerType.NamedStaticInnerType2"));
    boundaryTypesViaKenKen.add(kenkenProject.findType("boundary.InnerTypes.NamedNonStaticInnerType.NamedNonStaticInnerType2"));
    
    for(IType boundaryType : boundaryTypesViaKenKen) {
      assertNotNull(boundaryType);

      TypeMnemonic typeMnemonic = new TypeMnemonic(kenkenProject.getElementName(), IClasspathEntry.CPE_PROJECT, boundaryProject.getPath(), boundaryType.getFullyQualifiedName());
      typeMnemonic = typeMnemonic.resolve(root);
      assertTrue(typeMnemonic.exists());
      originalMnemonicsByString.put(typeMnemonic.toString(), typeMnemonic);

      // This type should be accessible from the boundary project since it
      // holds the source package fragment roots that kenken uses when it
      // references the boundary project
      assertNotNull(typeMnemonic.reassign(boundaryProject));
    }
  }
  
  @Test
  public void testReconstruction() {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    
    for (String mnemonic : originalMnemonicsByString.keySet()) {
      TypeMnemonic originalMnemonic = originalMnemonicsByString.get(mnemonic);
      TypeMnemonic recontructedMnemonic1 = new TypeMnemonic(mnemonic);
      TypeMnemonic recontructedMnemonic2 = new TypeMnemonic(mnemonic, root);
      
      assertTrue("Recontructed mnemonic does not equal original", originalMnemonic.equals(recontructedMnemonic1));
      assertTrue("Recontructed mnemonic does not equal original", originalMnemonic.equals(recontructedMnemonic2));
      
      TypeMnemonic recontructedMnemonic3 = recontructedMnemonic1.resolve(root);
      assertTrue("Recontructed mnemonic does not equal original", originalMnemonic.equals(recontructedMnemonic3));
      
      assertTrue("Recontructed mnemonic does not exist", recontructedMnemonic2.exists());
      assertTrue("Recontructed mnemonic does not exist", recontructedMnemonic3.exists());
    }
  }
  
  @Test
  public void testReassign() throws JavaModelException {
    IType javaArrayList = boundaryProject.findType("java.util.ArrayList");
    TypeMnemonic javaArrayListMnemonic = new TypeMnemonic(javaArrayList);
    
    IPackageFragmentRoot sourcePfr = null;
    for (IPackageFragmentRoot pfr : boundaryProject.getPackageFragmentRoots()) {
      if (pfr.getKind() == IPackageFragmentRoot.K_SOURCE) {
        sourcePfr = pfr;
        break;
      }
    }
    
    assertNotNull("No source folder found in the 'Boundary' project", sourcePfr);
    assertTrue("The source folder found in the 'Boundary' project does not exist", sourcePfr.exists());
    
    IPackageFragment pf = sourcePfr.createPackageFragment("java.util", true, null);
    ICompilationUnit cu = pf.createCompilationUnit("ArrayList.java", ARRAY_LIST_CONTENTS, true, null);
    
    IType phonyArrayList = cu.findPrimaryType();
    TypeMnemonic originalPhonyArrayListMnemonic = new TypeMnemonic(phonyArrayList);
    
    TypeMnemonic phonyArrayListMnemonic2 = javaArrayListMnemonic.resolve();
    assertEquals("TypeMneomnic.resolve() did not return the expected type", originalPhonyArrayListMnemonic, phonyArrayListMnemonic2);
    
    TypeMnemonic javaArrayListMnemonic2 = phonyArrayListMnemonic2.resolve(pathplannerProject);
    assertTrue("TypeMneomnic.resolve() did not return the expected type", javaArrayListMnemonic2.getClasspath().isPrefixOf(new Path(JavaRuntime.JRE_CONTAINER)));
  }
  
}
