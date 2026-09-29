package smartsporthub.model;

/** Khách thường không có giảm giá. */
public class StandardCustomer extends Customer {

    public StandardCustomer(String customerId, String fullName, String phoneNumber) {
        super(customerId, fullName, phoneNumber);
    }

    @Override
    public double calculateDiscount(double amount) {
        validateAmount(amount);
        return 0;
    }
}
