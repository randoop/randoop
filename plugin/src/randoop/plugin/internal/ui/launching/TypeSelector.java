package randoop.plugin.internal.ui.launching;

import java.util.ArrayList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.swt.SWT;
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
  private Map<String, TreeItem> fTreeItemsByHandlerId;
  private Tree fTypeTree;

  /**
   * 
   * IMPORTANT: emptyTree must use the SWT.CHECK style bit.
   * 
   * @param typeTree
   *          tree that can be used to display elements
   */
  public TypeSelector(Tree typeTree) {
    Assert.isNotNull(typeTree);

    fTreeItemsByHandlerId = new HashMap<String, TreeItem>();
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
  public TypeSelector(Tree emptyTree, Collection<String> allTypes,
      Collection<String> checkedElements) {
    this(emptyTree);

    for (String id : allTypes) {
      IJavaElement element = JavaCore.create(id);
      if (element instanceof IType) {
        IType type = (IType) element;

        addType(type, false);
      }
    }

    // Iterate through the checked elements and check the corresponding elements
    // in the Tree
    for (String id : checkedElements) {
      IJavaElement element = JavaCore.create(id);
      if (element instanceof IType) {
        IType type = (IType) element;
        String handlerId = type.getHandleIdentifier();

        TreeItem typeTreeItem = fTreeItemsByHandlerId.get(handlerId);
        setChecked(typeTreeItem, true);
      } else if (element instanceof IMethod) {
        IMethod method = (IMethod) element;
        String handlerId = method.getHandleIdentifier();

        TreeItem methodTreeItem = fTreeItemsByHandlerId.get(handlerId);
        setChecked(methodTreeItem, true);
      }
    }
  }

  /**
   * Adds a type to this tree. All of the types methods will also be added as
   * children to this tree.
   * 
   * @param type
   */
  public TreeItem addType(IType type, boolean checked) {
    TreeItem root = new TreeItem(fTypeTree, SWT.NONE);
    root.setText(type.getFullyQualifiedName());
    fTreeItemsByHandlerId.put(type.getHandleIdentifier(), root);

    // XXX set images for different types and methods
    try {
      IMethod[] methods = type.getMethods();
      for (IMethod m : methods) {
        TreeItem methodItem = new TreeItem(root, SWT.NONE);
        methodItem.setText(Signature.toString(m.getSignature(), m
            .getElementName(), null, false, true));

        fTreeItemsByHandlerId.put(m.getHandleIdentifier(), methodItem);
      }
    } catch (JavaModelException e) {
      RandoopPlugin.log(e);
    }

    setChecked(root, checked);
    return root;
  }

  /**
   * 
   * @return list of handlers for types that are fully checked
   */
  public List<String> getCheckedHandlerIds() {
    List<String> types = new ArrayList<String>();

    for (String id : fTreeItemsByHandlerId.keySet()) {
      TreeItem item = fTreeItemsByHandlerId.get(id);

      if (item.getChecked()) {
        // If the parent item is null, then this must be a type
        if (item.getParentItem() == null) {
          // Put this type's handler ID in the list
          if (!item.getGrayed()) {
            types.add(id);
          }
        } else {
          // Otherwise, this is a method. Only methods contained by unchecked
          // types should be added, so check if the parent is checked
          if (item.getParentItem().getChecked()
              && item.getParentItem().getGrayed()) {
            // Put this method's handler ID in the list
            types.add(id);
          }
        }
      }
    }

    return types;
  }

  public List<String> getAllTypeHandlerIds() {
    List<String> types = new ArrayList<String>();

    for (String id : fTreeItemsByHandlerId.keySet()) {
      TreeItem item = fTreeItemsByHandlerId.get(id);

      // If the parent item is null, then this must be a type
      if (item.getParentItem() == null) {
        // Put this type's handler ID in the list
        types.add(id);
      }
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

  public void removeSelectedTypes() {
    TreeItem[] items = fTypeTree.getSelection();

    for (TreeItem item : items) {
      // Only remove root elements
      if (item.getParentItem() == null) {
        item.dispose();
      }
    }

    // Find the keys for the TreeItems that have been disposed and add them to a
    // list
    List<String> disposables = new ArrayList<String>();
    for (String id : fTreeItemsByHandlerId.keySet()) {
      TreeItem item = fTreeItemsByHandlerId.get(id);
      if (item.isDisposed()) {
        disposables.add(id);
      }
    }

    // remove the TreeItems that have been disposed
    for (String id : disposables) {
      fTreeItemsByHandlerId.remove(id);
    }
  }
}
