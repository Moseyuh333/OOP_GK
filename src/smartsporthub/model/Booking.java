package smartsporthub.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import smartsporthub.interfaces.IPeakHourCalculable;

/**
 * Một lượt đặt sân của khách hàng.
 * <p>
 * Lớp này là nơi hội tụ đa hình của Field Domain: giữ reference kiểu
 * {@link SportField} nhưng gọi {@code calculateRentalFee(hours)} để đúng
 * công thức của từng loại sân, đồng thời áp ưu đãi qua
 * {@link Customer#calculatePromotionalDiscount(double)}.
 */
public class Booking {

    private final String bookingId;
    private final Customer customer;
    private final SportField field;
    private Service service;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;

    public Booking(String bookingId, Customer customer, SportField field, Service service,
            LocalDateTime startTime, LocalDateTime endTime, BookingStatus status) {
        this.bookingId = requireText(bookingId, "Mã đặt sân không được để trống.");
        if (customer == null) {
            throw new IllegalArgumentException("Booking phải có khách hàng.");
        }
        if (field == null) {
            throw new IllegalArgumentException("Booking phải có sân.");
        }
        this.customer = customer;
        this.field = field;
        this.service = service;
        setStartTime(startTime);
        setEndTime(endTime);
        setStatus(status);
    }

    /** Thời lượng thuê tính bằng giờ. */
    public double getHours() {
        return Duration.between(startTime, endTime).toMinutes() / 60.0;
    }

    /** Tiền thuê sân theo công thức riêng của loại sân (đa hình). */
    public double getRentalFee() {
        return field.calculateRentalFee(getHours());
    }

    /** Phụ phí giờ cao điểm, chỉ áp dụng nếu sân hỗ trợ và giờ bắt đầu nằm trong khung. */
    public double getPeakSurcharge() {
        if (field instanceof IPeakHourCalculable peak && peak.isPeakHour(startTime.toLocalTime())) {
            return peak.calculatePeakHourSurcharge(getHours());
        }
        return 0;
    }

    public boolean isPeakHourBooking() {
        return field instanceof IPeakHourCalculable peak && peak.isPeakHour(startTime.toLocalTime());
    }

    /** Tiền dịch vụ bổ sung, bằng 0 nếu booking không dùng dịch vụ. */
    public double getServiceFee() {
        return service == null ? 0 : service.getUnitPrice();
    }

    /** Tổng trước khi giảm. */
    public double getSubtotal() {
        return getRentalFee() + getPeakSurcharge() + getServiceFee();
    }

    /** Tiền giảm theo chính sách ưu đãi của khách (đa hình). */
    public double getDiscount() {
        return customer.calculatePromotionalDiscount(getSubtotal());
    }

    /** Số tiền khách thực trả. */
    public double getTotalAmount() {
        return getSubtotal() - getDiscount();
    }

    public String getBookingId() {
        return bookingId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public SportField getField() {
        return field;
    }

    public Service getService() {
        return service;
    }

    public final void setService(Service service) {
        this.service = service;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public final void setStartTime(LocalDateTime startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu không được null.");
        }
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public final void setEndTime(LocalDateTime endTime) {
        if (endTime == null) {
            throw new IllegalArgumentException("Thời gian kết thúc không được null.");
        }
        if (endTime.isBefore(this.startTime) || endTime.isEqual(this.startTime)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        }
        this.endTime = endTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public final void setStatus(BookingStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái đặt sân không được null.");
        }
        this.status = status;
    }

    public LocalTime getStartTimeOfDay() {
        return startTime.toLocalTime();
    }

    public String displayInfo() {
        return String.format("%s | %s | %s | %s -> %s | %s | %,.0f VND",
                bookingId, customer.getCustomerId(), field.getFieldId(),
                startTime, endTime, status, getTotalAmount());
    }

    @Override
    public String toString() {
        return displayInfo();
    }

    private static String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}