package smartsporthub.model;

/** Dịch vụ bổ sung; đơn giá không bao gồm số lượng. */
public class Service {

    private final String serviceId;
    private String serviceName;
    private double price;

    public Service(String serviceId, String serviceName, double price) {
        this.serviceId = requireText(serviceId, "Mã dịch vụ không được trống.");
        setServiceName(serviceName);
        setPrice(price);
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public final void setServiceName(String serviceName) {
        this.serviceName = requireText(serviceName, "Tên dịch vụ không được trống.");
    }

    public double getPrice() {
        return price;
    }

    public final void setPrice(double price) {
        if (!Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException("Giá dịch vụ phải hữu hạn và không âm.");
        }
        this.price = price;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
