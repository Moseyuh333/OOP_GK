package smartsporthub.api;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import smartsporthub.data.SeedData;
import smartsporthub.data.SmartSportHubData;
import smartsporthub.dto.BookingDto;
import smartsporthub.dto.CustomerDto;
import smartsporthub.dto.FieldDto;
import smartsporthub.dto.InvoiceDto;
import smartsporthub.dto.JsonDto;
import smartsporthub.dto.ServiceDto;
import smartsporthub.json.JsonWriter;
import smartsporthub.service.DataExportService;

/**
 * API JSON phục vụ Developer B (C# ASP.NET Core).
 * <p>
 * Dùng {@code com.sun.net.httpserver} đi kèm JDK nên không cần thêm thư viện.
 * Contract được mô tả đầy đủ trong {@code API_CONTRACT.md}.
 */
public final class SmartSportHubApi {

    public static final int DEFAULT_PORT = 8080;
    private static final String JSON_TYPE = "application/json; charset=utf-8";

    private SmartSportHubApi() {
        // Utility class
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        SmartSportHubData data = SeedData.create();
        HttpServer server = createServer(port, data);
        server.start();
        System.out.println("SmartSportHub API đang chạy tại http://localhost:" + port + "/api/data");
        System.out.println("Dữ liệu: " + data.summary());
    }

    /** Tạo server đã cấu hình đầy đủ, dùng được cả cho test. */
    public static HttpServer createServer(int port, SmartSportHubData data) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(4));

        server.createContext("/api/health", wrap(exchange -> {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", "ok");
            body.put("service", "SmartSportHub");
            body.put("data", data.summary());
            body.put("totalRevenue", data.getTotalRevenue());
            respond(exchange, 200, JsonWriter.toJson(body));
        }));

        registerCollection(server, "/api/customers",
                () -> data.getCustomers().stream().map(CustomerDto::new).toList());
        registerCollection(server, "/api/fields",
                () -> data.getFields().stream().map(FieldDto::new).toList());
        registerCollection(server, "/api/services",
                () -> data.getServices().stream().map(ServiceDto::new).toList());
        registerCollection(server, "/api/bookings",
                () -> data.getBookings().stream().map(BookingDto::new).toList());
        registerCollection(server, "/api/invoices",
                () -> data.getInvoices().stream().map(InvoiceDto::new).toList());

        server.createContext("/api/data", wrap(exchange ->
                respond(exchange, 200, DataExportService.toSnapshotJson(data))));

        server.createContext("/api/export", wrap(exchange -> {
            String path = DataExportService.writeSnapshotToFile(data, DataExportService.DEFAULT_EXPORT_PATH)
                    .toString();
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("message", "Đã xuất dữ liệu ra file.");
            body.put("file", path);
            respond(exchange, 200, JsonWriter.toJson(body));
        }));

        server.createContext("/", wrap(exchange ->
                respondError(exchange, 404, "Không tìm thấy endpoint: " + exchange.getRequestURI().getPath())));

        return server;
    }

    /** Đăng ký một endpoint danh sách (đọc live từ kho dữ liệu mỗi request). */
    private static void registerCollection(HttpServer server, String path,
            Supplier<List<? extends JsonDto>> supplier) {
        server.createContext(path, wrap(exchange ->
                respond(exchange, 200, arrayJson(exchange, supplier.get()))));
    }

    /** Áp dụng phân trang {@code ?page=&pageSize=} rồi trả về mảng JSON. */
    private static String arrayJson(HttpExchange exchange, List<? extends JsonDto> source) {
        int page = readPositiveInt(exchange, "page", 1);
        int pageSize = readPositiveInt(exchange, "pageSize", source.size());
        int from = Math.min((page - 1) * pageSize, source.size());
        int to = Math.min(from + pageSize, source.size());
        List<? extends JsonDto> slice = new ArrayList<>(source.subList(from, to));
        return DataExportService.toArrayJson(slice);
    }

    private static int readPositiveInt(HttpExchange exchange, String name, int fallback) {
        String rawQuery = exchange.getRequestURI().getRawQuery();
        if (rawQuery == null || rawQuery.isBlank()) {
            return fallback;
        }
        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && parts[0].equals(name)) {
                try {
                    int value = Integer.parseInt(parts[1]);
                    return value > 0 ? value : fallback;
                } catch (NumberFormatException ignored) {
                    return fallback;
                }
            }
        }
        return fallback;
    }

    private static HttpHandler wrap(ApiBody handler) {
        return exchange -> {
            try {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    respondError(exchange, 405, "Chỉ hỗ trợ phương thức GET.");
                    return;
                }
                handler.handle(exchange);
            } catch (Exception exception) {
                // Mã -1 nghĩa là header phản hồi chưa gửi, vẫn còn cơ hội báo lỗi 500.
                if (exchange.getResponseCode() == -1) {
                    try {
                        respondError(exchange, 500, "Lỗi máy chủ: " + exception.getMessage());
                    } catch (IOException ignored) {
                        // Kết nối đã hỏng, không còn gì để trả về.
                    }
                }
            } finally {
                exchange.close();
            }
        };
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", JSON_TYPE);
        // Cho phép C# hoặc trình duyệt gọi trực tiếp trong lúc tích hợp.
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, payload.length);
        try (OutputStream stream = exchange.getResponseBody()) {
            stream.write(payload);
        }
    }

    private static void respondError(HttpExchange exchange, int status, String message) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        respond(exchange, status, JsonWriter.toJson(body));
    }

    @FunctionalInterface
    private interface ApiBody {
        void handle(HttpExchange exchange) throws IOException;
    }
}