package randoop.plugin.internal.ui.views;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import randoop.ErrorRevealed;
import randoop.plugin.RandoopPlugin;

/**
 * Provides labels for the various trees displayed in the Randoop view.
 */
public class RandoopLabelProvider extends LabelProvider {

  private Map<ImageDescriptor, Image> imageCache = new LinkedHashMap<ImageDescriptor, Image>();
  
  @Override
  public Image getImage(Object element) {
    ImageDescriptor descriptor = null;
    if (element instanceof ErrorRevealed) {
      descriptor = RandoopPlugin.getImageDescriptor("icons/bug_error.png");
    } else if (element instanceof String) {
      // The class icon should come from the platform's shared images as shown below, but
      // doing so returns null, so we're saving the icon under randoop's icons/ directory
      // until we figure out what's going wrong with shared images.
      //
      // PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS_DEFAULT);
      // or
      // RandoopPlugin.getDefault().getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_CFILE);
      descriptor = RandoopPlugin.getImageDescriptor("icons/class_obj.gif");
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
    if (element instanceof ErrorRevealed) {
      return ((ErrorRevealed) element).description;
    } else if (element instanceof String) {
      return (String) element;
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
