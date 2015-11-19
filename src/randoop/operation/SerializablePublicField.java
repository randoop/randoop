package randoop.operation;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;

import randoop.types.TypeNames;

public class SerializablePublicField implements Serializable {

  private static final long serialVersionUID = 9109946164794213814L;
  private final String fieldRep;
  
  public SerializablePublicField(Field field) {
    this.fieldRep = field.getDeclaringClass().getName() + "." + field.getName();
  }
  
  private Object readResolve() throws ObjectStreamException, ClassNotFoundException {
    int pos = fieldRep.lastIndexOf('.');
    String className = fieldRep.substring(0,pos);
    String fieldName = fieldRep.substring(pos + 1);
    Class<?> c = TypeNames.recognizeType(className);
    Field field = PublicFieldParser.fieldFor(c, fieldName);
    if (field != null) {
      return PublicFieldParser.recognize(field);
    }
    return null;
  }
  
}
