package randoop.plugin.internal.ui.options;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.MethodMnemonic;
import randoop.plugin.internal.core.RandoopCoreUtil;
import randoop.plugin.internal.core.TypeMnemonic;

/**
 * The TypeSelector class is used to manage a Tree object so that the user can
 * browse and select or unselect certain IJavaElements. The class is
 * specifically meant for displaying a list of ITypes that can be expanded to
 * show the methods that are contained.
 */
public class ClassSelector {
  private static Image IMG_ERROR = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJS_ERROR_TSK);
  private static Image IMG_ENUM = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_ENUM);
  private static Image IMG_CLASS = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);

  private static Image IMG_METHOD_PUBLIC = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PUBLIC);
  private static Image IMG_METHOD_PRIVATE = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PRIVATE);
  private static Image IMG_METHOD_PROTECTED = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PROTECTED);
  private static Image IMG_METHOD_DEFAULT = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_DEFAULT);
  
  private static final String ID_ELEMENT_TYPE = "ClassSelector.ID_ELEMENT_TYPE"; //$NON-NLS-1$
  
  private static final String ID_MNEMONIC = "ClassSelector.ID_MNEMONIC"; //$NON-NLS-1$

  private static final String DEFAULT_PACKAGE_DISPLAY_NAME = "(default package)";
  
  private Tree fTypeTree;
  private IJavaProject fJavaProject;

  /**
   * 
   * IMPORTANT: emptyTree must use the SWT.CHECK style bit.
   * 
   * @param classTree
   *          tree that can be used to display elements
   * @param selectedMethods 
   * @param selectedTypes 
   * @param availableTypes 
   */
  ClassSelector(Tree classTree) {
    Assert.isLegal(classTree != null, "The Tree cannot be null"); //$NON-NLS-1$
//    Assert.isLegal(classTree.getItemCount() == 0, "The Tree must be empty"); //$NON-NLS-1$
    Assert.isLegal((classTree.getStyle() & SWT.CHECK) != 0, "The Tree must use the SWT.CHECK style"); //$NON-NLS-1$

    fTypeTree = classTree;
    fTypeTree.removeAll();
    fJavaProject = null;
    
    // Add a selection listener that will check/uncheck descendants of a node
    // when it is checked/unchecked as well as gray some of its ancestors as
    // needed.
    fTypeTree.addListener(SWT.Selection, new Listener() {
      
      @Override
      public void handleEvent(Event event) {
        if (event.detail == SWT.CHECK) {
          TreeItem item = (TreeItem) event.item;
          updateTree(item);
        }
      }
    });
  }

  /**
   * 
   * IMPORTANT: emptyTree must use the SWT.CHECK style bit.
   * 
   * @param classTree
   *          empty tree that can be used to add
   */
  ClassSelector(Tree classTree, IJavaProject javaProject, List<String> availableTypes,
      List<String> selectedTypes, List<String> availableMethods, List<String> selectedMethods) {

    this(classTree);

    Assert.isLegal(availableTypes != null);
    Assert.isLegal(selectedTypes != null);
    Assert.isLegal(selectedMethods != null);
    Assert.isLegal(availableTypes.size() >= selectedTypes.size(), "There must be more available types than selected ones"); //$NON-NLS-1$
    for (String s : selectedTypes) {
      Assert.isLegal(availableTypes.contains(s), "Each selected type must also be an available one"); //$NON-NLS-1$
    }
    
    if (javaProject != null) {
      Assert.isLegal(javaProject.exists(), "Java project must exist"); //$NON-NLS-1$

      try {
        javaProject.open(new NullProgressMonitor());
      } catch (JavaModelException e) {
        Assert.isLegal(javaProject.isOpen(), "Java project could not be opened"); //$NON-NLS-1$
      }
    }
    
    fJavaProject = javaProject;

    IWorkspaceRoot root = getWorkspaceRoot();
    
    // Create a mapping between type mnemonics and method mnemonics. Each type
    // has a list of methods that is stored in the map.
    HashMap<String, List<String>> methodsByType = new HashMap<String, List<String>>();
    for(String mnemonic : availableMethods) {
      MethodMnemonic methodMnemonic = new MethodMnemonic(mnemonic);
      
      String fqtypename = methodMnemonic.getDeclaringTypeMnemonic().getFullyQualifiedName();
      List<String> list = methodsByType.get(fqtypename);
      if(list == null) {
        list = new ArrayList<String>();
        methodsByType.put(fqtypename, list);
      }
      list.add(mnemonic);
    }

    // Add each type and it's methods to the Tree. If the type cannot be found,
    // then the mapping that was just created, methodsByType, will be used to
    // add its methods to the Tree
    for (String typeMnemonicString : availableTypes) {
      IType type = null;

      TypeMnemonic typeMnemonic = new TypeMnemonic(typeMnemonicString, root);
      type = typeMnemonic.getType();

      if (type == null) {
        addClass(typeMnemonic, selectedTypes.contains(typeMnemonicString),
            methodsByType.get(typeMnemonicString), selectedMethods);
      } else {
        TreeItem typeItem = addClass(type, selectedTypes.contains(typeMnemonicString));
        
        for (TreeItem methodItem : typeItem.getItems()) {
          String methodMnemonic = getMnemonicString(methodItem);

          if (selectedMethods.contains(methodMnemonic)) {
            methodItem.setChecked(true);
            updateTree(methodItem);
          }
        }
      }
    }
  }

  /**
   * Adds the <code>IPackageFragment</code> to the class tree. If the
   * <code>IPackageFragment</code> is already contained in the class tree, the
   * existing <code>TreeItem</code> is returned. Duplicates are not allow.
   * 
   * @param packageFragment
   *          <code>IPackageFragment</code> to add to the class tree
   * @return a new or pre-existing <code>TreeItem</code> containing to <code>IPackageFragment</code>
   * @throws JavaModelException 
   */
  TreeItem addPackage(String packageFragmentName) {
    if (packageFragmentName == null)
      return null;
    
    TreeItem packageItem = getPackageItem(packageFragmentName);
    if(packageItem != null) {
      return packageItem;
    }
    
    // Search for the item in the class tree
    String text = packageFragmentName;
    if (text.isEmpty()) {
      text = DEFAULT_PACKAGE_DISPLAY_NAME;
    }
    int insertionIndex = getInsertionIndex(fTypeTree.getItems(), text);
    
    // Add the package to the class tree since it does not already exist
    TreeItem root = new TreeItem(fTypeTree, SWT.NONE, insertionIndex);
    Image image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);

    root.setImage(image);
    root.setText(text);
    
    setMnemonic(root, IJavaElement.PACKAGE_FRAGMENT, packageFragmentName);
    
    return root;
  }
  
  /**
   * Adds a type to this tree. The type is assumed to not exist, and methods
   * will not be searched for.
   * 
   * @param type
   * @return the <code>TreeItem</code> added to the <code>Tree</code> or
   *         <code>null</code> if it was not added
   */
  TreeItem addClass(TypeMnemonic typeMnemonic, boolean classIsChecked, List<String> methods, List<String> selectedMethods) {
    // First, check if the class item already exists in the tree
    TreeItem classItem = getClassItem(typeMnemonic);
    if (classItem != null) {
      return classItem;
    }
    
    String packageName = RandoopCoreUtil.getPackageName(typeMnemonic.getFullyQualifiedName());
    TreeItem parent = addPackage(packageName);
    
    String text = typeMnemonic.getFullyQualifiedName();
    int insertionIndex = getInsertionIndex(parent.getItems(), text);
    classItem = new TreeItem(parent, SWT.NONE, insertionIndex);

    Image errorImage = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJS_ERROR_TSK);
    classItem.setImage(errorImage);

    classItem.setText(text);
    setMnemonic(classItem, IJavaElement.TYPE, typeMnemonic.toString());

    setChecked(classItem, classIsChecked);
    
    if (methods != null) {
      for (String methodMnemonicString : methods) {
        TreeItem methodItem = new TreeItem(classItem, SWT.NONE);
        MethodMnemonic methodMnemonic = new MethodMnemonic(methodMnemonicString);

        setMnemonic(methodItem, IJavaElement.METHOD, methodMnemonicString);
        methodItem.setImage(errorImage);

        methodItem.setText(Signature.toString(methodMnemonic.getMethodSignature(),
            methodMnemonic.getMethodName(), null, false, true));

        if (classIsChecked || selectedMethods.contains(methodMnemonicString)) {
          methodItem.setChecked(true);
          updateTree(methodItem);
        }
      }
    }
    
    return classItem;
  }
  
  /**
   * Adds a type to this tree. All of the types methods will also be added as
   * children to this tree.
   * 
   * @param type
   * @return the <code>TreeItem</code> that already exists or was added to the
   *         <code>Tree</code> for this class, or <code>null</code> if the class
   *         was not added
   */
  TreeItem addClass(IType type, boolean checked) {
    try {
      if (type == null || type.isInterface() || Flags.isAbstract(type.getFlags())) {
        return null;
      }
      
      TypeMnemonic typeMnemonic = new TypeMnemonic(type);
      
      // First, check if the class item already exists in the tree
      TreeItem classItem = getClassItem(typeMnemonic);
      if (classItem != null) {
        return classItem;
      }
      
      TreeItem parent = addPackage(type.getPackageFragment().getElementName());

      String text = typeMnemonic.getFullyQualifiedName();
      int insertionIndex = getInsertionIndex(parent.getItems(), text);
      classItem = new TreeItem(parent, SWT.NONE, insertionIndex);

      classItem.setImage(getImageForType(type));
      classItem.setText(text);
      
      setMnemonic(classItem, IJavaElement.TYPE, typeMnemonic.toString());
      setMethods(classItem, type);
      
      setChecked(classItem, checked);
      return classItem;
      
    } catch (JavaModelException e) {
      RandoopPlugin.log(e);
      return null;
    }
  }
  
  private int getInsertionIndex(TreeItem[] items, String text) {
    int insertionIndex = 0;
    for (TreeItem item : items) {
      String otherText = item.getText();
      if (text.compareToIgnoreCase(otherText) > 0) {
        insertionIndex++;
      }
    }
    return insertionIndex;
  }
  
  private static void setMethods(TreeItem classItem, IType type) throws JavaModelException {
    classItem.removeAll();
    IMethod[] methods = type.getMethods();
    for (IMethod m : methods) {
      TreeItem methodItem = new TreeItem(classItem, SWT.NONE);
      methodItem.setText(Signature.toString(m.getSignature(), m.getElementName(), null, false, true));

      methodItem.setImage(getImageMethod(m));

      setMnemonic(methodItem, IJavaElement.METHOD, new MethodMnemonic(m).toString());
    }
  }
  
  private static Image getImageForType(IType type) {
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

  private static Image getImageMethod(IMethod method) {
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

  List<String> getAllClasses() {
    List<String> types = new ArrayList<String>();

    for (TreeItem packageItem : fTypeTree.getItems()) {
      for (TreeItem classItem : packageItem.getItems()) {
        Assert.isTrue(getType(classItem) == IJavaElement.TYPE);

        types.add(getMnemonicString(classItem));
      }
    }
    
    return types;
  }
  
  /**
   * 
   * @return list of handlers for types that are fully checked (not grayed)
   */
  List<String> getCheckedClasses() {
    List<String> classes = new ArrayList<String>();

    for (TreeItem packageItem : fTypeTree.getItems()) {
      for (TreeItem classItem : packageItem.getItems()) {
        if (classItem.getChecked() && !classItem.getGrayed()) {
          Assert.isTrue(getType(classItem) == IJavaElement.TYPE);

          classes.add(getMnemonicString(classItem));
        }
      }
    }

    return classes;
  }

  /**
   * Returns a list of <code>IMethod</code>s that are unchecked, including those
   * whose containing types are fully checked.
   * 
   * @return list
   */
  List<String> getAllMethods() {
    List<String> types = new ArrayList<String>();

    for (TreeItem packageItem : fTypeTree.getItems()) {
      for (TreeItem classItem : packageItem.getItems()) {
        for (TreeItem methodItem : classItem.getItems()) {
          Assert.isTrue(getType(methodItem) == IJavaElement.METHOD);

          types.add(getMnemonicString(methodItem));
        }
      }
    }

    return types;
  }

  /**
   * Returns a list of <code>IMethod</code>s that are checked but whose
   * containing types are not fully checked.
   * 
   * @return list
   */
  List<String> getCheckedMethods() {
    List<String> types = new ArrayList<String>();

    for (TreeItem packageItem : fTypeTree.getItems()) {
      for (TreeItem classItem : packageItem.getItems()) {
        for (TreeItem methodItem : classItem.getItems()) {
          if (methodItem.getChecked() && !methodItem.getGrayed()) {
            // According to typical tree behavior, the parent item must be
            // checked. (Items that are grayed are also checked)
            Assert.isTrue(classItem.getChecked());
            
            // Only add methods whose containing types are grayed
            if (classItem.getGrayed()) {
              Assert.isTrue(getType(methodItem) == IJavaElement.METHOD);
              types.add(getMnemonicString(methodItem));
            }
          }
        }
      }
    }

    return types;
  }
  
  private static void setMnemonic(TreeItem treeItem, int type, String mnemonic) {
    treeItem.setData(ID_ELEMENT_TYPE, type);
    treeItem.setData(ID_MNEMONIC, mnemonic);
  }

  private static String getMnemonicString(TreeItem treeItem) {
    Object mnemonic = treeItem.getData(ID_MNEMONIC);
    
    Assert.isNotNull(mnemonic, "Mnemonic must be set"); //$NON-NLS-1$
    Assert.isTrue(mnemonic instanceof String, "Mnemonics must be instances of String"); //$NON-NLS-1$
    
    return (String) mnemonic;
  }
  
  private static int getType(TreeItem treeItem) {
    Object type = treeItem.getData(ID_ELEMENT_TYPE);
    
    Assert.isNotNull(type, "Java element type must be set"); //$NON-NLS-1$
    Assert.isTrue(type instanceof Integer, "Java element types must be instances of Integer"); //$NON-NLS-1$
    
    return (Integer) type;
  }

  /**
   * Updates the checked state of this item and its descendants and ancestors.
   * 
   * @param item
   *          the TreeItem which has just been changed
   * @param checked
   *          the new checked state of item
   */
  protected void setChecked(TreeItem item, boolean checked) {
    item.setChecked(checked);
    updateTree(item);
  }

  /**
   * Updates the checked state of this item's descendants and ancestors.
   * 
   * @param item
   *          the TreeItem for whom the descendants and ancestors will update
   */
  protected void updateTree(TreeItem item) {
    // Items that have just been checked should never be grayed.
    item.setGrayed(false);

    if (item != null) {
      boolean checked = item.getChecked();
      checkItems(item, checked);
      checkPath(item.getParentItem(), item.getChecked(), false);
    }
  }

  /**
   * Recursively iterates backwards through the tree to update the grayed status
   * of each. This code has been adapted from SWT snippet 274.
   * 
   * @param parent
   *          the item to examine, all ancestors will also be examined
   * @param childChecked
   *          true if this item is checked
   * @param childGrayed
   *          true if this item is grayed
   */
  private static void checkPath(TreeItem parent, boolean childChecked,
      boolean childGrayed) {
    boolean parentChecked = childChecked;
    boolean parentGrayed = childGrayed;

    if (parent == null)
      return;
    if (childGrayed) {
      // If the child item is grayed, then it must also have been checked
      // (grayed items are not visible unless they are also checked). In this
      // case, we already know that this parent item must also be grayed since
      // at least one of its children is already grayed.
      parentChecked = true;
    } else {
      // Otherwise, if the child item was not gray, it could have either be
      // fully checked or fully unchecked, meaning that this parent item could
      // also be in the same state as the child that called this routine. First,
      // we must examine each other child element to see if this item should be
      // grayed.
      TreeItem[] items = parent.getItems();
      for (int i = 0; i < items.length; i++) {
        TreeItem child = items[i];
        if (child.getGrayed() || childChecked != child.getChecked()) {
          parentChecked = true;
          parentGrayed = true;
          break;
        }
      }
    }
    
    parent.setChecked(parentChecked);
    parent.setGrayed(parentGrayed);
    checkPath(parent.getParentItem(), childChecked, childGrayed);
  }

  /**
   * Checks/unchecks this item and all of its descendants. This code has been
   * adapted from SWT snippet 274.
   * 
   * @param item
   * @param checked
   */
  private static void checkItems(TreeItem item, boolean checked) {
    if (item == null)
      return;
    item.setGrayed(false);
    item.setChecked(checked);

    // check/uncheck all of this TreeItem's children as well
    TreeItem[] items = item.getItems();
    for (int i = 0; i < items.length; i++) {
      checkItems(items[i], checked);
    }
  }
  
  /**
   * Returns <code>true</code> if some of the selected <code>TreeItem</code>s
   * can be removed from the <code>Tree</code>
   */
  boolean canRemoveFromSelection() {
    TreeItem[] packages = getSelectedPackageFragments();
    TreeItem[] classes = getSelectedClasses();

    return packages.length != 0 || classes.length != 0;
  }
  
  void removeSelectedTypes() {
    TreeItem[] packages = getSelectedPackageFragments();
    TreeItem[] classes = getSelectedClasses();

    for (TreeItem packageItem : packages) {
      packageItem.removeAll();
      packageItem.dispose();
    }
    
    for (TreeItem classItem : classes) {
      if (!classItem.isDisposed()) {
        classItem.removeAll();
        TreeItem packageItem = classItem.getParentItem();
        classItem.dispose();

        if (packageItem.getItemCount() == 0) {
          packageItem.dispose();
        }
      }
    }
  }
  
  private TreeItem[] getSelectedPackageFragments() {
    TreeItem[] items = fTypeTree.getSelection();
    List<TreeItem> roots = new ArrayList<TreeItem>();

    for (TreeItem item : items) {
      // Only move root elements
      if (item.getParentItem() == null) {
        roots.add(item);
      }
    }

    return roots.toArray(new TreeItem[roots.size()]);
  }
  
  private TreeItem[] getSelectedClasses() {
    TreeItem[] items = fTypeTree.getSelection();
    List<TreeItem> classes = new ArrayList<TreeItem>();

    for (TreeItem item : items) {
      if (item.getParentItem() != null && item.getParentItem().getParentItem() == null) {
        classes.add(item);
      }
    }

    return classes.toArray(new TreeItem[classes.size()]);
  }

  void checkAll() {
    for(TreeItem rootItem : fTypeTree.getItems()) {
      checkRootItem(rootItem, true);
    }
  }

  void uncheckAll() {
    for(TreeItem rootItem : fTypeTree.getItems()) {
      checkRootItem(rootItem, false);
    }
  }

  private TreeItem getPackageItem(String packageFragmentName) {
    for (TreeItem packageItem : fTypeTree.getItems()) {
      if (getMnemonicString(packageItem).equals(packageFragmentName)) {
        return packageItem;
      }
    }

    return null;
  }
  
  private TreeItem getClassItem(TypeMnemonic typeMnemonic) {
    String fqname = typeMnemonic.getFullyQualifiedName();
    String packageName = RandoopCoreUtil.getPackageName(fqname);

    for(TreeItem packageItem : fTypeTree.getItems()) {
      if (getMnemonicString(packageItem).equals(packageName)) {
        for (TreeItem classItem : packageItem.getItems()) {
          if (typeMnemonic.toString().equals(getMnemonicString(classItem))) {
            return classItem;
          }
        }
      }
    }
    
    return null;
  }
  
  private void checkRootItem(TreeItem parentItem, boolean isChecked) {
    if (parentItem == null)
      return;
    
    for(TreeItem item : parentItem.getItems()) {
      checkRootItem(item, isChecked);
    }
    
    parentItem.setChecked(isChecked);
    parentItem.setGrayed(false);
  }
  
  void resolveMissingClasses() throws JavaModelException {
    Assert.isNotNull(fJavaProject);

    Map<String, List<MethodMnemonic>> checkedMethodsByFQTypeName = new HashMap<String, List<MethodMnemonic>>();
    List<String> methodMnemonics = getCheckedMethods();
    for (String methodMnemonicString : methodMnemonics) {
      MethodMnemonic methodMnemonic = new MethodMnemonic(methodMnemonicString, getWorkspaceRoot());

      String fqname = methodMnemonic.getDeclaringTypeMnemonic().toString();
      List<MethodMnemonic> methods = checkedMethodsByFQTypeName.get(fqname);
      if (methods == null) {
        methods = new ArrayList<MethodMnemonic>();
        checkedMethodsByFQTypeName.put(fqname, methods);
      }
      methods.add(methodMnemonic);
    }

    for (TreeItem packageItem : fTypeTree.getItems()) {
      for (TreeItem classItem : packageItem.getItems()) {
        Assert.isTrue(getType(classItem) == IJavaElement.TYPE);

        TypeMnemonic typeMnemonic = new TypeMnemonic(getMnemonicString(classItem), getWorkspaceRoot());
        IType type = typeMnemonic.getType();
        if (type == null) {
          type = fJavaProject.findType(typeMnemonic.getFullyQualifiedName(), (IProgressMonitor) null);
          if (type != null) {
            typeMnemonic = new TypeMnemonic(type);

            setMnemonic(classItem, IJavaElement.TYPE, typeMnemonic.toString());
            setMethods(classItem, type);
          }
          
          boolean hasIndividuallyCheckedMethods = classItem.getGrayed();
          if (classItem.getGrayed()) {
            classItem.setGrayed(false);
          }
          updateTree(classItem);
          
          if(hasIndividuallyCheckedMethods) {
            String fqname = new TypeMnemonic(getMnemonicString(classItem)).getFullyQualifiedName();
            List<MethodMnemonic> checkedMethods = checkedMethodsByFQTypeName.get(fqname);
            
            for (TreeItem methodItem : classItem.getItems()) {
              MethodMnemonic candidateMethodMneomnic = new MethodMnemonic(getMnemonicString(methodItem));
              
              for (MethodMnemonic checkedMethod : checkedMethods) {
                // Check the method if the name and signature match
                if (candidateMethodMneomnic.getMethodName().equals(checkedMethod.getMethodName())
                    && candidateMethodMneomnic.getMethodSignature().equals(
                        checkedMethod.getMethodSignature())) {
                  methodItem.setChecked(true);
                  updateTree(methodItem);
                  break;
                }
              }
            }
          }
        }
      }
    }
  }
  
  /**
   * Sets the IJavaProject to be used for this class selector. This will search
   * for the pre-existing types of this ClassSelector in the new projects
   * classpath.
   * 
   * 
   * @param javaProject
   */
  void setJavaProject(IJavaProject javaProject) {
    fJavaProject = javaProject;
    
    if (fJavaProject == null) {
      // Set each TreeItem's image to a red X, indicating it is invalid
      Image image = IMG_ERROR;
      for (TreeItem packageItem : fTypeTree.getItems()) {
        for (TreeItem classItem : packageItem.getItems()) {
          for (TreeItem methodItem : classItem.getItems()) {
            methodItem.setImage(image);
          }
        }
      }
    } else {
      IWorkspaceRoot root = getWorkspaceRoot();

      for (TreeItem packageItem : fTypeTree.getItems()) {
        for (TreeItem classItem : packageItem.getItems()) {
          TypeMnemonic oldMnemonic = new TypeMnemonic(getMnemonicString(classItem), root);
          
          if (!fJavaProject.equals(oldMnemonic.getJavaProject())) {
            TypeMnemonic newMnemonic = oldMnemonic.reassign(fJavaProject);

            // If newMnemonic is not null, the IType was found in a classpath
            // entry of the new Java project
            if (newMnemonic != null && newMnemonic.exists()) {
              // Update the mnemonic for this TreeItem
              setMnemonic(classItem, IJavaElement.TYPE, newMnemonic.toString());
              classItem.setImage(getImageForType(newMnemonic.getType()));

              // Make a list of methods that are are currently checked under
              // this
              // TreeItem
              List<MethodMnemonic> checkedMethods = new ArrayList<MethodMnemonic>();
              for (TreeItem methodItem : classItem.getItems()) {
                if (methodItem.getChecked()) {
                  checkedMethods.add(new MethodMnemonic(getMnemonicString(methodItem)));
                }
              }
              classItem.removeAll();

              try {
                setMethods(classItem, newMnemonic.getType());

                for (TreeItem methodItem : classItem.getItems()) {
                  MethodMnemonic methodMnemonic = new MethodMnemonic(getMnemonicString(methodItem));

                  // Check if the TreeItem for this method should be checked
                  for (MethodMnemonic checkedMethodMnemonic : checkedMethods) {
                    // See if the new method mnemonic is similar to one that was
                    // checked
                    if (methodMnemonic.getMethodName().equals(checkedMethodMnemonic.getMethodName())
                        && checkedMethodMnemonic.getMethodSignature().equals(
                            methodMnemonic.getMethodSignature())) {
                      methodItem.setChecked(true);
                      break;
                    }
                  }

                  updateTree(methodItem);
                }
              } catch (JavaModelException e) {
                RandoopPlugin.log(e);
              }
            } else {
              // Otherwise the IType was not found in a classpath, set this
              // TreeItem's image to an error image
              classItem.setImage(IMG_ERROR);
              for (TreeItem methodItem : classItem.getItems()) {
                methodItem.setImage(IMG_ERROR);
              }
            }
          }
        }
      }
    }
  }
  
  /*
   * Convenience method to get the workspace root.
   */
  private static IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }
  
}
