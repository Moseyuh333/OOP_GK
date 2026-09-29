package smartsporthub.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import smartsporthub.interfaces.IPromotional;
import smartsporthub.manager.BookingManager;
import smartsporthub.manager.FieldManager;
import smartsporthub.manager.ReportManager;
import smartsporthub.model.BadmintonField;
import smartsporthub.model.Booking;
import smartsporthub.model.BookingStatus;
import smartsporthub.model.Customer;
import smartsporthub.model.FieldStatus;
import smartsporthub.model.FootballField;
import smartsporthub.model.Invoice;
import smartsporthub.model.PickleballField;
import smartsporthub.model.Service;
import smartsporthub.model.SportField;
import smartsporthub.model.StandardCustomer;
import smartsporthub.model.TennisField;
import smartsporthub.model.VipCustomer;

/** Kiểm thử console độc lập cho phần B, không dùng thư viện ngoài. */
public final class BookingBillingTest {

    private static int passed;
    private static int total;

    private BookingBillingTest() {
    }

    public static void main(String[] args) {
        run("01 StandardCustomer", BookingBillingTest::testStandardCustomer);
        run("02 VipCustomer", BookingBillingTest::testVipCustomer);
        run("03 Giảm giá đa hình qua IPromotional", BookingBillingTest::testDiscount);
        run("04 Đặt sân AVAILABLE", BookingBillingTest::testCreateBooking);
        run("05 Từ chối sân MAINTENANCE", BookingBillingTest::testMaintenance);
        run("06 Chặn lịch trùng, cho phép lịch nối tiếp", BookingBillingTest::testConflict);
        run("07 Dịch vụ có số lượng và đơn giá lưu riêng", BookingBillingTest::testServices);
        run("08 Phụ phí theo giờ bắt đầu và biên 17:00/20:00", BookingBillingTest::testPeakHour);
        run("09 In hóa đơn đủ thành phần từ dữ liệu thật", BookingBillingTest::testInvoice);
        run("10 Hủy trước giờ và giải phóng lịch", BookingBillingTest::testCancelBeforeStart);
        run("11 Từ chối hủy từ giờ bắt đầu", BookingBillingTest::testCancelAfterStart);
        run("12 Đổi lịch kiểm tra conflict, giữ dữ liệu khi thất bại", BookingBillingTest::testChangeConflict);
        run("13 Doanh thu chỉ tính COMPLETED", BookingBillingTest::testRevenue);
        run("14 Top 3 loại sân và quy tắc hòa", BookingBillingTest::testTop3);
        run("15 Tìm sân AVAILABLE không trùng lịch", BookingBillingTest::testAvailableFields);
        run("Validation khách hàng và dịch vụ", BookingBillingTest::testCustomerServiceValidation);
        run("Validation booking và quantity", BookingBillingTest::testBookingValidation);
        run("Giờ lẻ và lịch qua ngày", BookingBillingTest::testFractionalHours);
        run("Tích hợp công thức bốn loại sân và đèn", BookingBillingTest::testFieldFees);
        run("ID trùng, sân lạ và OCCUPIED", BookingBillingTest::testIdentityAndOccupied);
        run("Đổi sân và loại trừ booking đang đổi", BookingBillingTest::testChangeField);
        run("Không đổi booking đã bắt đầu hoặc lịch mới quá khứ", BookingBillingTest::testChangeTimeBoundary);
        run("Trạng thái kết thúc không thể thanh toán/hủy lần nữa", BookingBillingTest::testTerminalStates);
        run("Hóa đơn đã thanh toán không đổi theo bảng giá", BookingBillingTest::testInvoiceSnapshot);
        run("Không thể sửa lịch quản lý qua getter", BookingBillingTest::testEncapsulation);
        run("Giá trị hóa đơn tràn hoặc giảm giá sai bị chặn", BookingBillingTest::testInvalidInvoice);
        run("Báo cáo rỗng và tham số không hợp lệ", BookingBillingTest::testEmptyAndInvalidQueries);
        run("DisplayBooking hoạt động với booking đã hủy", BookingBillingTest::testDisplayCancelled);

        System.out.printf("%n===== KẾT QUẢ BOOKING/BILLING: %d/%d PASS =====%n", passed, total);
        if (passed != total) {
            throw new AssertionError("Có kiểm thử phần B không đạt.");
        }
    }

