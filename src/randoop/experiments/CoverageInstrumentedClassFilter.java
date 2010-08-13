package randoop.experiments;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;

public class CoverageInstrumentedClassFilter implements ClassFilter {

  public boolean include(JavaClass cls) {
    for (Field f : cls.getFields()) {
      if (f.getName().equals(cov.Constants.isInstrumentedField))
        return true;
    }
    return false;
  }

}
