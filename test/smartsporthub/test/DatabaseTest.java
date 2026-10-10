package smartsporthub.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import smartsporthub.data.SeedData;
import smartsporthub.data.SmartSportHubData;
import smartsporthub.db.Database;
import smartsporthub.db.Repository;
import smartsporthub.model.Booking;
import smartsporthub.model.BookingStatus;
import smartsporthub.model.Customer;
import smartsporthub.model.FootballField;
import smartsporthub.model.Invoice;
import smartsporthub.model.InvoiceStatus;
import smartsporthub.model.Service;
import smartsporthub.model.SportField;
import smartsporthub.model.FieldStatus;
import smartsporthub.model.StandardCustomer;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Kiểm thử tầng lưu trữ: schema, vòng ghi-đọc, và dữ liệu còn nguyên sau khi mở lại. */
public class DatabaseTest {

    private static int passed;
    private static int failed;
    private static Path workDir;

    public static void main(String[] args) throws Exception {
        workDir = Files.createTempDirectory("ssh-db-test");
        try {
            run("Tạo schema không lỗi", DatabaseTest::testCreateSchema);
            run("Ghi rồi đọc lại khớp hoàn toàn", DatabaseTest::testRoundTrip);
            run("Dữ liệu còn sau khi mở kết nối mới", DatabaseTest::testPersistence);
            run("Ghi đè không nhân bản dữ liệu", DatabaseTest::testSaveTwice);
            run("Ràng buộc FK chặn booking khách không tồn tại", DatabaseTest::testForeignKey);
            run("CHECK chặn trạng thái không hợp lệ", DatabaseTest::testCheckConstraint);
            run("Lịch trùng nhận diện đúng", DatabaseTest::testOverlapDetection);
            run("DB rỗng thì hasData false", DatabaseTest::testEmptyDatabase);
            run("ClearAll giữ schema, mất dữ liệu", DatabaseTest::testClearAll);
        } finally {
            cleanup(workDir);
        }

        System.out.println();
        System.out.printf("===== KẾT QUẢ DATABASE: %d/%d PASS =====%n", passed, passed + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------ test

    private static void testCreateSchema() throws Exception {
        try (Connection connection = Database.open(workDir.resolve("a.db").toString())) {
            Database.createSchema(connection);
            // Chạy lần hai phải không nổi: CREATE TABLE IF NOT EXISTS.
            Database.createSchema(connection);
            Map<String, Integer> counts = new Repository(connection).countRows();
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                assertTrue(entry.getKey() + " phải rỗng", entry.getValue() == 0);
            }
        }
    }

    private static void testRoundTrip() throws Exception {
        SmartSportHubData original = SeedData.create();
        try (Connection connection = Database.open(workDir.resolve("b.db").toString())) {
            Database.createSchema(connection);
            Repository repository = new Repository(connection);
            repository.saveAll(original.getFieldManager(), original.getCustomerManager(),
                    original.getServiceManager(), original.getBookingManager(), original.getInvoiceManager());

            SmartSportHubData reloaded = new SmartSportHubData();
            assertTrue("DB có dữ liệu để nạp", repository.loadInto(reloaded.getFieldManager(),
                    reloaded.getCustomerManager(), reloaded.getServiceManager(),
                    reloaded.getBookingManager(), reloaded.getInvoiceManager()));

            assertTrue("số sân", original.getFields().size() == reloaded.getFields().size());
            assertTrue("số khách", original.getCustomers().size() == reloaded.getCustomers().size());
            assertTrue("số dịch vụ", original.getServices().size() == reloaded.getServices().size());
            assertTrue("số booking", original.getBookings().size() == reloaded.getBookings().size());
            assertTrue("số hóa đơn", original.getInvoices().size() == reloaded.getInvoices().size());

            for (Booking before : original.getBookings()) {
                Booking after = reloaded.getBookingManager().findBookingById(before.getBookingId());
                assertTrue("tồn tại " + before.getBookingId(), after != null);
                assertTrue("tổng tiền " + before.getBookingId(),
                        closeEnough(before.getTotalAmount(), after.getTotalAmount()));
                assertTrue("trạng thái " + before.getBookingId(),
                        before.getStatus() == after.getStatus());
                assertTrue("giờ bắt đầu " + before.getBookingId(),
                        before.getStartTime().equals(after.getStartTime()));
                assertTrue("giờ kết thúc " + before.getBookingId(),
                        before.getEndTime().equals(after.getEndTime()));
                assertTrue("loại sân " + before.getBookingId(),
                        before.getField().getClass() == after.getField().getClass());
            }

            for (var before : original.getCustomers()) {
                Customer after = reloaded.getCustomerManager().findCustomerById(before.getCustomerId());
                assertTrue("khách " + before.getCustomerId(), after != null);
                assertTrue("loại khách " + before.getCustomerId(),
                        before.getCustomerType() == after.getCustomerType());
                assertTrue("tên " + before.getCustomerId(), before.getFullName().equals(after.getFullName()));
            }
        }
    }

    private static void testPersistence() throws Exception {
        Path file = workDir.resolve("c.db");
        SmartSportHubData seed = SeedData.create();
        try (Connection connection = Database.open(file.toString())) {
            Database.createSchema(connection);
            new Repository(connection).saveAll(seed.getFieldManager(), seed.getCustomerManager(),
                    seed.getServiceManager(), seed.getBookingManager(), seed.getInvoiceManager());
        }
        // Đóng hẳn rồi mở lại: dữ liệu phải còn.
        try (Connection reopened = Database.open(file.toString())) {
            SmartSportHubData reloaded = new SmartSportHubData();
            assertTrue("nạp lại được", new Repository(reopened).loadInto(
                    reloaded.getFieldManager(), reloaded.getCustomerManager(),
                    reloaded.getServiceManager(), reloaded.getBookingManager(), reloaded.getInvoiceManager()));
            assertTrue("6 sân", reloaded.getFields().size() == 6);
            assertTrue("12 booking", reloaded.getBookings().size() == 12);
        }
    }

    private static void testSaveTwice() throws Exception {
        SmartSportHubData seed = SeedData.create();
        try (Connection connection = Database.open(workDir.resolve("d.db").toString())) {
            Database.createSchema(connection);
            Repository repository = new Repository(connection);
            for (int i = 0; i < 3; i++) {
                repository.saveAll(seed.getFieldManager(), seed.getCustomerManager(),
                        seed.getServiceManager(), seed.getBookingManager(), seed.getInvoiceManager());
            }
            // Ghi 3 lần mà số dòng không nhân lên: saveAll phải xóa trước rồi chèn.
            assertTrue("không nhân bản khách", repository.countRows().get("customers") == 6);
            assertTrue("không nhân bản booking", repository.countRows().get("bookings") == 12);
        }
    }

    private static void testForeignKey() throws Exception {
        try (Connection connection = Database.open(workDir.resolve("e.db").toString())) {
            Database.createSchema(connection);
            boolean rejected = false;
            try (var st = connection.createStatement()) {
                st.executeUpdate("""
                        INSERT INTO customers (id, full_name, phone_number, email, customer_type, discount_rate)
                        VALUES ('C1', 'Test', '0900000000', 'a@b.c', 'STANDARD', 0)""");
                st.executeUpdate("INSERT INTO services (id, name, category, unit_price, active) VALUES ('S1', 'X', 'Y', 1, 1)");
                st.executeUpdate("INSERT INTO fields (id, name, type, base_price_per_hour, status) VALUES ('F1', 'Sân', 'FOOTBALL', 1000, 'AVAILABLE')");
                // Khách KHX_999 không tồn tại: phải bị FK chặn.
                st.executeUpdate("""
                        INSERT INTO bookings (id, customer_id, field_id, service_id, start_time, end_time,
                                              status, peak_hour, hours, field_price, service_fee, discount, total_amount)
                        VALUES ('B1', 'KHX_999', 'F1', 'S1', '2026-03-01T08:00', '2026-03-01T10:00',
                                'CONFIRMED', 0, 2, 2000, 0, 0, 2000)""");
            } catch (SQLException expected) {
                rejected = true;
            }
            assertTrue("FK phải chặn khách không tồn tại", rejected);
        }
    }

    private static void testCheckConstraint() throws Exception {
        try (Connection connection = Database.open(workDir.resolve("f.db").toString())) {
            Database.createSchema(connection);
            boolean rejected = false;
            try (var st = connection.createStatement()) {
                st.executeUpdate("INSERT INTO services (id, name, category, unit_price, active) VALUES ('S1', 'X', 'Y', 1, 1)");
                // "HAC" không phải trạng thái hợp lệ.
                st.executeUpdate("INSERT INTO fields (id, name, type, base_price_per_hour, status) VALUES ('F1', 'Sân', 'FOOTBALL', 1000, 'HAC')");
            } catch (SQLException expected) {
                rejected = true;
            }
            assertTrue("CHECK phải chặn trạng thái sai", rejected);
        }
    }

    private static void testOverlapDetection() throws Exception {
        try (Connection connection = Database.open(workDir.resolve("g.db").toString())) {
            Database.createSchema(connection);
            SmartSportHubData data = SeedData.create();
            Repository repository = new Repository(connection);
            repository.saveAll(data.getFieldManager(), data.getCustomerManager(),
                    data.getServiceManager(), data.getBookingManager(), data.getInvoiceManager());

            var slots = repository.loadBookedSlotsOfField("F004");
            assertTrue("F004 có booking PENDING/CONFIRMED", !slots.isEmpty());

            // Sân chỉ có booking COMPLETED thì không tính là đã bị chiếm lịch:
            // khách hủa xong thì sân phải đặt lại được.
            assertTrue("F001 chỉ có booking cũ nên vẫn trống",
                    repository.loadBookedSlotsOfField("F001").isEmpty());

            boolean overlapDetected = false;
            boolean falsePositive = false;
            for (var slot : slots) {
                // Dùng đúng khung giờ của booking đang có: chắc chắn trùng.
                if (slot.overlaps(slot.start(), slot.end())) {
                    overlapDetected = true;
                }
                // Lùi 1 giờ, kết thúc đúng lúc booking bắt đầu: không trùng.
                if (slot.overlaps(slot.start().minusHours(1), slot.start())) {
                    falsePositive = true;
                }
                // Bắt đầu đúng lúc booking kết thúc: không trùng.
                if (slot.overlaps(slot.end(), slot.end().plusHours(1))) {
                    falsePositive = true;
                }
                // Lùi 30 phút vẫn lấn sang giờ đầu: trùng thật.
                if (slot.overlaps(slot.start().minusMinutes(30), slot.start().plusMinutes(30))) {
                    overlapDetected = true;
                }
            }
            assertTrue("nhận diện trùng lịch", overlapDetected);
            assertTrue("không báo trùng sai", !falsePositive);
        }
    }

    private static void testEmptyDatabase() throws Exception {
        try (Connection connection = Database.open(workDir.resolve("h.db").toString())) {
            Database.createSchema(connection);
            assertTrue("DB mới phải rỗng", !Database.hasData(connection));
        }
    }

    private static void testClearAll() throws Exception {
        SmartSportHubData seed = SeedData.create();
        try (Connection connection = Database.open(workDir.resolve("i.db").toString())) {
            Database.createSchema(connection);
            Repository repository = new Repository(connection);
            repository.saveAll(seed.getFieldManager(), seed.getCustomerManager(),
                    seed.getServiceManager(), seed.getBookingManager(), seed.getInvoiceManager());
            assertTrue("trước khi xóa có dữ liệu", Database.hasData(connection));

            Database.clearAll(connection);
            assertTrue("sau khi xóa rỗng", !Database.hasData(connection));

            // Schema phải còn nguyên: nạp vào một kho mới không được lỗi.
            SmartSportHubData fresh = new SmartSportHubData();
            assertTrue("schema còn nguyên để nạp lại", !new Repository(connection).loadInto(
                    fresh.getFieldManager(), fresh.getCustomerManager(),
                    fresh.getServiceManager(), fresh.getBookingManager(), fresh.getInvoiceManager()));
            assertTrue("kho sau khi xóa rỗng", fresh.getCustomers().isEmpty());

            // Ghi lại được sau khi đã xóa: xác nhận không còn ràng buộc ẩn.
            SmartSportHubData again = SeedData.create();
            new Repository(connection).saveAll(again.getFieldManager(), again.getCustomerManager(),
                    again.getServiceManager(), again.getBookingManager(), again.getInvoiceManager());
            assertTrue("ghi lại thành công", repository.countRows().get("bookings") == 12);
        }
    }

    // ------------------------------------------------------------------ tiện ích

    private static boolean closeEnough(double a, double b) {
        return Math.abs(a - b) < 0.01;
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

    private static void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private interface CheckedRunnable {
        void run() throws Exception;
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
                            // Bỏ qua: thư mục tạm, không ảnh hưởng kết quả test.
                        }
                    });
        }
    }
}