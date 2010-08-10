package randoop.plugin.internal.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.MutableBoolean;

public class MessageUtil {
  
  public static boolean openQuestion(final String message) {
    final MutableBoolean okToProceed = new MutableBoolean(false);
    
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        okToProceed.setValue(MessageDialog.openQuestion(
            RandoopPlugin.getDisplay().getActiveShell(), "Randoop", message));
      }
    });

    return okToProceed.getValue();
  }

  public static boolean openInformation(final String message) {
    final MutableBoolean okToProceed = new MutableBoolean(false);

    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        MessageDialog.openInformation(
            RandoopPlugin.getDisplay().getActiveShell(), "Randoop", message);
      }
    });

    return okToProceed.getValue();
  }
  
}
