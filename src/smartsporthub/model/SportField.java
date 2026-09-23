package smartsporthub.model;

/**
 * Lớp abstract mô tả dữ liệu chung và hành vi bắt buộc của mọi sân.
 */
public abstract class SportField {

    private String fieldId;
    private String fieldName;
    private double basePricePerHour;
    private FieldStatus status;

    protected SportField(String fieldId, String fieldName, double basePricePerHour, FieldStatus status) {
        setFieldId(fieldId);
        setFieldName(fieldName);
        setBasePricePerHour(basePricePerHour);
        setStatus(status);
    }

    /** Tính tiền thuê cơ bản và phụ phí riêng của loại sân, chưa gồm giờ cao điểm. */
    public abstract double calculateRentalFee(double hours);

    public String getFieldId() {
        return fieldId;
    }

    public final void setFieldId(String fieldId) {
        this.fieldId = requireText(fieldId, "Mã sân không được để trống.");
    }

    public String getFieldName() {
        return fieldName;
    }

    public final void setFieldName(String fieldName) {
        this.fieldName = requireText(fieldName, "Tên sân không được để trống.");
    }

    public double getBasePricePerHour() {
        return basePricePerHour;
    }

    public final void setBasePricePerHour(double basePricePerHour) {
        if (!Double.isFinite(basePricePerHour) || basePricePerHour <= 0) {
            throw new IllegalArgumentException("Giá sân phẳng phải lớn hơn 0.");
        }
        this.basePricePerHour = basePricePerHour;
    }

    public FieldStatus getStatus() {
        return status;
    }

    public final void setStatus(FieldStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái sân không được null.");
        }
        this.status = status;
    }

    public String displayInfo() {
        return String.format("%s | %s | %s | %.0f VND/giờ | %s",
                fieldId, fieldName, getClass().getSimpleName(), basePricePerHour, status);
    }

    @Override
    public String toString() {
        return displayInfo();
    }

    protected final void validateHours(double hours) {
        if (!Double.isFinite(hours) || hours <= 0) {
            throw new IllegalArgumentException("Số giờ phải lớn hơn 0.");
        }
    }

    protected static void validateNonNegativeFee(double fee, String message) {
        if (!Double.isFinite(fee) || fee < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    protected final double calculatePeakFee(double hours) {
        validateHours(hours);
        return getBasePricePerHour() * hours * FieldPricing.PEAK_SURCHARGE_RATE;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
