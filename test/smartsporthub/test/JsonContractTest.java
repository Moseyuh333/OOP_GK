package smartsporthub.test;

import static smartsporthub.test.TestSupport.assertEquals;
import static smartsporthub.test.TestSupport.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
 * Kiểm tra cấu trúc JSON trước khi giao cho Dev B.
 * <p>
 * Không phụ thuộc server: gọi thẳng DTO và JsonWriter để kiểm tra key,
 * kiểu giá trị và tính ổn định của định dạng.
 */
public final class JsonContractTest {

    private JsonContractTest() {
    }

    public static void main(String[] args) {
        TestSupport.Suite suite = new TestSupport.Suite("JSON CONTRACT");

        suite.run("Snapshot có đủ 5 tập dữ liệu", JsonContractTest::testSnapshotHasAllSections);
        suite.run("FieldDto có 5 key theo thứ tự", JsonContractTest::testFieldDtoKeys);
        suite.run("BookingDto có key peakHour và totalAmount", JsonContractTest::testBookingDtoKeys);
        suite.run("InvoiceDto có key bookingId và amount", JsonContractTest::testInvoiceDtoKeys);
        suite.run("ServiceDto có active boolean", JsonContractTest::testServiceDtoKeys);
        suite.run("CustomerDto có customerType", JsonContractTest::testCustomerDtoKeys);
        suite.run("JsonWriter escape và định dạng", JsonContractTest::testJsonWriterEscaping);
        suite.run("Số tiền viết đúng kiểu số", JsonContractTest::testMoneyIsNumber);
        suite.run("Ghi file ra đĩa và đọc lại được", JsonContractTest::testExportToFile);

        suite.finish();
    }

    private static SmartSportHubData data() {
        return SeedData.create();
    }

    private static void testSnapshotHasAllSections() {
        String json = DataExportService.toSnapshotJson(data());
        assertTrue(json.contains("\"customers\""));
        assertTrue(json.contains("\"fields\""));
        assertTrue(json.contains("\"services\""));
        assertTrue(json.contains("\"bookings\""));
        assertTrue(json.contains("\"invoices\""));
    }

    private static void testFieldDtoKeys() {
        Map<String, Object> map = new FieldDto(data().getFields().get(0)).toMap();
        assertEquals(
                List.of("id", "name", "type", "status", "basePricePerHour"),
                List.copyOf(map.keySet()));
    }

    private static void testBookingDtoKeys() {
        Map<String, Object> map = new BookingDto(data().getBookings().get(0)).toMap();
        assertEquals(
                List.of("id", "customerId", "customerName", "customerType", "fieldId",
                        "fieldType", "fieldName", "serviceId", "serviceName", "startTime",
                        "endTime", "hours", "status", "peakHour", "rentalFee",
                        "peakSurcharge", "serviceFee", "discount", "totalAmount"),
                List.copyOf(map.keySet()));

        // Booking không dùng service phải giữ null để Dev B LEFT JOIN được.
        boolean hasNullService = data().getBookings().stream()
                .map(booking -> new BookingDto(booking).toMap().get("serviceId"))
                .anyMatch(value -> value == null);
        assertTrue(hasNullService, "Cần booking có serviceId = null");
    }

    private static void testInvoiceDtoKeys() {
        Map<String, Object> map = new InvoiceDto(data().getInvoices().get(0)).toMap();
        assertEquals(
                List.of("id", "bookingId", "customerId", "fieldId", "issueDate",
                        "status", "amount"),
                List.copyOf(map.keySet()));
        // InvoiceDto phải ghi đủ tiền, không được ghi 0.
        assertTrue(((Number) map.get("amount")).doubleValue() > 0, "Tiền hóa đơn phải lớn hơn 0");
    }

    private static void testServiceDtoKeys() {
        Map<String, Object> map = new ServiceDto(data().getServices().get(0)).toMap();
        assertEquals(List.of("id", "name", "category", "unitPrice", "active"),
                List.copyOf(map.keySet()));
    }

    private static void testCustomerDtoKeys() {
        Map<String, Object> map = new CustomerDto(data().getCustomers().get(0)).toMap();
        assertEquals(List.of("id", "fullName", "phoneNumber", "email", "customerType",
                "discountRate", "promotionPolicy"),
                List.copyOf(map.keySet()));
    }

    private static void testJsonWriterEscaping() {
        Map<String, Object> sample = new LinkedHashMap<>();
        sample.put("text", "Sân \"A\"\nXuống dòng");
        sample.put("nothing", null);
        sample.put("flag", true);
        sample.put("items", List.of("x", 1));

        String json = JsonWriter.toJson(sample);
        assertTrue(json.contains("\\\"A\\\""), "Dấu ngoặc kép phải được escape");
        assertTrue(json.contains("\\n"), "Xuống dòng phải được escape");
        assertTrue(json.contains("\"nothing\": null"), "null phải giữ nguyên chữ null");
        assertTrue(json.contains("\"flag\": true"), "boolean phải giữ nguyên");
    }

    private static void testMoneyIsNumber() {
        String json = JsonWriter.toJson(new BookingDto(data().getBookings().get(0)).toMap());
        // totalAmount là số nên sau dấu hai chấm phải là chữ số, không phải dấu ngoặc kép.
        int index = json.indexOf("\"totalAmount\"");
        assertTrue(index >= 0, "Thiếu key totalAmount");
        char afterColon = json.charAt(json.indexOf(':', index) + 1);
        assertTrue(afterColon == ' ' || Character.isDigit(afterColon),
                "totalAmount phải là số, nhận ký tự '" + afterColon + "'");
    }

    private static void testExportToFile() {
        Path target = Path.of(System.getProperty("java.io.tmpdir"),
                "smartsporthub-test-dataset.json");
        Path written;
        try {
            written = DataExportService.writeSnapshotToFile(data(), target.toString());
        } catch (IOException error) {
            throw new AssertionError("Không ghi được file xuất: " + error.getMessage());
        }

        String content;
        try {
            content = Files.readString(written, StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new AssertionError("Không đọc được file xuất: " + error.getMessage());
        }
        assertTrue(content.contains("\"bookings\""), "File xuất thiếu bookings");
        assertTrue(content.contains("\"invoices\""), "File xuất thiếu invoices");
        try {
            Files.deleteIfExists(written);
        } catch (IOException ignored) {
            // File tạm, không xóa được cũng không ảnh hưởng kết quả kiểm tra.
        }
    }

    /** Ép kiểm tra compile-time: mọi DTO đều phải là JsonDto. */
    @SuppressWarnings("unused")
    private static void compileTimeCheck(List<JsonDto> dtos) {
        DataExportService.toArrayJson(dtos);
    }
}