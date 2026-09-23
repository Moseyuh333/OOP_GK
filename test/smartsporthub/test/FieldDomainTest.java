package smartsporthub.test;

import java.time.LocalTime;
import java.util.List;

import smartsporthub.interfaces.IPeakHourCalculable;
import smartsporthub.manager.FieldManager;
import smartsporthub.model.BadmintonField;
import smartsporthub.model.FieldStatus;
import smartsporthub.model.FootballField;
import smartsporthub.model.PickleballField;
import smartsporthub.model.SportField;
import smartsporthub.model.TennisField;

/**
 * Bộ kiểm thử console độc lập cho Field Domain.
 * Không dùng thư viện ngoài để phù hợp bài OOP giữa kỳ.
 */
public final class FieldDomainTest {

    private static int passed;
    private static int total;

    private FieldDomainTest() {
    }

    public static void main(String[] args) {
        run("Tạo FootballField", FieldDomainTest::testCreateFootballField);
        run("Tạo BadmintonField", FieldDomainTest::testCreateBadmintonField);
        run("Tạo TennisField", FieldDomainTest::testCreateTennisField);
        run("Tạo PickleballField", FieldDomainTest::testCreatePickleballField);
        run("Tính tiền thuê đa hình", FieldDomainTest::testRentalFeePolymorphism);
        run("Tính phí giờ cao điểm riêng", FieldDomainTest::testPeakHourSurcharge);
        run("Xác định khung giờ 17:00-20:00", FieldDomainTest::testPeakHourRange);
        run("Tính phí đèn bóng ban đêm", FieldDomainTest::testFootballNightLightFee);
        run("Validation từ chối dữ liệu sai", FieldDomainTest::testValidation);
        run("FieldManager tìm sân theo ID", FieldDomainTest::testFindFieldById);
        run("FieldManager lọc sân AVAILABLE", FieldDomainTest::testFindAvailableFields);
        run("FieldManager cập nhật MAINTENANCE", FieldDomainTest::testUpdateFieldStatus);
        run("FieldManager chặn ID trùng", FieldDomainTest::testDuplicateId);
        run("FieldManager lọc theo loại sân", FieldDomainTest::testSearchByType);

        System.out.printf("%n===== KẾT QUẢ FIELD DOMAIN: %d/%d PASS =====%n", passed, total);
        if (passed != total) {
            throw new AssertionError("Có kiểm thử không đạt.");
        }
    }

    private static void testCreateFootballField() {
        FootballField field = new FootballField("F001", "Sân bóng A", 100_000, FieldStatus.AVAILABLE, 50_000);
        assertEquals("F001", field.getFieldId());
        assertEquals("Sân bóng A", field.getFieldName());
        assertEquals(50_000, field.getNightLightFee());
    }

    private static void testCreateBadmintonField() {
        BadmintonField field = new BadmintonField("B001", "Sân cầu lông B", 80_000,
                FieldStatus.AVAILABLE, 20_000, 10_000);
        assertEquals(20_000, field.getRacketFee());
        assertEquals(10_000, field.getMatFee());
    }

    private static void testCreateTennisField() {
        TennisField field = new TennisField("T001", "Sân tennis C", 150_000,
                FieldStatus.AVAILABLE, 40_000);
        assertEquals(40_000, field.getCourtEquipmentFee());
    }

    private static void testCreatePickleballField() {
        PickleballField field = new PickleballField("P001", "Sân pickleball D", 120_000,
                FieldStatus.AVAILABLE, 15_000);
        assertEquals(15_000, field.getEquipmentFee());
    }

    private static void testRentalFeePolymorphism() {
        FootballField football = new FootballField("F001", "Sân bóng", 100_000, FieldStatus.AVAILABLE, 50_000);
        BadmintonField badminton = new BadmintonField("B001", "Sân cầu lông", 80_000,
                FieldStatus.AVAILABLE, 20_000, 10_000);
        TennisField tennis = new TennisField("T001", "Sân tennis", 150_000, FieldStatus.AVAILABLE, 40_000);
        PickleballField pickleball = new PickleballField("P001", "Sân pickleball", 120_000,
                FieldStatus.AVAILABLE, 15_000);

        List<SportField> fields = List.of(football, badminton, tennis, pickleball);
        assertEquals(200_000, fields.get(0).calculateRentalFee(2));
        assertEquals(190_000, fields.get(1).calculateRentalFee(2));
        assertEquals(340_000, fields.get(2).calculateRentalFee(2));
        assertEquals(255_000, fields.get(3).calculateRentalFee(2));
    }

    private static void testPeakHourSurcharge() {
        FootballField field = new FootballField("F001", "Sân bóng", 100_000, FieldStatus.AVAILABLE, 50_000);
        IPeakHourCalculable peakField = field;

        assertEquals(40_000, peakField.calculatePeakHourSurcharge(2));
        assertEquals(200_000, field.calculateRentalFee(2),
                "Phí giờ cao điểm không được tự cộng vào calculateRentalFee");
    }

    private static void testPeakHourRange() {
        IPeakHourCalculable field = new BadmintonField("B001", "Sân cầu lông", 80_000,
                FieldStatus.AVAILABLE, 20_000, 10_000);

        assertTrue(field.isPeakHour(LocalTime.of(17, 0)));
        assertTrue(field.isPeakHour(LocalTime.of(19, 59)));
        assertFalse(field.isPeakHour(LocalTime.of(16, 59)));
        assertFalse(field.isPeakHour(LocalTime.of(20, 0)));
    }

