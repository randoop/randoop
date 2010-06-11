/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package randoop.plugin.internal.ui;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.model.IWorkbenchAdapter;

import randoop.plugin.RandoopPlugin;

/**
 * This is a specialization of <code>FilteredItemsSelectionDialog</code> used to
 * present users with a listing of <code>IType</code>s that contain main methods
 * 
 * @since 3.3
 * 
 */
public class DebugTypeSelectionDialog extends FilteredItemsSelectionDialog {

  /**
   * Main list label provider
   */
  public class DebugTypeLabelProvider implements ILabelProvider {
    HashMap fImageMap = new HashMap();

    public Image getImage(Object element) {
      if (element instanceof IAdaptable) {
        IWorkbenchAdapter adapter = (IWorkbenchAdapter) ((IAdaptable) element)
            .getAdapter(IWorkbenchAdapter.class);
        if (adapter != null) {
          ImageDescriptor descriptor = adapter.getImageDescriptor(element);
          Image image = (Image) fImageMap.get(descriptor);
          if (image == null) {
            image = descriptor.createImage();
            fImageMap.put(descriptor, image);
          }
          return image;
        }
      }
      return null;
    }

    public String getText(Object element) {
      if (element instanceof IType) {
        IType type = (IType) element;
        String label = type.getTypeQualifiedName();
        String container = getDeclaringContainerName(type);
        if (container != null && !"".equals(container)) { //$NON-NLS-1$
          label += " - " + container; //$NON-NLS-1$
        }
        return label;
      }
      return null;
    }

    /**
     * Returns the name of the declaring container name
     * 
     * @param type
     *          the type to find the container name for
     * @return the container name for the specified type
     */
    protected String getDeclaringContainerName(IType type) {
      IType outer = type.getDeclaringType();
      if (outer != null) {
        return outer.getFullyQualifiedName('.');
      } else {
        String name = type.getPackageFragment().getElementName();
        if ("".equals(name)) { //$NON-NLS-1$
          name = "(default package)";
        }
        return name;
      }
    }

    /**
     * Returns the narrowest enclosing <code>IJavaElement</code> which is either
     * an <code>IType</code> (enclosing) or an <code>IPackageFragment</code>
     * (contained in)
     * 
     * @param type
     *          the type to find the enclosing <code>IJavaElement</code> for.
     * @return the enclosing element or <code>null</code> if none
     */
    protected IJavaElement getDeclaringContainer(IType type) {
      IJavaElement outer = type.getDeclaringType();
      if (outer == null) {
        outer = type.getPackageFragment();
      }
      return outer;
    }

    public void dispose() {
      fImageMap.clear();
      fImageMap = null;
    }

    public void addListener(ILabelProviderListener listener) {
    }

    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    }
  }

  /**
   * Provides a label and image for the details area of the dialog
   */
  class DebugTypeDetailsLabelProvider extends DebugTypeLabelProvider {
    @Override
    public String getText(Object element) {
      if (element instanceof IType) {
        IType type = (IType) element;
        String name = getDeclaringContainerName(type);
        if (name != null) {
          if (name.equals("(default package)")) {
            IJavaProject project = type.getJavaProject();
            if (project != null) {
              try {
                return project.getOutputLocation().toOSString().substring(1)
                    + " - " + name; //$NON-NLS-1$
              } catch (JavaModelException e) {
                RandoopPlugin.log(e);
              }
            }
          } else {
            return name;
          }
        }
      }
      return null;
    }

    @Override
    public Image getImage(Object element) {
      if (element instanceof IType) {
        return super.getImage(getDeclaringContainer(((IType) element)));
      }
      return super.getImage(element);
    }
  }

  /**
   * Simple items filter
   */
  class DebugTypeItemsFilter extends ItemsFilter {
    @Override
    public boolean isConsistentItem(Object item) {
      return item instanceof IType;
    }

    @Override
    public boolean matchItem(Object item) {
      if (!(item instanceof IType) || !Arrays.asList(fTypes).contains(item)) {
        return false;
      }
      return matches(((IType) item).getElementName());
    }
  }

  /**
   * The selection history for the dialog
   */
  class DebugTypeSelectionHistory extends SelectionHistory {
    @Override
    protected Object restoreItemFromMemento(IMemento memento) {
      IJavaElement element = JavaCore.create(memento.getTextData());
      return (element instanceof IType ? element : null);
    }

    @Override
    protected void storeItemToMemento(Object item, IMemento memento) {
      if (item instanceof IType) {
        memento.putTextData(((IType) item).getHandleIdentifier());
      }
    }
  }

  private static final String SETTINGS_ID = RandoopPlugin.PLUGIN_ID
      + ".MAIN_METHOD_SELECTION_DIALOG"; //$NON-NLS-1$
  private IType[] fTypes = null;

  /**
   * Constructor
   * 
   * @param elements
   *          the types to display in the dialog
   */
  public DebugTypeSelectionDialog(Shell shell, IType[] elements, String title) {
    super(shell, true);
    setTitle(title);
    fTypes = elements;
    setMessage("Select &type (? = any character, * = any String, TZ = TimeZone):");
    setInitialPattern("**"); //$NON-NLS-1$
    setListLabelProvider(new DebugTypeLabelProvider());
    setDetailsLabelProvider(new DebugTypeDetailsLabelProvider());
    setSelectionHistory(new DebugTypeSelectionHistory());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createDialogArea(org
   * .eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Control ctrl = super.createDialogArea(parent);
    PlatformUI.getWorkbench().getHelpSystem().setHelp(ctrl,
        IJavaDebugHelpContextIds.SELECT_MAIN_METHOD_DIALOG);
    return ctrl;
  }

  /**
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getDialogSettings()
   */
  @Override
  protected IDialogSettings getDialogSettings() {
    IDialogSettings settings = RandoopPlugin.getDefault().getDialogSettings();
    IDialogSettings section = settings.getSection(SETTINGS_ID);
    if (section == null) {
      section = settings.addNewSection(SETTINGS_ID);
    }
    return section;
  }

  /**
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getItemsComparator()
   */
  @Override
  protected Comparator getItemsComparator() {
    Comparator comp = new Comparator() {
      public int compare(Object o1, Object o2) {
        if (o1 instanceof IType && o2 instanceof IType) {
          return ((IType) o1).getElementName().compareTo(
              ((IType) o2).getElementName());
        }
        return -1;
      }
    };
    return comp;
  }

  /**
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#validateItem(java.lang.Object)
   */
  @Override
  protected IStatus validateItem(Object item) {
    return Status.OK_STATUS;
  }

  /**
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createExtendedContentArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createExtendedContentArea(Composite parent) {
    return null;
  }

  /**
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createFilter()
   */
  @Override
  protected ItemsFilter createFilter() {
    return new DebugTypeItemsFilter();
  }

  /**
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#fillContentProvider(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.AbstractContentProvider,
   *      org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  protected void fillContentProvider(AbstractContentProvider contentProvider,
      ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
      throws CoreException {
    if (fTypes != null && fTypes.length > 0) {
      for (int i = 0; i < fTypes.length; i++) {
        if (itemsFilter.isConsistentItem(fTypes[i])) {
          contentProvider.add(fTypes[i], itemsFilter);
        }
      }
    }
  }

  /**
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getElementName(java.lang.Object)
   */
  @Override
  public String getElementName(Object item) {
    if (item instanceof IType) {
      return ((IType) item).getElementName();
    }
    return null;
  }
}
