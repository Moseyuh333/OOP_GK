package smartsporthub.model;

/** Khách hàng thông thường: không có ưu đãi. */
public class StandardCustomer extends Customer {

    public StandardCustomer(String customerId, String fullName, String phoneNumber, String email) {
        super(customerId, fullName, phoneNumber, email);
    }

    @Override
    public CustomerType getCustomerType() {
        return CustomerType.STANDARD;
    }

    @Override
    public double calculatePromotionalDiscount(double amount) {
        validateAmount(amount);
        return 0;
    }

    @Override
    public String getPromotionPolicy() {
        return "Không áp dụng ưu đãi";
    }
}