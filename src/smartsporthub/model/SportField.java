package smartsporthub.model;

/**
 * Lớp TRỪU TƯỢNG mô tả dữ liệu chung và hành vi bắt buộc của mọi loại sân.
 *
 * <p>Đây là <b>nhánh kế thừa lớn nhất</b> của đồ án: từ lớp này sinh ra
 * {@link FootballField}, {@link BadmintonField}, {@link TennisField},
 * {@link PickleballField} — tương ứng đúng bốn bảng chuyên biệt hoá
 * FOOTBALL_FIELD, BADMINTON_FIELD, TENNIS_FIELD, PICKLEBALL_FIELD trong ERD.
 *
 * <p><b>Vì sao phải là abstract class mà không phải lớp thường?</b>
 * Trong nghiệp vụ không tồn tại "một sân chung chung": mọi sân đều thuộc một loại
 * cụ thể, và mỗi loại có công thức tính tiền khác nhau. Nếu cho phép tạo
 * {@code new SportField(...)} thì đối tượng đó không biết tính tiền theo công thức
 * nào. Khai báo abstract chặn ngay việc đó ở mức biên dịch.
 *
 * <p><b>Đa hình nằm ở đâu?</b> Ở phương thức trừu tượng
 * {@link #calculateRentalFee(double)}. Lớp {@link Booking} chỉ giữ một biến kiểu
 * {@code SportField} rồi gọi {@code field.calculateRentalFee(hours)}; Java tự chọn
 * đúng phiên bản của lớp con đang nằm trong bộ nhớ. Nhờ vậy, khi thêm loại sân mới
 * (ví dụ sân bóng rổ) chỉ cần tạo thêm một lớp con, không phải sửa một dòng nào
 * trong {@code Booking} — đây chính là nguyên lý Open/Closed.
 */
public abstract class SportField {

    /** Khoá chính của sân, bất biến (xem giải thích ở {@link Customer}). */
    private final String fieldId;

    /** Tên sân hiển thị cho khách, ví dụ "Sân bóng A". */
    private String fieldName;

    /** Giá thuê cơ bản cho một giờ, chưa gồm phí riêng của từng loại sân. */
    private double basePricePerHour;

    /** Trạng thái hiện tại: đang mở, đang bảo trì hay đang có khách dùng. */
    private FieldStatus status;

    /**
     * Hàm dựng dùng chung cho bốn lớp con.
     *
     * <p>{@code protected} để chỉ lớp con gọi được {@code super(...)}; bên ngoài
     * không thể khởi tạo trực tiếp lớp trừu tượng này.
     */
    protected SportField(String fieldId, String fieldName, double basePricePerHour, FieldStatus status) {
        this.fieldId = requireText(fieldId, "Mã sân không được để trống.");
        setFieldName(fieldName);
        setBasePricePerHour(basePricePerHour);
        setStatus(status);
    }

    /**
     * Phương thức TRỪU TƯỢNG quan trọng nhất của đồ án: tính tiền thuê sân.
     *
     * <p>Mỗi lớp con buộc phải cài đặt theo công thức riêng:
     * <ul>
     *   <li>Bóng đá: {@code giá × giờ} — phí đèn là tuỳ chọn, chỉ cộng khi thực sự bật đèn</li>
     *   <li>Cầu lông: {@code giá × giờ + phí vợt + phí thảm}</li>
     *   <li>Tennis: {@code giá × giờ + phí dụng cụ}</li>
     *   <li>Pickleball: {@code (giá + phí dụng cụ mỗi giờ) × giờ}</li>
     * </ul>
     *
     * <p>Lưu ý: phụ phí giờ cao điểm <b>không</b> nằm trong hàm này. Nó được tính
     * riêng ở {@link Booking#getPeakSurcharge()} vì phụ thuộc vào giờ bắt đầu của
     * lượt đặt, không phải đặc tính của bản thân sân.
     *
     * @param hours số giờ thuê, phải lớn hơn 0
     * @return tiền thuê sân theo công thức của loại sân cụ thể
     */
    public abstract double calculateRentalFee(double hours);

    public String getFieldId() {
        return fieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public final void setFieldName(String fieldName) {
        this.fieldName = requireText(fieldName, "Tên sân không được để trống.");
    }

    public double getBasePricePerHour() {
        return basePricePerHour;
    }

    /**
     * Cập nhật giá thuê cơ bản.
     *
     * <p>Chặn cả {@code NaN} và {@code Infinity} bằng {@link Double#isFinite(double)}:
     * nếu chỉ so sánh {@code <= 0} thì {@code NaN} sẽ lọt qua (mọi phép so sánh với
     * NaN đều trả {@code false}), và giá trị rác đó lan ra toàn bộ hoá đơn.
     */
    public final void setBasePricePerHour(double basePricePerHour) {
        if (!Double.isFinite(basePricePerHour) || basePricePerHour <= 0) {
            throw new IllegalArgumentException("Giá sân phẳng phải lớn hơn 0.");
        }
        this.basePricePerHour = basePricePerHour;
    }

    public FieldStatus getStatus() {
        return status;
    }

    public final void setStatus(FieldStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái sân không được null.");
        }
        this.status = status;
    }

    /**
     * Mô tả ngắn để in ra console.
     *
     * <p>Dùng {@code getClass().getSimpleName()} để in đúng tên lớp con
     * ("FootballField", "TennisField"...) — cách quan sát đa hình rất trực quan
     * khi chạy thử.
     */
    public String displayInfo() {
        return String.format("%s | %s | %s | %.0f VND/giờ | %s",
                fieldId, fieldName, getClass().getSimpleName(), basePricePerHour, status);
    }

    @Override
    public String toString() {
        return displayInfo();
    }

    /**
     * Kiểm tra số giờ thuê dùng chung cho mọi lớp con.
     *
     * <p>Đặt ở lớp cha để luật "số giờ phải dương" chỉ tồn tại một chỗ. {@code final}
     * ngăn lớp con vô tình bỏ qua luật này.
     */
    protected final void validateHours(double hours) {
        if (!Double.isFinite(hours) || hours <= 0) {
            throw new IllegalArgumentException("Số giờ phải lớn hơn 0.");
        }
    }

    /**
     * Kiểm tra một khoản phí không được âm.
     *
     * <p>Để {@code protected static} vì bốn lớp con đều cần dùng (phí đèn, phí vợt,
     * phí thảm, phí dụng cụ) với thông báo lỗi khác nhau.
     */
    protected static void validateNonNegativeFee(double fee, String message) {
        if (!Double.isFinite(fee) || fee < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Tính phụ phí giờ cao điểm theo tỷ lệ cấu hình trong {@link FieldPricing}.
     *
     * <p>Công thức: {@code giá cơ bản × số giờ × 20%}. Việc "có phải giờ cao điểm
     * hay không" do lớp {@link Booking} quyết định, vì chỉ nó biết giờ bắt đầu.
     */
    protected final double calculatePeakFee(double hours) {
        validateHours(hours);
        return getBasePricePerHour() * hours * FieldPricing.PEAK_SURCHARGE_RATE;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
