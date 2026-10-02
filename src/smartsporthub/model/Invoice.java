package smartsporthub.model;

import java.time.LocalDate;

/**
 * Hóa đơn sinh ra từ một {@link Booking}.
 * <p>
 * Giữ reference tới Booking để truy ngược khách hàng và sân mà không cần lưu trùng dữ liệu.
 */
public class Invoice {

    private final String invoiceId;
    private final Booking booking;
    private LocalDate issueDate;
    private InvoiceStatus status;

    public Invoice(String invoiceId, Booking booking, LocalDate issueDate, InvoiceStatus status) {
        this.invoiceId = requireText(invoiceId, "Mã hóa đơn không được để trống.");
        if (booking == null) {
            throw new IllegalArgumentException("Hóa đơn phải gắn với một booking.");
        }
        this.booking = booking;
        setIssueDate(issueDate);
        setStatus(status);
    }

    /** Số tiền lấy trực tiếp từ tổng tiền của booking. */
    public double getAmount() {
        return booking.getTotalAmount();
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public Booking getBooking() {
        return booking;
    }

    public String getBookingId() {
        return booking.getBookingId();
    }

    public String getCustomerId() {
        return booking.getCustomer().getCustomerId();
    }

    public String getFieldId() {
        return booking.getField().getFieldId();
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public final void setIssueDate(LocalDate issueDate) {
        if (issueDate == null) {
            throw new IllegalArgumentException("Ngày xuất hóa đơn không được null.");
        }
        this.issueDate = issueDate;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public final void setStatus(InvoiceStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái hóa đơn không được null.");
        }
        this.status = status;
    }

    public String displayInfo() {
        return String.format("%s | booking=%s | %s | %,.0f VND | %s",
                invoiceId, getBookingId(), issueDate, getAmount(), status);
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