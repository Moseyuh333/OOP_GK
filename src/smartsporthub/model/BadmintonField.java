package smartsporthub.model;

import smartsporthub.interfaces.IPeakHourCalculable;

/** Sân cầu lông. */
public class BadmintonField extends SportField implements IPeakHourCalculable {

    private double racketFee;
    private double matFee;

    public BadmintonField(String fieldId, String fieldName, double basePricePerHour, FieldStatus status) {
        this(fieldId, fieldName, basePricePerHour, status,
                FieldPricing.BADMINTON_RACKET_FEE, FieldPricing.BADMINTON_MAT_FEE);
    }

    public BadmintonField(String fieldId, String fieldName, double basePricePerHour,
            FieldStatus status, double racketFee, double matFee) {
        super(fieldId, fieldName, basePricePerHour, status);
        validateNonNegativeFee(racketFee, "Phí vợt không được âm.");
        validateNonNegativeFee(matFee, "Phí thảm không được âm.");
        this.racketFee = racketFee;
        this.matFee = matFee;
    }

    @Override
    public double calculateRentalFee(double hours) {
        validateHours(hours);
        return getBasePricePerHour() * hours + racketFee + matFee;
    }

    @Override
    public double calculatePeakHourSurcharge(double hours) {
        return calculatePeakFee(hours);
    }

    public double getRacketFee() {
        return racketFee;
    }

    public final void setRacketFee(double racketFee) {
        validateNonNegativeFee(racketFee, "Phí vợt không được âm.");
        this.racketFee = racketFee;
    }

    public double getMatFee() {
        return matFee;
    }

    public final void setMatFee(double matFee) {
        validateNonNegativeFee(matFee, "Phí thảm không được âm.");
        this.matFee = matFee;
    }
}