    private static void testStandardCustomer() {
        Customer customer = standard();
        equal("C001", customer.getCustomerId());
        equal("Khách thường", customer.getFullName());
        equal("0901234567", customer.getPhoneNumber());
        customer.setFullName("  An  ");
        customer.setPhoneNumber("+84901234567");
        equal("An", customer.getFullName());
        equal("+84901234567", customer.getPhoneNumber());
        money(0, customer.calculateDiscount(100_000));
    }

    private static void testVipCustomer() {
        VipCustomer customer = vip();
        equal("V001", customer.getCustomerId());
        money(0.1, customer.getDiscountRate());
        customer.setDiscountRate(0.2);
        money(20_000, customer.calculateDiscount(100_000));
    }

    private static void testDiscount() {
        IPromotional[] customers = { standard(), vip() };
        money(0, customers[0].calculateDiscount(200_000));
        money(20_000, customers[1].calculateDiscount(200_000));
        Customer special = new Customer("X", "Ưu đãi riêng", "0901234567") {
            @Override
            public double calculateDiscount(double amount) {
                return amount * 0.25;
            }
        };
        Booking booking = new Booking("X", special, football(), time(10), time(12));
        money(150_000, booking.getInvoice().getTotal());
    }

    private static void testCreateBooking() {
        Fixture f = new Fixture();
        Booking draft = f.booking("B1", time(10), time(12));
        check(f.bookings.createBooking(draft));
        equal(BookingStatus.PENDING, draft.getStatus());
        equal(BookingStatus.CONFIRMED, f.bookings.findBookingById(" B1 ").getStatus());
        equal(FieldStatus.AVAILABLE, f.field.getStatus());
        money(200_000, f.bookings.calculateBookingTotal("B1"));
    }

    private static void testMaintenance() {
        Fixture f = new Fixture();
        f.field.setStatus(FieldStatus.MAINTENANCE);
        check(!f.bookings.createBooking(f.booking("B1", time(10), time(12))));
        check(f.bookings.getLastMessage().contains("F001 đang bảo trì"));
        equal(0, f.bookings.getAllBookings().size());
    }

    private static void testConflict() {
        Fixture f = new Fixture();
        check(f.bookings.createBooking(f.booking("B1", time(18), time(20))));
        check(!f.bookings.createBooking(f.booking("B2", time(19), time(21))));
        check(!f.bookings.createBooking(f.booking("B3", time(17), time(21))));
        check(!f.bookings.createBooking(f.booking("B4", time(18), time(19))));
        check(!f.bookings.createBooking(f.booking("B5", time(18), time(20))));
        check(f.bookings.createBooking(f.booking("B6", time(20), time(22))));
        check(f.bookings.createBooking(f.booking("B7", time(16), time(18))));
        check(f.bookings.createBooking(f.booking("B8", time(18).plusDays(1), time(20).plusDays(1))));
    }

    private static void testServices() {
        Service water = new Service("W", "Nước", 10_000);
        Booking.ServiceItem item = new Booking.ServiceItem(water, 3);
        water.setPrice(50_000);
        water.setServiceName("Tên mới");
        Booking booking = new Booking("B", standard(), football(), time(10), time(12),
                Arrays.asList(item, new Booking.ServiceItem(new Service("T", "Khăn", 5_000), 2)), false);
        equal("W", item.getServiceId());
        equal("Nước", item.getServiceName());
        equal(3, item.getQuantity());
        money(10_000, item.getUnitPrice());
        money(40_000, booking.getInvoice().getServiceFee());
        money(240_000, booking.getInvoice().getTotal());
    }

    private static void testPeakHour() {
        SportField field = football();
        money(40_000, new Booking("B1", standard(), field, time(17), time(19))
                .getInvoice().getPeakHourSurcharge());
        money(0, new Booking("B2", standard(), field, time(20), time(22))
                .getInvoice().getPeakHourSurcharge());
        money(0, new Booking("B3", standard(), field, time(16), time(18))
                .getInvoice().getPeakHourSurcharge());
        money(40_000, new Booking("B4", standard(), field, time(19), time(21))
                .getInvoice().getPeakHourSurcharge());
    }

