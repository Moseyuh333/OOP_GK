package smartsporthub.test;

import static smartsporthub.test.TestSupport.assertEquals;
import static smartsporthub.test.TestSupport.assertThrows;
import static smartsporthub.test.TestSupport.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import smartsporthub.data.SeedData;
import smartsporthub.data.SmartSportHubData;
import smartsporthub.model.Booking;
import smartsporthub.model.BookingStatus;
import smartsporthub.model.Customer;
import smartsporthub.model.CustomerType;
import smartsporthub.model.FieldStatus;
import smartsporthub.model.FootballField;
import smartsporthub.model.Invoice;
import smartsporthub.model.InvoiceStatus;
import smartsporthub.model.Service;
import smartsporthub.model.SportField;
import smartsporthub.model.StandardCustomer;
import smartsporthub.model.TennisField;
import smartsporthub.model.VipCustomer;

/** Kiểm thử Booking/Billing domain và logic tính tiền tổng hợp. */
public final class BookingDomainTest {

    private BookingDomainTest() {
    }

    public static void main(String[] args) {
        TestSupport.Suite suite = new TestSupport.Suite("BOOKING/BILLING DOMAIN");

        suite.run("Tạo khách Standard và VIP", BookingDomainTest::testCreateCustomers);
        suite.run("VIP giảm 10%, Standard không giảm", BookingDomainTest::testPromotionalDiscount);
        suite.run("Tính tổng booking có phí cao điểm và dịch vụ", BookingDomainTest::testBookingTotalAmount);
        suite.run("Booking không dùng dịch vụ", BookingDomainTest::testBookingWithoutService);
        suite.run("Booking ngoài giờ cao điểm không bị phụ phí", BookingDomainTest::testNonPeakHour);
        suite.run("Validation booking sai dữ liệu", BookingDomainTest::testBookingValidation);
        suite.run("Hóa đơn lấy tiền từ booking", BookingDomainTest::testInvoiceAmount);
        suite.run("BookingManager truy vấn và cập nhật", BookingDomainTest::testBookingManager);
        suite.run("InvoiceManager tính tổng đã thanh toán", BookingDomainTest::testInvoiceManager);
        suite.run("Tổng doanh thu bỏ qua booking hủy", BookingDomainTest::testRevenueIgnoresCancelled);
        suite.run("Công thức tiền sân vẫn đúng qua Booking", BookingDomainTest::testFieldPolymorphismThroughBooking);

        suite.finish();
    }

    private static SportField football() {
        return new FootballField("F001", "Sân bóng A", 100_000, FieldStatus.AVAILABLE, 50_000);
    }

    private static Service service() {
        return new Service("SV001", "Nước uống", "Nước", 30_000, true);
    }

    private static void testCreateCustomers() {
        Customer standard = new StandardCustomer("C002", "Trần Thị Bình", "0912345678", "binh@gmail.com");
        Customer vip = new VipCustomer("C001", "Nguyễn Văn An", "0901234567", "an@gmail.com");

        assertEquals("C002", standard.getCustomerId());
        assertEquals(CustomerType.STANDARD, standard.getCustomerType());
        assertEquals(CustomerType.VIP, vip.getCustomerType());
        assertEquals(1, ((VipCustomer) vip).getTierLevel());
    }

    private static void testPromotionalDiscount() {
        Customer standard = new StandardCustomer("C002", "Trần Thị Bình", "0912345678", "binh@gmail.com");
        Customer vip = new VipCustomer("C001", "Nguyễn Văn An", "0901234567", "an@gmail.com");

        assertEquals(0, standard.calculatePromotionalDiscount(500_000));
        assertEquals(50_000, vip.calculatePromotionalDiscount(500_000));

        // Tỷ lệ giảm được cấu hình riêng cho từng VIP.
        Customer vipGold = new VipCustomer("C005", "Võ Hải Đăng", "0945678901", "dang@gmail.com", 0.15, 3);
        assertEquals(75_000, vipGold.calculatePromotionalDiscount(500_000));
    }

    private static void testBookingTotalAmount() {
        Customer vip = new VipCustomer("C001", "Nguyễn Văn An", "0901234567", "an@gmail.com");
        // 18:00 nằm trong khung cao điểm [17:00, 20:00).
        Booking booking = new Booking("B001", vip, football(), service(),
                LocalDateTime.parse("2026-02-15T18:00"),
                LocalDateTime.parse("2026-02-15T20:00"),
                BookingStatus.COMPLETED);

        assertEquals(2.0, booking.getHours());
        assertEquals(200_000, booking.getRentalFee());
        assertTrue(booking.isPeakHourBooking());
        assertEquals(40_000, booking.getPeakSurcharge());
        assertEquals(30_000, booking.getServiceFee());
        assertEquals(270_000, booking.getSubtotal());
        assertEquals(27_000, booking.getDiscount());
        assertEquals(243_000, booking.getTotalAmount());
    }

    private static void testBookingWithoutService() {
        Customer standard = new StandardCustomer("C002", "Trần Thị Bình", "0912345678", "binh@gmail.com");
        Booking booking = new Booking("B003", standard, football(), null,
                LocalDateTime.parse("2026-02-17T09:00"),
                LocalDateTime.parse("2026-02-17T11:00"),
                BookingStatus.COMPLETED);

        assertEquals(0, booking.getServiceFee());
        assertEquals(0, booking.getDiscount());
        assertEquals(200_000, booking.getTotalAmount());
        assertEquals(9, booking.getStartTimeOfDay().getHour());
    }

