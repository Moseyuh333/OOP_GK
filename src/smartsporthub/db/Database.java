package smartsporthub.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Quản lý kết nối SQLite và tạo schema.
 * <p>
 * Tầng C# không bao giờ chạm vào DB này — nó chỉ đọc dữ liệu qua HTTP API.
 * Nhờ vậy 20 câu LINQ vẫn chạy trên collection in-memory đúng yêu cầu đề bài,
 * đồng th���i dữ liệu vẫn còn sau khi tắt ứng dụng.
 */
public final class Database {

    private static final String DRIVER = "org.sqlite.JDBC";

    private Database() {
        // Utility class
    }

    /**
     * Mở kết nối tới file SQLite, tạo thư mục cha nếu chưa có.
     *
     * @param filePath đường dẫn file DB
     * @return kết nối mở, người gọi phải đóng
     */
    public static Connection open(String filePath) throws SQLException, java.io.IOException {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException error) {
            throw new SQLException("Khong tim thay driver SQLite. Kiem tra lib/sqlite-jdbc.jar co trong classpath.",
                    error);
        }
        Path path = Path.of(filePath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + filePath);
        try (Statement statement = connection.createStatement()) {
            // WAL giup doc khong bi chan khi dang ghi, foreign_keys bat buoc cho rang buoc FK that.
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA journal_mode = WAL");
        }
        return connection;
    }

    /** Tạo toàn bộ bảng nếu chưa tồn tại. Không mất dữ liệu cũ. */
    public static void createSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS customers (
                        id              TEXT PRIMARY KEY,
                        full_name       TEXT NOT NULL,
                        phone_number    TEXT NOT NULL,
                        email           TEXT,
                        customer_type   TEXT NOT NULL CHECK (customer_type IN ('VIP', 'STANDARD')),
                        discount_rate   REAL NOT NULL CHECK (discount_rate >= 0 AND discount_rate < 1),
                        promotion_policy TEXT
                    )""");

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS services (
                        id          TEXT PRIMARY KEY,
                        name        TEXT NOT NULL,
                        category    TEXT NOT NULL,
                        unit_price  REAL NOT NULL CHECK (unit_price >= 0),
                        active      INTEGER NOT NULL CHECK (active IN (0, 1))
                    )""");

            // SPORTFIELD khong tach bang phan loai con; khoa thanh phan loai luu dang
            // TEXT de giu doc lap giua Java va C# (C# doc ten, khong can biet so thu tu).
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS fields (
                        id                  TEXT PRIMARY KEY,
                        name                TEXT NOT NULL,
                        type                TEXT NOT NULL CHECK (type IN
                                                ('FOOTBALL', 'BADMINTON', 'TENNIS', 'PICKLEBALL')),
                        base_price_per_hour REAL NOT NULL CHECK (base_price_per_hour > 0),
                        status              TEXT NOT NULL CHECK (status IN
                                                ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE')),
                        racket_fee          REAL NOT NULL DEFAULT 0,
                        mat_fee             REAL NOT NULL DEFAULT 0
                    )""");

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS bookings (
                        id           TEXT PRIMARY KEY,
                        customer_id  TEXT NOT NULL REFERENCES customers(id),
                        field_id     TEXT NOT NULL REFERENCES fields(id),
                        service_id   TEXT REFERENCES services(id),
                        start_time   TEXT NOT NULL,
                        end_time     TEXT NOT NULL,
                        status       TEXT NOT NULL CHECK (status IN
                                          ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
                        peak_hour    INTEGER NOT NULL CHECK (peak_hour IN (0, 1)),
                        hours        REAL NOT NULL CHECK (hours > 0),
                        field_price  REAL NOT NULL CHECK (field_price >= 0),
                        service_fee  REAL NOT NULL CHECK (service_fee >= 0),
                        discount     REAL NOT NULL CHECK (discount >= 0),
                        total_amount REAL NOT NULL CHECK (total_amount >= 0),
                        created_at   TEXT NOT NULL DEFAULT (datetime('now'))
                    )""");

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS invoices (
                        id         TEXT PRIMARY KEY,
                        booking_id TEXT NOT NULL UNIQUE REFERENCES bookings(id),
                        issue_date TEXT NOT NULL,
                        status     TEXT NOT NULL CHECK (status IN ('PAID', 'UNPAID', 'REFUNDED')),
                        amount     REAL NOT NULL CHECK (amount >= 0)
                    )""");

            // Truy van bao cao cham nhat: booking theo khoa thoi gian va theo khach.
            statement.executeUpdate("""
                    CREATE INDEX IF NOT EXISTS idx_bookings_field_time
                        ON bookings(field_id, start_time)""");
            statement.executeUpdate("""
                    CREATE INDEX IF NOT EXISTS idx_bookings_customer
                        ON bookings(customer_id)""");
            statement.executeUpdate("""
                    CREATE INDEX IF NOT EXISTS idx_bookings_status
                        ON bookings(status)""");
        }
    }

    /** Xóa sạch dữ liệu nhưng giữ nguyên schema. Dùng khi nạp lại seed. */
    public static void clearAll(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM invoices");
            statement.executeUpdate("DELETE FROM bookings");
            statement.executeUpdate("DELETE FROM services");
            statement.executeUpdate("DELETE FROM fields");
            statement.executeUpdate("DELETE FROM customers");
        }
    }

    /** Trả về true nếu DB đã có dữ liệu. */
    public static boolean hasData(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             var result = statement.executeQuery("SELECT COUNT(*) FROM customers")) {
            return result.next() && result.getInt(1) > 0;
        }
    }
}