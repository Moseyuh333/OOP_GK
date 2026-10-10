package smartsporthub.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import smartsporthub.data.SmartSportHubData;
import smartsporthub.db.Repository;
import smartsporthub.manager.BookingManager;
import smartsporthub.manager.CustomerManager;
import smartsporthub.manager.FieldManager;
import smartsporthub.manager.InvoiceManager;
import smartsporthub.manager.ServiceManager;
import smartsporthub.model.Booking;
import smartsporthub.model.BookingStatus;
import smartsporthub.model.Customer;
import smartsporthub.model.FieldStatus;
import smartsporthub.model.Invoice;
import smartsporthub.model.InvoiceStatus;
import smartsporthub.model.Service;
import smartsporthub.model.SportField;

/**
 * Nghiệp vụ đặt sân: tạo booking, đổi lịch, hủy, hoàn thành, thu và hoàn tiền.
 * <p>
 * Mọi thay đổi đều ghi xuống SQLite sau khi kiểm tra hợp lệ, và mọi thao tác
 * đều đi qua một khoá duy nhất: server xử lý nhiều request song song, nếu không
 * khoá thì hai lượt đặt cùng một sân có thể cùng thành công.
 */
public class BookingService {

    /** Lỗi nghiệp vụ: trả về cho client dưới dạng HTTP 4xx kèm thông điệp rõ ràng. */
    public static class BusinessException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private final int statusCode;

        public BusinessException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public static BusinessException notFound(String message) {
            return new BusinessException(message, 404);
        }

        public static BusinessException invalid(String message) {
            return new BusinessException(message, 400);
        }

