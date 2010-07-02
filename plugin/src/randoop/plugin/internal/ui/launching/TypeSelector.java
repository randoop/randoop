package randoop.plugin.internal.ui.launching;

import java.util.ArrayList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
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
import org.eclipse.core.runtime.Assert;

import randoop.plugin.RandoopPlugin;

/**
 * The TypeSelector class is used to manage a Tree object so that the user can
 * browse and select or unselect certain IJavaElements. The class is
 * specifically meant for displaying a list of ITypes that can be expanded to
 * show the methods that are contained.
 */
public class TypeSelector {
  private static final String HANDLER_ID = "TypeSelector.HANDLER_ID"; //$NON-NLS-1$
  
  private Tree fTypeTree;

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
  public TypeSelector(Tree typeTree) {
    Assert.isNotNull(typeTree);

    fTypeTree = typeTree;
    fTypeTree.removeAll();

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
  public TypeSelector(Tree emptyTree, Collection<String> availableTypes, Collection<String> selectedTypes, Collection<String> selectedMethods) {
    this(emptyTree);

    for (String id : availableTypes) {
      IJavaElement element = JavaCore.create(id);
      
      Assert.isTrue(element instanceof IType);
      IType type = (IType) element;

      TreeItem typeItem = addType(type, selectedTypes.contains(id));
      for (TreeItem methodItem : typeItem.getItems()) {
        Object methodId = methodItem.getData(HANDLER_ID);
        if (selectedMethods.contains(methodId)) {
          methodItem.setChecked(true);
          updateTree(methodItem);
        }
      }
    }
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
    // XXX set images for different types and methods
    try {
      TreeItem root = new TreeItem(fTypeTree, SWT.NONE);
      
      Image image = null;
      if (type.isInterface()) {
        image = JavaUI.getSharedImages().getImage(
            ISharedImages.IMG_OBJS_INTERFACE);
      } else if (type.isEnum()) {
        image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_ENUM);
      } else if (type.isClass()) {
        image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
      }
      
      if (image != null) {
        root.setImage(image);
      }

      root.setText(type.getFullyQualifiedName());
      root.setData(HANDLER_ID, type.getHandleIdentifier());

      IMethod[] methods = type.getMethods();
      for (IMethod m : methods) {
        TreeItem methodItem = new TreeItem(root, SWT.NONE);
        methodItem.setText(Signature.toString(m.getSignature(),
            m.getElementName(), null, false, true));

        int flags = m.getFlags();
        if (Flags.isPublic(flags)) {
          image = JavaUI.getSharedImages().getImage(
              ISharedImages.IMG_OBJS_PUBLIC);
        } else if (Flags.isPrivate(flags)) {
          image = JavaUI.getSharedImages().getImage(
              ISharedImages.IMG_OBJS_PRIVATE);
        } else if (Flags.isProtected(flags)) {
          image = JavaUI.getSharedImages().getImage(
              ISharedImages.IMG_OBJS_PROTECTED);
        } else {
          image = JavaUI.getSharedImages().getImage(
              ISharedImages.IMG_OBJS_DEFAULT);
        }

        methodItem.setImage(image);
        methodItem.setData(HANDLER_ID, m.getHandleIdentifier());
      }

      setChecked(root, checked);
      return root;
      
    } catch (JavaModelException e) {
      RandoopPlugin.log(e);
      return null;
    }
  }

  /**
   * 
   * @return list of handlers for types that are fully checked (not grayed)
   */
  public List<String> getSelectedTypes() {
    List<String> types = new ArrayList<String>();

    for(TreeItem item : fTypeTree.getItems()) {
      if(item.getChecked() && !item.getGrayed()) {
        types.add((String) item.getData(HANDLER_ID));
      }
    }

    return types;
  }

  /**
   * Returns a list of <code>IMethod</code>s that are checked, but whose
   * containing types are grayed.
   * 
   * @return list
   */
  public List<String> getSelectedMethods() {
    List<String> types = new ArrayList<String>();

    for(TreeItem item : fTypeTree.getItems()) {
      for (TreeItem child : item.getItems()) {
        if (child.getChecked() && !child.getGrayed()) {
          if (child.getParentItem().getGrayed()) {
            types.add((String) child.getData(HANDLER_ID));
          }
        }
      }
    }

    return types;
  }
  
  public List<String> getAllTypes() {
    List<String> types = new ArrayList<String>();

    for (TreeItem item : fTypeTree.getItems()) {
      types.add((String) item.getData(HANDLER_ID));
    }
    
    return types;
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

  private TreeItem[] getSelectedRoots() {
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
  
  public void moveSelectedTypesUp() {
    moveSelectedTypes(true);
  }

  public void moveSelectedTypesDown() {
    moveSelectedTypes(false);
  }
  
  private void moveSelectedTypes(boolean up) {
    TreeItem[] selectedRoots = getSelectedRoots();
    Map<TreeItem, TreeItem> newItemsByDisposedItems = new HashMap<TreeItem, TreeItem>();

    if (up) {
      for (int i = 0; i < selectedRoots.length; i++) {
        moveItem(selectedRoots[i], true, newItemsByDisposedItems);
      }
    } else {
      for (int i = selectedRoots.length - 1; i >= 0; i--) {
        moveItem(selectedRoots[i], false, newItemsByDisposedItems);
      }
    }

    // Update the TreeItems mapped to by the HandlerId map
    System.out.println("done!!");
  }
  
  private TreeItem moveItem(TreeItem oldItem, boolean up, Map<TreeItem, TreeItem> newItemsByDisposedItems) {
    int index = fTypeTree.indexOf(oldItem);
    TreeItem children[] = oldItem.getItems();
    
    int direction = up ? -1 : 2;
    TreeItem newItem = new TreeItem(fTypeTree, SWT.NONE, index + direction);
    newItem.setChecked(oldItem.getChecked());
    newItem.setGrayed(oldItem.getGrayed());
    newItem.setText(oldItem.getText());
    newItem.setImage(oldItem.getImage());
    newItem.setExpanded(oldItem.getExpanded());
    newItem.setData(HANDLER_ID, oldItem.getData(HANDLER_ID));

    for (TreeItem oldChild : children) {
      TreeItem newChild = new TreeItem(newItem, SWT.NONE);
      newChild.setChecked(oldChild.getChecked());
      newChild.setText(oldChild.getText());
      newChild.setImage(oldChild.getImage());
      newChild.setData(HANDLER_ID, oldChild.getData(HANDLER_ID));
      
      oldChild.dispose();
      newItemsByDisposedItems.put(oldChild, newChild);
    }
    
    oldItem.dispose();
    newItemsByDisposedItems.put(oldItem, newItem);
    
    return newItem;
  }

  public void removeSelectedTypes() {
    TreeItem[] items = fTypeTree.getSelection();

    for (TreeItem item : items) {
      // Only remove root elements
      if (item.getParentItem() == null) {
        item.dispose();
      }
    }
  }
}
