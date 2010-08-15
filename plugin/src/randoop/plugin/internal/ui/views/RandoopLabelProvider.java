package randoop.plugin.internal.ui.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.ui.RandoopPluginImages;
import randoop.plugin.model.resultstree.FailingMember;
import randoop.plugin.model.resultstree.FailureKind;
import randoop.plugin.model.resultstree.Failures;
import randoop.plugin.model.resultstree.RunResultsTree;

/**
 * Provides labels for the various trees displayed in the Randoop view.
 */
public class RandoopLabelProvider extends LabelProvider {

  @Override
  public Image getImage(Object element) {
    Image image = null;
    if (element instanceof FailingMember) {
      image = RandoopPlugin.getDefault().getImageRegistry().get(RandoopPluginImages.IMG_OBJS_CUNIT);
    } else if (element instanceof FailureKind) {
      // The class icon should come from the platform's shared images as shown below, but
      // doing so returns null, so we're saving the icon under randoop's icons/ directory
      // until we figure out what's going wrong with shared images.
      //
      // PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS_DEFAULT);
      // or
      // RandoopPlugin.getDefault().getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_CFILE);
      
      // TODO: Failure kinds should be stored in an enum or public static ints 
      FailureKind kind = (FailureKind) element;
      if (kind.getClassName().equals("class randoop.EqualsToNullRetFalse")) {
        image = RandoopPlugin.getDefault().getImageRegistry().get(RandoopPluginImages.IMG_OBJS_FAILR_EQNULL);
      } else  if (kind.getClassName().equals("NPEs / Assertion violations")) {
        image = RandoopPlugin.getDefault().getImageRegistry().get(RandoopPluginImages.IMG_OBJS_FAILR_NPE);
      } else  if (kind.getClassName().equals("class randoop.EqualsReflexive")) {
        image = RandoopPlugin.getDefault().getImageRegistry().get(RandoopPluginImages.IMG_OBJS_FAILR_EQ);
      } else  if (kind.getClassName().equals("class randoop.EqualsHashcode")) {
        image = RandoopPlugin.getDefault().getImageRegistry().get(RandoopPluginImages.IMG_OBJS_FAILR_HASHCD);
      } else {
        // Return a generic error image
        return PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_DEC_FIELD_ERROR);
      }
    } else if (element instanceof Failures) {
      image = RandoopPlugin.getDefault().getImageRegistry().get(RandoopPluginImages.IMG_OBJS_FAILRS_PARENT);
    } else if (element instanceof RunResultsTree) {
      return null;
    } else {
      throw new RuntimeException("unknown tree element: " + element.getClass());
    }

    return image;  
  }

  @Override
  public String getText(Object element) {
    
    if (element instanceof FailingMember) {
      return ((FailingMember)element).description;
    } else if (element instanceof FailureKind) {
      return ((FailureKind)element).className;
    } else if (element instanceof Failures) {
      return "Failures";
    } else if (element instanceof RunResultsTree) {
      return "Results";
    } else {
        throw new RuntimeException("unknown tree element: " + element.getClass());
    }
  }

}
