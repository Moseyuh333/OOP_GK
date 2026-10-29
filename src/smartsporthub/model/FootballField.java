package smartsporthub.model;

import smartsporthub.interfaces.IPeakHourCalculable;

/**
 * Sân bóng đá — lớp con cụ thể của {@link SportField}.
 *
 * <p><b>Ví dụ đa hình:</b> nơi khác chỉ giữ biến {@code SportField} và gọi
 * {@code calculateRentalFee(hours)}; nếu đối tượng thật là lớp này, Java chạy đúng
 * công thức bên dưới. Không cần {@code if (field instanceof FootballField)}.
 *
 * <p><b>Điểm nghiệp vụ riêng:</b> phí đèn ({@code nightLightFee}) <i>không</i> tự
 * cộng vào giá thuê, vì ban ngày không bật đèn thì khách không phải trả khoản này.
 * Vì vậy lớp này có thêm bản {@code calculateRentalFee(hours, useNightLight)} để
 * cộng phí đèn khi thực sự dùng — đây là nạp chồng phương thức (overloading), khác
 * với ghi đè (overriding) ở phương thức một tham số.
 */
public class FootballField extends SportField implements IPeakHourCalculable {

    /** Phí bật đèn sân cho một lượt thuê; mặc định lấy từ {@link FieldPricing}. */
    private double nightLightFee;

    /** Hàm dựng ngắn: dùng phí đèn mặc định trong cấu hình. */
    public FootballField(String fieldId, String fieldName, double basePricePerHour, FieldStatus status) {
        this(fieldId, fieldName, basePricePerHour, status, FieldPricing.FOOTBALL_NIGHT_LIGHT_FEE);
    }

    /**
     * Hàm dựng đầy đủ: cho phép truyền phí đèn riêng (dùng trong test và khi giá
     * điện thay đổi).
     *
     * <p>Dòng {@code super(...)} là bắt buộc: nó chuyển phần dữ liệu chung lên lớp
     * cha để lớp cha kiểm tra và lưu. Lớp con chỉ giữ phần riêng của mình.
     */
    public FootballField(String fieldId, String fieldName, double basePricePerHour,
            FieldStatus status, double nightLightFee) {
        super(fieldId, fieldName, basePricePerHour, status);
        validateNonNegativeFee(nightLightFee, "Phí đèn không được âm.");
        this.nightLightFee = nightLightFee;
    }

    /**
     * GHI ĐÈ phương thức trừu tượng của lớp cha.
     *
     * <p>Công thức sân bóng đá: {@code giá mỗi giờ × số giờ}. Chưa cộng phí đèn —
     * xem giải thích ở javadoc lớp.
     *
     * <p>{@code @Override} không bắt buộc về mặt cú pháp nhưng nên có: nếu gõ sai
     * chữ ký, trình biên dịch báo lỗi ngay thay vì âm thầm tạo phương thức mới.
     */
    @Override
    public double calculateRentalFee(double hours) {
        validateHours(hours);
        return getBasePricePerHour() * hours;
    }

    /**
     * NẠP CHỒNG (overload) phương thức tính phí để cộng thêm phí đèn khi có dùng.
     *
     * <p>Khác với ghi đè: cùng tên nhưng khác danh sách tham số, nên đây là một
     * phương thức mới chứ không thay thế phương thức của lớp cha.
     *
     * @param hours        số giờ thuê
     * @param useNightLight {@code true} nếu lượt này có bật đèn
     * @return tiền thuê đã cộng phí đèn (nếu có)
     */
    public double calculateRentalFee(double hours, boolean useNightLight) {
        double fee = calculateRentalFee(hours);
        return useNightLight ? fee + nightLightFee : fee;
    }

    /** Hiện thực phần còn lại của {@link IPeakHourCalculable}: phụ phí 20% theo giờ. */
    @Override
    public double calculatePeakHourSurcharge(double hours) {
        return calculatePeakFee(hours);
    }

    public double getNightLightFee() {
        return nightLightFee;
    }

    public final void setNightLightFee(double nightLightFee) {
        validateNonNegativeFee(nightLightFee, "Phí đèn không được âm.");
        this.nightLightFee = nightLightFee;
    }
}
