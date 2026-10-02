package smartsporthub.test;

/**
 * Bộ khung kiểm thử dùng chung cho các test console.
 * <p>
 * Không dùng JUnit để giữ đúng phạm vi bài OOP giữa kỳ:
 * chỉ cần đếm test đạt, không cần phân loại test theo loại lỗi.
 */
public final class TestSupport {

    private TestSupport() {
        // Utility class
    }

    /** Một bộ test gồm nhiều ca kiểm tra, in kết quả khi kết thúc. */
    public static final class Suite {

        private final String title;
        private int passed;
        private int total;

        public Suite(String title) {
            this.title = title;
        }

        public void run(String name, Runnable test) {
            total++;
            try {
                test.run();
                passed++;
                System.out.println("PASS: " + name);
            } catch (AssertionError error) {
                System.err.println("FAIL: " + name + " - " + error.getMessage());
            }
        }

        /** In tổng kết và ném lỗi nếu còn ca nào không đạt. */
        public void finish() {
            System.out.printf("%n===== KẾT QUẢ %s: %d/%d PASS =====%n", title, passed, total);
            if (passed != total) {
                throw new AssertionError("Có kiểm thử không đạt.");
            }
        }
    }

    public static void assertTrue(boolean condition) {
        throwIf(!condition, "Điều kiện phải là true");
    }

    public static void assertTrue(boolean condition, String message) {
        throwIf(!condition, message);
    }

    public static void assertFalse(boolean condition) {
        throwIf(condition, "Điều kiện phải là false");
    }

    private static void throwIf(boolean failure, String message) {
        if (failure) {
            throw new AssertionError(message);
        }
    }

    public static void assertEquals(double expected, double actual) {
        if (Double.compare(expected, actual) != 0) {
            throw new AssertionError("expected=" + expected + ", actual=" + actual);
        }
    }

    public static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("expected=" + expected + ", actual=" + actual);
        }
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + "; expected=" + expected + ", actual=" + actual);
        }
    }

    public static void assertNull(Object actual) {
        if (actual != null) {
            throw new AssertionError("Giá trị phải null nhưng nhận " + actual);
        }
    }

    public static void assertNotNull(Object actual, String label) {
        if (actual == null) {
            throw new AssertionError(label + " không được null");
        }
    }

    public static void assertThrows(Class<? extends Throwable> expectedType, Runnable action) {
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