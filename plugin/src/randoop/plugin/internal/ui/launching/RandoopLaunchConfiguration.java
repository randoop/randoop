package randoop.plugin.internal.ui.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class RandoopLaunchConfiguration {
  private static String ATTR_GENERATED_TEST_FILES = RandoopPlugin.getPluginId() + ".GENERATED_TEST_FILES"; //$NON-NLS-1$
  
  ILaunchConfiguration fConfig;

  public RandoopLaunchConfiguration(ILaunchConfiguration config) {
    fConfig = config;
  }

  public RandoopArgumentCollector getArguments() {
    return new RandoopArgumentCollector(fConfig);
  }

  private static List<ICompilationUnit> toCompilationUnits(List<String> handlerIds)
      throws CoreException {
    List<ICompilationUnit> compilationUnits = new ArrayList<ICompilationUnit>();

    // Verify that each handlerIds represents an IJavaElement that exists and
    // is an ICompilationUnit. Add these to a list of IPaths.
    for (String handlerId : handlerIds) {
      IJavaElement element = JavaCore.create(handlerId);

      if (!element.exists()) {
        IStatus status = StatusFactory.createErrorStatus("IJavaElement "
            + element.getElementName() + " does not exist.");
        throw new CoreException(status);
      }

      if (element instanceof ICompilationUnit) {
        compilationUnits.add((ICompilationUnit) element);
      } else {
        IStatus status = StatusFactory.createErrorStatus("IJavaElement "
            + element.getElementName()
            + " is not an instance of ICompilationUnit.");
        throw new CoreException(status);
      }
    }

    return compilationUnits;
  }
  
  public void setGeneratedTests(List<String> testFiles) throws CoreException {
    List<String> handlerIds = new ArrayList<String>();
    // Verify that each handlerIds represents an IJavaElement that exists and
    // is an ICompilationUnit. Add these to a list of IPaths.
    for (String relativePath : testFiles) {
      Path path = new Path(relativePath);
      IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
      IJavaElement element = JavaCore.create(file);
      
      if (!element.exists()) {
        IStatus status = StatusFactory.createErrorStatus("IJavaElement "
            + element.getElementName() + " does not exist.");
        throw new CoreException(status);
      }

      if (element instanceof ICompilationUnit) {
        handlerIds.add(element.getHandleIdentifier());
      } else {
        IStatus status = StatusFactory.createErrorStatus("IJavaElement "
            + element.getElementName()
            + " is not an instance of ICompilationUnit.");
        throw new CoreException(status);
      }
    }

    // Store the handlerIds in the configuration
    ILaunchConfigurationWorkingCopy config = fConfig.getWorkingCopy().getWorkingCopy();
    config.setAttribute(ATTR_GENERATED_TEST_FILES, handlerIds);
    fConfig = config.doSave();
  }
  
  
  public List<ICompilationUnit> getCompilationUnits() throws CoreException {
    List<String> handlerIds = fConfig.getAttribute(ATTR_GENERATED_TEST_FILES, IConstants.EMPTY_STRING_LIST);
    List<ICompilationUnit> compilationUnits = new ArrayList<ICompilationUnit>();

    // Verify that each handlerIds represents an IJavaElement that exists and
    // is an ICompilationUnit. Add these to a list of IPaths.
    for (String handlerId : handlerIds) {
      IJavaElement element = JavaCore.create(handlerId);

      if (!element.exists()) {
        IStatus status = StatusFactory.createErrorStatus("IJavaElement "
            + element.getElementName() + " does not exist.");
        throw new CoreException(status);
      }

      if (element instanceof ICompilationUnit) {
        compilationUnits.add((ICompilationUnit) element);
      } else {
        IStatus status = StatusFactory.createErrorStatus("IJavaElement "
            + element.getElementName()
            + " is not an instance of ICompilationUnit.");
        throw new CoreException(status);
      }
    }
    
    return compilationUnits;
  }
}