    private static void testInvoice() {
        Booking booking = new Booking("B-VIP", vip(), football(), time(18), time(20),
                Arrays.asList(new Booking.ServiceItem(new Service("W", "Nước", 10_000), 2)), true);
        Invoice invoice = booking.getInvoice();
        money(250_000, invoice.getFieldFee());
        money(40_000, invoice.getPeakHourSurcharge());
        money(20_000, invoice.getServiceFee());
        money(310_000, invoice.getSubtotal());
        money(31_000, invoice.getDiscount());
        money(279_000, invoice.getTotal());
        equal("B-VIP", invoice.getBookingId());
        String output = capture(invoice::printInvoice);
        for (String value : Arrays.asList("SMARTSPORTHUB", "B-VIP", "Khách VIP", "VipCustomer",
                "F001 - Sân bóng", time(18).toString(), time(20).toString(), "Tiền sân: 250,000.00",
                "Phụ phí cao điểm: 40,000.00", "Dịch vụ: 20,000.00", "Nước x 2", "Tạm tính: 310,000.00",
                "Giảm giá: 31,000.00", "TỔNG THANH TOÁN: 279,000.00")) {
            check(output.contains(value));
        }
    }

    private static void testCancelBeforeStart() {
        Fixture f = new Fixture();
        check(f.bookings.createBooking(f.booking("B1", time(18), time(20))));
        check(f.bookings.cancelBooking("B1", time(18).minusNanos(1)));
        equal(BookingStatus.CANCELLED, f.bookings.findBookingById("B1").getStatus());
        check(f.bookings.createBooking(f.booking("B2", time(18), time(20))));
    }

    private static void testCancelAfterStart() {
        Fixture f = new Fixture();
        check(f.bookings.createBooking(f.booking("B1", time(18), time(20))));
        check(!f.bookings.cancelBooking("B1", time(18)));
        check(!f.bookings.cancelBooking("B1", time(18).plusMinutes(30)));
        equal(BookingStatus.CONFIRMED, f.bookings.findBookingById("B1").getStatus());
    }

    private static void testChangeConflict() {
        Fixture f = new Fixture();
        check(f.bookings.createBooking(f.booking("B1", time(10), time(12))));
        check(f.bookings.createBooking(f.booking("B2", time(14), time(16))));
        Booking before = f.bookings.findBookingById("B1");
        check(!f.bookings.changeBooking("B1", "F001", time(15), time(17), time(8)));
        check(before == f.bookings.findBookingById("B1"));
        check(f.bookings.changeBooking("B1", "F001", time(12), time(14), time(8)));
        equal(time(12), f.bookings.findBookingById("B1").getStartTime());
        check(f.bookings.isTimeSlotAvailable("F001", time(10), time(12)));
    }

    private static void testRevenue() {
        Fixture f = new Fixture();
        check(f.bookings.createBooking(f.booking("DONE", time(10), time(12))));
        check(f.bookings.createBooking(f.booking("CANCEL", time(12), time(14))));
        check(f.bookings.createBooking(f.booking("CONFIRM", time(14), time(16))));
        // Bản nháp PENDING chưa được đăng ký không được tính doanh thu.
        Booking pending = f.booking("PENDING", time(16), time(17));
        equal(BookingStatus.PENDING, pending.getStatus());
        money(0, f.reports.calculateTotalRevenue());
        check(f.bookings.cancelBooking("CANCEL", time(8)));
        check(f.bookings.completeBooking("DONE", time(12)));
        money(200_000, f.reports.calculateTotalRevenue());
    }

    private static void testTop3() {
        Fixture f = new Fixture();
        SportField[] fields = {f.field,
                new BadmintonField("BD", "Cầu lông", 80_000, FieldStatus.AVAILABLE),
                new TennisField("T", "Tennis", 150_000, FieldStatus.AVAILABLE),
                new PickleballField("P", "Pickleball", 120_000, FieldStatus.AVAILABLE)};
        int[] counts = {4, 3, 2, 1};
        for (int type = 0; type < fields.length; type++) {
            if (type > 0) {
                check(f.fields.addField(fields[type]));
            }
            for (int index = 0; index < counts[type]; index++) {
                check(f.bookings.createBooking(new Booking("B" + type + index, standard(), fields[type],
                        time(10).plusDays(index), time(12).plusDays(index))));
            }
        }
        check(f.bookings.cancelBooking("B00", time(8)));
        check(f.bookings.completeBooking("B10", time(12)));
        Map<String, Integer> top = f.reports.getTop3FieldTypes();
        equal(Arrays.asList("Badminton", "Football", "Tennis"), new ArrayList<>(top.keySet()));
        equal(Arrays.asList(3, 3, 2), new ArrayList<>(top.values()));
    }

