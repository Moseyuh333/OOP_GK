package smartsporthub.model;

/** Trạng thái của một lượt đặt sân. */
public enum BookingStatus {
    /** Đã hoàn tất sử dụng sân. */
    COMPLETED,
    /** Đã đặt, chưa hoàn tất. */
    CONFIRMED,
    /** Đã hủy, không tính doanh thu. */
    CANCELLED
}