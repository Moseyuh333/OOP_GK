package smartsporthub.model;

import smartsporthub.interfaces.IPeakHourCalculable;

/** Sân pickleball. */
public class PickleballField extends SportField implements IPeakHourCalculable {

    private double equipmentFee;

    public PickleballField(String fieldId, String fieldName, double basePricePerHour, FieldStatus status) {
        this(fieldId, fieldName, basePricePerHour, status, FieldPricing.PICKLEBALL_EQUIPMENT_FEE);
    }

    public PickleballField(String fieldId, String fieldName, double basePricePerHour,
            FieldStatus status, double equipmentFee) {
        super(fieldId, fieldName, basePricePerHour, status);
        validateNonNegativeFee(equipmentFee, "Phí thiết bị pickleball không được âm.");
        this.equipmentFee = equipmentFee;
    }

    @Override
    public double calculateRentalFee(double hours) {
        validateHours(hours);
        return getBasePricePerHour() * hours + equipmentFee;
    }

    @Override
    public double calculatePeakHourSurcharge(double hours) {
        return calculatePeakFee(hours);
    }

    public double getEquipmentFee() {
        return equipmentFee;
    }

    public final void setEquipmentFee(double equipmentFee) {
        validateNonNegativeFee(equipmentFee, "Phí thiết bị pickleball không được âm.");
        this.equipmentFee = equipmentFee;
    }
}