    private static void testAvailableFields() {
        Fixture f = new Fixture();
        SportField free = new TennisField("T", "Tennis", 150_000, FieldStatus.AVAILABLE);
        check(f.fields.addField(free));
        check(f.fields.addField(new BadmintonField("BD", "Bảo trì", 80_000, FieldStatus.MAINTENANCE)));
        check(f.fields.addField(new PickleballField("P", "Đang bận", 120_000, FieldStatus.OCCUPIED)));
        check(f.bookings.createBooking(f.booking("B1", time(18), time(20))));
        equal(Arrays.asList(free), f.reports.findAvailableFields(time(19), time(21)));
        equal(2, f.reports.findAvailableFields(time(20), time(22)).size());
    }

    private static void testCustomerServiceValidation() {
        throwsType(IllegalArgumentException.class, () -> new StandardCustomer(" ", "A", "0901234567"));
        throwsType(IllegalArgumentException.class, () -> new StandardCustomer("C", null, "0901234567"));
        throwsType(IllegalArgumentException.class, () -> new StandardCustomer("C", "A", "phone"));
        throwsType(IllegalArgumentException.class, () -> new StandardCustomer("C", "A", "12345678"));
        throwsType(IllegalArgumentException.class, () -> new StandardCustomer("C", "A", null));
        VipCustomer vip = vip();
        for (double invalid : new double[] {-1, 1.01, Double.NaN, Double.POSITIVE_INFINITY}) {
            throwsType(IllegalArgumentException.class, () -> vip.setDiscountRate(invalid));
        }
        money(0.1, vip.getDiscountRate());
        throwsType(IllegalArgumentException.class, () -> vip.calculateDiscount(-1));
        throwsType(IllegalArgumentException.class, () -> standard().calculateDiscount(Double.NaN));
        throwsType(IllegalArgumentException.class, () -> new Service(null, "Nước", 1));
        throwsType(IllegalArgumentException.class, () -> new Service("S", " ", 1));
        Service water = new Service("S", "Nước", 0);
        for (double invalid : new double[] {-1, Double.NaN, Double.POSITIVE_INFINITY}) {
            throwsType(IllegalArgumentException.class, () -> water.setPrice(invalid));
        }
        money(0, water.getPrice());
    }

    private static void testBookingValidation() {
        Fixture f = new Fixture();
        throwsType(IllegalArgumentException.class, () -> f.booking(" ", time(10), time(12)));
        throwsType(IllegalArgumentException.class, () -> f.booking("B", time(12), time(12)));
        throwsType(IllegalArgumentException.class, () -> f.booking("B", time(12), time(10)));
        throwsType(IllegalArgumentException.class, () -> f.booking("B", null, time(12)));
        throwsType(IllegalArgumentException.class, () -> new Booking("B", null, f.field, time(10), time(12)));
        throwsType(IllegalArgumentException.class, () -> new Booking("B", standard(), null, time(10), time(12)));
        throwsType(IllegalArgumentException.class, () -> new Booking("B", standard(), f.field,
                time(10), time(12), null, false));
        throwsType(IllegalArgumentException.class, () -> new Booking("B", standard(), f.field,
                time(10), time(12), Arrays.asList((Booking.ServiceItem) null), false));
        Service water = new Service("W", "Nước", 10_000);
        throwsType(IllegalArgumentException.class, () -> new Booking.ServiceItem(water, 0));
        throwsType(IllegalArgumentException.class, () -> new Booking.ServiceItem(water, -1));
        throwsType(IllegalArgumentException.class, () -> new Booking.ServiceItem(null, 1));
        throwsType(IllegalArgumentException.class, () -> new Booking("B", standard(),
                new TennisField("T", "Tennis", 1, FieldStatus.AVAILABLE), time(10), time(12),
                new ArrayList<>(), true));
    }

