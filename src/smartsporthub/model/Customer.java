package smartsporthub.model;

import smartsporthub.interfaces.IPromotional;

/**
 * Lớp abstract mô tả dữ liệu chung của khách hàng.
 * <p>
 * Khách hàng cụ thể (StandardCustomer, VipCustomer) chỉ khác nhau ở
 * chính sách ưu đãi, nên phần giảm giá được đặt ở dạng abstract và
 * hiện thực qua {@link IPromotional}.
 */
public abstract class Customer implements IPromotional {

    private final String customerId;
    private String fullName;
    private String phoneNumber;
    private String email;

    protected Customer(String customerId, String fullName, String phoneNumber, String email) {
        this.customerId = requireText(customerId, "Mã khách hàng không được để trống.");
        setFullName(fullName);
        setPhoneNumber(phoneNumber);
        setEmail(email);
    }

    /** Trả về loại khách để JSON contract sử dụng. */
    public abstract CustomerType getCustomerType();

    public String getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public final void setFullName(String fullName) {
        this.fullName = requireText(fullName, "Tên khách hàng không được để trống.");
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public final void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = requireText(phoneNumber, "Số điện thoại không được để trống.");
    }

    public String getEmail() {
        return email;
    }

    public final void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }
        this.email = email.trim();
    }

    public String displayInfo() {
        return String.format("%s | %s | %s | %s", customerId, fullName, phoneNumber, getCustomerType());
    }

    @Override
    public String toString() {
        return displayInfo();
    }

    /**
     * Kiểm tra khoản tiền trước khi áp dụng ưu đãi.
     * <p>
     * Đặt ở lớp cha để mọi subclass dùng chung, tránh lặp lại validation.
     */
    protected final void validateAmount(double amount) {
        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Số tiền không hợp lệ.");
        }
    }

    protected static String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}