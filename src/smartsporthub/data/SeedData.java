package smartsporthub.data;

import java.time.LocalDate;
import java.time.LocalDateTime;

import smartsporthub.model.BadmintonField;
import smartsporthub.model.Booking;
import smartsporthub.model.BookingStatus;
import smartsporthub.model.Customer;
import smartsporthub.model.FieldStatus;
import smartsporthub.model.FootballField;
import smartsporthub.model.Invoice;
import smartsporthub.model.InvoiceStatus;
import smartsporthub.model.PickleballField;
import smartsporthub.model.Service;
import smartsporthub.model.SportField;
import smartsporthub.model.StandardCustomer;
import smartsporthub.model.TennisField;
import smartsporthub.model.VipCustomer;

/**
 * Sinh bộ dữ liệu mẫu phục vụ phân tích.
 * <p>
 * Bộ dữ liệu được thiết kế có chủ đích để đủ điều kiện chạy các truy vấn LINQ phía C#:
 * có cả ba trạng thái booking, khách chưa từng đặt sân, khách có nhiều booking
 * COMPLETED, khách dùng nhiều loại sân, sân không có booking COMPLETED,
 * booking trong và ngoài giờ cao điểm, service chưa từng dùng, và booking không dùng service.
 */
public final class SeedData {

    private SeedData() {
        // Utility class
    }

