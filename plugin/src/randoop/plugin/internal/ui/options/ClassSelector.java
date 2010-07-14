package randoop.plugin.internal.ui.options;

import java.util.ArrayList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import randoop.plugin.RandoopPlugin;

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
  
  private static final String ID_MNEMONIC = "TypeSelector.ID_MNEMONIC"; //$NON-NLS-1$

  private Tree fTypeTree;
  private IJavaProject fJavaProject;

  /**
   * 
   * IMPORTANT: emptyTree must use the SWT.CHECK style bit.
   * 
   * @param typeTree
   *          tree that can be used to display elements
   * @param selectedMethods 
   * @param selectedTypes 
   * @param availableTypes 
   */
  public ClassSelector(Tree typeTree) {
    Assert.isLegal(typeTree != null, "The Tree cannot be null");
    Assert.isLegal((typeTree.getStyle() & SWT.CHECK) != 0, "The Tree must use the SWT.CHECK style");
    
    fTypeTree = typeTree;
    fTypeTree.removeAll();
    fJavaProject = null;
    
    // Add a selection listener that will check/uncheck descendants of a node
    // when it is checked/unchecked as well as gray some of its ancestors as
    // needed.
    fTypeTree.addListener(SWT.Selection, new Listener() {
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
   * @param emptyTree
   *          empty tree that can be used to add
   */
  public ClassSelector(Tree emptyTree, IJavaProject javaProject, List<String> availableTypes,
      List<String> selectedTypes, List<String> availableMethods, List<String> selectedMethods) {

    this(emptyTree);

    Assert.isLegal(availableTypes != null);
    Assert.isLegal(selectedTypes != null);
    Assert.isLegal(selectedMethods != null);
    Assert.isLegal(availableTypes.size() >= selectedTypes.size(), "There must be more available types than selected ones");
    for (String s : selectedTypes) {
      Assert.isLegal(availableTypes.contains(s), "Each selected type must also be an available one");
    }
    if (!availableTypes.isEmpty()) {
      Assert.isLegal(javaProject != null, "Java project must not be null");
      Assert.isLegal(javaProject.exists(), "Java project must exist");
      
      try {
        javaProject.open(new NullProgressMonitor());
      } catch (JavaModelException e) {
      }
    }
    fJavaProject = javaProject;

    HashMap<String, List<String>> methodsByType = new HashMap<String, List<String>>();
    for(String mnemonic : availableMethods) {
      String[] info = Mnemonics.splitMethodMnemonic(mnemonic);
     
      List<String> list = methodsByType.get(info[0]);
      if(list == null) {
        list = new ArrayList<String>();
        methodsByType.put(info[0], list);
      }
      list.add(mnemonic);
    }
    
    for (String typeMnemonic : availableTypes) {
      IType type = Mnemonics.getType(fJavaProject, typeMnemonic);

      if (type == null) {
        addType(Mnemonics.getFullyQualifiedName(typeMnemonic), selectedTypes.contains(typeMnemonic),
            methodsByType.get(typeMnemonic), selectedMethods);
      } else {
        TreeItem typeItem = addType(type, selectedTypes.contains(typeMnemonic));
        
        for (TreeItem methodItem : typeItem.getItems()) {
          String methodMnemonic = getMnemonic(methodItem);

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
   */
  public TreeItem addPackage(String packageFragmentName) {
    // Search for the item in the class tree
    for(TreeItem item : fTypeTree.getItems()) {
      String existingPackageName = (String) item.getData(ID_MNEMONIC);
      
      if (packageFragmentName.equals(existingPackageName)) {
        return item;
      }
    }
    
    // Add the package to the class tree since it does not already exist
    TreeItem root = new TreeItem(fTypeTree, SWT.NONE);

    Image image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);

    root.setImage(image);

    String text = packageFragmentName;
    if (text.isEmpty()) {
      text = "(default package)";
    }
    root.setText(text);
    
    root.setData(ID_ELEMENT_TYPE, IJavaElement.PACKAGE_FRAGMENT);
    root.setData(ID_MNEMONIC, packageFragmentName);
    
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
  public TreeItem addType(String fqname, boolean checked, List<String> methods, List<String> selectedMethods) {
    String[] splitName = Mnemonics.splitFullyQualifiedName(fqname);
    TreeItem parent = addPackage(splitName[0]);

    TreeItem classItem = new TreeItem(parent, SWT.NONE);

    Image errorImage = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJS_ERROR_TSK);
    classItem.setImage(errorImage);

    classItem.setText(fqname);
    classItem.setData(ID_ELEMENT_TYPE, IJavaElement.TYPE);
    classItem.setData(ID_MNEMONIC, fqname);

    setChecked(classItem, checked);
    
    if (methods != null) {
      for (String methodMnemonic : methods) {
        TreeItem methodItem = new TreeItem(classItem, SWT.NONE);
        String[] info = Mnemonics.splitMethodMnemonic(methodMnemonic);

        methodItem.setData(ID_ELEMENT_TYPE, IJavaElement.METHOD);
        methodItem.setData(ID_MNEMONIC, methodMnemonic);
        methodItem.setImage(errorImage);

        methodItem.setText(Signature.toString(info[2], info[1], null, false, true));

        if (selectedMethods.contains(methodMnemonic)) {
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
   * @return the <code>TreeItem</code> added to the <code>Tree</code> or
   *         <code>null</code> if it was not added
   */
  public TreeItem addType(IType type, boolean checked) {
    try {
      TreeItem parent = addPackage(type.getPackageFragment().getElementName());

      TreeItem classItem = new TreeItem(parent, SWT.NONE);

      Assert.isTrue(!type.isInterface());
      Assert.isTrue(!Flags.isAbstract(type.getFlags()));

      classItem.setImage(getImageForType(type));

      classItem.setText(type.getFullyQualifiedName());
      classItem.setData(ID_ELEMENT_TYPE, IJavaElement.TYPE);
      classItem.setData(ID_MNEMONIC, type.getFullyQualifiedName());

      IMethod[] methods = type.getMethods();
      for (IMethod m : methods) {
        TreeItem methodItem = new TreeItem(classItem, SWT.NONE);
        methodItem.setText(Signature.toString(m.getSignature(),
            m.getElementName(), null, false, true));

        methodItem.setImage(getImageMethod(m));
        
        methodItem.setData(ID_ELEMENT_TYPE, IJavaElement.METHOD);
        methodItem.setData(ID_MNEMONIC, Mnemonics.getMethodMnemonic(m));
      }

      setChecked(classItem, checked);
      return classItem;
      
    } catch (JavaModelException e) {
      RandoopPlugin.log(e);
      return null;
    }
  }
  
  private static Image getImageForType(IType type) {
    try {
      if (type == null || !type.exists()) {
        return IMG_ERROR;
      } else if (type.isEnum()) {
        return IMG_ENUM;
      } else if (type.isClass()) {
        return IMG_CLASS;
      }
    } catch (JavaModelException e) {
      RandoopPlugin.log(e);
    }
    return null;
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

  public List<String> getAllClasses() {
    List<String> types = new ArrayList<String>();

    for (TreeItem packageItem : fTypeTree.getItems()) {
      for (TreeItem classItem : packageItem.getItems()) {
        Assert.isTrue(getType(classItem) == IJavaElement.TYPE);

        types.add(getMnemonic(classItem));
      }
    }
    
    return types;
  }
  
  /**
   * 
   * @return list of handlers for types that are fully checked (not grayed)
   */
  public List<String> getCheckedClasses() {
    List<String> classes = new ArrayList<String>();

    for (TreeItem packageItem : fTypeTree.getItems()) {
      for (TreeItem classItem : packageItem.getItems()) {
        if (classItem.getChecked() && !classItem.getGrayed()) {
          Assert.isTrue(getType(classItem) == IJavaElement.TYPE);

          classes.add(getMnemonic(classItem));
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
  public List<String> getAllMethods() {
    List<String> types = new ArrayList<String>();

    for (TreeItem packageItem : fTypeTree.getItems()) {
      for (TreeItem classItem : packageItem.getItems()) {
        for (TreeItem methodItem : classItem.getItems()) {
          Assert.isTrue(getType(methodItem) == IJavaElement.METHOD);

          types.add(getMnemonic(methodItem));
        }
      }
    }

    return types;
  }

  /**
   * Returns a list of <code>IMethod</code>s that are checked, including those
   * whose containing types are fully checked.
   * 
   * @return list
   */
  public List<String> getCheckedMethods() {
    List<String> types = new ArrayList<String>();

    for (TreeItem packageItem : fTypeTree.getItems()) {
      for (TreeItem classItem : packageItem.getItems()) {
        for (TreeItem methodItem : classItem.getItems()) {
          if (methodItem.getChecked() && !methodItem.getGrayed()) {
            // According to typical tree behavior, the parent item must be checked
            Assert.isTrue(classItem.getChecked());

            Assert.isTrue(getType(methodItem) == IJavaElement.METHOD);
            types.add(getMnemonic(methodItem));
          }
        }
      }
    }

    return types;
  }
  
  public String getMnemonic(TreeItem treeItem) {
    Object mnemonic = treeItem.getData(ID_MNEMONIC);
    
    Assert.isNotNull(mnemonic, "Mnemonic must be set");
    Assert.isTrue(mnemonic instanceof String, "Mnemonics must be instances of String");
    
    return (String) mnemonic;
  }
  
  public int getType(TreeItem treeItem) {
    Object type = treeItem.getData(ID_ELEMENT_TYPE);
    
    Assert.isNotNull(type, "Java element type must be set");
    Assert.isTrue(type instanceof Integer, "Java element types must be instances of Integer");
    
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

  TreeItem[] getMoveableSelection() {
    TreeItem[] roots = getSelectedPackageFragments();
    TreeItem[] classes = getSelectedClasses();
    
    if (roots.length != 0 && classes.length == 0) {
      return roots;
    } else if (roots.length == 0 && classes.length != 0) {
      return classes;
    } else {
      return null;
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
  
  public void moveSelectedTypesUp() {
    moveSelectedTypes(true);
  }

  public void moveSelectedTypesDown() {
    moveSelectedTypes(false);
  }
  
  private void moveSelectedTypes(boolean up) {
    TreeItem[] selectedRoots = getMoveableSelection();

    if (selectedRoots != null) {
      if (up) {
        for (int i = 0; i < selectedRoots.length; i++) {
          moveItem(selectedRoots[i], true);
        }
      } else {
        for (int i = selectedRoots.length - 1; i >= 0; i--) {
          moveItem(selectedRoots[i], false);
        }
      }
    }
  }
  
  private TreeItem moveItem(TreeItem oldItem, boolean up) {
    TreeItem parent = oldItem.getParentItem();
    
    int direction = up ? -1 : 2;
    TreeItem newItem;
    if (parent == null) {
      int index = fTypeTree.indexOf(oldItem);
      newItem = new TreeItem(fTypeTree, SWT.NONE, index + direction);
    } else {
      int index = parent.indexOf(oldItem);
      newItem = new TreeItem(parent, SWT.NONE, index + direction);
    }
    
    copy(oldItem, newItem);
    
    return newItem;
  }

  private void copy(TreeItem oldItem, TreeItem newItem) {
    newItem.setChecked(oldItem.getChecked());
    newItem.setGrayed(oldItem.getGrayed());
    newItem.setText(oldItem.getText());
    newItem.setImage(oldItem.getImage());
    newItem.setData(ID_ELEMENT_TYPE, oldItem.getData(ID_ELEMENT_TYPE));
    newItem.setData(ID_MNEMONIC, oldItem.getData(ID_MNEMONIC));

    TreeItem children[] = oldItem.getItems();
    for (TreeItem oldChild : children) {
      TreeItem newChild = new TreeItem(newItem, SWT.NONE);
      copy(oldChild, newChild);
      
      oldChild.dispose();
    }
    
    oldItem.dispose();
  }

  /**
   * Returns <code>true</code> if some of the selected <code>TreeItem</code>s
   * can be removed from the <code>Tree</code>
   */
  boolean canRemoveFromSelection() {
    TreeItem[] items = fTypeTree.getSelection();

    for (TreeItem item : items) {
      // Only remove package fragments or classes (roots or their children)
      if (item.getParentItem() == null || item.getParentItem().getParentItem() == null) {
        return true;
      }
    }
    
    return false;
  }
  
  public void removeSelectedTypes() {
    TreeItem[] items = fTypeTree.getSelection();

    for (TreeItem item : items) {
      // Only remove package fragments or classes (roots or their children)
      if (item.getParentItem() == null) {
        item.dispose();
      } else if (item.getParentItem().getParentItem() == null) {
        TreeItem parent = item.getParentItem();
        item.dispose();
        
        // If their are not classes left in the package fragment, dispose it
        if(parent.getItemCount() == 0) {
          parent.dispose();
        }
      }
    }
  }

  public void setJavaProject(IJavaProject javaProject) {
    fJavaProject = javaProject;
    
    Image image = null;
    if (fJavaProject == null)
      image = IMG_ERROR;

    for (TreeItem packageItem : fTypeTree.getItems()) {
      for (TreeItem classItem : packageItem.getItems()) {
        if (fJavaProject != null) {
          IType type = Mnemonics.getType(fJavaProject, getMnemonic(classItem));
          image = getImageForType(type);
        }
        classItem.setImage(image);

        for (TreeItem methodItem : classItem.getItems()) {
          if (fJavaProject != null) {
            IMethod method = Mnemonics.getMethod(fJavaProject, getMnemonic(methodItem));
            image = getImageMethod(method);
          }
          methodItem.setImage(image);
        }
      }
    }
  }
  
}