        public static BusinessException conflict(String message) {
            return new BusinessException(message, 409);
        }
    }

    private final SmartSportHubData data;
    private final Repository repository;
    private final Object writeLock = new Object();
    private final AtomicInteger idCounter = new AtomicInteger(9000);

    public BookingService(SmartSportHubData data, Repository repository) {
        this.data = data;
        this.repository = repository;
    }

    // ------------------------------------------------------------- đặt sân

    /**
     * Đặt sân mới.
     *
     * @param customerId mã khách
     * @param fieldId mã sân
     * @param startTime ISO, ví dụ 2026-03-01T08:00
     * @param endTime ISO
     * @param serviceId mã dịch vụ, có thể null
     */
    public Booking createBooking(String customerId, String fieldId, String startTime,
            String endTime, String serviceId) {
        Objects.requireNonNull(startTime, "startTime");
        Objects.requireNonNull(endTime, "endTime");

        LocalDateTime start = parseTime(startTime, "startTime");
        LocalDateTime end = parseTime(endTime, "endTime");

        synchronized (writeLock) {
            Customer customer = requireCustomer(customerId);
            SportField field = requireField(fieldId);
            Service service = resolveService(serviceId);

            if (field.getStatus() == FieldStatus.MAINTENANCE) {
                throw BusinessException.conflict("Sân " + field.getFieldName() + " đang bảo trì, chưa thể đặt.");
            }
            if (!end.isAfter(start)) {
                throw BusinessException.invalid("Giờ kết thúc phải sau giờ bắt đầu.");
            }
            requireSlotFree(field.getFieldId(), start, end, null);

            String bookingId = nextBookingId();
            Booking booking = new Booking(bookingId, customer, field, service, start, end,
                    BookingStatus.CONFIRMED);
            if (!data.getBookingManager().addBooking(booking)) {
                throw BusinessException.conflict("Không tạo được booking " + bookingId + ".");
            }

            // Sân đang được dùng: đánh dấu để dashboard khớp với thực tế.
            if (field.getStatus() == FieldStatus.AVAILABLE) {
                field.setStatus(FieldStatus.OCCUPIED);
            }

            // Hoá đơn khởi tạo chưa thu: khách trả sau khi dùng xong.
            Invoice invoice = new Invoice("I" + bookingId.substring(1), booking,
                    booking.getStartTime().toLocalDate(), InvoiceStatus.UNPAID);
            data.getInvoiceManager().addInvoice(invoice);

            persist();
            return booking;
        }
    }

    // ------------------------------------------------------------- đổi lịch

    /** Đổi khung giờ của một booking đang còn hiệu lực. */
    public Booking rescheduleBooking(String bookingId, String newStartTime, String newEndTime) {
        synchronized (writeLock) {
            Booking booking = requireBooking(bookingId);
            if (booking.getStatus() == BookingStatus.COMPLETED) {
                throw BusinessException.conflict("Booking đã hoàn thành, không thể đổi lịch.");
            }
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                throw BusinessException.conflict("Booking đã hủy, không thể đổi lịch.");
            }

            LocalDateTime start = parseTime(newStartTime, "newStartTime");
            LocalDateTime end = parseTime(newEndTime, "newEndTime");
            if (!end.isAfter(start)) {
                throw BusinessException.invalid("Giờ kết thúc phải sau giờ bắt đầu.");
            }

            String fieldId = booking.getField().getFieldId();
            requireSlotFree(fieldId, start, end, bookingId);

            booking.setStartTime(start);
            booking.setEndTime(end);
            persist();
            return booking;
        }
    }

    // ------------------------------------------------------------- trạng thái

    /** Khách hủy lượt đặt. Hóa đơn đi kèm chuyển sang hoàn tiền nếu đã trả. */
    public Booking cancelBooking(String bookingId) {
        synchronized (writeLock) {
            Booking booking = requireBooking(bookingId);
            if (booking.getStatus() == BookingStatus.COMPLETED) {
                throw BusinessException.conflict("Booking đã hoàn thành, không thể hủy.");
            }
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                throw BusinessException.conflict("Booking đã hủy trước đó.");
            }

            booking.setStatus(BookingStatus.CANCELLED);

            // Đã thu thì hoàn tiền; chưa thu thì huỷ nợ.
            Invoice invoice = findInvoiceOf(bookingId);
            if (invoice != null) {
                invoice.setStatus(InvoiceStatus.REFUNDED);
            }
            // Ghi trước: truy vấn "còn lượt nào chiếm sân" phải phản ánh trạng thái
            // sau khi hủy, nếu không booking vừa hủy vẫn bị tính là đang chiếm.
            persist();
            releaseFieldIfFree(booking);
            persist();
            return booking;
        }
    }

    /** Đánh dấu đã dùng xong. Chỉ nhận khi khung giờ đã qua hoặc đang diễn ra. */
    public Booking completeBooking(String bookingId) {
        synchronized (writeLock) {
            Booking booking = requireBooking(bookingId);
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                throw BusinessException.conflict("Booking đã hủy, không thể hoàn thành.");
            }
            if (booking.getStatus() == BookingStatus.COMPLETED) {
                throw BusinessException.conflict("Booking đã hoàn thành trước đó.");
            }

            booking.setStatus(BookingStatus.COMPLETED);
            persist();
            releaseFieldIfFree(booking);
            persist();
            return booking;
        }
    }

    // ------------------------------------------------------------- hóa đơn

    /** Thu tiền một hoá đơn. */
    public Invoice payInvoice(String invoiceId) {
        synchronized (writeLock) {
            Invoice invoice = requireInvoice(invoiceId);
            if (invoice.getStatus() == InvoiceStatus.PAID) {
                throw BusinessException.conflict("Hoá đơn đã được thu rồi.");
            }
            if (invoice.getStatus() == InvoiceStatus.REFUNDED) {
                throw BusinessException.conflict("Hoá đơn đã hoàn tiền, không thể thu.");
            }
            invoice.setStatus(InvoiceStatus.PAID);
            persist();
            return invoice;
        }
    }

    /** Hoàn tiền một hoá đơn đã thu. */
    public Invoice refundInvoice(String invoiceId) {
        synchronized (writeLock) {
            Invoice invoice = requireInvoice(invoiceId);
            if (invoice.getStatus() == InvoiceStatus.REFUNDED) {
                throw BusinessException.conflict("Hoá đơn đã hoàn tiền trước đó.");
            }
            if (invoice.getStatus() == InvoiceStatus.UNPAID) {
                throw BusinessException.conflict("Hoá đơn chưa thu, không có gì để hoàn.");
            }
            invoice.setStatus(InvoiceStatus.REFUNDED);
            persist();
            return invoice;
        }
    }

    // ------------------------------------------------------------- sân

    /** Chuyển trạng thái sân: AVAILABLE / MAINTENANCE. Không cho bảo trì sân đang có lịch. */
    public SportField setFieldStatus(String fieldId, String status) {
        synchronized (writeLock) {
            SportField field = requireField(fieldId);
            FieldStatus target;
            try {
                target = FieldStatus.valueOf(status);
            } catch (IllegalArgumentException | NullPointerException error) {
                throw BusinessException.invalid("Trạng thái sân không hợp lệ: " + status);
            }
            if (target == FieldStatus.MAINTENANCE && hasActiveSlots(fieldId)) {
                throw BusinessException.conflict("Sân còn lượt đặt đang hiệu lực, không thể đưa vào bảo trì.");
            }
            field.setStatus(target);
            persist();
            return field;
        }
    }

    // ------------------------------------------------------------- nội bộ

    /** Đọc lịch đang chiếm của một sân, quy đổi lỗi DB thành lỗi nghiệp vụ. */
    private List<Repository.TimeSlot> loadSlots(String fieldId) {
        try {
            return repository.loadBookedSlotsOfField(fieldId);
        } catch (SQLException error) {
            throw new BusinessException("Lỗi đọc lịch sân: " + error.getMessage(), 500);
        }
    }

    /**
     * Chặn khi khung giờ mới chồng lên lượt đang hiệu lực.
     *
     * @param ignoreBookingId booking đang được đổi lịch: bỏ qua chính nó, vì lịch cũ
     * của nó vẫn còn trong DB cho tới khi lưu lại
     */
    private void requireSlotFree(String fieldId, LocalDateTime start, LocalDateTime end, String ignoreBookingId) {
        for (Repository.TimeSlot slot : loadSlots(fieldId)) {
            if (ignoreBookingId != null && ignoreBookingId.equals(slot.bookingId())) {
                continue;
            }
            if (slot.overlaps(start, end)) {
                String from = slot.start().toLocalDate() + " " + slot.start().toLocalTime();
                String to = String.valueOf(slot.end().toLocalTime());
                throw BusinessException.conflict(
                        "Sân đã có lượt đặt từ " + from + " đến " + to + ". Chọn khung giờ khác.");
            }
        }
    }

    private boolean hasActiveSlots(String fieldId) {
        return !loadSlots(fieldId).isEmpty();
    }

    /**
     * Trả sân về AVAILABLE nếu không còn lượt nào đang hiệu lực.
     * <p>
     * Dùng chính query của DB thay vì duyệt danh sách trong RAM, để quy tắc "lượt nào
     * chiếm sân" chỉ tồn tại ở một chỗ. Booking COMPLETED hoặc CANCELLED không còn chiếm.
     */
    private void releaseFieldIfFree(Booking booking) {
        String fieldId = booking.getField().getFieldId();
        if (!hasActiveSlots(fieldId)) {
            booking.getField().setStatus(FieldStatus.AVAILABLE);
        }
    }

    private Invoice findInvoiceOf(String bookingId) {
        for (Invoice invoice : data.getInvoiceManager().getAllInvoices()) {
            if (invoice.getBookingId().equals(bookingId)) {
                return invoice;
            }
        }
        return null;
    }

    private Customer requireCustomer(String customerId) {
        Customer customer = customerId == null ? null
                : data.getCustomerManager().findCustomerById(customerId.trim());
        if (customer == null) {
            throw BusinessException.notFound("Không tìm thấy khách hàng: " + customerId);
        }
        return customer;
    }

    private SportField requireField(String fieldId) {
        SportField field = fieldId == null ? null
                : data.getFieldManager().findFieldById(fieldId.trim());
        if (field == null) {
            throw BusinessException.notFound("Không tìm thấy sân: " + fieldId);
        }
        return field;
    }

    private Booking requireBooking(String bookingId) {
        Booking booking = bookingId == null ? null
                : data.getBookingManager().findBookingById(bookingId.trim());
        if (booking == null) {
            throw BusinessException.notFound("Không tìm thấy booking: " + bookingId);
        }
        return booking;
    }

    private Invoice requireInvoice(String invoiceId) {
        Invoice invoice = invoiceId == null ? null
                : data.getInvoiceManager().findInvoiceById(invoiceId.trim());
        if (invoice == null) {
            throw BusinessException.notFound("Không tìm thấy hóa đơn: " + invoiceId);
        }
        return invoice;
    }

    private Service resolveService(String serviceId) {
        if (serviceId == null || serviceId.isBlank()) {
            return null;
        }
        Service service = data.getServiceManager().findServiceById(serviceId.trim());
        if (service == null) {
            throw BusinessException.notFound("Không tìm thấy dịch vụ: " + serviceId);
        }
        if (!service.isActive()) {
            throw BusinessException.conflict("Dịch vụ " + service.getServiceName() + " đã ngừng cung cấp.");
        }
        return service;
    }

    private String nextBookingId() {
        while (true) {
            String candidate = "B" + idCounter.incrementAndGet();
            if (data.getBookingManager().findBookingById(candidate) == null) {
                return candidate;
            }
        }
    }

    private LocalDateTime parseTime(String raw, String field) {
        try {
            return LocalDateTime.parse(raw.trim());
        } catch (DateTimeParseException | NullPointerException error) {
            throw BusinessException.invalid(
                    field + " sai định dạng. Cần dạng 2026-03-01T08:00, nhận được: " + raw);
        }
    }

    /** Ghi toàn bộ kho xuống SQLite. Lỗi DB phải nổi lên, không được nuốt. */
    private void persist() {
        try {
            repository.saveAll(data.getFieldManager(), data.getCustomerManager(),
                    data.getServiceManager(), data.getBookingManager(), data.getInvoiceManager());
        } catch (SQLException error) {
            throw new BusinessException("Lỗi ghi cơ sở dữ liệu: " + error.getMessage(), 500);
        }
    }
}