    /** Tạo kho dữ liệu đầy đủ: 6 sân, 6 khách, 6 dịch vụ, 12 booking, 12 hóa đơn. */
    public static SmartSportHubData create() {
        SmartSportHubData data = new SmartSportHubData();

        data.getFieldManager().addField(new FootballField("F001", "Sân bóng A", 100000, FieldStatus.AVAILABLE));
        data.getFieldManager().addField(new FootballField("F002", "Sân bóng B", 110000, FieldStatus.MAINTENANCE));
        data.getFieldManager().addField(new BadmintonField("F003", "Sân cầu lông C", 60000, FieldStatus.AVAILABLE));
        data.getFieldManager().addField(new TennisField("F004", "Sân tennis D", 150000, FieldStatus.OCCUPIED));
        data.getFieldManager().addField(new PickleballField("F005", "Sân pickleball E", 90000, FieldStatus.AVAILABLE));
        data.getFieldManager().addField(new BadmintonField("F006", "Sân cầu lông F", 65000, FieldStatus.MAINTENANCE));

        data.getCustomerManager().addCustomer(new VipCustomer("C001", "Nguyễn Văn An", "0901234567", "an.nguyen@gmail.com"));
        data.getCustomerManager().addCustomer(new StandardCustomer("C002", "Trần Thị Bình", "0912345678", "binh.tran@gmail.com"));
        data.getCustomerManager().addCustomer(new VipCustomer("C003", "Lê Văn Chiến", "0923456789", "chien.le@gmail.com", 0.12, 2));
        data.getCustomerManager().addCustomer(new StandardCustomer("C004", "Phạm Thu Dung", "0934567890", "dung.pham@gmail.com"));
        data.getCustomerManager().addCustomer(new VipCustomer("C005", "Võ Hải Đăng", "0945678901", "dang.vo@gmail.com", 0.15, 3));
        data.getCustomerManager().addCustomer(new StandardCustomer("C006", "Đặng Thu Hà", "0956789012", "ha.dang@gmail.com"));

        data.getServiceManager().addService(new Service("SV001", "Nước uống", "Nước", 30000, true));
        data.getServiceManager().addService(new Service("SV002", "Dụng cụ thể thao", "Thiết bị", 150000, true));
        data.getServiceManager().addService(new Service("SV003", "Tiệc cư trang", "Tiệc cư trang", 200000, true));
        data.getServiceManager().addService(new Service("SV004", "Huấn luyện viên", "Dịch vụ", 250000, true));
        data.getServiceManager().addService(new Service("SV005", "Wifi premium", "Tiện ích", 50000, true));
        data.getServiceManager().addService(new Service("SV006", "Dọn sân sau", "Tiện ích", 40000, false));

        addBooking(data, "B001", "C001", "F001", "SV001", "2026-02-15T18:00", "2026-02-15T20:00", BookingStatus.COMPLETED);
        addBooking(data, "B002", "C001", "F003", "SV003", "2026-02-16T08:00", "2026-02-16T10:00", BookingStatus.COMPLETED);
        addBooking(data, "B003", "C001", "F004", null, "2026-02-17T19:00", "2026-02-17T21:00", BookingStatus.COMPLETED);
        addBooking(data, "B004", "C002", "F001", "SV004", "2026-02-18T06:00", "2026-02-18T08:00", BookingStatus.COMPLETED);
        addBooking(data, "B005", "C002", "F003", null, "2026-02-19T07:00", "2026-02-19T09:00", BookingStatus.COMPLETED);
        addBooking(data, "B006", "C003", "F003", "SV002", "2026-02-20T17:30", "2026-02-20T20:00", BookingStatus.COMPLETED);
        addBooking(data, "B007", "C003", "F001", "SV001", "2026-02-21T09:00", "2026-02-21T11:00", BookingStatus.COMPLETED);
        addBooking(data, "B008", "C004", "F002", "SV001", "2026-02-22T18:00", "2026-02-22T20:00", BookingStatus.CONFIRMED);
        addBooking(data, "B009", "C004", "F004", null, "2026-02-23T10:00", "2026-02-23T12:00", BookingStatus.CONFIRMED);
        addBooking(data, "B010", "C005", "F005", "SV003", "2026-02-24T19:00", "2026-02-24T21:00", BookingStatus.CONFIRMED);
        addBooking(data, "B011", "C002", "F006", "SV001", "2026-02-25T20:30", "2026-02-25T22:00", BookingStatus.CANCELLED);
        addBooking(data, "B012", "C005", "F005", "SV004", "2026-02-26T08:00", "2026-02-26T10:00", BookingStatus.CANCELLED);

        addInvoice(data, "I001", "B001", InvoiceStatus.PAID);
        addInvoice(data, "I002", "B002", InvoiceStatus.PAID);
        addInvoice(data, "I003", "B003", InvoiceStatus.PAID);
        addInvoice(data, "I004", "B004", InvoiceStatus.PAID);
        addInvoice(data, "I005", "B005", InvoiceStatus.UNPAID);
        addInvoice(data, "I006", "B006", InvoiceStatus.PAID);
        addInvoice(data, "I007", "B007", InvoiceStatus.PAID);
        addInvoice(data, "I008", "B008", InvoiceStatus.UNPAID);
        addInvoice(data, "I009", "B009", InvoiceStatus.UNPAID);
        addInvoice(data, "I010", "B010", InvoiceStatus.UNPAID);
        addInvoice(data, "I011", "B011", InvoiceStatus.REFUNDED);
        addInvoice(data, "I012", "B012", InvoiceStatus.REFUNDED);

        return data;
    }

    private static void addBooking(SmartSportHubData data, String bookingId, String customerId,
            String fieldId, String serviceId, String start, String end, BookingStatus status) {
        Customer customer = data.getCustomerManager().findCustomerById(customerId);
        SportField field = data.getFieldManager().findFieldById(fieldId);
        Service service = serviceId == null ? null : data.getServiceManager().findServiceById(serviceId);
        Booking booking = new Booking(bookingId, customer, field, service,
                LocalDateTime.parse(start), LocalDateTime.parse(end), status);
        data.getBookingManager().addBooking(booking);
    }

    private static void addInvoice(SmartSportHubData data, String invoiceId, String bookingId, InvoiceStatus status) {
        Booking booking = data.getBookingManager().findBookingById(bookingId);
        LocalDate issueDate = booking.getStartTime().toLocalDate();
        data.getInvoiceManager().addInvoice(new Invoice(invoiceId, booking, issueDate, status));
    }
}