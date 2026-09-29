package smartsporthub.interfaces;

/** Chính sách giảm giá trả về số tiền được giảm. */
public interface IPromotional {
    double calculateDiscount(double amount);
}
