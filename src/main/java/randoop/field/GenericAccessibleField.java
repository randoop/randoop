package randoop.field;

import java.lang.reflect.Field;
import java.util.Objects;

import randoop.types.GeneralType;
import randoop.types.GenericType;
import randoop.types.GenericTypeTuple;
import randoop.types.Substitution;

/**
 * {@code GenericAccessibleField} is an abstract class representing a field of a generic class.
 *  The field may have concrete or generic type.
 */
public abstract class GenericAccessibleField {

    /** The reflective field */
    private final Field field;

    /** The generic class type to which this field belongs */
    private final GenericType declaringType;

    /** The generic type of this field */
    private final GeneralType valueType;

    /**
     * Creates a generic field.
     *
     * @param field  the reflective field type
     * @param declaringType  the generic class type in which this field is declared
     * @param valueType  the declared generic type of this field
     */
    public GenericAccessibleField(Field field, GenericType declaringType, GeneralType valueType) {
        this.field = field;
        this.declaringType = declaringType;
        this.valueType = valueType;
    }

    /**
     * Return the declaring type for this field.
     *
     * @return the declaring class for this field
     */
    public GenericType getDeclaringType() { return declaringType; }
    public GeneralType getType() { return valueType; }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof GenericAccessibleField)) { return false; }
        GenericAccessibleField f = (GenericAccessibleField)obj;
        return this.field.equals(f.field)
                && this.declaringType.equals(f.declaringType)
                && this.valueType.equals(f.valueType);
    }

    @Override
    public int hashCode() { return Objects.hash(field, declaringType, valueType); }

    public abstract AccessibleField instantiate(Substitution substitution);

    public abstract GenericTypeTuple getAccessTypes();

    public abstract GenericTypeTuple getSetTypes();
}
