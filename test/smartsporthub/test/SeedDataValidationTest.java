package smartsporthub.test;

import static smartsporthub.test.TestSupport.assertEquals;
import static smartsporthub.test.TestSupport.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import smartsporthub.data.SeedData;
import smartsporthub.data.SmartSportHubData;
import smartsporthub.dto.FieldType;
import smartsporthub.model.Booking;
import smartsporthub.model.BookingStatus;
import smartsporthub.model.Customer;
import smartsporthub.model.CustomerType;
import smartsporthub.model.FieldStatus;
import smartsporthub.model.Invoice;
import smartsporthub.model.InvoiceStatus;
import smartsporthub.model.Service;
import smartsporthub.model.SportField;

/**
 * Kiểm tra bộ dữ liệu mẫu có đủ phong phú để C# chạy được các truy vấn LINQ.
 * <p>
 * Mỗi ca kiểm tra ứng với một điều kiện bắt buộc trong đề bài mục 6 và 10.
 */
public final class SeedDataValidationTest {

    private SeedDataValidationTest() {
    }

    public static void main(String[] args) {
        TestSupport.Suite suite = new TestSupport.Suite("SEED DATA");

        suite.run("Số lượng entity tối thiểu", SeedDataValidationTest::testMinimumCounts);
        suite.run("Đủ 4 loại sân", SeedDataValidationTest::testAllFieldTypesPresent);
        suite.run("Có khách Standard và VIP", SeedDataValidationTest::testCustomerTypes);
        suite.run("Có field AVAILABLE và MAINTENANCE", SeedDataValidationTest::testFieldStatuses);
        suite.run("Đủ 3 trạng thái booking", SeedDataValidationTest::testBookingStatusDistribution);
        suite.run("Đủ 3 trạng thái hóa đơn", SeedDataValidationTest::testInvoiceStatusDistribution);
        suite.run("Có khách chưa từng đặt sân", SeedDataValidationTest::testCustomerWithoutBooking);
        suite.run("Có khách nhiều booking COMPLETED", SeedDataValidationTest::testCustomerWithManyCompleted);
        suite.run("Có khách dùng nhiều loại sân", SeedDataValidationTest::testCustomerWithManyFieldTypes);
        suite.run("Có sân chưa có booking COMPLETED", SeedDataValidationTest::testFieldWithoutCompleted);
        suite.run("Có sân nhiều booking COMPLETED", SeedDataValidationTest::testFieldWithManyCompleted);
        suite.run("Có booking peak và non-peak", SeedDataValidationTest::testPeakAndNonPeak);
        suite.run("Có service chưa từng dùng", SeedDataValidationTest::testUnusedService);
        suite.run("Có booking không dùng service", SeedDataValidationTest::testBookingWithoutService);
        suite.run("Có booking thuộc cả 4 loại sân", SeedDataValidationTest::testBookingsCoverAllFieldTypes);
        suite.run("ID không trùng lặp", SeedDataValidationTest::testUniqueIdentifiers);
        suite.run("Mọi booking đều có hóa đơn", SeedDataValidationTest::testInvoicePerBooking);
        suite.run("Doanh thu khớp tổng booking", SeedDataValidationTest::testRevenueConsistency);

        suite.finish();
    }

    private static SmartSportHubData data() {
        return SeedData.create();
    }

    private static void testMinimumCounts() {
        SmartSportHubData data = data();
        assertTrue(data.getCustomers().size() >= 5, "Cần >= 5 khách hàng");
        assertTrue(data.getFields().size() >= 5, "Cần >= 5 sân");
        assertTrue(data.getServices().size() >= 5, "Cần >= 5 dịch vụ");
        assertTrue(data.getBookings().size() >= 10, "Cần >= 10 booking");
        assertTrue(data.getInvoices().size() >= 10, "Cần >= 10 hóa đơn");
    }

    private static void testAllFieldTypesPresent() {
        Set<FieldType> types = new HashSet<>();
        for (SportField field : data().getFields()) {
            types.add(FieldType.of(field));
        }
        assertEquals(Set.of(FieldType.FOOTBALL, FieldType.BADMINTON, FieldType.TENNIS, FieldType.PICKLEBALL), types);
    }

    private static void testCustomerTypes() {
        SmartSportHubData data = data();
        assertEquals(3, data.getCustomerManager().findByType(CustomerType.STANDARD).size());
        assertEquals(3, data.getCustomerManager().findByType(CustomerType.VIP).size());
    }

    private static void testFieldStatuses() {
        List<FieldStatus> statuses = data().getFields().stream().map(SportField::getStatus).toList();
        assertTrue(statuses.contains(FieldStatus.AVAILABLE), "Cần sân AVAILABLE");
        assertTrue(statuses.contains(FieldStatus.MAINTENANCE), "Cần sân MAINTENANCE");
    }

    private static void testBookingStatusDistribution() {
        var manager = data().getBookingManager();
        assertEquals(7, manager.findByStatus(BookingStatus.COMPLETED).size());
        assertEquals(3, manager.findByStatus(BookingStatus.CONFIRMED).size());
        assertEquals(2, manager.findByStatus(BookingStatus.CANCELLED).size());
    }

