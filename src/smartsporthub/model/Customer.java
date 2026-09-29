package smartsporthub.model;

import smartsporthub.interfaces.IPromotional;

/** Khách hàng dùng chung; chính sách giảm giá do subclass quyết định. */
public abstract class Customer implements IPromotional {

    private final String customerId;
    private String fullName;
    private String phoneNumber;

    protected Customer(String customerId, String fullName, String phoneNumber) {
        this.customerId = requireText(customerId, "Mã khách hàng không được trống.");
        setFullName(fullName);
        setPhoneNumber(phoneNumber);
    }

    @Override
    public abstract double calculateDiscount(double amount);

    public String getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public final void setFullName(String fullName) {
        this.fullName = requireText(fullName, "Tên khách hàng không được trống.");
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public final void setPhoneNumber(String phoneNumber) {
        String normalized = requireText(phoneNumber, "Số điện thoại không được trống.");
        if (!normalized.matches("\\+?[0-9]{9,15}")) {
            throw new IllegalArgumentException("Số điện thoại phải có 9-15 chữ số, có thể bắt đầu bằng +.");
        }
        this.phoneNumber = normalized;
    }

    protected static void validateAmount(double amount) {
        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Số tiền phải hữu hạn và không âm.");
        }
    }

    private static String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
