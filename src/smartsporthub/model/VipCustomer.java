package smartsporthub.model;

/**
 * Khách hàng VIP: giảm {@link #DEFAULT_DISCOUNT_RATE} trên tổng chi phí.
 * <p>
 * Tỷ lệ giảm nằm ở dạng instance field để seed data có thể tạo VIP
 * với mức ưu đãi khác nhau mà không sửa code.
 */
public class VipCustomer extends Customer {

    public static final double DEFAULT_DISCOUNT_RATE = 0.10;
    public static final int DEFAULT_TIER_LEVEL = 1;

    private double discountRate;
    private int tierLevel;

    public VipCustomer(String customerId, String fullName, String phoneNumber, String email) {
        this(customerId, fullName, phoneNumber, email, DEFAULT_DISCOUNT_RATE, DEFAULT_TIER_LEVEL);
    }

    public VipCustomer(String customerId, String fullName, String phoneNumber, String email,
            double discountRate, int tierLevel) {
        super(customerId, fullName, phoneNumber, email);
        setDiscountRate(discountRate);
        setTierLevel(tierLevel);
    }

    @Override
    public CustomerType getCustomerType() {
        return CustomerType.VIP;
    }

    @Override
    public double calculatePromotionalDiscount(double amount) {
        validateAmount(amount);
        return amount * discountRate;
    }

    @Override
    public String getPromotionPolicy() {
        return String.format("VIP hạng %d giảm %.0f%%", tierLevel, discountRate * 100);
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public final void setDiscountRate(double discountRate) {
        if (!Double.isFinite(discountRate) || discountRate < 0 || discountRate > 1) {
            throw new IllegalArgumentException("Tỷ lệ giảm phải nằm trong khoảng [0, 1].");
        }
        this.discountRate = discountRate;
    }

    public int getTierLevel() {
        return tierLevel;
    }

    public final void setTierLevel(int tierLevel) {
        if (tierLevel < 1) {
            throw new IllegalArgumentException("Hạng VIP phải bắt đầu từ 1.");
        }
        this.tierLevel = tierLevel;
    }
}