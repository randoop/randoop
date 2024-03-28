package constantmining.hospital;

// Doctor.java
public class Doctor {
    public String assessAge(int age) {
        if (age <= AgeConstants.CHILD_AGE) {
            if (age == AgeConstants.CHILD_AGE) {
                return "Patient is a child with age " + AgeConstants.CHILD_AGE + ".";
            }
            return "Patient is a child.";
        } else if (age < AgeConstants.MINOR_AGE) {
            return "Patient is a teenager.";
        } else if (age < AgeConstants.ELDERLY_AGE) {
            return "Patient is an adult.";
        } else {
            return "Patient is elderly.";
        }
    }
}