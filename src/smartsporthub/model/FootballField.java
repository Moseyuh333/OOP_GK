package smartsporthub.model;

import smartsporthub.interfaces.IPeakHourCalculable;

/** Sân bóng đá. */
public class FootballField extends SportField implements IPeakHourCalculable {

    private double nightLightFee;

    public FootballField(String fieldId, String fieldName, double basePricePerHour, FieldStatus status) {
        this(fieldId, fieldName, basePricePerHour, status, FieldPricing.FOOTBALL_NIGHT_LIGHT_FEE);
    }

    public FootballField(String fieldId, String fieldName, double basePricePerHour,
            FieldStatus status, double nightLightFee) {
        super(fieldId, fieldName, basePricePerHour, status);
        validateNonNegativeFee(nightLightFee, "Phí đèn không được âm.");
        this.nightLightFee = nightLightFee;
    }

    @Override
    public double calculateRentalFee(double hours) {
        validateHours(hours);
        return getBasePricePerHour() * hours;
    }

    /** Phương thức mở rộng để Booking có thể cộng phí đèn khi thực sự sử dụng. */
    public double calculateRentalFee(double hours, boolean useNightLight) {
        double fee = calculateRentalFee(hours);
        return useNightLight ? fee + nightLightFee : fee;
    }

    @Override
    public double calculatePeakHourSurcharge(double hours) {
        return calculatePeakFee(hours);
    }

    public double getNightLightFee() {
        return nightLightFee;
    }

    public final void setNightLightFee(double nightLightFee) {
        validateNonNegativeFee(nightLightFee, "Phí đèn không được âm.");
        this.nightLightFee = nightLightFee;
    }
}
