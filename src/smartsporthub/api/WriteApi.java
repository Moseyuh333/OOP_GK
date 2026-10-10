package smartsporthub.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import smartsporthub.data.SmartSportHubData;
import smartsporthub.dto.BookingDto;
import smartsporthub.dto.InvoiceDto;
import smartsporthub.json.JsonWriter;
import smartsporthub.service.BookingService;

/**
 * Các endpoint ghi dữ liệu: đặt sân, đổi lịch, hủy, hoàn thành, thu/hoàn tiền, đổi trạng thái sân.
 * <p>
 * Tách khỏi {@link SmartSportHubApi} vì nhóm endpoint này có vòng đời khác: chỉ ghi,
 * trả lỗi nghiệp vụ có mã HTTP cụ thể, và cần đọc body của request.
 */
final class WriteApi {

    private static final String JSON_TYPE = "application/json; charset=utf-8";

    private WriteApi() {
        // Utility class
    }

    /**
     * Đăng ký toàn bộ endpoint ghi lên server.
     * <p>
     * Chỉ đăng ký các context có dấu {@code /} ở cuối: {@code /api/bookings} không có
     * dấu gạch đã được phần đọc đăng ký rồi, đăng ký lần hai sẽ ném
     * {@code IllegalArgumentException: cannot add context to list}.
     */
    static void register(HttpServer server, SmartSportHubData data, BookingService bookingService) {
        // /api/bookings/{id}/cancel | /complete | /reschedule
        server.createContext("/api/bookings/", SmartSportHubApi.wrap(exchange -> {
            String path = exchange.getRequestURI().getPath();
            String id = path.substring("/api/bookings/".length());
            if (id.contains("/")) {
                id = id.substring(0, id.indexOf('/'));
            }
            String action = path.substring(path.lastIndexOf('/') + 1);

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())
                    && !"PATCH".equalsIgnoreCase(exchange.getRequestMethod())) {
                SmartSportHubApi.respondError(exchange, 405, "Chỉ nhận POST hoặc PATCH.");
                return;
            }
            Map<String, String> body = readJsonObject(exchange);

            switch (action) {
                case "cancel" -> {
                    var booking = bookingService.cancelBooking(id);
                    SmartSportHubApi.respond(exchange, 200,
                            JsonWriter.toJson(new BookingDto(booking).toMap()));
                }
                case "complete" -> {
                    var booking = bookingService.completeBooking(id);
                    SmartSportHubApi.respond(exchange, 200,
                            JsonWriter.toJson(new BookingDto(booking).toMap()));
                }
                case "reschedule" -> {
                    var booking = bookingService.rescheduleBooking(id,
                            requireField(exchange, body, "newStartTime"),
                            requireField(exchange, body, "newEndTime"));
                    SmartSportHubApi.respond(exchange, 200,
                            JsonWriter.toJson(new BookingDto(booking).toMap()));
                }
                default -> SmartSportHubApi.respondError(exchange, 404,
                        "Hành động không hợp lệ: " + action);
            }
        }));

        // /api/invoices/{id}/pay | /refund
        server.createContext("/api/invoices/", SmartSportHubApi.wrap(exchange -> {
            String path = exchange.getRequestURI().getPath();
            String id = path.substring("/api/invoices/".length());
            if (id.contains("/")) {
                id = id.substring(0, id.indexOf('/'));
            }
            String action = path.substring(path.lastIndexOf('/') + 1);

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())
                    && !"PATCH".equalsIgnoreCase(exchange.getRequestMethod())) {
                SmartSportHubApi.respondError(exchange, 405, "Chỉ nhận POST hoặc PATCH.");
                return;
            }

            switch (action) {
                case "pay" -> {
                    var invoice = bookingService.payInvoice(id);
                    SmartSportHubApi.respond(exchange, 200,
                            JsonWriter.toJson(new InvoiceDto(invoice).toMap()));
                }
                case "refund" -> {
                    var invoice = bookingService.refundInvoice(id);
                    SmartSportHubApi.respond(exchange, 200,
                            JsonWriter.toJson(new InvoiceDto(invoice).toMap()));
                }
                default -> SmartSportHubApi.respondError(exchange, 404,
                        "Hành động không hợp lệ: " + action);
            }
        }));

        // /api/fields/{id}/status
        server.createContext("/api/fields/", SmartSportHubApi.wrap(exchange -> {
            String path = exchange.getRequestURI().getPath();
            String id = path.substring("/api/fields/".length());
            if (id.contains("/")) {
                id = id.substring(0, id.indexOf('/'));
            }
            String action = path.substring(path.lastIndexOf('/') + 1);

            if (!"PATCH".equalsIgnoreCase(exchange.getRequestMethod())
                    && !"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                SmartSportHubApi.respondError(exchange, 405, "Chỉ nhận POST hoặc PATCH.");
                return;
            }
            if (!"status".equals(action)) {
                SmartSportHubApi.respondError(exchange, 404, "Hành động không hợp lệ: " + action);
                return;
            }

            var field = bookingService.setFieldStatus(id,
                    requireField(exchange, readJsonObject(exchange), "status"));
            SmartSportHubApi.respond(exchange, 200, JsonWriter.toJson(new smartsporthub.dto.FieldDto(field).toMap()));
        }));

        // Trang chủ để kiểm tra sống: tổng quan nhanh.
        server.createContext("/api/overview", SmartSportHubApi.wrap(exchange -> {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("data", data.summary());
            body.put("totalRevenue", data.getTotalRevenue());
            SmartSportHubApi.respond(exchange, 200, JsonWriter.toJson(body));
        }));
    }

    /**
     * Đọc body JSON thành map phẳng giá trị chuỗi.
     * <p>
     * Không dùng thư viện JSON vì body chỉ có vài trường đơn giản; một regex nhỏ giữ
     * phần JSON contract ở đúng một chỗ và tránh thêm phụ thuộc.
     */
    static Map<String, String> readJsonObject(HttpExchange exchange) throws IOException {
        String raw = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
        Map<String, String> result = new LinkedHashMap<>();
        if (raw.isEmpty()) {
            return result;
        }
        var matcher = java.util.regex.Pattern.compile("\"([A-Za-z0-9_]+)\"\\s*:\\s*\"([^\"]*)\"")
                .matcher(raw);
        while (matcher.find()) {
            result.put(matcher.group(1), matcher.group(2));
        }
        return result;
    }

    /**
     * Lấy trường bắt buộc.
     * <p>
     * Thiếu thì trả 400 rồi ném {@link StopRequest} để dừng xử lý: tiếp tục chạy sẽ
     * đọc null rồi ném lỗi khác, khiến client nhận 500 thay vì 400.
     */
    static String requireField(HttpExchange exchange, Map<String, String> body, String key)
            throws IOException {
        String value = body.get(key);
        if (value == null || value.isBlank()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Thiếu trường bắt buộc: " + key);
            SmartSportHubApi.respond(exchange, 400, JsonWriter.toJson(error));
            throw new StopRequest();
        }
        return value;
    }

    /** Đã gửi phản hồi rồi thì dừng, không cần báo lỗi 500 nữa. */
    static class StopRequest extends RuntimeException {
        private static final long serialVersionUID = 1L;

        StopRequest() {
            super(null, null, false, false);
        }
    }
}