package constantmining.pharmacy;

public class Prescription {
    public String generateDosageAdvice(int dosage) {
        if (dosage <= MedicationConstants.MIN_DOSAGE) {
            return "Low dosage medication";
        } else if (dosage < MedicationConstants.STANDARD_DOSAGE) {
            return "Standard dosage medication";
        } else if (dosage < MedicationConstants.MAX_DOSAGE) {
            return "High dosage medication";
        } else {
            return "Very high dosage medication";
        }
    }
}