    private static void testFractionalHours() {
        Booking fractional = new Booking("B", standard(), football(), time(10), time(11).plusMinutes(30));
        money(1.5, fractional.getDurationHours());
        money(150_000, fractional.getInvoice().getTotal());
        Fixture f = new Fixture();
        Booking overnight = f.booking("N", time(23), time(1).plusDays(1));
        money(2, overnight.getDurationHours());
        check(f.bookings.createBooking(overnight));
        check(!f.bookings.isTimeSlotAvailable("F001", time(0).plusDays(1), time(2).plusDays(1)));
        Booking seconds = f.booking("S", time(10), time(10).plusSeconds(30).plusNanos(500_000_000));
        money(30.5 / 3600, seconds.getDurationHours());
    }

    private static void testFieldFees() {
        SportField[] fields = {football(),
                new BadmintonField("BD", "Cầu lông", 80_000, FieldStatus.AVAILABLE, 20_000, 10_000),
                new TennisField("T", "Tennis", 150_000, FieldStatus.AVAILABLE, 40_000),
                new PickleballField("P", "Pickleball", 120_000, FieldStatus.AVAILABLE, 15_000)};
        double[] expected = {200_000, 190_000, 340_000, 270_000};
        double[] peak = {40_000, 32_000, 60_000, 48_000};
        for (int index = 0; index < fields.length; index++) {
            Invoice invoice = new Booking("B", standard(), fields[index], time(18), time(20)).getInvoice();
            money(expected[index], invoice.getFieldFee());
            money(peak[index], invoice.getPeakHourSurcharge());
        }
        money(250_000, new Booking("B", standard(), football(), time(10), time(12),
                new ArrayList<>(), true).getInvoice().getTotal());
    }

    private static void testIdentityAndOccupied() {
        Fixture f = new Fixture();
        check(f.bookings.createBooking(f.booking("B1", time(10), time(12))));
        check(!f.bookings.createBooking(f.booking(" B1 ", time(14), time(16))));
        check(!f.bookings.createBooking(new Booking("B2", standard(), football(), time(14), time(16))));
        check(!f.bookings.createBooking(f.booking("B3", time(14), time(16)).confirm()));
        f.field.setStatus(FieldStatus.OCCUPIED);
        check(!f.bookings.createBooking(f.booking("B4", time(14), time(16))));
        check(!f.bookings.isTimeSlotAvailable("F001", time(14), time(16)));
        equal(1, f.bookings.getAllBookings().size());
    }

    private static void testChangeField() {
        Fixture f = new Fixture();
        SportField tennis = new TennisField("T", "Tennis", 150_000, FieldStatus.AVAILABLE, 40_000);
        check(f.fields.addField(tennis));
        check(f.bookings.createBooking(f.booking("B1", time(10), time(12))));
        check(f.bookings.changeBooking("B1", "F001", time(11), time(13), time(8)));
        check(f.bookings.changeBooking("B1", "T", time(11), time(13), time(8)));
        money(340_000, f.bookings.calculateBookingTotal("B1"));
        check(f.bookings.isTimeSlotAvailable("F001", time(11), time(13)));
        check(f.bookings.createBooking(new Booking("LIGHT", standard(), f.field,
                time(18), time(20), new ArrayList<>(), true)));
        check(!f.bookings.changeBooking("LIGHT", "T", time(18), time(20), time(8)));
        check(!f.bookings.changeBooking("B1", "UNKNOWN", time(11), time(13), time(8)));
        f.field.setStatus(FieldStatus.MAINTENANCE);
        check(!f.bookings.changeBooking("B1", "F001", time(11), time(13), time(8)));
        equal("T", f.bookings.findBookingById("B1").getField().getFieldId());
    }

    private static void testChangeTimeBoundary() {
        Fixture f = new Fixture();
        check(f.bookings.createBooking(f.booking("B1", time(10), time(12))));
        check(!f.bookings.changeBooking("B1", "F001", time(14), time(16), time(10)));
        check(!f.bookings.changeBooking("B1", "F001", time(7), time(9), time(8)));
        throwsType(IllegalArgumentException.class,
                () -> f.bookings.changeBooking("B1", "F001", time(12), time(10), time(8)));
        equal(time(10), f.bookings.findBookingById("B1").getStartTime());
    }

