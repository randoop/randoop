package randoop.generation.constanttfidf;

import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.main.GenInputsAbstract;
import randoop.main.GenInputsAbstract.ClassLiteralsMode;
import randoop.main.RandoopBug;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;

/** This class contains a {@link ConstantMiningStatistics}. */
public class ConstantMiningStorageManager {

  // The reason why the fields are enumerated for each literal level instead of having one generic
  // one is that we do not wish to introduce generic constants to ComponentManager.

  // All of the next 3 fields are null when ConstantMining is disabled, otherwise at most one of
  // is non-null. It is based on the user's input about the literals level.

  /** The storage for the class level constant mining information. */
  public final @Nullable ConstantMiningStatistics<ClassOrInterfaceType> classLevel;

  /** The storage for the package level constant mining information. */
  public final @Nullable ConstantMiningStatistics<Package> packageLevel;

  /** The storage for the all level constant mining information. */
  // Note: The generic type doesn't matter
  public final @Nullable ConstantMiningStatistics<Object> allLevel;

  /**
   * Creates a new ConstantMiningStorageManager with empty classLevel, packageLevel, and allLevel.
   */
  public ConstantMiningStorageManager() {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        classLevel = new ConstantMiningStatistics<>();
        packageLevel = null;
        allLevel = null;
        break;
      case PACKAGE:
        classLevel = null;
        packageLevel = new ConstantMiningStatistics<>();
        allLevel = null;
        break;
      case ALL:
        classLevel = null;
        packageLevel = null;
        allLevel = new ConstantMiningStatistics<>();
        break;
      default:
        throw new RuntimeException("Unknown literals level");
    }
  }

  // An advantage of having three different get*Level methods is that their return type differs.

  /**
   * Returns the class level constant mining storage.
   *
   * @return the class level constant mining storage
   */
  public ConstantMiningStatistics<ClassOrInterfaceType> getClassLevel() {
    if (GenInputsAbstract.literals_level == ClassLiteralsMode.CLASS && classLevel != null) {
      return classLevel;
    }
    throw new RandoopBug(
        String.format(
            "getClassLevel(): literals_level=%s, classLevel=%s",
            GenInputsAbstract.literals_level, classLevel));
  }

  /**
   * Returns the package level constant mining storage.
   *
   * @return the package level constant mining storage
   */
  public ConstantMiningStatistics<Package> getPackageLevel() {
    if (GenInputsAbstract.literals_level == ClassLiteralsMode.PACKAGE && packageLevel != null) {
      return packageLevel;
    }
    throw new RandoopBug(
        String.format(
            "getPackageLevel(): literals_level=%s, packageLevel=%s",
            GenInputsAbstract.literals_level, packageLevel));
  }

  /**
   * Returns the all level constant mining storage.
   *
   * @return the all level constant mining storage
   */
  public ConstantMiningStatistics<Object> getAllLevel() {
    if (GenInputsAbstract.literals_level == ClassLiteralsMode.ALL && allLevel != null) {
      return allLevel;
    }
    throw new RandoopBug(
        String.format(
            "getAllLevel(): literals_level=%s, allLevel=%s",
            GenInputsAbstract.literals_level, allLevel));
  }

  /**
   * Adds the frequency of the sequence to the corresponding storage based on the literals level.
   *
   * @param type the type of the class
   * @param seq the sequence
   * @param frequency the frequency of the sequence
   */
  public void addUses(Object type, Sequence seq, int frequency) {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        classLevel.addUses((ClassOrInterfaceType) type, seq, frequency);
        break;
      case PACKAGE:
        Package pkg = ((ClassOrInterfaceType) type).getPackage();
        packageLevel.addUses(pkg, seq, frequency);
        break;
      case ALL:
        allLevel.addUses(null, seq, frequency);
        break;
      default:
        throw new RuntimeException("Unknown literals level");
    }
  }

  /**
   * Adds the classesWithConstant of the sequence to the corresponding storage based on the literals
   * level.
   *
   * @param type the type of the class
   * @param seq the sequence
   * @param classesWithConstant the number of classes in the current scope that contain the sequence
   */
  public void addToNumClassesWith(Object type, Sequence seq, int classesWithConstant) {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        throw new RuntimeException("Should not update classesWithConstant in CLASS level");
      case PACKAGE:
        Package pkg = ((ClassOrInterfaceType) type).getPackage();
        packageLevel.addToNumClassesWith(pkg, seq, classesWithConstant);
        break;
      case ALL:
        allLevel.addToNumClassesWith(null, seq, classesWithConstant);
        break;
      default:
        throw new RuntimeException("Unknown literals level");
    }
  }

  /**
   * Adds the totalClasses of the sequence to the corresponding storage based on the literals level.
   *
   * @param type the type of the class
   * @param totalClasses the total number of classes in the current scope
   */
  public void addToTotalClasses(Object type, int totalClasses) {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        throw new RuntimeException("Should not update totalClasses in CLASS level");
      case PACKAGE:
        Package pkg = ((ClassOrInterfaceType) type).getPackage();
        packageLevel.addToTotalClasses(pkg, totalClasses);
        break;
      case ALL:
        allLevel.addToTotalClasses(null, totalClasses);
        break;
      default:
        throw new RuntimeException("Unknown literals level");
    }
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();

    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        sb.append("Class Level");
        sb.append(System.lineSeparator());
        sb.append("Class Frequency Map");
        sb.append(System.lineSeparator());
        ConstantMiningStatistics<ClassOrInterfaceType> classLevel = getClassLevel();
        ConstantMiningStatistics.formatMapMap(sb, "  ", "class=", classLevel.getNumUses());
        break;
      case PACKAGE:
        sb.append("Package Level");
        sb.append(System.lineSeparator());
        sb.append("Package Frequency Map");
        sb.append(System.lineSeparator());
        ConstantMiningStatistics<Package> packageLevel = getPackageLevel();
        ConstantMiningStatistics.formatMapMap(sb, "  ", "package=", packageLevel.getNumUses());
        sb.append("Package classWithConstant Map");
        sb.append(System.lineSeparator());
        ConstantMiningStatistics.formatMapMap(sb, "  ", "class=", packageLevel.getNumClassesWith());
        break;
      case ALL:
        sb.append("All Level");
        sb.append(System.lineSeparator());
        sb.append("Global Frequency Map");
        sb.append(System.lineSeparator());
        ConstantMiningStatistics<Object> allLevel = getAllLevel();
        ConstantMiningStatistics.formatMap(sb, "  ", allLevel.getNumUses().get(null));
        sb.append("Global classesWithConstants Map");
        sb.append(System.lineSeparator());
        ConstantMiningStatistics.formatMap(sb, "  ", allLevel.getNumClassesWith().get(null));
        break;
      default:
        throw new RandoopBug("Unexpected literals level: " + GenInputsAbstract.literals_level);
    }

    return sb.toString();
  }
}
