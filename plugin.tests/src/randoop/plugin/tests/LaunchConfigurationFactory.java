package randoop.plugin.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Assert;

import randoop.plugin.internal.core.TypeMnemonic;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.ui.launching.RandoopLaunchConfigurationUtil;

public class LaunchConfigurationFactory {
  private static final Random RAND = new Random(0);
  
  public static ILaunchConfigurationWorkingCopy createNewRandoopLaunchConfiguration(String name) throws CoreException {
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfigurationType randoopType = launchManager
        .getLaunchConfigurationType(IRandoopLaunchConfigurationConstants.ID_RANDOOP_TEST_GENERATION);

    return randoopType.newInstance(null, launchManager.generateLaunchConfigurationName(name));
  }

  public static ILaunchConfigurationWorkingCopy createConfig(IJavaProject javaProject, String name, int max, boolean useSource, boolean useBinary) throws CoreException {

    ILaunchConfigurationWorkingCopy config = createNewRandoopLaunchConfiguration(name);

    List<IType> types = getRandomTypes(javaProject, max, useSource, useBinary);

    List<String> typeMnemonics = new ArrayList<String>();
    for (IType type : types) {
      TypeMnemonic typeMnemonic = new TypeMnemonic(type);

      typeMnemonics.add(typeMnemonic.toString());
    }
    
    RandoopArgumentCollector.setAvailableTypes(config, typeMnemonics);
    RandoopArgumentCollector.setSelectedTypes(config, typeMnemonics);
  
    return config;
  }
  
  public static List<IType> getRandomTypes(IJavaProject p, int max, boolean useSource, boolean useBinary) throws JavaModelException {
    IClasspathEntry[] entries = p.getRawClasspath();
    
    List<IPackageFragment> packageFragments = new ArrayList<IPackageFragment>();
    for (IClasspathEntry entry : entries) {
      for (IPackageFragmentRoot pfr : p.findPackageFragmentRoots(entry)) {
        for (IJavaElement e : pfr.getChildren()) {
          Assert.assertTrue("IPackageFragment expected", e instanceof IPackageFragment); //$NON-NLS-1$
          IPackageFragment pf = (IPackageFragment) e;

          if ((pf.getKind() == IPackageFragmentRoot.K_SOURCE && useSource)
              || (pf.getKind() == IPackageFragmentRoot.K_BINARY && useBinary)) {
            packageFragments.add(pf);
          }
        }
      }
    }

    List<IType> types = new ArrayList<IType>();
    int numTypesToAddFromPackage = Math.round((float) max / packageFragments.size());
    if (numTypesToAddFromPackage == 0) {
      numTypesToAddFromPackage = 1;
    }
    
    for (IPackageFragment pf : packageFragments) {
      types.addAll(getRandomTypes(pf, numTypesToAddFromPackage));
    }

    return types;
  }

  private static List<IType> getRandomTypes(IPackageFragment pf, int max) throws JavaModelException {
    Assert.assertNotNull(pf);
    Assert.assertTrue(pf.exists());
    
    int size = pf.getChildren().length;
    if (size == 0) {
      return new ArrayList<IType>();
    }
    
    double ratio = (double) max / size;
    int count = 0;

    List<IType> types = new ArrayList<IType>();
    switch (pf.getKind()) {
    case IPackageFragmentRoot.K_BINARY:
      for (IClassFile cf : pf.getClassFiles()) {
        if (RAND.nextDouble() < ratio) {
          types.addAll(RandoopLaunchConfigurationUtil.findTypes(cf, true, null));
          count++;
        }

        if (count >= max) {
          return types;
        }
      }
      break;
    case IPackageFragmentRoot.K_SOURCE:
      for (ICompilationUnit cu : pf.getCompilationUnits()) {
        if (RAND.nextDouble() < ratio) {
          types.addAll(RandoopLaunchConfigurationUtil.findTypes(cu, true, null));
          count++;
        }

        if (count >= max) {
          return types;
        }
      }
      break;
    }

    return types;
  }

}
