package smartsporthub.model;

import smartsporthub.interfaces.IPeakHourCalculable;

/** Sân pickleball. */
public class PickleballField extends SportField implements IPeakHourCalculable {

    private double equipmentFeePerHour;

    public PickleballField(String fieldId, String fieldName, double basePricePerHour, FieldStatus status) {
        this(fieldId, fieldName, basePricePerHour, status, FieldPricing.PICKLEBALL_EQUIPMENT_FEE_PER_HOUR);
    }

    public PickleballField(String fieldId, String fieldName, double basePricePerHour,
            FieldStatus status, double equipmentFeePerHour) {
        super(fieldId, fieldName, basePricePerHour, status);
        validateNonNegativeFee(equipmentFeePerHour, "Phí thiết bị pickleball theo giờ không được âm.");
        this.equipmentFeePerHour = equipmentFeePerHour;
    }

    @Override
    public double calculateRentalFee(double hours) {
        validateHours(hours);
        return (getBasePricePerHour() + equipmentFeePerHour) * hours;
    }

    @Override
    public double calculatePeakHourSurcharge(double hours) {
        return calculatePeakFee(hours);
    }

    public double getEquipmentFeePerHour() {
        return equipmentFeePerHour;
    }

    public final void setEquipmentFeePerHour(double equipmentFeePerHour) {
        validateNonNegativeFee(equipmentFeePerHour, "Phí thiết bị pickleball theo giờ không được âm.");
        this.equipmentFeePerHour = equipmentFeePerHour;
    }
}
