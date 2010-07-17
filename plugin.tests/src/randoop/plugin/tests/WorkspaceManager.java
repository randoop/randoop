package randoop.plugin.tests;

import java.util.HashMap;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import randoop.plugin.internal.core.MutableBoolean;

public class WorkspaceManager extends TestCase {
  private static HashMap<String, Boolean> okToDeleteByWorkspace = new HashMap<String, Boolean>();
  
  /**
   * Checks if the user has authorized the deletion of the given workspace
   * 
   * @return <code>true</code> if the workspace can be deleted,
   *         <code>false</code> if the user has not authorized this workspaces
   *         deletion or has never been asked.
   */
  public static boolean canDeleteWorkspace(IWorkspaceRoot root) {
    Boolean okToDeleteWorkspace = okToDeleteByWorkspace.get(getKey(root));
    if (okToDeleteWorkspace != null) {
      return okToDeleteWorkspace.booleanValue();
    }
    
    return false;
  }

  /**
   * Checks if a request has been made earlier to delete the given workspace
   * 
   * @param root
   * @return <code>true</code> the user has explicitely chosen to or not to
   *         delete the given workspace
   */
  public static boolean hasDeletionBeenRequestion(IWorkspaceRoot root) {
    return okToDeleteByWorkspace.containsKey(getKey(root));
  }
  
  /**
   * Opens a dialog requesting to delete the given workspace
   * 
   * @param root
   * @return <code>true</code> if it is okay to delete the workspace
   */
  public static boolean requestDeletionOfWorkspace(final IWorkspaceRoot root) {
    // Otherwise, unknown. Prompt the user.
    final MutableBoolean userResponse = new MutableBoolean(false);
    Display.getDefault().syncExec(new Runnable() {
      @Override
      public void run() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

        userResponse.setValue(MessageDialog.openQuestion(window.getShell(), "Warning", //$NON-NLS-1$
            "This test will delete all contents of the active workspace:\n" //$NON-NLS-1$
                + root.getLocation().toOSString() + "\n\n" //$NON-NLS-1$
                + "Do you want to continue? (Pressing Yes will delete workspace)")); //$NON-NLS-1$
      }
    });

    return userResponse.getValue();
  }

  private static String getKey(IWorkspaceRoot root) {
    return root.getLocation().toOSString();
  }

  private static boolean clearWorkspace(IWorkspaceRoot root) {
    boolean doClearWorkspace = canDeleteWorkspace(root);
    
    // Prompt the user for deletion if necessary
    if (!doClearWorkspace) {
      if (!hasDeletionBeenRequestion(root)) {
        doClearWorkspace = requestDeletionOfWorkspace(root);
        okToDeleteByWorkspace.put(getKey(root), doClearWorkspace);
      }
    }
    
    if (doClearWorkspace) {
      for (IProject project : root.getProjects()) {
        try {
          project.delete(true, true, null);
        } catch (CoreException e) {
          e.printStackTrace();
        }
      }
      return true;
    }
    return false;
  }
  
  /**
   * Clears the current workspace if the user allows
   * 
   * @return <code>true</code> if the workspace was cleared, <code>false</code>
   *         otherwise
   */
  public static boolean clearActiveWorkspace() {
    IWorkspaceRoot root = getWorkspaceRoot();
    return clearWorkspace(root);
  }

  private static IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }

}

