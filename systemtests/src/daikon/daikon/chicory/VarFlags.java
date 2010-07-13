package daikon.chicory;

/**
 * An enumeration of various flags that give information about
 * variables. This enum is also present in Daikon (in the
 * <code>VarInfo</code> class), but is <i>intentionally</i> not shared
 * in an effort to keep Daikon and Chicory completely independent.
 * <p>
 * These names are written into decl files; they should be the same
 * across every Daikon front-end (Java or otherwise), so please do not
 * change them without a very good reason.
 */
public enum VarFlags {
    IS_PARAM,
    NO_DUPS,
    NOT_ORDERED,
    NO_SIZE,
    NOMOD,
    SYNTHETIC,
    CLASSNAME,
    TO_STRING,
    NON_NULL
};
