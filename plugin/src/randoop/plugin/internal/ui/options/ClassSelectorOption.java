package randoop.plugin.internal.ui.options;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.dialogs.ITypeInfoFilterExtension;
import org.eclipse.jdt.ui.dialogs.ITypeInfoRequestor;
import org.eclipse.jdt.ui.dialogs.TypeSelectionExtension;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.MethodMnemonic;
import randoop.plugin.internal.core.RandoopCoreUtil;
import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.core.TypeMnemonic;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.ui.ClasspathLabelProvider;
import randoop.plugin.internal.ui.MessageUtil;

public class ClassSelectorOption extends Option implements IOptionChangeListener {
  private IRunnableContext fRunnableContext;
  private Shell fShell;
  private ClassSelector fTypeSelector;
  private Tree fTypeTree;
  private Button fClassAddFromSources;
  private Button fClassAddFromClasspaths;
  private Button fResolveClasses;
  private Button fSelectAll;
  private Button fSelectNone;
  private Button fClassRemove;
  private Button fIgnoreJUnitTestCases;
  private IJavaProject fJavaProject;
  
  public ClassSelectorOption(Composite parent, IRunnableContext runnableContext,
      final SelectionListener listener) {
    
    this(parent, runnableContext, listener, true);
  }
  
  public ClassSelectorOption(Composite parent, IRunnableContext runnableContext,
      final SelectionListener listener, IJavaProject project) {
    
    this(parent, runnableContext, listener, false);
    fJavaProject = project;
  }

