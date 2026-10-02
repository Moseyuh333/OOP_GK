package smartsporthub.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import smartsporthub.model.Customer;

/** DTO phẳng cho khách hàng. */
public final class CustomerDto implements JsonDto {

    private final String id;
    private final String fullName;
    private final String phoneNumber;
    private final String email;
    private final String customerType;
    private final double discountRate;
    private final String promotionPolicy;

    public CustomerDto(Customer customer) {
        id = customer.getCustomerId();
        fullName = customer.getFullName();
        phoneNumber = customer.getPhoneNumber();
        email = customer.getEmail();
        customerType = customer.getCustomerType().name();
        double base = 1_000_000;
        discountRate = base <= 0 ? 0 : customer.calculatePromotionalDiscount(base) / base;
        promotionPolicy = customer.getPromotionPolicy();
    }

    /** Thứ tự key cố định để Dev B dễ đối chiếu schema. */
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("fullName", fullName);
        map.put("phoneNumber", phoneNumber);
        map.put("email", email);
        map.put("customerType", customerType);
        map.put("discountRate", discountRate);
        map.put("promotionPolicy", promotionPolicy);
        return map;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getCustomerType() {
        return customerType;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public String getPromotionPolicy() {
        return promotionPolicy;
    }
}