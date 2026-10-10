package smartsporthub.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;

import smartsporthub.data.SeedData;
import smartsporthub.data.SmartSportHubData;
import smartsporthub.db.Database;
import smartsporthub.db.Repository;
import smartsporthub.model.Booking;
import smartsporthub.model.BookingStatus;
import smartsporthub.model.FieldStatus;
import smartsporthub.model.Invoice;
import smartsporthub.model.InvoiceStatus;
import smartsporthub.service.BookingService;
import smartsporthub.service.BookingService.BusinessException;

/** Kiểm thử nghiệp vụ đặt sân trên DB thật (file tạm), không mock. */
public class BookingServiceTest {

    private static int passed;
    private static int failed;

    private static Path workDir;
    private static Connection connection;
    private static SmartSportHubData data;
    private static BookingService service;
    private static Repository repository;

    public static void main(String[] args) throws Exception {
        workDir = Files.createTempDirectory("ssh-service-test");
        try {
            run("Đặt sân thành công tạo booking và hoá đơn", BookingServiceTest::testCreateBooking);
            run("Đặt sân chặn trùng khung giờ", BookingServiceTest::testRejectOverlap);
            run("Đặt sân cho phép lịch liền kề", BookingServiceTest::testAdjacentSlotsAllowed);
            run("Đặt sân trùng một phút cũng bị chặn", BookingServiceTest::testOverlapByOneMinute);
            run("Không đặt được sân đang bảo trì", BookingServiceTest::testRejectMaintenanceField);
            run("Sai định dạng giờ bị từ chối", BookingServiceTest::testRejectBadTimeFormat);
            run("Giờ kết thúc trước giờ bắt đầu bị từ chối", BookingServiceTest::testRejectEndBeforeStart);
            run("Khách không tồn tại bị từ chối", BookingServiceTest::testRejectUnknownCustomer);
            run("Dịch vụ đã ngừng không dùng được", BookingServiceTest::testRejectInactiveService);
            run("Dữ liệu ghi xuống DB sau khi đặt", BookingServiceTest::testPersistedToDatabase);
            run("Dữ liệu còn sau khi mở lại DB", BookingServiceTest::testSurvivesRestart);
            run("Hủy booking giải phóng sân", BookingServiceTest::testCancelReleasesField);
            run("Không hủy được booking đã hoàn thành", BookingServiceTest::testRejectCancelCompleted);
            run("Không hủy hai lần", BookingServiceTest::testRejectDoubleCancel);
            run("Đổi lịch sang khung trống", BookingServiceTest::testReschedule);
            run("Đổi lịch sang khung đã có người khác chặn", BookingServiceTest::testRescheduleConflict);
            run("Không đổi lịch được booking đã hủy", BookingServiceTest::testRescheduleCancelled);
            run("Hoàn thành booking đổi trạng thái", BookingServiceTest::testComplete);
            run("Thu tiền hoá đơn", BookingServiceTest::testPayInvoice);
            run("Không thu tiền hai lần", BookingServiceTest::testRejectDoublePay);
            run("Hoàn tiền hoá đơn đã thu", BookingServiceTest::testRefundInvoice);
            run("Sân bảo trì bị chặn khi còn lịch", BookingServiceTest::testRejectMaintenanceWithBookings);
        } finally {
            if (connection != null) {
                connection.close();
            }
            cleanup(workDir);
        }

        System.out.println();
        System.out.printf("===== KẾT QUẢ NGHIỆP VỤ: %d/%d PASS =====%n", passed, passed + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------ test

    private static void testCreateBooking() throws Exception {
        reset();
        Booking booking = service.createBooking("C001", "F001", "2026-03-01T08:00", "2026-03-01T10:00", "SV001");

        assertTrue("booking mới xuất hiện trong kho",
                data.getBookingManager().findBookingById(booking.getBookingId()) != null);
        assertTrue("trạng thái CONFIRMED", booking.getStatus() == BookingStatus.CONFIRMED);
        assertTrue("giờ = 2", Math.abs(booking.getHours() - 2) < 0.001);

        Invoice invoice = findInvoice(booking.getBookingId());
        assertTrue("tự sinh hoá đơn UNPAID", invoice != null && invoice.getStatus() == InvoiceStatus.UNPAID);
        assertTrue("tiền hoá đơn khớp tổng tiền booking",
                Math.abs(invoice.getAmount() - booking.getTotalAmount()) < 0.01);
    }

    private static void testRejectOverlap() throws Exception {
        reset();
        service.createBooking("C001", "F001", "2026-03-01T08:00", "2026-03-01T10:00", null);
        expectConflict(() -> service.createBooking("C002", "F001", "2026-03-01T09:00", "2026-03-01T11:00", null));
    }

    private static void testAdjacentSlotsAllowed() throws Exception {
        reset();
        service.createBooking("C001", "F001", "2026-03-01T08:00", "2026-03-01T10:00", null);
        // 10:00-12:00 chạm đuôi lúc 10:00: không chồng, phải đặt được.
        Booking second = service.createBooking("C002", "F001", "2026-03-01T10:00", "2026-03-01T12:00", null);
        assertTrue("lịch nối tiếp vẫn đặt được", second != null);
    }

    private static void testOverlapByOneMinute() throws Exception {
        reset();
        service.createBooking("C001", "F001", "2026-03-01T08:00", "2026-03-01T10:00", null);
        // Lấn vào 1 phút: 09:59-10:01.
        expectConflict(() -> service.createBooking("C002", "F001", "2026-03-01T09:59", "2026-03-01T10:01", null));
    }

    private static void testRejectMaintenanceField() throws Exception {
        reset();
        // F002 đang MAINTENANCE trong seed.
        expectConflict(() -> service.createBooking("C001", "F002", "2026-03-01T08:00", "2026-03-01T10:00", null));
    }

    private static void testRejectBadTimeFormat() throws Exception {
        reset();
        expectBadRequest(() -> service.createBooking("C001", "F001", "01/03/2026 08:00", "2026-03-01T10:00", null));
    }

    private static void testRejectEndBeforeStart() throws Exception {
        reset();
        expectBadRequest(() -> service.createBooking("C001", "F001", "2026-03-01T10:00", "2026-03-01T08:00", null));
    }

    private static void testRejectUnknownCustomer() throws Exception {
        reset();
        expectNotFound(() -> service.createBooking("KHONG_CO", "F001", "2026-03-01T08:00", "2026-03-01T10:00", null));
    }

    private static void testRejectInactiveService() throws Exception {
        reset();
        // SV006 đang inactive trong seed.
        String inactive = data.getServices().stream()
                .filter(s -> !s.isActive()).findFirst().orElseThrow().getServiceId();
        expectConflict(() -> service.createBooking("C001", "F001", "2026-03-01T08:00", "2026-03-01T10:00", inactive));
    }

    private static void testPersistedToDatabase() throws Exception {
        reset();
        Booking booking = service.createBooking("C001", "F001", "2026-03-01T08:00", "2026-03-01T10:00", null);

        try (Connection check = Database.open(dbFile().toString())) {
            var count = new Repository(check).countRows();
            // Seed 12 booking + 1 mới = 13.
            assertTrue("13 booking trong DB", count.get("bookings") == 13);
            assertTrue("13 hoá đơn trong DB", count.get("invoices") == 13);
            assertTrue("booking mới tồn tại", queryCount(check, booking.getBookingId()) == 1);
        }
    }

    private static void testSurvivesRestart() throws Exception {
        reset();
        Booking booking = service.createBooking("C001", "F001", "2026-03-01T08:00", "2026-03-01T10:00", "SV001");
        double expected = booking.getTotalAmount();

        // Mở lại DB và nạp vào một kho trống: booking phải còn nguyên.
        try (Connection reopened = Database.open(dbFile().toString())) {
            SmartSportHubData fresh = new SmartSportHubData();
            assertTrue("nạp lại được", new Repository(reopened).loadInto(
                    fresh.getFieldManager(), fresh.getCustomerManager(),
                    fresh.getServiceManager(), fresh.getBookingManager(), fresh.getInvoiceManager()));

            Booking restored = fresh.getBookingManager().findBookingById(booking.getBookingId());
            assertTrue("booking còn sau restart", restored != null);
            assertTrue("tổng tiền không đổi", Math.abs(restored.getTotalAmount() - expected) < 0.01);
            assertTrue("dịch vụ gắn đúng", "SV001".equals(restored.getService().getServiceId()));
        }
    }

    private static void testCancelReleasesField() throws Exception {
        reset();
        service.setFieldStatus("F003", "AVAILABLE");
        Booking booking = service.createBooking("C001", "F003", "2026-03-01T08:00", "2026-03-01T10:00", null);
        assertTrue("sân chuyển sang OCCUPIED sau khi đặt",
                data.getFieldManager().findFieldById("F003").getStatus() == FieldStatus.OCCUPIED);

        service.cancelBooking(booking.getBookingId());
        assertTrue("booking CANCELLED", booking.getStatus() == BookingStatus.CANCELLED);
        assertTrue("sân trả về AVAILABLE",
                data.getFieldManager().findFieldById("F003").getStatus() == FieldStatus.AVAILABLE);
        assertTrue("khung giờ đã nhả ra, đặt lại được",
                service.createBooking("C002", "F003", "2026-03-01T08:00", "2026-03-01T10:00", null) != null);
    }

    private static void testRejectCancelCompleted() throws Exception {
        reset();
        Booking booking = service.createBooking("C001", "F001", "2026-03-01T08:00", "2026-03-01T10:00", null);
        service.completeBooking(booking.getBookingId());
        expectConflict(() -> service.cancelBooking(booking.getBookingId()));
    }

    private static void testRejectDoubleCancel() throws Exception {
        reset();
        Booking booking = service.createBooking("C001", "F001", "2026-03-01T08:00", "2026-03-01T10:00", null);
        service.cancelBooking(booking.getBookingId());
        expectConflict(() -> service.cancelBooking(booking.getBookingId()));
    }

    private static void testReschedule() throws Exception {
        reset();
        Booking booking = service.createBooking("C001", "F001", "2026-03-01T08:00", "2026-03-01T10:00", null);
        service.rescheduleBooking(booking.getBookingId(), "2026-03-01T14:00", "2026-03-01T16:00");

        assertTrue("bắt đầu đã đổi", booking.getStartTime().getHour() == 14);
        assertTrue("kết thúc đã đổi", booking.getEndTime().getHour() == 16);

        // Khung cũ phải nhả ra trong DB.
        assertTrue("khung 08:00-10:00 không còn bị chiếm",
                service.createBooking("C002", "F001", "2026-03-01T08:00", "2026-03-01T10:00", null) != null);
    }

    private static void testRescheduleConflict() throws Exception {
        reset();
        Booking first = service.createBooking("C001", "F001", "2026-03-01T08:00", "2026-03-01T10:00", null);
        service.createBooking("C002", "F001", "2026-03-01T14:00", "2026-03-01T16:00", null);
        // Khung cũ của chính first vẫn chưa lưu: phải bỏ qua, nhưng 14:00 thì bị chặn.
        expectConflict(() -> service.rescheduleBooking(first.getBookingId(),
                "2026-03-01T15:00", "2026-03-01T17:00"));
    }

    private static void testRescheduleCancelled() throws Exception {
        reset();
        Booking booking = service.createBooking("C001", "F001", "2026-03-01T08:00", "2026-03-01T10:00", null);
        service.cancelBooking(booking.getBookingId());
        expectConflict(() -> service.rescheduleBooking(booking.getBookingId(),
                "2026-03-01T14:00", "2026-03-01T16:00"));
    }

    private static void testComplete() throws Exception {
        reset();
        Booking booking = service.createBooking("C001", "F001", "2026-03-01T08:00", "2026-03-01T10:00", null);
        service.completeBooking(booking.getBookingId());
        assertTrue("COMPLETED", booking.getStatus() == BookingStatus.COMPLETED);
        expectConflict(() -> service.completeBooking(booking.getBookingId()));
    }

    private static void testPayInvoice() throws Exception {
        reset();
        Booking booking = service.createBooking("C001", "F001", "2026-03-01T08:00", "2026-03-01T10:00", null);
        Invoice invoice = findInvoice(booking.getBookingId());

        Invoice paid = service.payInvoice(invoice.getInvoiceId());
        assertTrue("PAID", paid.getStatus() == InvoiceStatus.PAID);
        assertTrue("ghi vào DB", storedInvoiceStatus(invoice.getInvoiceId()).equals("PAID"));
    }

    private static void testRejectDoublePay() throws Exception {
        reset();
        Booking booking = service.createBooking("C001", "F001", "2026-03-01T08:00", "2026-03-01T10:00", null);
        Invoice invoice = findInvoice(booking.getBookingId());
        service.payInvoice(invoice.getInvoiceId());
        expectConflict(() -> service.payInvoice(invoice.getInvoiceId()));
    }

    private static void testRefundInvoice() throws Exception {
        reset();
        Booking booking = service.createBooking("C001", "F001", "2026-03-01T08:00", "2026-03-01T10:00", null);
        Invoice invoice = findInvoice(booking.getBookingId());
        service.payInvoice(invoice.getInvoiceId());
        service.refundInvoice(invoice.getInvoiceId());
        assertTrue("REFUNDED", invoice.getStatus() == InvoiceStatus.REFUNDED);
        // Đã hoàn rồi thì không thu lại được.
        expectConflict(() -> service.payInvoice(invoice.getInvoiceId()));
    }

    private static void testRejectMaintenanceWithBookings() throws Exception {
        reset();
        service.setFieldStatus("F003", "AVAILABLE");
        service.createBooking("C001", "F003", "2026-03-01T08:00", "2026-03-01T10:00", null);
        // Sân còn lượt CONFIRMED thì không cho vào bảo trì.
        expectConflict(() -> service.setFieldStatus("F003", "MAINTENANCE"));
    }

    // ------------------------------------------------------------------ tiện ích

    private static void reset() throws Exception {
        if (connection != null) {
            connection.close();
        }
        data = SeedData.create();
        // Ứng dụng thật giữ một kết nối suốt vòng đời; test cũng vậy.
        connection = Database.open(dbFile().toString());
        Database.createSchema(connection);
        repository = new Repository(connection);
        repository.saveAll(data.getFieldManager(), data.getCustomerManager(),
                data.getServiceManager(), data.getBookingManager(), data.getInvoiceManager());
        service = new BookingService(data, repository);
    }

    private static Path dbFile() {
        return workDir.resolve("service.db");
    }

    private static Invoice findInvoice(String bookingId) {
        return data.getInvoices().stream()
                .filter(i -> i.getBookingId().equals(bookingId)).findFirst().orElse(null);
    }

    private static int queryCount(Connection connection, String bookingId) throws Exception {
        try (var ps = connection.prepareStatement("SELECT COUNT(*) FROM bookings WHERE id = ?")) {
            ps.setString(1, bookingId);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static String storedInvoiceStatus(String invoiceId) throws Exception {
        try (var connection = Database.open(dbFile().toString());
             var ps = connection.prepareStatement("SELECT status FROM invoices WHERE id = ?")) {
            ps.setString(1, invoiceId);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : "";
            }
        }
    }

    private static void run(String name, CheckedRunnable body) {
        try {
            body.run();
            passed++;
            System.out.println("  [PASS] " + name);
        } catch (Throwable error) {
            failed++;
            System.out.println("  [FAIL] " + name + " -> " + error);
        }
    }

    private interface CheckedRunnable {
        void run() throws Exception;
    }

    private static void expectConflict(Runnable action) {
        expect(action, 409, "xung đột");
    }

    private static void expectBadRequest(Runnable action) {
        expect(action, 400, "định dạng/giá trị");
    }

    private static void expectNotFound(Runnable action) {
        expect(action, 404, "không tìm thấy");
    }

    private static void expect(Runnable action, int status, String label) {
        try {
            action.run();
        } catch (BusinessException error) {
            if (error.getStatusCode() != status) {
                throw new AssertionError("cần HTTP " + status + " (" + label + ") nhưng nhận "
                        + error.getStatusCode() + ": " + error.getMessage());
            }
            return;
        }
        throw new AssertionError("phải bị từ chối (" + label + ") nhưng lại thành công");
    }

    private static void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void cleanup(Path dir) throws Exception {
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        try (var stream = Files.walk(dir)) {
            stream.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception ignored) {
                            // Thư mục tạm: không ảnh hưởng kết quả.
                        }
                    });
        }
    }
}