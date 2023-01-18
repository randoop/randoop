package randoop.reflection;

/**
 * Test input based on class from BoofCV, {@code
 * boofcv.alg.background.moving.BackgroundMovingBasic_IL}, where {@code BMB} represents this class.
 */
interface IT<T extends IT<T>> {}

@SuppressWarnings("rawtypes")
abstract class II<T extends II> {}

class AT implements IT<AT> {}

@SuppressWarnings("rawtypes")
class AI extends II {}

@SuppressWarnings("rawtypes")
public class BMB<T extends II, M extends IT<M>> {}
