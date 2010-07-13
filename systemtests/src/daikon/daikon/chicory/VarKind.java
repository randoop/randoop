package daikon.chicory;

/**
 * An enumeration of the various kinds of variables. This same
 * information is present within Daikon (in the <code>VarInfo</code>
 * class), but is <i>intentionally</i> not shared with Daikon in an
 * effort to keep Daikon and Chicory completely independent.
 * <p>
 * These names are written into decl files; they should be the same
 * across every Daikon front-end (Java or otherwise), so please do not
 * change them without a very good reason.
 */
public enum VarKind {
    FIELD,
    FUNCTION,
    ARRAY,
    VARIABLE,
    RETURN
};
