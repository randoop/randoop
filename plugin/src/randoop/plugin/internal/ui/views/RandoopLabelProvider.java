package randoop.plugin.internal.ui.views;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.model.resultstree.FailureKind;
import randoop.plugin.model.resultstree.FailingMember;
import randoop.plugin.model.resultstree.Failures;
import randoop.plugin.model.resultstree.RunResultsTree;
import randoop.plugin.model.resultstree.UnitTest;

/**
 * Provides labels for the various trees displayed in the Randoop view.
 */
public class RandoopLabelProvider extends LabelProvider {

  private Map<ImageDescriptor, Image> imageCache = new LinkedHashMap<ImageDescriptor, Image>();
  
  @Override
  public Image getImage(Object element) {
    ImageDescriptor descriptor = null;
    if (element instanceof FailingMember) {
      descriptor = RandoopPlugin.getImageDescriptor("icons/methpub_obj.gif");
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
        descriptor = RandoopPlugin.getImageDescriptor("icons/failure_equalsToNull.png");
      } else  if (kind.getClassName().equals("NPEs / Assertion violations")) {
        descriptor = RandoopPlugin.getImageDescriptor("icons/failure_npe.png");
      } else  if (kind.getClassName().equals("class randoop.EqualsReflexive")) {
        descriptor = RandoopPlugin.getImageDescriptor("icons/failure_equals.png");
      } else  if (kind.getClassName().equals("class randoop.EqualsHashcode")) {
        descriptor = RandoopPlugin.getImageDescriptor("icons/failure_equalsHashCode.png");
      }
    } else if (element instanceof Failures) {
      descriptor = RandoopPlugin.getImageDescriptor("icons/bomb.png");
    } else if (element instanceof RunResultsTree) {
      return null;
    } else {
      throw new RuntimeException("unknown tree element: " + element.getClass());
    }

    //obtain the cached image corresponding to the descriptor
    Image image = imageCache.get(descriptor);
    if (image == null) {
        image = descriptor.createImage();
        imageCache.put(descriptor, image);
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

  @Override
  public void dispose() {
    for (Image image : imageCache.values()) {
      image.dispose();
    }
    imageCache.clear();
  }
}
