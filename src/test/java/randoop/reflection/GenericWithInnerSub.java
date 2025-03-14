package randoop.reflection;

/**
 * Input class based on BoofCV class {@code
 * boofcv.alg.feature.detect.template.TemplateDiffSquared.F32} which extends {@code
 * boofcv.alg.feature.detect.template.TemplateDiffSquared<Gray32>} which extends {@code
 * boofcv.alg.feature.detect.template.BaseTemplateIntensity<T>} which implements {@code
 * boofcv.alg.feature.detect.template.TemplateMatchingIntensity<T>}
 *
 * <p>Scenario is that member class of a generic extends an instantiation of the generic enclosing
 * class.
 */
public abstract class GenericWithInnerSub<T> extends GenericBaseForInnerSub<T> {
  public static class Inner extends GenericWithInnerSub<String> {}
}
