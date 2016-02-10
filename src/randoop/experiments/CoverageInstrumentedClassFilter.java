package randoop.experiments;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;

public class CoverageInstrumentedClassFilter implements ClassFilter {

  @Override
  public boolean include(JavaClass cls) {
    for (Field f : cls.getFields()) {
      if (f.getName().equals(cov.Constants.IS_INSTRUMENTED_FIELD))
        return true;
    }
    return false;
  }

}