    private static void testInvoiceStatusDistribution() {
        var manager = data().getInvoiceManager();
        assertTrue(manager.findByStatus(InvoiceStatus.PAID).size() > 0);
        assertTrue(manager.findByStatus(InvoiceStatus.UNPAID).size() > 0);
        assertTrue(manager.findByStatus(InvoiceStatus.REFUNDED).size() > 0);
    }

    private static void testCustomerWithoutBooking() {
        SmartSportHubData data = data();
        long withoutBooking = data.getCustomers().stream()
                .filter(customer -> data.getBookingManager().findByCustomer(customer.getCustomerId()).isEmpty())
                .count();
        assertTrue(withoutBooking > 0, "Cần ít nhất 1 khách chưa từng đặt sân");
    }

    private static void testCustomerWithManyCompleted() {
        SmartSportHubData data = data();
        long multiBooking = data.getCustomers().stream()
                .filter(customer -> data.getBookingManager().findByCustomer(customer.getCustomerId()).stream()
                        .filter(booking -> booking.getStatus() == BookingStatus.COMPLETED)
                        .count() >= 2)
                .count();
        assertTrue(multiBooking >= 2, "Cần ít nhất 2 khách có >= 2 booking COMPLETED");
    }

    private static void testCustomerWithManyFieldTypes() {
        SmartSportHubData data = data();
        long manyTypes = data.getCustomers().stream()
                .filter(customer -> data.getBookingManager().findByCustomer(customer.getCustomerId()).stream()
                        .map(booking -> FieldType.of(booking.getField()))
                        .distinct()
                        .count() >= 2)
                .count();
        assertTrue(manyTypes >= 3, "Cần khách đặt sân nhiều loại sân khác nhau");
    }

    private static void testFieldWithoutCompleted() {
        SmartSportHubData data = data();
        long withoutCompleted = data.getFields().stream()
                .filter(field -> data.getBookingManager().findByField(field.getFieldId()).stream()
                        .noneMatch(booking -> booking.getStatus() == BookingStatus.COMPLETED))
                .count();
        assertTrue(withoutCompleted >= 2, "Cần ít nhất 2 sân chưa có booking COMPLETED (LEFT JOIN có kết quả null)");
    }

    private static void testFieldWithManyCompleted() {
        SmartSportHubData data = data();
        long manyCompleted = data.getFields().stream()
                .filter(field -> data.getBookingManager().findByField(field.getFieldId()).stream()
                        .filter(booking -> booking.getStatus() == BookingStatus.COMPLETED)
                        .count() >= 2)
                .count();
        assertTrue(manyCompleted >= 2, "Cần ít nhất 2 sân có nhiều booking COMPLETED");
    }

    private static void testPeakAndNonPeak() {
        var bookings = data().getBookings();
        long peak = bookings.stream().filter(Booking::isPeakHourBooking).count();
        assertTrue(peak >= 3, "Cần booking trong khung 17:00-20:00");
        assertTrue(peak < bookings.size(), "Cần booking ngoài khung cao điểm");
    }

    private static void testUnusedService() {
        SmartSportHubData data = data();
        long unused = data.getServices().stream()
                .filter(service -> data.getBookingManager().findByService(service.getServiceId()).isEmpty())
                .count();
        assertTrue(unused >= 1, "Cần ít nhất 1 dịch vụ chưa từng dùng");
    }

    private static void testBookingWithoutService() {
        long withoutService = data().getBookings().stream()
                .filter(booking -> booking.getService() == null)
                .count();
        assertTrue(withoutService >= 2, "Cần booking không dùng dịch vụ (serviceId = null)");
    }

    private static void testBookingsCoverAllFieldTypes() {
        Set<FieldType> types = data().getBookings().stream()
                .map(booking -> FieldType.of(booking.getField()))
                .collect(java.util.stream.Collectors.toSet());
        assertEquals(Set.of(FieldType.FOOTBALL, FieldType.BADMINTON, FieldType.TENNIS, FieldType.PICKLEBALL), types);
    }

    private static void testUniqueIdentifiers() {
        SmartSportHubData data = data();
        assertEquals(6, distinctCount(data.getCustomers().stream().map(Customer::getCustomerId).toList()));
        assertEquals(6, distinctCount(data.getFields().stream().map(SportField::getFieldId).toList()));
        assertEquals(6, distinctCount(data.getServices().stream().map(Service::getServiceId).toList()));
        assertEquals(12, distinctCount(data.getBookings().stream().map(Booking::getBookingId).toList()));
        assertEquals(12, distinctCount(data.getInvoices().stream().map(Invoice::getInvoiceId).toList()));
    }

    private static void testInvoicePerBooking() {
        SmartSportHubData data = data();
        Set<String> bookingIds = new HashSet<>();
        for (Invoice invoice : data.getInvoices()) {
            bookingIds.add(invoice.getBookingId());
        }
        assertEquals(data.getBookings().size(), bookingIds.size(), "Mỗi booking cần có hóa đơn");
    }

    private static void testRevenueConsistency() {
        SmartSportHubData data = data();
        double expected = 0;
        for (Booking booking : data.getBookings()) {
            if (booking.getStatus() != BookingStatus.CANCELLED) {
                expected += booking.getTotalAmount();
            }
        }
        assertEquals(expected, data.getTotalRevenue());
    }

    private static int distinctCount(List<String> values) {
        return (int) values.stream().distinct().count();
    }
}