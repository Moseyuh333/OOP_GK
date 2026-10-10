package smartsporthub.test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpServer;

import smartsporthub.api.SmartSportHubApi;
import smartsporthub.data.SeedData;
import smartsporthub.data.SmartSportHubData;
import smartsporthub.db.Database;
import smartsporthub.db.Repository;
import smartsporthub.service.BookingService;

/**
 * Kiểm thử endpoint ghi qua HTTP thật: dựng server thật, gọi bằng HttpClient thật.
 * <p>
 * Không mock tầng HTTP, vì phần dễ hỏng nhất chính là ánh xạ phương thức, đường dẫn
 * và mã lỗi — những thứ chỉ lộ ra khi có request thật.
 */
public class WriteApiTest {

    private static int passed;
    private static int failed;

    private static HttpServer server;
    private static HttpClient client;
    private static String base;
    private static Path workDir;
    private static Connection connection;
    private static SmartSportHubData data;

    /** Số lượt đặt đã tạo thành công, dùng để kiểm tra dữ liệu xuống DB. */
    private static int createdBookings;

    public static void main(String[] args) throws Exception {
        workDir = Files.createTempDirectory("ssh-writeapi-test");
        startServer();
        createdBookings = 0;
        try {
            run("POST đặt sân trả 201 kèm booking", WriteApiTest::testCreateBooking);
            run("Đặt sân trùng lịch trả 409", WriteApiTest::testCreateConflict);
            run("Thiếu trường bắt buộc trả 400", WriteApiTest::testMissingField);
            run("Sai định dạng giờ trả 400", WriteApiTest::testBadTimeFormat);
            run("Khách không tồn tại trả 404", WriteApiTest::testUnknownCustomer);
            run("GET /api/bookings thấy booking mới", WriteApiTest::testBookingAppearsInList);
            run("POST cancel hủy được booking", WriteApiTest::testCancel);
            run("Hủy hai lần trả 409", WriteApiTest::testDoubleCancel);
            run("POST complete đổi trạng thái", WriteApiTest::testComplete);
            run("POST reschedule đổi khung giờ", WriteApiTest::testReschedule);
            run("POST pay thu tiền hoá đơn", WriteApiTest::testPayInvoice);
            run("POST refund hoàn tiền", WriteApiTest::testRefundInvoice);
            run("PATCH đổi trạng thái sân", WriteApiTest::testSetFieldStatus);
            run("Sân còn lịch thì không cho bảo trì (409)", WriteApiTest::testMaintenanceBlocked);
            run("GET vẫn hoạt động sau khi ghi", WriteApiTest::testReadsStillWork);
            run("Phương thức sai trả 405", WriteApiTest::testWrongMethod);
            run("Action lạ trả 404", WriteApiTest::testUnknownAction);
            run("Dữ liệu ghi xuống file DB", WriteApiTest::testWritesHitDatabase);
        } finally {
            stopServer();
            cleanup(workDir);
        }

        System.out.println();
        System.out.printf("===== KẾT QUẢ API GHI: %d/%d PASS =====%n", passed, passed + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------ test

    private static void testCreateBooking() throws Exception {
        Response response = post("/api/bookings", """
                {"customerId":"C001","fieldId":"F001","startTime":"2026-04-01T08:00",
                 "endTime":"2026-04-01T10:00","serviceId":"SV001"}""");

        assertTrue("HTTP 201, nhận " + response.status, response.status == 201);
        assertTrue("có mã booking: " + response.body, response.body.contains("\"id\""));
        assertTrue("trạng thái CONFIRMED", response.body.contains("CONFIRMED"));
    }

    private static void testCreateConflict() throws Exception {
        post("/api/bookings", """
                {"customerId":"C001","fieldId":"F001","startTime":"2026-04-01T08:00",
                 "endTime":"2026-04-01T10:00"}""");

        Response second = post("/api/bookings", """
                {"customerId":"C002","fieldId":"F001","startTime":"2026-04-01T09:00",
                 "endTime":"2026-04-01T11:00"}""");
        assertTrue("HTTP 409, nhận " + second.status, second.status == 409);
        assertTrue("thông điệp tiếng Việt giải thích lý do", second.body.contains("lượt đặt"));
    }

    private static void testMissingField() throws Exception {
        Response response = post("/api/bookings", "{\"customerId\":\"C001\"}");
        assertTrue("HTTP 400, nhận " + response.status, response.status == 400);
        assertTrue("nêu đúng trường thiếu", response.body.contains("fieldId"));
    }

    private static void testBadTimeFormat() throws Exception {
        Response response = post("/api/bookings", """
                {"customerId":"C001","fieldId":"F003","startTime":"01/04/2026",
                 "endTime":"2026-04-01T10:00"}""");
        assertTrue("HTTP 400, nhận " + response.status, response.status == 400);
    }

    private static void testUnknownCustomer() throws Exception {
        Response response = post("/api/bookings", """
                {"customerId":"KHONG_CO","fieldId":"F003","startTime":"2026-04-02T08:00",
                 "endTime":"2026-04-02T10:00"}""");
        assertTrue("HTTP 404, nhận " + response.status, response.status == 404);
    }

    private static void testBookingAppearsInList() throws Exception {
        Response created = post("/api/bookings", """
                {"customerId":"C003","fieldId":"F003","startTime":"2026-04-03T08:00",
                 "endTime":"2026-04-03T09:00"}""");
        String bookingId = extract(created.body, "id");

        Response list = get("/api/bookings");
        assertTrue("HTTP 200", list.status == 200);
        assertTrue("booking mới xuất hiện trong GET", list.body.contains(bookingId));
    }

    private static void testCancel() throws Exception {
        Response created = post("/api/bookings", """
                {"customerId":"C002","fieldId":"F005","startTime":"2026-04-05T08:00",
                 "endTime":"2026-04-05T10:00"}""");
        String bookingId = extract(created.body, "id");

        Response cancelled = post("/api/bookings/" + bookingId + "/cancel", "{}");
        assertTrue("HTTP 200, nhận " + cancelled.status, cancelled.status == 200);
        assertTrue("trạng thái CANCELLED", cancelled.body.contains("CANCELLED"));
    }

    private static void testDoubleCancel() throws Exception {
        Response created = post("/api/bookings", """
                {"customerId":"C002","fieldId":"F005","startTime":"2026-04-06T08:00",
                 "endTime":"2026-04-06T10:00"}""");
        String bookingId = extract(created.body, "id");

        post("/api/bookings/" + bookingId + "/cancel", "{}");
        Response again = post("/api/bookings/" + bookingId + "/cancel", "{}");
        assertTrue("HTTP 409, nhận " + again.status, again.status == 409);
    }

    private static void testComplete() throws Exception {
        Response created = post("/api/bookings", """
                {"customerId":"C003","fieldId":"F003","startTime":"2026-04-07T08:00",
                 "endTime":"2026-04-07T10:00"}""");
        String bookingId = extract(created.body, "id");

        Response done = post("/api/bookings/" + bookingId + "/complete", "{}");
        assertTrue("HTTP 200, nhận " + done.status, done.status == 200);
        assertTrue("trạng thái COMPLETED", done.body.contains("COMPLETED"));
    }

    private static void testReschedule() throws Exception {
        Response created = post("/api/bookings", """
                {"customerId":"C002","fieldId":"F005","startTime":"2026-04-08T08:00",
                 "endTime":"2026-04-08T10:00"}""");
        String bookingId = extract(created.body, "id");

        Response moved = post("/api/bookings/" + bookingId + "/reschedule", """
                {"newStartTime":"2026-04-08T15:00","newEndTime":"2026-04-08T17:00"}""");
        assertTrue("HTTP 200, nhận " + moved.status, moved.status == 200);
        assertTrue("giờ mới trong phản hồi", moved.body.contains("T15:00"));
    }

    private static void testPayInvoice() throws Exception {
        Response created = post("/api/bookings", """
                {"customerId":"C001","fieldId":"F001","startTime":"2026-04-09T08:00",
                 "endTime":"2026-04-09T10:00"}""");
        String bookingId = extract(created.body, "id");
        String invoiceId = findInvoiceIdOf(bookingId);

        Response paid = post("/api/invoices/" + invoiceId + "/pay", "{}");
        assertTrue("HTTP 200, nhận " + paid.status, paid.status == 200);
        assertTrue("trạng thái PAID", paid.body.contains("PAID"));

        Response twice = post("/api/invoices/" + invoiceId + "/pay", "{}");
        assertTrue("thu hai lần trả 409", twice.status == 409);
    }

    private static void testRefundInvoice() throws Exception {
        Response created = post("/api/bookings", """
                {"customerId":"C001","fieldId":"F001","startTime":"2026-04-10T08:00",
                 "endTime":"2026-04-10T10:00"}""");
        String bookingId = extract(created.body, "id");
        String invoiceId = findInvoiceIdOf(bookingId);

        post("/api/invoices/" + invoiceId + "/pay", "{}");
        Response refunded = post("/api/invoices/" + invoiceId + "/refund", "{}");
        assertTrue("HTTP 200, nhận " + refunded.status, refunded.status == 200);
        assertTrue("trạng thái REFUNDED", refunded.body.contains("REFUNDED"));
    }

    private static void testSetFieldStatus() throws Exception {
        Response response = patch("/api/fields/F006/status", "{\"status\":\"MAINTENANCE\"}");
        assertTrue("HTTP 200, nhận " + response.status, response.status == 200);
        assertTrue("sân sang MAINTENANCE", response.body.contains("MAINTENANCE"));

        // Trả lại AVAILABLE để test sau không bị ảnh hưởng.
        patch("/api/fields/F006/status", "{\"status\":\"AVAILABLE\"}");
    }

    private static void testMaintenanceBlocked() throws Exception {
        // F001 trống lúc đầu: đặt vào được, rồi phải không bỏ vào bảo trì được nữa.
        Response created = post("/api/bookings", """
                {"customerId":"C002","fieldId":"F001","startTime":"2026-04-12T08:00",
                 "endTime":"2026-04-12T10:00"}""");
        assertTrue("đặt được lúc đầu, nhận " + created.status, created.status == 201);

        Response blocked = patch("/api/fields/F001/status", "{\"status\":\"MAINTENANCE\"}");
        assertTrue("sân còn lịch phải trả 409, nhận " + blocked.status, blocked.status == 409);
    }

    private static void testReadsStillWork() throws Exception {
        for (String path : new String[] {"/api/data", "/api/health", "/api/bookings",
                "/api/invoices", "/api/fields", "/api/customers", "/api/services"}) {
            Response response = get(path);
            assertTrue(path + " phải 200, nhận " + response.status, response.status == 200);
        }
    }

    private static void testWrongMethod() throws Exception {
        Response response = send("DELETE", "/api/bookings", "{}");
        assertTrue("HTTP 405, nhận " + response.status, response.status == 405);
    }

    private static void testUnknownAction() throws Exception {
        Response response = post("/api/bookings/HAY_BO_123/xyz", "{}");
        assertTrue("HTTP 404, nhận " + response.status, response.status == 404);
    }

    private static void testWritesHitDatabase() throws Exception {
        post("/api/bookings", """
                {"customerId":"C003","fieldId":"F003","startTime":"2026-04-13T08:00",
                 "endTime":"2026-04-13T10:00"}""");

        // Đọc bằng kết nối riêng: xác nhận dữ liệu nằm trong file, không chỉ trong RAM.
        try (Connection check = Database.open(dbPath().toString())) {
            var counts = new Repository(check).countRows();
            // Đếm thật: 12 booking seed + các lượt đặt thành công trong suite.
            assertTrue("số booking khớp seed + lượt đã đặt, nhận " + counts.get("bookings"),
                    counts.get("bookings") == 12 + createdBookings);
        }
    }

    // ------------------------------------------------------------------ hạ tầng

    private static void startServer() throws Exception {
        data = SeedData.create();
        connection = Database.open(dbPath().toString());
        Database.createSchema(connection);
        Repository repository = new Repository(connection);
        repository.saveAll(data.getFieldManager(), data.getCustomerManager(),
                data.getServiceManager(), data.getBookingManager(), data.getInvoiceManager());

        server = SmartSportHubApi.createServer(0, data, new BookingService(data, repository));
        server.start();
        base = "http://localhost:" + server.getAddress().getPort();
        client = HttpClient.newHttpClient();
    }

    private static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ignored) {
                // Kết thúc test: không cần báo.
            }
        }
    }

    private static Path dbPath() {
        return workDir.resolve("writeapi.db");
    }

    private record Response(int status, String body) {
    }

    private static Response get(String path) throws Exception {
        return send("GET", path, null);
    }

    private static Response post(String path, String body) throws Exception {
        Response response = send("POST", path, body);
        if (response.status() == 201 && path.equals("/api/bookings")) {
            createdBookings++;
        }
        return response;
    }

    private static Response patch(String path, String body) throws Exception {
        return send("PATCH", path, body);
    }

    private static Response send(String method, String path, String body) throws Exception {
        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + path))
                .header("Content-Type", "application/json")
                .method(method, publisher)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new Response(response.statusCode(), response.body());
    }

    /** Bóc một giá trị chuỗi từ JSON: "key":"value". */
    private static String extract(String json, String key) {
        var matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        if (!matcher.find()) {
            throw new AssertionError("không tìm thấy " + key + " trong: " + json);
        }
        return matcher.group(1);
    }

    /** Hoá đơn sinh ra có mã theo quy tắc I + phần đuôi mã booking. */
    private static String findInvoiceIdOf(String bookingId) throws Exception {
        Response list = get("/api/invoices");
        // Mã hoá đơn khớp booking, tìm trong danh sách trả về.
        String invoiceId = "I" + bookingId.substring(1);
        if (!list.body.contains(invoiceId)) {
            throw new AssertionError("không thấy hoá đơn " + invoiceId + " trong danh sách");
        }
        return invoiceId;
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
                            // Thư mục tạm.
                        }
                    });
        }
    }
}