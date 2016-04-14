package randoop.types;

/**
 * Created by bjkeller on 4/13/16.
 */
public interface TypeArgument {
  TypeBound getBound();
  boolean isGeneric();
}