  private ClassSelectorOption(Composite parent, IRunnableContext runnableContext,
      final SelectionListener listener, boolean hasResolveButton) {
    
    fRunnableContext = runnableContext;
    Group comp = SWTFactory.createGroup(parent, "Classes/Methods Un&der Test", 2, 1, GridData.FILL_BOTH);
    fShell = comp.getShell();

    final Composite leftcomp = SWTFactory.createComposite(comp, 1, 1, GridData.FILL_BOTH);
    GridLayout ld = (GridLayout) leftcomp.getLayout();
    ld.marginWidth = 1;
    ld.marginHeight = 1;
    GridData gd = (GridData) leftcomp.getLayoutData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;

    final Composite rightcomp = SWTFactory.createComposite(comp, 1, 1, GridData.FILL);
    gd = (GridData) rightcomp.getLayoutData();
    gd.horizontalAlignment = SWT.LEFT;
    gd.verticalAlignment = SWT.TOP;

    fTypeTree = new Tree(leftcomp, SWT.MULTI | SWT.CHECK | SWT.BORDER);
    fTypeTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    
    // Add type selector functionalities to this tree. This will also add a
    // Listener to fTypeTree that adds default tree behavior. That is, checking/
    // unchecking a parent item also checks/unchecks all descendant items
    fTypeSelector = new ClassSelector(fTypeTree);
    
    fTypeTree.addSelectionListener(new SelectionListener() {
      
      @Override
      public void widgetSelected(SelectionEvent e) {
        fClassRemove.setEnabled(fTypeSelector.canRemoveFromSelection());
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }
    });
    fTypeTree.addSelectionListener(listener);
    
    SWTFactory.createLabel(rightcomp, "Add classes from:", 1);
    
    fClassAddFromSources = SWTFactory.createPushButton(rightcomp, "Project So&urces...", null);
    fClassAddFromSources.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        IJavaElement[] elements = { fJavaProject };
        IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(elements, IJavaSearchScope.SOURCES);
        handleSearchButtonSelected(searchScope);
      }
    });
    fClassAddFromSources.addSelectionListener(listener);
    
    fClassAddFromClasspaths = SWTFactory.createPushButton(rightcomp, "Referenced Classpat&hs...", null);
    fClassAddFromClasspaths.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          IClasspathEntry classpathEntry = chooseClasspathEntry();
          if (classpathEntry != null) {
            IJavaElement[] elements = RandoopCoreUtil.findPackageFragmentRoots(fJavaProject, classpathEntry);
            IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(elements);
            handleSearchButtonSelected(searchScope);
          }
        } catch (JavaModelException jme) {
          RandoopPlugin.log(jme);
        }
      }
    });
    fClassAddFromClasspaths.addSelectionListener(listener);
    
    if (hasResolveButton) {
      fResolveClasses = SWTFactory.createPushButton(rightcomp, "Resolve M&issing Classes", null);
      fResolveClasses.setToolTipText("Finds classes in the project's classpath\nthat match those that are missing");
      
      fResolveClasses.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          String message = "This will attempt to find classes in the project's classpath with fully-qualified names identical to those that are missing. The classes found may differ from those originally intended to be tested.";
          String question = "Proceed with operation?";
          if (MessageUtil.openQuestion(message + "\n\n" + question)) { //$NON-NLS-1$
            try {
              fTypeSelector.resolveMissingClasses();
            } catch (JavaModelException jme) {
              RandoopPlugin.log(jme);
            }
          }
        }
      });
      fResolveClasses.setEnabled(fJavaProject != null && fJavaProject.exists() && fTypeSelector.hasMissingClasses());
      fResolveClasses.addSelectionListener(listener);
    }
    
    // Create a spacer
    SWTFactory.createLabel(rightcomp, "", 1);
    fSelectAll = SWTFactory.createPushButton(rightcomp, "Select &All", null);
    fSelectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fTypeSelector.checkAll();
      }
    });
    fSelectAll.addSelectionListener(listener);
    
    fSelectNone = SWTFactory.createPushButton(rightcomp, "Select Non&e", null);
    fSelectNone.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fTypeSelector.uncheckAll();
      }
    });
    fSelectNone.addSelectionListener(listener);
    
    fClassRemove = SWTFactory.createPushButton(rightcomp, "&Remove", null);
    fClassRemove.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fTypeSelector.removeSelectedTypes();
      }
    });
    fClassRemove.addSelectionListener(listener);
    
    
    fIgnoreJUnitTestCases = SWTFactory.createCheckButton(leftcomp,
        "Ignore JUni&t tests cases when searching for class inputs", null, true, 2);
    gd = (GridData) fIgnoreJUnitTestCases.getLayoutData();
    gd.horizontalIndent = 5;
    fIgnoreJUnitTestCases.setLayoutData(gd);
  }

  public ClassSelectorOption(Composite parent, IRunnableContext runnableContext,
      final SelectionListener listener, IJavaProject javaProject, List<TypeMnemonic> types,
      Map<TypeMnemonic, List<MethodMnemonic>> methodsByDeclaringTypes) {

    this(parent, runnableContext, listener, false);

    fJavaProject = javaProject;

    fTypeSelector.setJavaProject(fJavaProject);

    for (TypeMnemonic type : types) {
      fTypeSelector.addClass(type, true, methodsByDeclaringTypes.get(type));
    }
    
  }
  
  private void handleSearchButtonSelected(IJavaSearchScope searchScope) {
    try {
      IJavaSearchScope junitSearchScope = new FilterJUnitSearchScope(searchScope, fIgnoreJUnitTestCases.getSelection());

      SelectionDialog dialog = JavaUI.createTypeDialog(fShell, fRunnableContext, junitSearchScope,
          IJavaElementSearchConstants.CONSIDER_CLASSES_AND_ENUMS, true, "",
          new RandoopTestInputSelectionExtension());
      dialog.setMessage("Add class input");
      dialog.setMessage("Enter type name prefix or pattern (*, ?, or camel case):");
      dialog.open();

      // Add all of the types to the type selector
      Object[] results = dialog.getResult();
      if (results != null && results.length > 0) {
        for (Object element : results) {
          if (element instanceof IType) {
            IType type = (IType) element;
            if (type != null) {
              fTypeSelector.addClass(type, true);
            }
          }
        }
      }
    } catch (JavaModelException jme) {
      RandoopPlugin.log(jme);
    }
  }
  
  @Override
  public IStatus canSave() {
    if (fRunnableContext == null || fShell == null || fTypeSelector == null || fTypeTree == null
        || fClassAddFromSources == null || fClassAddFromClasspaths == null || fSelectAll == null
        || fSelectNone == null || fClassRemove == null) {

      return StatusFactory.ERROR_STATUS;
    }

    return StatusFactory.OK_STATUS;
  }

  @Override
  public IStatus isValid(ILaunchConfiguration config) {
    List<String> selectedTypes = RandoopArgumentCollector.getSelectedTypes(config);
    List<String> selectedMethods = RandoopArgumentCollector.getSelectedMethods(config);

    return validate(fJavaProject, selectedTypes, selectedMethods);
  }

  @Override
  public void initializeFrom(ILaunchConfiguration config) {
    if (fTypeTree != null) {
      String projectName = RandoopArgumentCollector.getProjectName(config);
      fJavaProject = RandoopCoreUtil.getProjectFromName(projectName);
      
      List<String> availableTypes = RandoopArgumentCollector.getAvailableTypes(config);
      List<String> selectedTypes = RandoopArgumentCollector.getSelectedTypes(config);
      List<String> availableMethods = RandoopArgumentCollector.getAvailableMethods(config); 
      List<String> selectedMethods = RandoopArgumentCollector.getSelectedMethods(config);

      fTypeSelector = new ClassSelector(fTypeTree, fJavaProject, availableTypes, selectedTypes, availableMethods, selectedMethods);
      fResolveClasses.setEnabled(fJavaProject != null && fJavaProject.exists() && fTypeSelector.hasMissingClasses());
    }
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fTypeSelector == null) {
      setDefaults(config);
    } else {
      RandoopArgumentCollector.setAvailableTypes(config, fTypeSelector.getAllClasses());
      RandoopArgumentCollector.setSelectedTypes(config, fTypeSelector.getCheckedClasses());
      RandoopArgumentCollector.setAvailableMethods(config, fTypeSelector.getAllMethods());
      RandoopArgumentCollector.setSelectedMethods(config, fTypeSelector.getCheckedMethods());
    }
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    writeDefaults(config);
  }
  
  public static void writeDefaults(ILaunchConfigurationWorkingCopy config) {
    RandoopArgumentCollector.restoreSelectedTypes(config);
    RandoopArgumentCollector.restoreAvailableTypes(config);
    RandoopArgumentCollector.restoreSelectedMethods(config);
    RandoopArgumentCollector.restoreAvailableMethods(config);
  }

  /**
   * Returns an OK <code>IStatus</code> if the specified arguments could be
   * passed to Randoop without raising any error. If the arguments are not
   * valid, an ERROR status is returned with a message indicating what is wrong.
   * 
   * @param selectedTypes
   * @param selectedMethods 
   * @return
   */
  protected static IStatus validate(IJavaProject javaProject, List<String> selectedTypes, List<String> selectedMethods) {
    boolean areTypesSelected = selectedTypes == null || !selectedTypes.isEmpty();
    boolean areMethodsSelected = selectedMethods == null || !selectedMethods.isEmpty();

    if (!areTypesSelected && !areMethodsSelected) {
      return StatusFactory.createErrorStatus("At least one existing type or method must be selected.");
    }
    
    if (javaProject == null) {
      if (areTypesSelected || areMethodsSelected) {
        return StatusFactory.createErrorStatus("Types cannot be selected if no Java project is set");
      }
      return StatusFactory.OK_STATUS;
    }
    
    for (String typeMnemonicString : selectedTypes) {
      TypeMnemonic typeMnemonic = new TypeMnemonic(typeMnemonicString, getWorkspaceRoot());
      IType type = typeMnemonic.getType();
      if (type == null || !type.exists()) {
        return StatusFactory.createErrorStatus("One of the selected types does not exist.");
      } else if (!javaProject.equals(typeMnemonic.getJavaProject())) {
        return StatusFactory.createErrorStatus("One of the selected types does not exist in the selected project.");
      }
    }

    for (String mnemonic : selectedMethods) {
      MethodMnemonic methodMnemonic = new MethodMnemonic(mnemonic, getWorkspaceRoot());
      IMethod m = methodMnemonic.getMethod();

      if (m == null || !m.exists()) {
        return StatusFactory.createErrorStatus("One of the selected methods is invalid.");
      }
    }

    return StatusFactory.OK_STATUS;
  }

  private IClasspathEntry chooseClasspathEntry() throws JavaModelException {
    ILabelProvider labelProvider = new ClasspathLabelProvider(fJavaProject);
    ElementListSelectionDialog dialog = new ElementListSelectionDialog(fShell, labelProvider);
    dialog.setTitle("Classpath Selection");
    dialog.setMessage("Select a classpath to constrain your search.");

    IClasspathEntry[] classpaths = fJavaProject.getRawClasspath();
    dialog.setElements(classpaths);

    if (dialog.open() == Window.OK) {
      return (IClasspathEntry) dialog.getFirstResult();
    }
    return null;
  }
  
  private class FilterJUnitSearchScope implements IJavaSearchScope {
    IJavaSearchScope fSearchScope;
    boolean fIgnoreJUnit;

    public FilterJUnitSearchScope(IJavaSearchScope searchScope, boolean ignoreJUnit) {
      fSearchScope = searchScope;
      fIgnoreJUnit = ignoreJUnit;
    }

    @Override
    public boolean encloses(String resourcePath) {
      if (fSearchScope.encloses(resourcePath)) {
        IWorkspaceRoot root = getWorkspaceRoot();
        
        String filePath;
        String subFilePath;
        
        int separator = resourcePath.indexOf(JAR_FILE_ENTRY_SEPARATOR);
        if (separator == -1) {
          filePath = resourcePath;
          subFilePath = null;
        } else {
          filePath = resourcePath.substring(0, separator);
          subFilePath = resourcePath.substring(separator + 1);
        }
        
        URI fileURI = root.getLocation().append(new Path(filePath)).toFile().toURI();
        IFile[] files = root.findFilesForLocationURI(fileURI);

        boolean doesEnclose = true;
        for (IFile file : files) {
          IJavaElement element = JavaCore.create(file);
          if (element != null) {
            ArrayList<IType> types = new ArrayList<IType>();
            
            if (element instanceof IPackageFragmentRoot) {
              separator = subFilePath.lastIndexOf(IPath.SEPARATOR);
              String packageName = subFilePath.substring(0, separator).replace(IPath.SEPARATOR, '.');
              String fileName = subFilePath.substring(separator + 1);
              
              IPackageFragmentRoot pfr = (IPackageFragmentRoot) element;
              IPackageFragment pf = pfr.getPackageFragment(packageName);

              if (JavaConventions.validateClassFileName(fileName,
                  IConstants.DEFAULT_COMPLIANCE_LEVEL, IConstants.DEFAULT_SOURCE_LEVEL).isOK()) { //$NON-NLS-1$//$NON-NLS-2$
                IClassFile cf = pf.getClassFile(fileName);
                collectTypes(cf, types);
              } else if (JavaConventions.validateCompilationUnitName(fileName,
                  IConstants.DEFAULT_COMPLIANCE_LEVEL, IConstants.DEFAULT_SOURCE_LEVEL).isOK()) { //$NON-NLS-1$//$NON-NLS-2$
                ICompilationUnit cu = pf.getCompilationUnit(fileName);
                collectTypes(cu, types);
              }
            } else if (element instanceof ICompilationUnit || element instanceof IClassFile){
              collectTypes(element, types);
            } else {
              RandoopPlugin.log(StatusFactory.createWarningStatus("Unknown element type, returning false"));
              doesEnclose = false;
            }

            for (IType type : types) {
              doesEnclose &= RandoopCoreUtil.isValidTestInput(type, fIgnoreJUnit);
            }
          }
        }
        return doesEnclose;
      }
      return false;
    }
    
    /*
     * Helper method to collect the ITypes from an ICompilationUnit or IClassFile
     */
    private void collectTypes(IJavaElement element, List<IType> types) {
      if (element == null || !element.exists())
        return;

      if (element instanceof ICompilationUnit) {
        try {
          ICompilationUnit cu = (ICompilationUnit) element;
          for (IType type : cu.getAllTypes()) {
            types.add(type);
          }
        } catch (JavaModelException e) {
          RandoopPlugin.log(e);
        }
      } else if (element instanceof IClassFile) {
        IClassFile cf = (IClassFile) element;
        types.add(cf.getType());
      }
    }
    
    @Override
    public boolean encloses(IJavaElement element) {
      if (fSearchScope.encloses(element)) {
        if (element instanceof IType) {
          IType type = (IType) element;
          return RandoopCoreUtil.isValidTestInput(type, fIgnoreJUnit);
        }
        return true;
      }
      return false;
    }
    
    @Override
    public IPath[] enclosingProjectsAndJars() {
      return fSearchScope.enclosingProjectsAndJars();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean includesBinaries() {
      return fSearchScope.includesBinaries();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean includesClasspaths() {
      return fSearchScope.includesClasspaths();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setIncludesBinaries(boolean includesBinaries) {
      fSearchScope.setIncludesBinaries(includesBinaries);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setIncludesClasspaths(boolean includesClasspaths) {
      fSearchScope.setIncludesClasspaths(includesClasspaths);
    }
  }
  
  private class RandoopTestInputSelectionExtension extends TypeSelectionExtension {
    
    @Override
    public ITypeInfoFilterExtension getFilterExtension() {
      return new NoAbstractClassesOrInterfacesFilterExtension();
    }

    private class NoAbstractClassesOrInterfacesFilterExtension implements ITypeInfoFilterExtension {

      @Override
      public boolean select(ITypeInfoRequestor typeInfoRequestor) {
        int flags = typeInfoRequestor.getModifiers();
        if (Flags.isInterface(flags) || Flags.isAbstract(flags)) {
          return false;
        }
        
        return true;
      }
    }
  }
  
  @Override
  public void handleEvent(IOptionChangeEvent event) {
    if (IRandoopLaunchConfigurationConstants.ATTR_PROJECT_NAME.equals(event.getAttribute())) {
      fJavaProject = RandoopCoreUtil.getProjectFromName(event.getValue());

      boolean enabled = fJavaProject != null && fJavaProject.exists();
      fClassAddFromSources.setEnabled(enabled);
      fClassAddFromClasspaths.setEnabled(enabled);

      fTypeSelector.setJavaProject(fJavaProject);
      fResolveClasses.setEnabled(enabled && fTypeSelector.hasMissingClasses());
    }
  }
  
  /*
   * Convenience method to get the workspace root.
   */
  private static IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }

  @Override
  public void restoreDefaults() {
    fTypeTree.removeAll();
  }
  
}
