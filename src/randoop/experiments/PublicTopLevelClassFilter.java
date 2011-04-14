package randoop.experiments;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.JavaClass;

public class PublicTopLevelClassFilter implements ClassFilter {

  private final Pattern omitPattern;

  /**
   * @param omitPatternString can be null.
   */
  public PublicTopLevelClassFilter(String omitPatternString) {
    if (omitPatternString != null) {
      this.omitPattern = Pattern.compile(omitPatternString);
    } else {
      this.omitPattern = null;
    }
  }

  public boolean include(JavaClass cls) {
    assert cls != null;

    // Do not include abstracts, interfaces or non-publics.
    if (cls.isAbstract())
      return false;
    if (cls.isInterface())
      return false;
    if (!cls.isPublic())
      return false;
    if (cls.getClassName().contains("$"))
      return false;

    String fullName = cls.getPackageName() + "." + cls.getClassName();

    if (omitPattern != null) {
      Matcher m = omitPattern.matcher(fullName);
      if (m.find() == true)
        return false;
    }

    return true;
  }
}
