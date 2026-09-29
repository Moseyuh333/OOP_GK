package smartsporthub.model;

/** Khách VIP mặc định được giảm 10% trên tạm tính. */
public class VipCustomer extends Customer {

    private static final double DEFAULT_DISCOUNT_RATE = 0.10;
    private double discountRate;

    public VipCustomer(String customerId, String fullName, String phoneNumber) {
        this(customerId, fullName, phoneNumber, DEFAULT_DISCOUNT_RATE);
    }

    public VipCustomer(String customerId, String fullName, String phoneNumber, double discountRate) {
        super(customerId, fullName, phoneNumber);
        setDiscountRate(discountRate);
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public final void setDiscountRate(double discountRate) {
        if (!Double.isFinite(discountRate) || discountRate < 0 || discountRate > 1) {
            throw new IllegalArgumentException("Tỷ lệ giảm giá phải nằm trong [0, 1].");
        }
        this.discountRate = discountRate;
    }

    @Override
    public double calculateDiscount(double amount) {
        validateAmount(amount);
        return amount * discountRate;
    }
}
