package smartsporthub.model;

import smartsporthub.interfaces.IPromotional;

/**
 * Lớp TRỪU TƯỢNG (abstract class) mô tả dữ liệu chung của mọi khách hàng.
 *
 * <p>ĐÂY LÀ MỘT TRONG HAI NHÁNH KẾ THỪA CHÍNH CỦA ĐỒ ÁN (nhánh còn lại là
 * {@link SportField}). Bốn trụ cột OOP thể hiện ngay trong lớp này:
 *
 * <ul>
 *   <li><b>Trừu tượng (Abstraction):</b> lớp này không mô tả một khách hàng cụ thể
 *       nào. Nó chỉ gom phần dữ liệu mà mọi loại khách đều có (mã, tên, điện thoại,
 *       email) và khai báo {@link #getCustomerType()} là abstract — buộc lớp con
 *       phải tự trả lời "tôi là loại khách gì".</li>
 *   <li><b>Đóng gói (Encapsulation):</b> mọi thuộc tính đều là {@code private}.
 *       Bên ngoài chỉ đọc qua getter và sửa qua setter; mỗi setter đều kiểm tra
 *       dữ liệu hợp lệ rồi mới gán, nên đối tượng không bao giờ rơi vào trạng thái
 *       sai (ví dụ email không có dấu {@code @}).</li>
 *   <li><b>Kế thừa (Inheritance):</b> {@link StandardCustomer} và {@link VipCustomer}
 *       dùng lại toàn bộ phần này qua từ khoá {@code extends}, không phải viết lại.</li>
 *   <li><b>Đa hình (Polymorphism):</b> lớp này implements {@link IPromotional}.
 *       Nơi gọi chỉ cần biết "đây là một IPromotional" rồi gọi
 *       {@code calculatePromotionalDiscount(amount)}; Java sẽ tự chạy đúng phiên bản
 *       của Standard (trả 0) hay VIP (nhân tỷ lệ giảm).</li>
 * </ul>
 *
 * <p><b>Vì sao {@code customerId} là {@code final}?</b> Mã khách hàng là khoá chính
 * trong cơ sở dữ liệu. Nếu cho phép sửa, mọi booking và hoá đơn đang trỏ tới mã cũ
 * sẽ bị "mồ côi". Khai báo {@code final} biến ràng buộc nghiệp vụ này thành ràng buộc
 * của trình biên dịch: không ai gán lại được, kể cả lớp con.
 */
public abstract class Customer implements IPromotional {

    /** Khoá chính — bất biến suốt vòng đời đối tượng (xem giải thích ở javadoc lớp). */
    private final String customerId;

    /** Họ tên khách; luôn được trim và không rỗng nhờ {@link #setFullName(String)}. */
    private String fullName;

    /** Số điện thoại liên hệ; không rỗng. */
    private String phoneNumber;

    /** Email nhận thông báo; phải chứa ký tự {@code @}. */
    private String email;

    /**
     * Hàm dựng dùng chung cho mọi lớp con.
     *
     * <p>Để {@code protected} chứ không {@code public}: chỉ lớp con mới được gọi
     * {@code super(...)}, bên ngoài không thể tạo trực tiếp một {@code Customer}
     * "trừu tượng" — đúng bản chất của abstract class.
     *
     * <p>Lưu ý thứ tự: gán {@code customerId} trước rồi mới gọi các setter, vì
     * {@link #displayInfo()} và các hàm khác có thể cần tới mã khách.
     */
    protected Customer(String customerId, String fullName, String phoneNumber, String email) {
        this.customerId = requireText(customerId, "Mã khách hàng không được để trống.");
        setFullName(fullName);
        setPhoneNumber(phoneNumber);
        setEmail(email);
    }

    /**
     * Phương thức TRỪU TƯỢNG: lớp cha biết "phải có loại khách" nhưng không biết
     * cụ thể là gì, nên để lớp con hiện thực.
     *
     * <p>Đây là ví dụ kinh điển của đa hình: biến kiểu {@code Customer} gọi
     * {@code getCustomerType()} sẽ nhận {@code STANDARD} hoặc {@code VIP} tuỳ đối
     * tượng thật đang nằm trong bộ nhớ. Tầng DTO dùng đúng cơ chế này để ghi
     * trường {@code customerType} ra JSON.
     *
     * @return loại khách của đối tượng cụ thể
     */
    public abstract CustomerType getCustomerType();

    public String getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    /**
     * Cập nhật tên khách.
     *
     * <p>{@code final} ở đây có nghĩa: lớp con được <i>dùng</i> nhưng không được
     * <i>ghi đè</i>. Quy tắc kiểm tra dữ liệu vì thế không thể bị vô hiệu hoá ở
     * lớp con — một cách bảo vệ tính đúng đắn của mô hình.
     */
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

    /**
     * Kiểm tra định dạng email tối thiểu (phải có {@code @}) rồi mới gán.
     *
     * <p>Đây là ví dụ của đóng gói: nếu cho phép truy cập trực tiếp vào field,
     * một email rác có thể lọt vào đối tượng và chỉ phát hiện khi gửi thất bại.
     */
    public final void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }
        this.email = email.trim();
    }

    /**
     * Tạo chuỗi mô tả ngắn để in ra màn hình console.
     *
     * <p>Trong này có gọi {@code getCustomerType()} — lời gọi đa hình, nên cùng một
     * dòng code in ra "STANDARD" hoặc "VIP" tuỳ đối tượng thật.
     */
    public String displayInfo() {
        return String.format("%s | %s | %s | %s", customerId, fullName, phoneNumber, getCustomerType());
    }

    /**
     * Ghi đè {@link Object#toString()} để khi in đối tượng bằng
     * {@code System.out.println(customer)} sẽ ra thông tin có nghĩa thay vì
     * địa chỉ bộ nhớ dạng {@code smartsporthub.model.VipCustomer@1b6d3586}.
     */
    @Override
    public String toString() {
        return displayInfo();
    }

    /**
     * Kiểm tra khoản tiền trước khi áp dụng ưu đãi.
     *
     * <p>Đặt ở lớp cha với {@code protected final} để hai lớp con dùng chung mà
     * không lớp nào sửa được luật kiểm tra. Nếu mỗi lớp con tự viết lại, rất dễ
     * một bên quên chặn số âm — lỗi chỉ lộ ra khi tính hoá đơn.
     *
     * @param amount số tiền cần kiểm tra
     * @throws IllegalArgumentException nếu tiền không hữu hạn hoặc âm
     */
    protected final void validateAmount(double amount) {
        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Số tiền không hợp lệ.");
        }
    }

    /**
     * Hàm tiện ích dùng chung: cắt khoảng trắng hai đầu và chặn chuỗi rỗng.
     *
     * <p>Để {@code protected static} để lớp con gọi lại được (ví dụ kiểm tra mã
     * trước khi tạo đối tượng), nhưng không lộ ra ngoài package như một API công khai.
     *
     * @param value   giá trị cần kiểm tra
     * @param message thông báo lỗi hiển thị cho người dùng
     * @return chuỗi đã trim
     */
    protected static String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