    private static void testTerminalStates() {
        Fixture f = new Fixture();
        check(f.bookings.createBooking(f.booking("DONE", time(10), time(12))));
        check(!f.bookings.completeBooking("DONE", time(11)));
        check(f.bookings.completeBooking("DONE", time(12)));
        check(!f.bookings.completeBooking("DONE", time(13)));
        check(!f.bookings.cancelBooking("DONE", time(8)));
        check(!f.bookings.changeBooking("DONE", "F001", time(14), time(16), time(8)));
        check(!f.bookings.isTimeSlotAvailable("F001", time(10), time(12)));
        check(f.bookings.createBooking(f.booking("CANCEL", time(14), time(16))));
        check(f.bookings.cancelBooking("CANCEL", time(8)));
        check(!f.bookings.completeBooking("CANCEL", time(16)));
        check(!f.bookings.cancelBooking("CANCEL", time(8)));
        throwsType(IllegalStateException.class, () -> f.bookings.calculateBookingTotal("CANCEL"));
    }

    private static void testInvoiceSnapshot() {
        Fixture f = new Fixture();
        VipCustomer customer = vip();
        Service water = new Service("W", "Nước", 10_000);
        check(f.bookings.createBooking(new Booking("B1", customer, f.field, time(18), time(20),
                Arrays.asList(new Booking.ServiceItem(water, 2)), true)));
        check(f.bookings.completeBooking("B1", time(20)));
        Invoice paid = f.bookings.findBookingById("B1").getInvoice();
        String original = paid.toString();
        f.field.setBasePricePerHour(900_000);
        f.field.setNightLightFee(1);
        f.field.setFieldName("Tên sân mới");
        customer.setDiscountRate(1);
        customer.setFullName("Tên mới");
        water.setPrice(1);
        check(paid == f.bookings.findBookingById("B1").getInvoice());
        equal(original, paid.toString());
        money(279_000, f.reports.calculateTotalRevenue());
        money(279_000, f.bookings.calculateBookingTotal("B1"));
        throwsType(IllegalArgumentException.class, () -> new Invoice(f.bookings.findBookingById("B1")));
    }

    private static void testEncapsulation() {
        Fixture f = new Fixture();
        List<Booking.ServiceItem> services = new ArrayList<>();
        services.add(new Booking.ServiceItem(new Service("W", "Nước", 10_000), 1));
        Booking draft = new Booking("B1", standard(), f.field, time(10), time(12), services, false);
        services.clear();
        equal(1, draft.getServices().size());
        check(f.bookings.createBooking(draft));
        throwsType(UnsupportedOperationException.class, () -> f.bookings.getAllBookings().clear());
        throwsType(UnsupportedOperationException.class, () -> draft.getServices().clear());
        Booking managed = f.bookings.findBookingById("B1");
        managed.cancel();
        managed.reschedule(f.field, time(14), time(16));
        managed.complete();
        equal(BookingStatus.CONFIRMED, f.bookings.findBookingById("B1").getStatus());
        equal(time(10), f.bookings.findBookingById("B1").getStartTime());
        money(0, f.reports.calculateTotalRevenue());
    }

    private static void testInvalidInvoice() {
        Service enormous = new Service("S", "Giá lớn", Double.MAX_VALUE);
        throwsType(IllegalArgumentException.class, () -> new Booking.ServiceItem(enormous, 2));
        SportField field = new FootballField("F", "Giá lớn", Double.MAX_VALUE, FieldStatus.AVAILABLE);
        throwsType(IllegalArgumentException.class,
                () -> new Booking("B", standard(), field, time(10), time(12)).getInvoice());
        Customer invalidDiscount = new Customer("C", "Sai giảm giá", "0901234567") {
            @Override
            public double calculateDiscount(double amount) {
                return amount + 1;
            }
        };
        Fixture f = new Fixture();
        check(f.bookings.createBooking(new Booking("B1", invalidDiscount, f.field, time(10), time(12))));
        throwsType(IllegalArgumentException.class, () -> f.bookings.completeBooking("B1", time(12)));
        equal(BookingStatus.CONFIRMED, f.bookings.findBookingById("B1").getStatus());
        money(0, f.reports.calculateTotalRevenue());
    }

