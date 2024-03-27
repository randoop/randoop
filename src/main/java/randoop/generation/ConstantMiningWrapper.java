package randoop.generation;

import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;

public class ConstantMiningWrapper {

    /** This class is a wrapper for the ConstantMining. The reason why the fields are enumerated for each literal level
     * instead of having one generic one is that we do not wish to introduce generic constants to ComponentManager.
     * If we finally decide to not have this wrapper, the migration would be easy enough. */

    public ConstantMiningStorage<ClassOrInterfaceType> classLevel;

    public ConstantMiningStorage<Package> packageLevel;

    // The generic type doesn't matter
    public ConstantMiningStorage<Object> allLevel;

    public ConstantMiningWrapper() {
        classLevel = new ConstantMiningStorage<>();
        packageLevel = new ConstantMiningStorage<>();
        allLevel = new ConstantMiningStorage<>();
    }

    public void addFrequency(Object type, Sequence seq, int frequency) {
        switch (GenInputsAbstract.literals_level) {
            case CLASS:
                classLevel.addFrequency((ClassOrInterfaceType) type, seq, frequency);
                break;
            case PACKAGE:
                packageLevel.addFrequency((Package) type, seq, frequency);
                break;
            case ALL:
                allLevel.addFrequency(null, seq, frequency);
                break;
        }
    }

    public void addClassesWithConstant(Object type, Sequence seq, int classesWithConstant) {
        switch (GenInputsAbstract.literals_level) {
            case CLASS:
                throw new RuntimeException("Should not update classesWithConstant in CLASS level");
            case PACKAGE:
                packageLevel.addClassesWithConstant((Package) type, seq, classesWithConstant);
                break;
            case ALL:
                allLevel.addClassesWithConstant(null, seq, classesWithConstant);
                break;
        }
    }

    public void addTotalClasses(Object type, int totalClasses) {
        switch (GenInputsAbstract.literals_level) {
            case CLASS:
                throw new RuntimeException("Should not update totalClasses in CLASS level");
            case PACKAGE:
                packageLevel.addTotalClasses((Package) type, totalClasses);
                break;
            case ALL:
                allLevel.addTotalClasses(null, totalClasses);
                break;
        }
    }

}
