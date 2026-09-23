package smartsporthub.model;

import smartsporthub.interfaces.IPeakHourCalculable;

/** Sân tennis. */
public class TennisField extends SportField implements IPeakHourCalculable {

    private double courtEquipmentFee;

    public TennisField(String fieldId, String fieldName, double basePricePerHour, FieldStatus status) {
        this(fieldId, fieldName, basePricePerHour, status, FieldPricing.TENNIS_EQUIPMENT_FEE);
    }

    public TennisField(String fieldId, String fieldName, double basePricePerHour,
            FieldStatus status, double courtEquipmentFee) {
        super(fieldId, fieldName, basePricePerHour, status);
        validateNonNegativeFee(courtEquipmentFee, "Phí thiết bị sân tennis không được âm.");
        this.courtEquipmentFee = courtEquipmentFee;
    }

    @Override
    public double calculateRentalFee(double hours) {
        validateHours(hours);
        return getBasePricePerHour() * hours + courtEquipmentFee;
    }

    @Override
    public double calculatePeakHourSurcharge(double hours) {
        return calculatePeakFee(hours);
    }

    public double getCourtEquipmentFee() {
        return courtEquipmentFee;
    }

    public final void setCourtEquipmentFee(double courtEquipmentFee) {
        validateNonNegativeFee(courtEquipmentFee, "Phí thiết bị sân tennis không được âm.");
        this.courtEquipmentFee = courtEquipmentFee;
    }
}
