package smartsporthub.interfaces;

/**
 * Khả năng áp dụng chương trình ưu đãi lên một khoản tiền.
 * <p>
 * Là interface thứ hai của SmartSportHub bên cạnh {@link IPeakHourCalculable},
 * thể hiện tính đa hình cho phía khách hàng.
 */
public interface IPromotional {

    /**
     * Tính số tiền được giảm trên khoản tiền gốc.
     *
     * @param amount khoản tiền trước khi giảm, phải lớn hơn 0
     * @return số tiền giảm, không bao giờ âm và không vượt quá {@code amount}
     */
    double calculatePromotionalDiscount(double amount);

    /**
     * Mô tả chính sách ưu đãi để hiển thị trên hóa đơn.
     *
     * @return mô tả ngắn gọn bằng tiếng Việt
     */
    String getPromotionPolicy();
}