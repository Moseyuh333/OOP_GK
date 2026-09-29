package smartsporthub.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Một phiên bản booking bất biến. Các thao tác trả về phiên bản mới để người gọi
 * không thể tự thay lịch/trạng thái của booking đã nằm trong BookingManager.
 */
public final class Booking {

    private final String bookingId;
    private final Customer customer;
    private final SportField field;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final List<ServiceItem> services;
    private final boolean useNightLight;
    private final BookingStatus status;
    private final Invoice invoice;

    public Booking(String bookingId, Customer customer, SportField field,
            LocalDateTime startTime, LocalDateTime endTime) {
        this(bookingId, customer, field, startTime, endTime, Collections.emptyList(), false);
    }

    public Booking(String bookingId, Customer customer, SportField field,
            LocalDateTime startTime, LocalDateTime endTime, List<ServiceItem> services,
            boolean useNightLight) {
        this(bookingId, customer, field, startTime, endTime, services, useNightLight,
                BookingStatus.PENDING, null);
    }

    private Booking(String bookingId, Customer customer, SportField field,
            LocalDateTime startTime, LocalDateTime endTime, List<ServiceItem> services,
            boolean useNightLight, BookingStatus status, Invoice invoice) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã booking không được trống.");
        }
        if (customer == null || field == null) {
            throw new IllegalArgumentException("Khách hàng và sân không được null.");
        }
        validateTimeRange(startTime, endTime);
        if (services == null) {
            throw new IllegalArgumentException("Danh sách dịch vụ không được null.");
        }
        for (ServiceItem item : services) {
            if (item == null) {
                throw new IllegalArgumentException("Danh sách dịch vụ không được chứa null.");
            }
        }
        if (useNightLight && !(field instanceof FootballField)) {
            throw new IllegalArgumentException("Tùy chọn đèn chỉ áp dụng cho sân bóng đá.");
        }
        this.bookingId = bookingId.trim();
        this.customer = customer;
        this.field = field;
        this.startTime = startTime;
        this.endTime = endTime;
        this.services = Collections.unmodifiableList(new ArrayList<>(services));
        this.useNightLight = useNightLight;
        this.status = status;
        this.invoice = invoice;
    }

    public static void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu.");
        }
    }

    public double getDurationHours() {
        Duration duration = Duration.between(startTime, endTime);
        return duration.getSeconds() / 3600.0 + duration.getNano() / 3_600_000_000_000.0;
    }

    public String getBookingId() { return bookingId; }
    public Customer getCustomer() { return customer; }
    public SportField getField() { return field; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public List<ServiceItem> getServices() { return services; }
    public boolean isUseNightLight() { return useNightLight; }
    public BookingStatus getStatus() { return status; }

    /** Báo giá hiện tại; booking hoàn tất luôn trả về hóa đơn đã chốt. */
    public Invoice getInvoice() {
        if (status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking đã hủy không thể lập hóa đơn.");
        }
        return invoice == null ? new Invoice(this) : invoice;
    }

    public Booking confirm() {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException("Chỉ xác nhận booking đang chờ.");
        }
        return copy(field, startTime, endTime, BookingStatus.CONFIRMED, null);
    }

    public Booking cancel() {
        requireActive();
        return copy(field, startTime, endTime, BookingStatus.CANCELLED, null);
    }

    public Booking reschedule(SportField newField, LocalDateTime start, LocalDateTime end) {
        requireActive();
        return copy(newField, start, end, status, null);
    }

    /** Hoàn tất đồng nghĩa đã thanh toán trong phạm vi bài console. */
    public Booking complete() {
        if (status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ hoàn tất booking đã xác nhận.");
        }
        return copy(field, startTime, endTime, BookingStatus.COMPLETED, new Invoice(this));
    }

    private void requireActive() {
        if (status != BookingStatus.PENDING && status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Booking đã hủy hoặc hoàn tất không thể thay đổi.");
        }
    }

    private Booking copy(SportField newField, LocalDateTime start, LocalDateTime end,
            BookingStatus newStatus, Invoice newInvoice) {
        return new Booking(bookingId, customer, newField, start, end, services,
                useNightLight, newStatus, newInvoice);
    }

    /** Dòng dịch vụ lưu đơn giá tại lúc thêm vào booking. */
    public static final class ServiceItem {

        private final String serviceId;
        private final String serviceName;
        private final double unitPrice;
        private final int quantity;

        public ServiceItem(Service service, int quantity) {
            if (service == null || quantity <= 0) {
                throw new IllegalArgumentException("Dịch vụ phải tồn tại và số lượng phải lớn hơn 0.");
            }
            if (!Double.isFinite(service.getPrice() * quantity)) {
                throw new IllegalArgumentException("Thành tiền dịch vụ vượt giới hạn.");
            }
            this.serviceId = service.getServiceId();
            this.serviceName = service.getServiceName();
            this.unitPrice = service.getPrice();
            this.quantity = quantity;
        }

        public String getServiceId() { return serviceId; }
        public String getServiceName() { return serviceName; }
        public double getUnitPrice() { return unitPrice; }
        public int getQuantity() { return quantity; }
        public double getTotal() { return unitPrice * quantity; }
    }
}
