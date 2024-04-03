package constantmining.hospital;

// Patient.java
public class Patient {
    public String checkAgeCategory(int age) {
        if (age <= AgeConstants.CHILD_AGE) {
            return "Child patient";
        } else if (age < AgeConstants.MINOR_AGE) {
            return "Teenage patient";
        } else if (age < AgeConstants.ELDERLY_AGE) {
            return "Adult patient";
        } else {
            return "Elderly patient";
        }
    }

    public boolean isPatientAdult(int age) {
        return age >= AgeConstants.ADULT_AGE;
    }
}