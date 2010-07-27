package randoop.plugin.internal.ui.options;

import randoop.plugin.internal.IConstants;

public class Mnemonics {
  // expects use of $
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
