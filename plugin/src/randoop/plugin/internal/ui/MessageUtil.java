package randoop.plugin.internal.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.ui.text.java.ClasspathFixProcessor.ClasspathFixProposal;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
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
            PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Randoop", message));
      }
    });

    return okToProceed.getValue();
  }
  
  public static boolean openResourcesQuestion(final String message, final IResource[] resources) {
    final MutableBoolean okToProceed = new MutableBoolean(false);
    
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        Dialog d = new ResourcesListQuestionDialog(PlatformUI.getWorkbench().getDisplay()
            .getActiveShell(), "Randoop", message, resources);
        
        okToProceed.setValue(d.open() == Dialog.OK);
      }
    });

    return okToProceed.getValue();
  }
  
  static private class ResourcesListQuestionDialog extends MessageDialog implements IDoubleClickListener {
    
    static class ResourceLabelProvider extends LabelProvider {
      @Override
      public Image getImage(Object element) {
        if (element instanceof IFile) {
          return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
        } else if (element instanceof IFolder) {
          return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
        }
        
        return null;
      }

      @Override
      public String getText(Object element) {
        if (element instanceof IResource) {
          return ((IResource) element).getName();
        }
        return null;
      }
    }
    
    private TableViewer fFixSelectionTable;
    
    private IResource[] fResourceList;
    
    public ResourcesListQuestionDialog(Shell parentShell, String title, String message,
        IResource[] resources) {
      
      super(parentShell, title, null, message, MessageDialog.QUESTION,
          new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
      
      fResourceList = resources;
    }

    @Override
    protected Control createCustomArea(Composite composite) {
      if (fResourceList.length != 0) {
        fFixSelectionTable = new TableViewer(composite, SWT.SINGLE | SWT.BORDER);
        fFixSelectionTable.setContentProvider(new ArrayContentProvider());
        fFixSelectionTable.setLabelProvider(new ResourceLabelProvider());
        fFixSelectionTable.setComparator(new ViewerComparator());
        fFixSelectionTable.setInput(fResourceList);
        fFixSelectionTable.addDoubleClickListener(this);
        fFixSelectionTable.setSelection(new StructuredSelection(fResourceList[0]));
      }
      
      GridData gridData= new GridData(SWT.FILL, SWT.FILL, true, true);
      gridData.heightHint= convertHeightInCharsToPixels(4);
      gridData.horizontalIndent= convertWidthInCharsToPixels(2);

      fFixSelectionTable.getControl().setLayoutData(gridData);
      
      return composite;
    }

    @Override
    public void doubleClick(DoubleClickEvent event) {
      IStructuredSelection selection= (IStructuredSelection) fFixSelectionTable.getSelection();
      Object firstElement = selection.getFirstElement();
      if (firstElement instanceof IFile) {
        IFile file = (IFile) firstElement;
        
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
        try {
          page.openEditor(new FileEditorInput(file), desc.getId());
        } catch (PartInitException e) {
          RandoopPlugin.log(e);
        }
      }
      
      setReturnCode(CANCEL);
      close();
    }
  }
  
}
