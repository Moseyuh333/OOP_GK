package smartsporthub.model;

/**
 * Dịch vụ bổ sung đi kèm khi đặt sân (nước uống, thuê dụng cụ, huấn luyện…).
 * <p>
 * Một Booking có thể không dùng dịch vụ, khi đó {@code service} là {@code null}.
 */
public class Service {

    private final String serviceId;
    private String serviceName;
    private String category;
    private double unitPrice;
    private boolean active;

    public Service(String serviceId, String serviceName, String category, double unitPrice, boolean active) {
        this.serviceId = requireText(serviceId, "Mã dịch vụ không được để trống.");
        setServiceName(serviceName);
        setCategory(category);
        setUnitPrice(unitPrice);
        this.active = active;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public final void setServiceName(String serviceName) {
        this.serviceName = requireText(serviceName, "Tên dịch vụ không được để trống.");
    }

    public String getCategory() {
        return category;
    }

    public final void setCategory(String category) {
        this.category = requireText(category, "Nhóm dịch vụ không được để trống.");
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public final void setUnitPrice(double unitPrice) {
        if (!Double.isFinite(unitPrice) || unitPrice < 0) {
            throw new IllegalArgumentException("Đơn giá dịch vụ không được âm.");
        }
        this.unitPrice = unitPrice;
    }

    public boolean isActive() {
        return active;
    }

    public final void setActive(boolean active) {
        this.active = active;
    }

    public String displayInfo() {
        return String.format("%s | %s | %s | %.0f VND | %s",
                serviceId, serviceName, category, unitPrice, active ? "Đang cung cấp" : "Ngừng cung cấp");
    }

    @Override
    public String toString() {
        return displayInfo();
    }

    private static String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}