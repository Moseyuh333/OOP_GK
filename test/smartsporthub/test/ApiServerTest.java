package smartsporthub.test;

import static smartsporthub.test.TestSupport.assertEquals;
import static smartsporthub.test.TestSupport.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpServer;

import smartsporthub.api.SmartSportHubApi;
import smartsporthub.data.SeedData;
import smartsporthub.data.SmartSportHubData;

/**
 * Kiểm thử API bằng cách chạy server thật trên cổng ngẫu nhiên.
 * <p>
 * Mỗi ca kiểm tra ứng với một endpoint mà Dev B sẽ gọi, nên nếu test này
 * xanh thì Dev B có thể dùng đúng URL trong test để tích hợp.
 */
public final class ApiServerTest {

    private ApiServerTest() {
    }

    public static void main(String[] args) throws IOException {
        SmartSportHubData data = SeedData.create();
        HttpServer server = SmartSportHubApi.createServer(0, data);
        server.start();
        int port = server.getAddress().getPort();
        String baseUrl = "http://localhost:" + port;

        TestSupport.Suite suite = new TestSupport.Suite("API SERVER");

        suite.run("Endpoint /api/health trả về ok", () -> {
            HttpResponse health = get(baseUrl + "/api/health");
            assertEquals(200, health.status());
            assertTrue(health.body().contains("\"status\": \"ok\""), "Thiếu status ok");
            assertTrue(health.body().contains("customers=6"), "Thiếu thống kê dữ liệu");
        });

        suite.run("Endpoint /api/customers trả về 6 khách", () -> {
            HttpResponse response = get(baseUrl + "/api/customers");
            assertEquals(200, response.status());
            assertTrue(response.body().contains("\"customerType\": \"VIP\""), "Thiếu khách VIP");
            assertTrue(response.body().contains("application/json"), "Sai Content-Type");
        });

        suite.run("Endpoint /api/fields trả về 6 sân", () -> {
            HttpResponse response = get(baseUrl + "/api/fields");
            assertEquals(200, response.status());
            assertTrue(response.body().contains("\"type\": \"FOOTBALL\""), "Thiếu sân FOOTBALL");
        });

        suite.run("Endpoint /api/bookings trả về 12 booking", () -> {
            HttpResponse response = get(baseUrl + "/api/bookings");
            assertEquals(200, response.status());
            assertEquals(12, countOccurrences(response.body(), "\"totalAmount\""));
        });

        suite.run("Endpoint /api/services trả về 6 dịch vụ", () -> {
            HttpResponse response = get(baseUrl + "/api/services");
            assertEquals(200, response.status());
            assertEquals(6, countOccurrences(response.body(), "\"unitPrice\""));
        });

        suite.run("Endpoint /api/invoices trả về 12 hóa đơn", () -> {
            HttpResponse response = get(baseUrl + "/api/invoices");
            assertEquals(200, response.status());
            assertEquals(12, countOccurrences(response.body(), "\"amount\""));
        });

        suite.run("Endpoint /api/data trả về snapshot 5 tập", () -> {
            HttpResponse response = get(baseUrl + "/api/data");
            assertEquals(200, response.status());
            assertTrue(response.body().contains("\"customers\""), "Thiếu customers");
            assertTrue(response.body().contains("\"invoices\""), "Thiếu invoices");
        });

        suite.run("Phân trang bookings?page=2&pageSize=5", () -> {
            HttpResponse response = get(baseUrl + "/api/bookings?page=2&pageSize=5");
            assertEquals(200, response.status());
            assertEquals(5, countOccurrences(response.body(), "\"totalAmount\""));
        });

        suite.run("Phân trang trang cuối chỉ còn 2 booking", () -> {
            HttpResponse response = get(baseUrl + "/api/bookings?page=3&pageSize=5");
            assertEquals(200, response.status());
            assertEquals(2, countOccurrences(response.body(), "\"totalAmount\""));
        });

        suite.run("Endpoint lạ trả về 404 JSON", () -> {
            HttpResponse response = get(baseUrl + "/api/khong-ton-tai");
            assertEquals(404, response.status());
            assertTrue(response.body().contains("\"error\""), "Lỗi 404 phải có key error");
        });

        suite.run("POST /api/data bị từ chối 405", () -> {
            HttpResponse response = post(baseUrl + "/api/data");
            assertEquals(405, response.status());
        });

        suite.finish();
        server.stop(0);
    }

    private static int countOccurrences(String text, String needle) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }

    private record HttpResponse(int status, String body, String contentType) {
    }

    private static HttpResponse get(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5_000);
            connection.setReadTimeout(5_000);
            int status = connection.getResponseCode();
            String contentType = connection.getHeaderField("Content-Type");
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String body = stream == null ? ""
                    : new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            // Nhét Content-Type vào body để test kiểm tra một lần.
            return new HttpResponse(status, body + " " + contentType, contentType);
        } catch (IOException error) {
            throw new AssertionError("Không gọi được " + url + ": " + error.getMessage());
        }
    }

    private static HttpResponse post(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5_000);
            connection.setReadTimeout(5_000);
            int status = connection.getResponseCode();
            String body = "";
            if (connection.getErrorStream() != null) {
                body = new String(connection.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            return new HttpResponse(status, body, null);
        } catch (IOException error) {
            throw new AssertionError("Không gọi được " + url + ": " + error.getMessage());
        }
    }
}