    private static void testNonPeakHour() {
        Customer standard = new StandardCustomer("C002", "Trần Thị Bình", "0912345678", "binh@gmail.com");
        // 20:00 nằm ngoài khung [17:00, 20:00) nên không tính phụ phí.
        Booking booking = new Booking("B004", standard, football(), null,
                LocalDateTime.parse("2026-02-18T20:00"),
                LocalDateTime.parse("2026-02-18T22:00"),
                BookingStatus.CONFIRMED);

        assertTrue(!booking.isPeakHourBooking());
        assertEquals(0, booking.getPeakSurcharge());
        assertEquals(200_000, booking.getTotalAmount());
    }

    private static void testBookingValidation() {
        Customer standard = new StandardCustomer("C002", "Trần Thị Bình", "0912345678", "binh@gmail.com");
        SportField field = football();

        assertThrows(IllegalArgumentException.class,
                () -> new Booking("B001", null, field, null,
                        LocalDateTime.parse("2026-02-15T08:00"),
                        LocalDateTime.parse("2026-02-15T10:00"),
                        BookingStatus.CONFIRMED));

        assertThrows(IllegalArgumentException.class,
                () -> new Booking("B001", standard, null, null,
                        LocalDateTime.parse("2026-02-15T08:00"),
                        LocalDateTime.parse("2026-02-15T10:00"),
                        BookingStatus.CONFIRMED));

        // Kết thúc phải sau bắt đầu.
        assertThrows(IllegalArgumentException.class,
                () -> new Booking("B001", standard, field, null,
                        LocalDateTime.parse("2026-02-15T10:00"),
                        LocalDateTime.parse("2026-02-15T10:00"),
                        BookingStatus.CONFIRMED));

        assertThrows(IllegalArgumentException.class,
                () -> new Booking("  ", standard, field, null,
                        LocalDateTime.parse("2026-02-15T08:00"),
                        LocalDateTime.parse("2026-02-15T10:00"),
                        BookingStatus.CONFIRMED));

        assertThrows(IllegalArgumentException.class, () -> new StandardCustomer("C002", " ", "0912", "a@b.com"));
        assertThrows(IllegalArgumentException.class, () -> new StandardCustomer("C002", "Bình", "0912", "email-khong-hop-le"));
        assertThrows(IllegalArgumentException.class, () -> new VipCustomer("C003", "A", "0912", "a@b.com", 1.5, 1));
    }

    private static void testInvoiceAmount() {
        Customer vip = new VipCustomer("C001", "Nguyễn Văn An", "0901234567", "an@gmail.com");
        Booking booking = new Booking("B001", vip, football(), service(),
                LocalDateTime.parse("2026-02-15T08:00"),
                LocalDateTime.parse("2026-02-15T10:00"),
                BookingStatus.COMPLETED);
        Invoice invoice = new Invoice("I001", booking, LocalDate.parse("2026-02-15"), InvoiceStatus.PAID);

        assertEquals(booking.getTotalAmount(), invoice.getAmount());
        assertEquals("B001", invoice.getBookingId());
        assertEquals("C001", invoice.getCustomerId());
        assertEquals("F001", invoice.getFieldId());
    }

    private static void testBookingManager() {
        SmartSportHubData data = SeedData.create();
        var manager = data.getBookingManager();

        assertEquals(12, manager.getAllBookings().size());
        assertEquals("B001", manager.findBookingById("B001").getBookingId());
        assertEquals(3, manager.findByCustomer("C001").size());
        assertEquals(5, manager.findPeakHourBookings().size());
        assertTrue(manager.getTotalRevenue() > 0);

        assertTrue(manager.updateStatus("B001", BookingStatus.CANCELLED));
        assertEquals(BookingStatus.CANCELLED, manager.findBookingById("B001").getStatus());
        assertTrue(!manager.updateStatus("B999", BookingStatus.COMPLETED));
    }

    private static void testInvoiceManager() {
        SmartSportHubData data = SeedData.create();
        var manager = data.getInvoiceManager();

        assertEquals(12, manager.getAllInvoices().size());
        assertEquals(6, manager.findByStatus(InvoiceStatus.PAID).size());
        assertEquals(4, manager.findByStatus(InvoiceStatus.UNPAID).size());
        assertEquals(2, manager.findByStatus(InvoiceStatus.REFUNDED).size());
        assertTrue(manager.getPaidTotal() > 0);
    }

    private static void testRevenueIgnoresCancelled() {
        SmartSportHubData data = SeedData.create();
        double before = data.getTotalRevenue();

        data.getBookingManager().updateStatus("B001", BookingStatus.CANCELLED);
        double after = data.getTotalRevenue();

        assertTrue(after < before);
    }

    /** Cùng một booking nhưng khác loại sân phải ra tiền khác nhau (đa hình). */
    private static void testFieldPolymorphismThroughBooking() {
        Customer standard = new StandardCustomer("C002", "Trần Thị Bình", "0912345678", "binh@gmail.com");
        SportField footballField = football();
        SportField tennisField = new TennisField("F004", "Sân tennis D", 150_000, FieldStatus.AVAILABLE, 30_000);

        List<SportField> fields = List.of(footballField, tennisField);
        double[] totals = new double[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            Booking booking = new Booking("B" + i, standard, fields.get(i), null,
                    LocalDateTime.parse("2026-02-15T08:00"),
                    LocalDateTime.parse("2026-02-15T10:00"),
                    BookingStatus.COMPLETED);
            totals[i] = booking.getTotalAmount();
        }

        assertEquals(200_000, totals[0]);
        assertEquals(330_000, totals[1]);
    }
}