    private static void testFootballNightLightFee() {
        FootballField field = new FootballField("F001", "Sân bóng", 100_000, FieldStatus.AVAILABLE, 50_000);

        assertEquals(200_000, field.calculateRentalFee(2));
        assertEquals(250_000, field.calculateRentalFee(2, true));
    }

    private static void testValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new FootballField(" ", "Sân", 100_000, FieldStatus.AVAILABLE, 50_000));
        assertThrows(IllegalArgumentException.class,
                () -> new FootballField("F001", "", 100_000, FieldStatus.AVAILABLE, 50_000));
        assertThrows(IllegalArgumentException.class,
                () -> new FootballField("F001", "Sân", 0, FieldStatus.AVAILABLE, 50_000));
        assertThrows(IllegalArgumentException.class,
                () -> new FootballField("F001", "Sân", 100_000, null, 50_000));
        assertThrows(IllegalArgumentException.class,
                () -> new FootballField("F001", "Sân", 100_000, FieldStatus.AVAILABLE, -1));

        FootballField field = new FootballField("F001", "Sân bóng", 100_000, FieldStatus.AVAILABLE, 50_000);
        assertThrows(IllegalArgumentException.class, () -> field.calculateRentalFee(0));
    }

    private static void testFindFieldById() {
        FieldManager manager = new FieldManager();
        FootballField football = new FootballField("F001", "Sân bóng", 100_000, FieldStatus.AVAILABLE, 50_000);
        manager.addField(football);

        assertSame(football, manager.findFieldById("F001"));
        assertNull(manager.findFieldById("UNKNOWN"));
        assertNull(manager.findFieldById(null));
    }

    private static void testFindAvailableFields() {
        FieldManager manager = new FieldManager();
        manager.addField(new FootballField("F001", "Sân bóng", 100_000, FieldStatus.AVAILABLE, 50_000));
        manager.addField(new BadmintonField("B001", "Sân cầu lông", 80_000,
                FieldStatus.MAINTENANCE, 20_000, 10_000));
        manager.addField(new TennisField("T001", "Sân tennis", 150_000, FieldStatus.OCCUPIED, 40_000));

        List<SportField> available = manager.findAvailableFields();
        assertEquals(1, available.size());
        assertEquals("F001", available.get(0).getFieldId());
    }

    private static void testUpdateFieldStatus() {
        FieldManager manager = new FieldManager();
        manager.addField(new FootballField("F001", "Sân bóng", 100_000, FieldStatus.AVAILABLE, 50_000));

        assertTrue(manager.updateFieldStatus("F001", FieldStatus.MAINTENANCE));
        assertEquals(FieldStatus.MAINTENANCE, manager.findFieldById("F001").getStatus());
        assertFalse(manager.updateFieldStatus("UNKNOWN", FieldStatus.OCCUPIED));
        assertFalse(manager.updateFieldStatus("F001", null));
    }

    private static void testDuplicateId() {
        FieldManager manager = new FieldManager();
        assertTrue(manager.addField(new FootballField("F001", "Sân 1", 100_000, FieldStatus.AVAILABLE, 50_000)));
        assertFalse(manager.addField(new TennisField("F001", "Sân 2", 150_000, FieldStatus.AVAILABLE, 40_000)));
        assertFalse(manager.addField(null));
        assertEquals(1, manager.getAllFields().size());
    }

    private static void testSearchByType() {
        FieldManager manager = new FieldManager();
        manager.addField(new FootballField("F001", "Sân bóng", 100_000, FieldStatus.AVAILABLE, 50_000));
        manager.addField(new TennisField("T001", "Sân tennis", 150_000, FieldStatus.AVAILABLE, 40_000));

        List<SportField> footballFields = manager.searchByType(FootballField.class);
        assertEquals(1, footballFields.size());
        assertTrue(footballFields.get(0) instanceof FootballField);
    }

    private static void run(String name, Runnable test) {
        total++;
        try {
            test.run();
            passed++;
            System.out.println("PASS: " + name);
        } catch (AssertionError error) {
            System.err.println("FAIL: " + name + " - " + error.getMessage());
        }
    }

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Điều kiện phải là true");
        }
    }

    private static void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("Điều kiện phải là false");
        }
    }

    private static void assertEquals(double expected, double actual) {
        assertEquals(expected, actual, null);
    }

    private static void assertEquals(double expected, double actual, String message) {
        if (Double.compare(expected, actual) != 0) {
            String prefix = message == null ? "" : message + "; ";
            throw new AssertionError(prefix + "expected=" + expected + ", actual=" + actual);
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("expected=" + expected + ", actual=" + actual);
        }
    }

    private static void assertSame(Object expected, Object actual) {
        if (expected != actual) {
            throw new AssertionError("Hai đối tượng phải là cùng một instance");
        }
    }

    private static void assertNull(Object actual) {
        if (actual != null) {
            throw new AssertionError("Giá trị phải null nhưng nhận " + actual);
        }
    }

    private static void assertThrows(Class<? extends Throwable> expectedType, Runnable action) {
        try {
            action.run();
        } catch (Throwable error) {
            if (expectedType.isInstance(error)) {
                return;
            }
            throw new AssertionError("Expected " + expectedType.getSimpleName() + " but got " + error);
        }
        throw new AssertionError("Expected exception " + expectedType.getSimpleName());
    }
}
