package randoop.types;

/**
 * Enum used to indicate whether types should be matched exactly or using widening.
 */
public enum Match {
  /** Types should be identical */
  EXACT_TYPE,

  /** Types may match by rules of widening */
  COMPATIBLE_TYPE
}