    private static void testEmptyAndInvalidQueries() {
        Fixture f = new Fixture();
        money(0, f.reports.calculateTotalRevenue());
        check(f.reports.getTop3FieldTypes().isEmpty());
        equal(null, f.bookings.findBookingById(null));
        check(!f.bookings.cancelBooking("UNKNOWN", time(8)));
        check(!f.bookings.completeBooking("UNKNOWN", time(12)));
        check(!f.bookings.changeBooking("UNKNOWN", "F001", time(10), time(12), time(8)));
        check(!f.bookings.isTimeSlotAvailable("UNKNOWN", time(10), time(12)));
        throwsType(IllegalArgumentException.class, () -> f.bookings.calculateBookingTotal("UNKNOWN"));
        throwsType(IllegalArgumentException.class, () -> f.bookings.cancelBooking("UNKNOWN", null));
        throwsType(IllegalArgumentException.class, () -> f.bookings.createBooking(null));
        throwsType(IllegalArgumentException.class, () -> new BookingManager(null));
        throwsType(IllegalArgumentException.class, () -> new ReportManager(null, f.bookings));
        throwsType(IllegalArgumentException.class, () -> f.reports.findAvailableFields(time(12), time(10)));
        throwsType(IllegalArgumentException.class, () -> f.bookings.isTimeSlotAvailable("F001", null, time(12)));
    }

    private static void testDisplayCancelled() {
        Fixture f = new Fixture();
        check(f.bookings.createBooking(f.booking("B1", time(10), time(12))));
        check(capture(() -> check(f.bookings.displayBooking("B1"))).contains("TỔNG THANH TOÁN"));
        check(f.bookings.cancelBooking("B1", time(8)));
        check(capture(() -> check(f.bookings.displayBooking("B1"))).contains("CANCELLED"));
        check(!f.bookings.displayBooking("UNKNOWN"));
    }

    private static Customer standard() {
        return new StandardCustomer("C001", "Khách thường", "0901234567");
    }

    private static VipCustomer vip() {
        return new VipCustomer("V001", "Khách VIP", "0901234567");
    }

    private static FootballField football() {
        return new FootballField("F001", "Sân bóng", 100_000, FieldStatus.AVAILABLE, 50_000);
    }

    private static LocalDateTime time(int hour) {
        return LocalDateTime.of(2030, 1, 15, hour, 0);
    }

    private static final class Fixture {

        private final FieldManager fields = new FieldManager();
        private final FootballField field = football();
        private final BookingManager bookings = new BookingManager(fields);
        private final ReportManager reports = new ReportManager(fields, bookings);

        private Fixture() {
            check(fields.addField(field));
        }

        private Booking booking(String id, LocalDateTime start, LocalDateTime end) {
            return new Booking(id, standard(), field, start, end);
        }
    }

    private static String capture(Runnable action) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8.name())) {
            System.setOut(output);
            action.run();
        } catch (java.io.UnsupportedEncodingException error) {
            throw new AssertionError(error);
        } finally {
            System.setOut(original);
        }
        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void run(String name, Runnable test) {
        total++;
        try {
            test.run();
            passed++;
            System.out.println("PASS: " + name);
        } catch (AssertionError | RuntimeException error) {
            System.err.println("FAIL: " + name + " - " + error);
            error.printStackTrace(System.err);
        }
    }

    private static void check(boolean condition) {
        if (!condition) {
            throw new AssertionError("Điều kiện không đúng.");
        }
    }

    private static void equal(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("expected=" + expected + ", actual=" + actual);
        }
    }

    private static void money(double expected, double actual) {
        if (!Double.isFinite(actual) || Math.abs(expected - actual) > 0.000001) {
            throw new AssertionError("expected=" + expected + ", actual=" + actual);
        }
    }

    private static void throwsType(Class<? extends Throwable> type, Runnable action) {
        try {
            action.run();
        } catch (Throwable error) {
            if (type.isInstance(error)) {
                return;
            }
            throw new AssertionError("Expected " + type.getSimpleName() + " but got " + error, error);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
}
