package randoop.generation;

import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;

/**
 * This class is a wrapper for the ConstantMining. The reason why the fields are enumerated for each
 * literal level instead of having one generic one is that we do not wish to introduce generic
 * constants to ComponentManager.
 */
public class ConstantMiningWrapper {

  // Either all of the next 3 fields are null, or at most one of them is non-null. It is based on
  // the
  // user's input about the literals level.

  /** The storage for the class level constant mining information. */
  public ConstantMiningStorage<ClassOrInterfaceType> classLevel;

  /** The storage for the package level constant mining information. */
  public ConstantMiningStorage<Package> packageLevel;

  /** The storage for the all level constant mining information. */
  public ConstantMiningStorage<Object> allLevel; // Note: The generic type doesn't matter

  /** Creates a new ConstantMiningWrapper with empty classLevel, packageLevel, and allLevel. */
  public ConstantMiningWrapper() {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        classLevel = new ConstantMiningStorage<>();
        break;
      case PACKAGE:
        packageLevel = new ConstantMiningStorage<>();
        break;
      case ALL:
        allLevel = new ConstantMiningStorage<>();
        break;
      default:
        throw new RuntimeException("Unknown literals level");
    }
  }

  /**
   * Returns the class level constant mining storage.
   *
   * @return the class level constant mining storage
   */
  public ConstantMiningStorage<ClassOrInterfaceType> getClassLevel() {
    return classLevel;
  }

  /**
   * Returns the package level constant mining storage.
   *
   * @return the package level constant mining storage
   */
  public ConstantMiningStorage<Package> getPackageLevel() {
    return packageLevel;
  }

  /**
   * Returns the all level constant mining storage.
   *
   * @return the all level constant mining storage
   */
  public ConstantMiningStorage<Object> getAllLevel() {
    return allLevel;
  }

  /**
   * Adds the frequency of the sequence to the corresponding storage based on the literals level.
   *
   * @param type the type of the class
   * @param seq the sequence
   * @param frequency the frequency of the sequence
   */
  public void addFrequency(Object type, Sequence seq, int frequency) {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        classLevel.addFrequency((ClassOrInterfaceType) type, seq, frequency);
        break;
      case PACKAGE:
        Package pkg = ((ClassOrInterfaceType) type).getPackage();
        packageLevel.addFrequency(pkg, seq, frequency);
        break;
      case ALL:
        allLevel.addFrequency(null, seq, frequency);
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
  public void addClassesWithConstant(Object type, Sequence seq, int classesWithConstant) {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        throw new RuntimeException("Should not update classesWithConstant in CLASS level");
      case PACKAGE:
        Package pkg = ((ClassOrInterfaceType) type).getPackage();
        packageLevel.addClassesWithConstant(pkg, seq, classesWithConstant);
        break;
      case ALL:
        allLevel.addClassesWithConstant(null, seq, classesWithConstant);
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
  public void addTotalClasses(Object type, int totalClasses) {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        throw new RuntimeException("Should not update totalClasses in CLASS level");
      case PACKAGE:
        Package pkg = ((ClassOrInterfaceType) type).getPackage();
        packageLevel.addTotalClasses(pkg, totalClasses);
        break;
      case ALL:
        allLevel.addTotalClasses(null, totalClasses);
        break;
      default:
        throw new RuntimeException("Unknown literals level");
    }
  }
}
