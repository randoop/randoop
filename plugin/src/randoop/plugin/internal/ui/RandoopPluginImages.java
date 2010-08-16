package randoop.plugin.internal.ui;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.osgi.framework.Bundle;

import randoop.plugin.RandoopPlugin;

/**
 * 
 * @author Peter Kalauskas
 */
public class RandoopPluginImages {

  public static final String NAME_PREFIX = RandoopPlugin.getPluginId();

  public static final IPath ICONS_PATH = new Path("/icons/"); //$NON-NLS-1$

  private static final String T_ELCL = "elcl16"; //$NON-NLS-1$
  private static final String T_ETOOL = "etool16"; //$NON-NLS-1$
  private static final String T_EVIEW = "eview16"; //$NON-NLS-1$
  private static final String T_OBJ = "obj16"; //$NON-NLS-1$
  private static final String T_WIZBAN = "wizban"; //$NON-NLS-1$

  public static final String IMG_VIEW_RANDOOP = NAME_PREFIX + "IMG_VIEW_RANDOOP"; //$NON-NLS-1$
  public static final String IMG_VIEW_ARGUMENTS_TAB = NAME_PREFIX
      + "IMG_VIEW_ARGUMENTS_TAB"; //$NON-NLS-1$

  public static final String IMG_OBJS_FAILR_EQNULL = NAME_PREFIX
      + "IMG_OBJS_FAILR_EQNULL"; //$NON-NLS-1$
  public static final String IMG_OBJS_FAILR_EQ = NAME_PREFIX + "IMG_OBJS_FAILR_EQ"; //$NON-NLS-1$
  public static final String IMG_OBJS_FAILR_HASHCD = NAME_PREFIX
      + "IMG_OBJS_FAILR_HASHCD"; //$NON-NLS-1$
  public static final String IMG_OBJS_FAILR_NPE = NAME_PREFIX + "IMG_OBJS_FAILR_NPE"; //$NON-NLS-1$
  public static final String IMG_OBJS_FAILRS_PARENT = NAME_PREFIX
      + "IMG_OBJS_FAILRS_PARENT"; //$NON-NLS-1$
  public static final String IMG_OBJS_CUNIT = NAME_PREFIX + "IMG_OBJS_CUNIT"; //$NON-NLS-1$

  public static final ImageDescriptor DESC_ELCL_DEBUG_JUNIT = create(T_ELCL, "debugjunit.gif"); //$NON-NLS-1$
  public static final ImageDescriptor DESC_ELCL_RUN_JUNIT = create(T_ELCL, "runjunit.gif");//$NON-NLS-1$
  public static final ImageDescriptor DESC_ELCL_RUN_RANDOOP = create(T_ELCL, "runrandoop.gif");//$NON-NLS-1$

  public static final ImageDescriptor DESC_WIZBAN_NEW_RNDP = create(T_WIZBAN, "new_randoop_wiz.png"); //$NON-NLS-1$

  public static void declareImages(ImageRegistry r) {
    declareImage(r, IMG_VIEW_ARGUMENTS_TAB, T_EVIEW, "variable_tab.gif"); //$NON-NLS-1$

    declareImage(r, IMG_OBJS_FAILR_EQNULL, T_OBJ, "failr_equls_null_obj.gif"); //$NON-NLS-1$
    declareImage(r, IMG_OBJS_FAILR_EQ, T_OBJ, "failr_equls_obj.gif"); //$NON-NLS-1$
    declareImage(r, IMG_OBJS_FAILR_HASHCD, T_OBJ, "failr_hashcd_obj.gif"); //$NON-NLS-1$
    declareImage(r, IMG_OBJS_FAILR_NPE, T_OBJ, "failr_npe_obj.gif"); //$NON-NLS-1$
    declareImage(r, IMG_OBJS_FAILRS_PARENT, T_OBJ, "failrs_parent_obj.gif"); //$NON-NLS-1$
    declareImage(r, IMG_OBJS_CUNIT, T_OBJ, "jcu_obj.gif"); //$NON-NLS-1$
  }

  private static void declareImage(ImageRegistry registry, String key, String type,
      String filename) {
    ImageDescriptor desc = create(type, filename);
    registry.put(key, desc);
  }

  private static ImageDescriptor create(String type, String filename) {
    ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
    IPath filepath = ICONS_PATH.append(type).append(filename);

    Bundle bundle = Platform.getBundle(RandoopPlugin.getPluginId());
    if (bundle != null) {
      URL url = FileLocator.find(bundle, filepath, null);
      if (url != null) {
        desc = ImageDescriptor.createFromURL(url);
      }
    }

    return desc;
  }

}
