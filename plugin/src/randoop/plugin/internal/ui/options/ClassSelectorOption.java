package randoop.plugin.internal.ui.options;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.soap.MessageFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.dialogs.ITypeInfoFilterExtension;
import org.eclipse.jdt.ui.dialogs.ITypeInfoRequestor;
import org.eclipse.jdt.ui.dialogs.TypeSelectionExtension;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.SelectionDialog;
import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.MethodMnemonic;
import randoop.plugin.internal.core.RandoopCoreUtil;
import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.core.TypeMnemonic;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.ui.AdaptablePropertyTester;
import randoop.plugin.internal.ui.ClasspathLabelProvider;
import randoop.plugin.internal.ui.MessageUtil;

public class ClassSelectorOption extends Option implements IOptionChangeListener {
  private static Image IMG_ERROR = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJS_ERROR_TSK);
  private static Image IMG_ENUM = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_ENUM);
  private static Image IMG_CLASS = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);

  private static Image IMG_METHOD_PUBLIC = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PUBLIC);
  private static Image IMG_METHOD_PRIVATE = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PRIVATE);
  private static Image IMG_METHOD_PROTECTED = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PROTECTED);
  private static Image IMG_METHOD_DEFAULT = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_DEFAULT);
  
  private static Image IMG_PACKAGE_FRAGMENT = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
  
  private static final String DEFAULT_PACKAGE_DISPLAY_NAME = "(default package)";
  
  private IRunnableContext fRunnableContext;
  private Shell fShell;
  
  private TreeInput fTreeInput;
  private CheckboxTreeViewer fTypeTreeViewer;
  private HashSet<String> fDeletedTypeNodes;
  private Map<IType, List<String>> fCheckedMethodsByType;
  private LabelProvider fTreeLabelProvider;
  private ITreeContentProvider fTypeTreeContentProvider;
  
  private Button fClassAddFromSources;
  private Button fClassAddFromClasspaths;
  private Button fResolveClasses;
  private Button fSelectAll;
  private Button fSelectNone;
  private Button fClassRemove;
  private Button fIgnoreJUnitTestCases;
  private IJavaProject fJavaProject;
  
  private static class TreeInput {
    private List<TreeNode> fRoots;
  
    public TreeInput() {
      fRoots = new ArrayList<TreeNode>();
    }
  
    public TreeNode addRoot(Object object) {
      for (TreeNode root : fRoots) {
        if (root.getObject().equals(object)) {
          return root;
        }
      }
  
      TreeNode root = new TreeNode(null, object, false, false);
      fRoots.add(root);
      return root;
    }
    
    public void removeRoot(TreeNode node) {
      fRoots.remove(node);
    }
  
    public TreeNode[] getRoots() {
      return (TreeNode[]) fRoots.toArray(new TreeNode[fRoots.size()]);
    }

    public boolean hasMissingClasses(IJavaProject currentProject) {
      for (TreeNode packageNode : getRoots()) {
        for (TreeNode classNode : packageNode.getChildren()) {
          if (!((TypeMnemonic) classNode.getObject()).getJavaProject().equals(currentProject)) {
            return true;
          }
        }
      }
      
      return false;
    }
    
  }


  private static class TreeNode {
    private TreeNode fParent;
    private List<TreeNode> fChildren;
    private Object fObject;
    
    private boolean fIsChecked;
    private boolean fIsGrayed;
    
    private TreeNode(TreeNode parent, Object object, boolean checkedState, boolean grayedState) {
      Assert.isLegal(object != null);
      fParent = parent;
      fObject = object;
      
      fIsChecked = checkedState;
      fIsGrayed = grayedState;
      
      fChildren = new ArrayList<TreeNode>();
      
      updateRelatives();
    }
    
    public TreeNode addChild(Object object, boolean checkedState, boolean grayedState) {
      TreeNode node = new TreeNode(this, object, checkedState, grayedState);
      fChildren.add(node);
  
      node.updateRelatives();
  
      return node;
    }
    
    public void setObject(Object object) {
      fObject = object;
    }
    
    public Object getObject() {
      return fObject;
    }
    
    public TreeNode getParent() {
      return fParent;
    }
  
    public void setChecked(boolean state) {
      fIsChecked = state;
    }
  
    public void setGrayed(boolean state) {
      fIsGrayed = state;
    }
    
    public boolean isChecked() {
      return fIsChecked;
    }
    
    public boolean isGrayed() {
      return fIsGrayed;
    }
    
    public boolean hasChildren() {
      return !fChildren.isEmpty();
    }
    
    public TreeNode[] getChildren() {
      return (TreeNode[]) fChildren.toArray(new TreeNode[fChildren.size()]);
    }
    
    public void removeAllChildren() {
      fChildren = new ArrayList<TreeNode>();
    }
    
    @Override
    public int hashCode() {
      return fObject.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof TreeNode) {
        TreeNode otherNode = (TreeNode) obj;
        boolean objectsEqual = getObject().equals(otherNode.getObject());
        boolean parentsEqual;
        if (getParent() == null) {
          parentsEqual = otherNode.getParent() == null;
        } else {
          parentsEqual = getParent().equals(otherNode.getParent());
        }
        return objectsEqual && parentsEqual;
      }
      return false;
    }
    
    public void delete() {
      if (fParent != null) {
        fParent.fChildren.remove(this);
        updateParent();
      }
    }
    
    public void updateRelatives() {
      updateParent();
      updateChildren();
    }
  
    private void updateParent() {
      if (fParent != null) {
        TreeNode[] siblings = fParent.getChildren();
        
        boolean allChecked = true;
        boolean noneChecked = true;
        for (TreeNode sibling : siblings) {
          allChecked &= sibling.isChecked() && !sibling.isGrayed();
          noneChecked &= !sibling.isChecked();
        }
        
        if (allChecked) {
          fParent.setChecked(true);
          fParent.setGrayed(false);
        } else if (noneChecked) {
          fParent.setChecked(false);
          fParent.setGrayed(false);
        } else {
          fParent.setChecked(true);
          fParent.setGrayed(true);
        }
        
        fParent.updateParent();
      }
    }
  
    private void updateChildren() {
      TreeNode[] children = getChildren();
  
      for (TreeNode child : children) {
        if (!isGrayed()) {
          child.setGrayed(false);
          child.setChecked(isChecked());
          child.updateChildren();
        }
      }
    }
    
  }

  private class TreeLabelProvider extends LabelProvider {
    @Override
    public Image getImage(Object element) {
      TreeNode node = (TreeNode) element;
      Object obj = node.getObject();
      if (obj instanceof String) {
        return IMG_PACKAGE_FRAGMENT;
      } else if (obj instanceof TypeMnemonic) {
        TypeMnemonic typeMnemonic = ((TypeMnemonic) obj);
        if (!typeMnemonic.exists() || !typeMnemonic.getJavaProject().equals(fJavaProject)) {
          return IMG_ERROR;
        } else {
          return getImageForType(typeMnemonic.getType());
        }
      } else if (obj instanceof MethodMnemonic) {
        if (getImage(node.getParent()).equals(IMG_ERROR)) {
          return IMG_ERROR;
        } else {
          return getImageForMethod(((MethodMnemonic) obj).getMethod());
        }
      }
  
      return null;
    }
    
    private Image getImageForType(IType type) {
      try {
        if (type != null && type.exists()) {
          if (type.isEnum()) {
            return IMG_ENUM;
          } else if (type.isClass()) {
            return IMG_CLASS;
          }
        }
      } catch (JavaModelException e) {
        RandoopPlugin.log(e);
      }
      return IMG_ERROR;
    }
  
    private Image getImageForMethod(IMethod method) {
      if (method == null || !method.exists()) {
        return IMG_ERROR;
      }
  
      try {
        int flags = method.getFlags();
        if (Flags.isPublic(flags)) {
          return IMG_METHOD_PUBLIC;
        } else if (Flags.isPrivate(flags)) {
          return IMG_METHOD_PRIVATE;
        } else if (Flags.isProtected(flags)) {
          return IMG_METHOD_PROTECTED;
        } else {
          return IMG_METHOD_DEFAULT;
        }
      } catch (JavaModelException e) {
        RandoopPlugin.log(e);
      }
      return null;
    }
    
    @Override
    public String getText(Object element) {
      Object obj = ((TreeNode) element).getObject();
  
      if (obj instanceof String) {
        String pfname = (String) obj;
        return pfname.isEmpty() ? DEFAULT_PACKAGE_DISPLAY_NAME : pfname;
      } else if (obj instanceof TypeMnemonic) {
        return RandoopCoreUtil.getClassName(((TypeMnemonic) obj).getFullyQualifiedName());
      } else if (obj instanceof MethodMnemonic) {
        return getReadable((MethodMnemonic) obj);
      }
  
      return obj.toString();
    }
    
    private String getReadable(MethodMnemonic methodMnemonic) {
      String methodSignature = methodMnemonic.getMethodSignature();
  
      StringBuilder readableMethod = new StringBuilder();
      if (!methodMnemonic.isConstructor()) {
        String returnSig = Signature.getReturnType(methodSignature);
        String returnFQTypename = Signature.toString(returnSig);
        readableMethod.append(RandoopCoreUtil.getClassName(returnFQTypename));
        readableMethod.append(' ');
      }
      readableMethod.append(methodMnemonic.getMethodName());
      readableMethod.append('(');
      String[] parameters = Signature.getParameterTypes(methodSignature);
      for (int i = 0; i < parameters.length; i++) {
        String fqname = Signature.toString(parameters[i]);
        readableMethod.append(RandoopCoreUtil.getClassName(fqname));
  
        if (i + 1 < parameters.length) {
          readableMethod.append(", "); //$NON-NLS-1$
        }
      }
      readableMethod.append(')');
  
      return readableMethod.toString();
    }
    
  }


  private class TreeContentProvider implements ITreeContentProvider {
    
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
    
    @Override
    public void dispose() {
    }
  
    @Override
    public boolean hasChildren(Object element) {
      TreeNode node = (TreeNode) element;
      if (node.getObject() instanceof TypeMnemonic) {
        try {
          IType type = ((TypeMnemonic) node.getObject()).getType();
          if (type != null) {
            return type.getMethods().length != 0;
          }
        } catch (JavaModelException e) {
          RandoopPlugin.log(e);
        }
      }
      return node.hasChildren();
    }
  
    @Override
    public Object getParent(Object element) {
      return ((TreeNode) element).getParent();
    }
    
    @Override
    public Object[] getElements(Object inputElement) {
      return fTreeInput.getRoots();
    }
  
    @Override
    public Object[] getChildren(Object parentElement) {
      TreeNode typeNode = (TreeNode) parentElement;
      
      if (typeNode.getObject() instanceof TypeMnemonic) {
        if (!typeNode.hasChildren()) {
          try {
            final boolean typeChecked = typeNode.isChecked();
            final boolean typeGrayed = typeNode.isGrayed();
            
            boolean allChecked = true;
            boolean noneChecked = true;
            
            IType type = ((TypeMnemonic) typeNode.getObject()).getType();
  
            if (type != null){
              List<String> checkedMethods = fCheckedMethodsByType.get(type);
              fCheckedMethodsByType.remove(type);
  
              IMethod[] methods = type.getMethods();
              for (IMethod method : methods) {
                if (AdaptablePropertyTester.isTestable(method)) {
                  MethodMnemonic methodMnemonic = new MethodMnemonic(method);

                  boolean methodChecked;
                  if (typeChecked && typeGrayed) {
                    if (checkedMethods != null) {
                      methodChecked = checkedMethods.contains(methodMnemonic.toString());
                    } else {
                      methodChecked = false;
                    }
                  } else {
                    methodChecked = typeChecked;
                  }
                  
                  allChecked &= methodChecked;
                  noneChecked &= !methodChecked;
                  
                  TreeNode methodNode = typeNode.addChild(methodMnemonic, methodChecked, false);
                  methodNode.updateRelatives();
                }
              }
              
              if (allChecked) {
                typeNode.setChecked(true);
                typeNode.setGrayed(false);
              } else if (noneChecked) {
                typeNode.setChecked(false);
                typeNode.setGrayed(false);
              } else {
                typeNode.setChecked(true);
                typeNode.setGrayed(true);
              }
              
              typeNode.updateRelatives();
              return typeNode.getChildren();
            }
          } catch (JavaModelException e) {
            RandoopPlugin.log(e);
          }
        }
      }
      return typeNode.getChildren();
    }
  }

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

    fDeletedTypeNodes = new HashSet<String>();
    
    fTreeLabelProvider = new TreeLabelProvider();
    fTypeTreeContentProvider = new TreeContentProvider();

    fTypeTreeViewer = new CheckboxTreeViewer(leftcomp, SWT.MULTI | SWT.BORDER);
    fTypeTreeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    fTypeTreeViewer.setLabelProvider(fTreeLabelProvider);
    fTypeTreeViewer.setContentProvider(fTypeTreeContentProvider);
    fTypeTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        ITreeSelection selection = (ITreeSelection) event.getSelection();

        Iterator<?> it = selection.iterator();
        while (it.hasNext()) {
          TreeNode node = (TreeNode) it.next();
          Object obj = node.getObject();
          if (obj instanceof TypeMnemonic || obj instanceof String) {
            fClassRemove.setEnabled(true);
            return;
          }
        }
        fClassRemove.setEnabled(false);
      }
    });
    
    fTypeTreeViewer.setCheckStateProvider(new ICheckStateProvider() {

      @Override
      public boolean isChecked(Object element) {
        TreeNode node = ((TreeNode) element);
        return node.isChecked();
      }

      @Override
      public boolean isGrayed(Object element) {
        TreeNode node = ((TreeNode) element);
        return node.isGrayed();
      }

    });
    
    fTypeTreeViewer.addCheckStateListener(new ICheckStateListener() {

      @Override
      public void checkStateChanged(CheckStateChangedEvent event) {
        TreeNode node = (TreeNode) event.getElement();
        node.setGrayed(false);
        node.setChecked(event.getChecked());
        node.updateRelatives();
        
        fTypeTreeViewer.refresh();
        listener.widgetSelected(null);
      }
    });
    
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
          IClasspathEntry[] classpathEntries = chooseClasspathEntry();
          if (classpathEntries != null) {
            ArrayList<IJavaElement> elements = new ArrayList<IJavaElement>();
            for (IClasspathEntry classpathEntry : classpathEntries) {
              IPackageFragmentRoot[] pfrs = RandoopCoreUtil.findPackageFragmentRoots(fJavaProject, classpathEntry);
              elements.addAll(Arrays.asList(pfrs));
            }
            
            IJavaElement[] elementArray = (IJavaElement[]) elements.toArray(new IJavaElement[elements.size()]);
            IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(elementArray);
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
              resolveMissingClasses();
            } catch (JavaModelException jme) {
              RandoopPlugin.log(jme);
            }
          }
        }
      });
      fResolveClasses.setEnabled(fJavaProject != null && fJavaProject.exists());
          //&& fTypeSelector.hasMissingClasses());
      fResolveClasses.addSelectionListener(listener);
    }
    
    // Create a spacer
    SWTFactory.createLabel(rightcomp, "", 1);
    fSelectAll = SWTFactory.createPushButton(rightcomp, "Select &All", null);
    fSelectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        for (TreeNode node : fTreeInput.getRoots()) {
          node.setGrayed(false);
          node.setChecked(true);
          node.updateRelatives();
        }
        fTypeTreeViewer.refresh();
      }
    });
    fSelectAll.addSelectionListener(listener);
    
    fSelectNone = SWTFactory.createPushButton(rightcomp, "Select Non&e", null);
    fSelectNone.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        for (TreeNode node : fTreeInput.getRoots()) {
          node.setGrayed(false);
          node.setChecked(false);
          node.updateRelatives();
        }
        fTypeTreeViewer.refresh();
      }
    });
    fSelectNone.addSelectionListener(listener);
    
    fClassRemove = SWTFactory.createPushButton(rightcomp, "&Remove", null);
    fClassRemove.addSelectionListener(new SelectionAdapter() {

      void remove(TreeNode node) throws JavaModelException {
        Object obj = node.getObject();

        if (obj instanceof TypeMnemonic) {
          fDeletedTypeNodes.add(obj.toString());

          node.delete();
          if (!node.getParent().hasChildren()) {
            remove(node.getParent());
          }
        } else if (obj instanceof String) {
          for (TreeNode child : node.getChildren()) {
            remove(child);
          }

          fTreeInput.removeRoot(node);
        }
      }
      
      @Override
      public void widgetSelected(SelectionEvent e) {
        ITreeSelection selection = (ITreeSelection) fTypeTreeViewer.getSelection();

        try {
          Iterator<?> it = selection.iterator();
          while (it.hasNext()) {
            remove((TreeNode) it.next());
          }
        } catch (JavaModelException jme) {
          RandoopPlugin.log(jme);
        }

        fTypeTreeViewer.refresh();
        fResolveClasses.setEnabled(fJavaProject != null && fTreeInput.hasMissingClasses(fJavaProject));
        fClassRemove.setEnabled(false);
      }
    });
    fClassRemove.addSelectionListener(listener);
    fClassRemove.setEnabled(false);
    
    fIgnoreJUnitTestCases = SWTFactory.createCheckButton(leftcomp,
        "Ignore JUni&t tests cases when searching for class inputs", null, true, 2);
    gd = (GridData) fIgnoreJUnitTestCases.getLayoutData();
    gd.horizontalIndent = 5;
    fIgnoreJUnitTestCases.setLayoutData(gd);
  }

  public ClassSelectorOption(Composite parent, IRunnableContext runnableContext,
      final SelectionListener listener, IJavaProject javaProject, List<TypeMnemonic> checkedTypes,
      List<TypeMnemonic> grayedTypes,
      Map<IType, List<String>> selectedMethodsByDeclaringTypes) {

    this(parent, runnableContext, listener, false);

    fJavaProject = javaProject;

    fTreeInput = new TreeInput();
    fTypeTreeViewer.setInput(fTreeInput);
    
    for (TypeMnemonic typeMnemonic : checkedTypes) {
      String pfname = RandoopCoreUtil.getPackageName(typeMnemonic.getFullyQualifiedName());
      TreeNode pfNode = fTreeInput.addRoot(pfname);

      pfNode.addChild(typeMnemonic, true, grayedTypes.contains(typeMnemonic));
    }
    fCheckedMethodsByType = selectedMethodsByDeclaringTypes;
    
  }
  
  private void handleSearchButtonSelected(IJavaSearchScope searchScope) {
    try {
      boolean ignoreJUnit = fIgnoreJUnitTestCases.getSelection();
      IJavaSearchScope junitSearchScope = new FilterJUnitSearchScope(searchScope, ignoreJUnit);

      SelectionDialog dialog = JavaUI.createTypeDialog(fShell, fRunnableContext, junitSearchScope,
          IJavaElementSearchConstants.CONSIDER_CLASSES_AND_ENUMS, true, "", //$NON-NLS-1$
          new RandoopTestInputSelectionExtension());
      dialog.setTitle("Add Classes");
      dialog.setMessage("Enter type name prefix or pattern (*, ?, or camel case):");
      dialog.open();
      
      // Add all of the types to the type selector
      Object[] results = dialog.getResult();
      if (results != null && results.length > 0) {
        
        for (Object element : results) {
          if (element instanceof IType) {
            IType type = (IType) element;
            if (type != null) {
              
              String pfname = type.getPackageFragment().getElementName();
              TreeNode packageNode = fTreeInput.addRoot(pfname);
              
              boolean typeAlreadyInTree = false;
              for (TreeNode node : packageNode.getChildren()) {
                if (node.getObject().equals(type)) {
                  typeAlreadyInTree = true;
                  break;
                }
              }

              if (!typeAlreadyInTree) {
                // Remove this from the list of deletes classes
                TypeMnemonic typeMnemonic = new TypeMnemonic(type).reassign(fJavaProject);
                fDeletedTypeNodes.remove(typeMnemonic.toString());
                
                packageNode.addChild(typeMnemonic, true, false);

                fTypeTreeViewer.refresh();
                if (results.length < 3) {
                  fTypeTreeViewer.setExpandedState(packageNode, true);
                }
              }
            }
          }
        }
        
        fTypeTreeViewer.refresh();
      }
    } catch (JavaModelException jme) {
      RandoopPlugin.log(jme);
    }
  }
  
  @Override
  public IStatus canSave() {
    if (fRunnableContext == null || fShell == null || fTypeTreeViewer == null || fTreeInput == null
        || fClassAddFromSources == null || fClassAddFromClasspaths == null || fSelectAll == null
        || fSelectNone == null || fClassRemove == null) {

      return StatusFactory.ERROR_STATUS;
    }

    return StatusFactory.OK_STATUS;
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
  @Override
  public IStatus isValid(ILaunchConfiguration config) {
    try {
      new RandoopArgumentCollector(config, getWorkspaceRoot());
    } catch (JavaModelException e) {
      return StatusFactory.createErrorStatus(e.getMessage());
    } catch (AssertionFailedException e) {
      return StatusFactory.createErrorStatus(e.getMessage().substring(18));
    }

    return StatusFactory.OK_STATUS;
  }

  @Override
  public void initializeFrom(ILaunchConfiguration config) {
    fTreeInput = new TreeInput();
    fTypeTreeViewer.setInput(fTreeInput);
    
    String projectName = RandoopArgumentCollector.getProjectName(config);
    fJavaProject = RandoopCoreUtil.getProjectFromName(projectName);

    fCheckedMethodsByType = new HashMap<IType, List<String>>();
    
    List<String> availableTypes = RandoopArgumentCollector.getAvailableTypes(config);
    List<String> grayedTypes = RandoopArgumentCollector.getGrayedTypes(config);
    List<String> checkedTypes = RandoopArgumentCollector.getCheckedTypes(config);

    for (String typeString : availableTypes) {
      TypeMnemonic typeMnemonic = new TypeMnemonic(typeString, getWorkspaceRoot());
      boolean typeIsChecked = checkedTypes.contains(typeString);
      boolean typeIsGrayed = grayedTypes.contains(typeString);
      
      String pfname = RandoopCoreUtil.getPackageName(typeMnemonic.getFullyQualifiedName());
      TreeNode pfNode = fTreeInput.addRoot(pfname);
      fTypeTreeViewer.refresh();
      
      List<String> checkedMethods = RandoopArgumentCollector.getCheckedMethods(config, typeString);
      if (typeMnemonic.getType() != null) {
        pfNode.addChild(typeMnemonic, typeIsChecked, typeIsGrayed);
        
        fCheckedMethodsByType.put(typeMnemonic.getType(), checkedMethods);
      } else {
        TreeNode typeNode = pfNode.addChild(typeMnemonic, typeIsChecked, typeIsGrayed);

        List<String> availableMethods = RandoopArgumentCollector.getAvailableMethods(config, typeString);

        for (String methodString : availableMethods) {
          MethodMnemonic methodMnemonic = new MethodMnemonic(methodString);
          
          
          if (typeIsChecked && !typeIsGrayed) {
            typeNode.addChild(methodMnemonic, true, false);
          } else {
            boolean methodIsChecked = checkedMethods.contains(methodString);
            boolean methodIsGrayed = false;

            typeNode.addChild(methodMnemonic, methodIsChecked, methodIsGrayed);
          }
        }
      }
    }
    
    fTypeTreeViewer.refresh();
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    // Ugly hack to determine if the apply button was pressed.
    // boolean applyPressed = false;
    // for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
    // applyPressed = ste.getMethodName().equals("handleApplyPressed");
    // if (applyPressed) {
    // break;
    // }
    // }
    
    ITreeContentProvider prov = (ITreeContentProvider) fTypeTreeViewer.getContentProvider();
    TreeInput input = (TreeInput) fTypeTreeViewer.getInput();
    fTypeTreeViewer.refresh();
    
    for (String mnemonic : fDeletedTypeNodes) {
      RandoopArgumentCollector.deleteAvailableMethods(config, mnemonic);
      RandoopArgumentCollector.deleteCheckedMethods(config, mnemonic);
    }

    List<String> availableTypes = new ArrayList<String>();
    List<String> grayedTypes = new ArrayList<String>();
    List<String> checkedTypes = new ArrayList<String>();

    for (TreeNode packageNode : input.getRoots()) {
      for (Object typeObject : prov.getChildren(packageNode)) {
        TreeNode typeNode = (TreeNode) typeObject;

        String typeMnemonic = null;
        if (typeNode.getObject() instanceof TypeMnemonic) {
          typeMnemonic = typeNode.getObject().toString();
        }
        availableTypes.add(typeMnemonic);

        if (typeNode.isChecked()) {
          checkedTypes.add(typeMnemonic);

          if (typeNode.isGrayed()) {
            grayedTypes.add(typeMnemonic);

            List<String> availableMethods = new ArrayList<String>();
            List<String> checkedMethods = new ArrayList<String>();
            for (Object methodObject : prov.getChildren(typeObject)) {
              TreeNode methodNode = (TreeNode) methodObject;
              String methodMnemonicString = methodNode.getObject().toString();

              availableMethods.add(methodMnemonicString);
              if (methodNode.isChecked()) {
                checkedMethods.add(methodMnemonicString);
              }
            }

            RandoopArgumentCollector.setAvailableMethods(config, typeMnemonic, availableMethods);
            RandoopArgumentCollector.setCheckedMethods(config, typeMnemonic, checkedMethods);
          }
        }
      }
    }

    RandoopArgumentCollector.setAvailableTypes(config, availableTypes);
    RandoopArgumentCollector.setGrayedTypes(config, grayedTypes);
    RandoopArgumentCollector.setCheckedTypes(config, checkedTypes);
    
    fTypeTreeViewer.refresh();
  }
  
  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    writeDefaults(config);
  }
  
  public static void writeDefaults(ILaunchConfigurationWorkingCopy config) {
    RandoopArgumentCollector.restoreAvailableTypes(config);
    RandoopArgumentCollector.restoreCheckedTypes(config);
    
    List<String> availableTypes = RandoopArgumentCollector.getAvailableTypes(config);
    for (String typeMnemonic : availableTypes) {
      RandoopArgumentCollector.deleteAvailableMethods(config, typeMnemonic);
      RandoopArgumentCollector.deleteCheckedMethods(config, typeMnemonic);
    }
  }

  private IClasspathEntry[] chooseClasspathEntry() throws JavaModelException {
    ILabelProvider labelProvider = new ClasspathLabelProvider(fJavaProject);
    ElementListSelectionDialog dialog = new ElementListSelectionDialog(fShell, labelProvider);
    dialog.setTitle("Classpath Selection");
    dialog.setMessage("Select a classpath to constrain your search.");

    IClasspathEntry[] classpaths = fJavaProject.getRawClasspath();
    dialog.setElements(classpaths);
    dialog.setMultipleSelection(true);

    if (dialog.open() == Window.OK) {
      List<IClasspathEntry> cpentries = new ArrayList<IClasspathEntry>();
      for (Object obj : dialog.getResult()) {
        if (obj instanceof IClasspathEntry) {
          cpentries.add((IClasspathEntry) obj);
        }
      }
      return (IClasspathEntry[]) cpentries.toArray(new IClasspathEntry[cpentries.size()]);
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
        
        ArrayList<IType> types = new ArrayList<IType>();

        int jarFileSeparatorIndex = resourcePath.indexOf(JAR_FILE_ENTRY_SEPARATOR);
        if (jarFileSeparatorIndex == -1) {
          IPath filePath = new Path(resourcePath);
          
          IFile file = root.getFile(filePath);

          if (JavaConventions.validateCompilationUnitName(file.getName(),
              IConstants.DEFAULT_SOURCE_LEVEL, IConstants.DEFAULT_COMPLIANCE_LEVEL).isOK()) {
            ICompilationUnit cu = (ICompilationUnit) JavaCore.create(file, fJavaProject);
            collectTypes(cu, types);
          } else if (JavaConventions.validateClassFileName(file.getName(),
              IConstants.DEFAULT_SOURCE_LEVEL, IConstants.DEFAULT_COMPLIANCE_LEVEL).isOK()) {
            IClassFile cf = (IClassFile) JavaCore.create(file, fJavaProject);
            collectTypes(cf, types);
          } else {
            RandoopPlugin.log(StatusFactory.createWarningStatus("Unable to get class files or compilation unit from " + file)); //$NON-NLS-1$
          }
        } else {
          IPath filePath = new Path(resourcePath.substring(0, jarFileSeparatorIndex));
          String subFilePath = resourcePath.substring(jarFileSeparatorIndex + 1);
          
          // TODO: How do you tell if resourcesPath is relative to the device or workspace?
          // This could cause a rare error if the user happened to place a Java
          // archive in the workspace that has a path relative to the workspace
          // that is equivalent to a path to another Java archive on the file system.
          

          IPackageFragmentRoot pfr;
          // First, search for the IPackageFragmentRoot in the workspace
          IFile f = root.getFile(filePath);
          IJavaElement element = JavaCore.create(f, fJavaProject);
          if (element != null)
            pfr = (IPackageFragmentRoot) element;
          else
            // Next, search for the IPackageFragmentRoot in the file system
            pfr = fJavaProject.getPackageFragmentRoot(filePath.toOSString());
          
          try {
            pfr.open(null);

            jarFileSeparatorIndex = subFilePath.lastIndexOf(IPath.SEPARATOR);
            String packageName = subFilePath.substring(0, jarFileSeparatorIndex).replace(IPath.SEPARATOR, '.');
            String fileName = subFilePath.substring(jarFileSeparatorIndex + 1);

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
          } catch (JavaModelException e) {
            RandoopPlugin.log(e);
          }
        }
        
        if (types.isEmpty()) {
          RandoopPlugin.log(StatusFactory.createWarningStatus("No classes found in " + resourcePath));
          return false;
        }
        
        for (IType type : types) {
          if (RandoopCoreUtil.isValidTestInput(type, fIgnoreJUnit)) {
            return true;
          }
        }
        return false;
      }
      return false;
    }
    
    /*
     * Helper method to collect the ITypes from an ICompilationUnit or IClassFile
     */
    private void collectTypes(IClassFile cf, List<IType> types) {
      if (cf == null || !cf.exists())
        return;

      try {
        cf.open(null);

        IType type = cf.getType();
        if (type.exists()) {
          types.add(type);
        }
      } catch (JavaModelException e) {
        RandoopPlugin.log(e);
      }
    }
    
    private void collectTypes(ICompilationUnit cu, List<IType> types) {
      if (cu == null || !cu.exists())
        return;

      try {
        cu.open(null);

        for (IType type : cu.getAllTypes()) {
          if (type.exists()) {
            types.add(type);
          }
        }
      } catch (JavaModelException e) {
        RandoopPlugin.log(e);
      }
    }
    
    @Override
    public boolean encloses(IJavaElement element) {
      if (fSearchScope.encloses(element)) {
        if (element instanceof IType) {
          IType type = (IType) element;
          return RandoopCoreUtil.isValidTestInput(type, fIgnoreJUnit);
        }
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
    ITypeInfoFilterExtension fRandoopClassInputFilterExtension;
    ISelectionStatusValidator fRandoopClassInputSelectionStatusValidator;
    
    public RandoopTestInputSelectionExtension() {
      fRandoopClassInputFilterExtension = new RandoopClassInputFilterExtension();
      fRandoopClassInputSelectionStatusValidator = new RandoopClassInputSelectionStatusValidator();
    }
    
    @Override
    public ITypeInfoFilterExtension getFilterExtension() {
      return fRandoopClassInputFilterExtension;
    }

    @Override
    public ISelectionStatusValidator getSelectionValidator() {
      return fRandoopClassInputSelectionStatusValidator;
    }
    
    private class RandoopClassInputFilterExtension implements ITypeInfoFilterExtension {

      @Override
      public boolean select(ITypeInfoRequestor typeInfoRequestor) {
        int flags = typeInfoRequestor.getModifiers();
        if (Flags.isInterface(flags) || Flags.isAbstract(flags)) {
          return false;
        }
        
        return true;
      }
    }
    
    private class RandoopClassInputSelectionStatusValidator implements ISelectionStatusValidator {

      @Override
      public IStatus validate(Object[] selection) {
        for (Object obj : selection) {
          try {
            if (obj instanceof IType) {
              IType type = (IType) obj;
              int flags = type.getFlags();
              if (type.isInterface()) {
                StatusFactory.createErrorStatus(MessageFormat.format("'{0}' is an interface", type.getElementName()));
              } else if (Flags.isAbstract(flags)) {
                StatusFactory.createErrorStatus(MessageFormat.format("'{0}' is abstract", type.getElementName()));
              } else if (!Flags.isPublic(flags)) {
                StatusFactory.createErrorStatus(MessageFormat.format("'{0}' is not public", type.getElementName()));
              }
            } else {
              return StatusFactory.createErrorStatus("One of the selected elements is not a Java class or enum");
            }
          } catch (JavaModelException e) {
            RandoopPlugin.log(e, "Error when validating selected elements in type dialog"); //$NON-NLS-1$
          }
        }
        return StatusFactory.OK_STATUS;
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

      boolean hasMissingClasses = setJavaProject(fJavaProject);
      fResolveClasses.setEnabled(fJavaProject != null && hasMissingClasses);
    }
  }
  
  private boolean setJavaProject(IJavaProject javaProject) {
    if (fTreeInput == null)
      return false;
    
    fJavaProject = javaProject;
    boolean hasMissingClasses = false;
    
    if (fJavaProject == null) {
      hasMissingClasses = true;
    } else {
      for (TreeNode pfNode : fTreeInput.getRoots()) {
        for (TreeNode typeNode : pfNode.getChildren()) {
          TypeMnemonic oldMnemonic = (TypeMnemonic) typeNode.getObject();
          
          if (!fJavaProject.equals(oldMnemonic.getJavaProject())) {
            TypeMnemonic newMnemonic = oldMnemonic.reassign(fJavaProject);

            // If newMnemonic is not null, the IType was found in a classpath
            // entry of the new Java project
            if (newMnemonic == null || !newMnemonic.exists()) {
              hasMissingClasses = true;
            } else {
              // Update the mnemonic for this TreeItem
              typeNode.setObject(newMnemonic);
            }
          }
        }
      }
    }
    
    fTypeTreeViewer.refresh();
    return hasMissingClasses;
  }
  
  public void resolveMissingClasses() throws JavaModelException {
    if (fJavaProject == null)
      return;
    
    // Create a mapping to remember which methods are checked in which class
//    Map<String, List<MethodMnemonic>> checkedMethodsByFQTypeName = new HashMap<String, List<MethodMnemonic>>();
//    List<String> methodMnemonics = getCheckedMethods();
//    for (String methodMnemonicString : methodMnemonics) {
//      MethodMnemonic methodMnemonic = new MethodMnemonic(methodMnemonicString, getWorkspaceRoot());
//
//      String fqname = methodMnemonic.getDeclaringTypeMnemonic().toString();
//      List<MethodMnemonic> methods = checkedMethodsByFQTypeName.get(fqname);
//      if (methods == null) {
//        methods = new ArrayList<MethodMnemonic>();
//        checkedMethodsByFQTypeName.put(fqname, methods);
//      }
//      methods.add(methodMnemonic);
//    }

    for (TreeNode packageItem : fTreeInput.getRoots()) {
      for (TreeNode classItem : packageItem.getChildren()) {

        TypeMnemonic oldTypeMnemonic = (TypeMnemonic) classItem.getObject();
        IType type = oldTypeMnemonic.getType();
        if (type == null || !fJavaProject.equals(type.getJavaProject())) {
          type = fJavaProject.findType(oldTypeMnemonic.getFullyQualifiedName(), (IProgressMonitor) null);
          if (type != null) {
            TypeMnemonic newTypeMnemonic = new TypeMnemonic(type);
            fDeletedTypeNodes.add(oldTypeMnemonic.toString());
            
            List<String> checkedMethods;
            if (classItem.hasChildren()) {
              checkedMethods = new ArrayList<String>();
              for (TreeNode methodItem : classItem.getChildren()) {
                checkedMethods.add(((MethodMnemonic) methodItem.getObject()).toString());
              }
            } else {
              // Otherwise, the item probably hasn't been expanded. Move the
              // list of checked methods from the old key to the new one
              checkedMethods = fCheckedMethodsByType.get(oldTypeMnemonic.getType());
            }
            fCheckedMethodsByType.put(newTypeMnemonic.getType(), checkedMethods);
            fCheckedMethodsByType.remove(oldTypeMnemonic.getType());

            classItem.setObject(newTypeMnemonic);
            classItem.removeAllChildren();
          }
        }
      }
    }
    
    fTypeTreeViewer.refresh();
  }
  
  /*
   * Convenience method to get the workspace root.
   */
  private static IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }

  @Override
  public void restoreDefaults() {
    if (fTreeInput != null) {
      for (TreeNode node : fTreeInput.getRoots()) {
        fTreeInput.removeRoot(node);
      }
    }
  }

}
