package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.Assert;
import randoop.plugin.internal.IConstants;

public class Mnemonics {
  final static char TYPE_NAME_END = '#';
  final static char METHOD_NAME_END = '%';
  
  // expects use of $
  public static String[] splitFullyQualifiedName(String fqname) {
    int seperator = fqname.lastIndexOf('.');
    if (seperator == -1) {
      String[] packageAndName = { IConstants.EMPTY_STRING, fqname };
      return packageAndName;
    } else {
      String[] packageAndName = { fqname.substring(0, seperator),
          fqname.substring(seperator + 1) };
      return packageAndName;
    }
  }
  
  public static String getFullyQualifiedName(String[] packageAndName) {
    Assert.isLegal(packageAndName.length == 2);
    
    return getFullyQualifiedName(packageAndName[0], packageAndName[1]);
  }
  
  public static String getPackageName(String fullyQualifiedName) {
    int lastDelimiter = fullyQualifiedName.lastIndexOf('.');
    
    if (lastDelimiter == -1) {
      return IConstants.EMPTY_STRING;
    } else {
      return fullyQualifiedName.substring(0, lastDelimiter);
    }
  }
  
  public static String getClassName(String fullyQualifiedName) {
    int lastDelimiter = fullyQualifiedName.lastIndexOf('.');
    
    if (lastDelimiter == -1) {
      return fullyQualifiedName;
    } else {
      return fullyQualifiedName.substring(lastDelimiter + 1);
    }
  }

  public static String getFullyQualifiedName(String packageName, String className) {
    if (packageName.equals(IConstants.EMPTY_STRING)) {
      return className;
    } else {
      return packageName + '.' + className;
    }
  }
  
}
