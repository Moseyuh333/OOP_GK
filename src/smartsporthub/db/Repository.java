package smartsporthub.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smartsporthub.manager.BookingManager;
import smartsporthub.manager.CustomerManager;
import smartsporthub.manager.FieldManager;
import smartsporthub.manager.InvoiceManager;
import smartsporthub.manager.ServiceManager;
import smartsporthub.model.BadmintonField;
import smartsporthub.model.Booking;
import smartsporthub.model.BookingStatus;
import smartsporthub.model.Customer;
import smartsporthub.model.FieldStatus;
import smartsporthub.model.FootballField;
import smartsporthub.model.Invoice;
import smartsporthub.model.InvoiceStatus;
import smartsporthub.model.PickleballField;
import smartsporthub.model.Service;
import smartsporthub.model.SportField;
import smartsporthub.model.StandardCustomer;
import smartsporthub.model.TennisField;
import smartsporthub.model.VipCustomer;

/**
 * Đọc và ghi toàn bộ dữ liệu xuống SQLite.
 * <p>
 * Nguồn sự thật vẫn là các manager trong RAM; lớp này chỉ là lớp bền vững hoá.
 * Đọc DB lên RAM lúc khởi động và ghi xuống sau mỗi thay đổi, nhờ vậy 20 câu LINQ
 * phía C# vẫn thấy dữ liệu dạng collection in-memory đúng yêu cầu đề bài.
 */
public class Repository {

    private final Connection connection;

    public Repository(Connection connection) {
        this.connection = connection;
    }

    // ---------------------------------------------------------------- ghi

    /** Ghi đè toàn bộ 5 bảng từ dữ liệu đang có trong RAM. */
    public void saveAll(FieldManager fields, CustomerManager customers, ServiceManager services,
            BookingManager bookings, InvoiceManager invoices) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            Database.clearAll(connection);
            saveCustomers(customers.getAllCustomers());
            saveFields(fields.getAllFields());
            saveServices(services.getAllServices());
            saveBookings(bookings);
            saveInvoices(invoices);
            connection.commit();
        } catch (SQLException | RuntimeException error) {
            connection.rollback();
            throw error;
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    private void saveCustomers(List<Customer> customers) throws SQLException {
        String sql = """
                INSERT INTO customers (id, full_name, phone_number, email, customer_type,
                                       discount_rate, promotion_policy)
                VALUES (?, ?, ?, ?, ?, ?, ?)""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Customer customer : customers) {
                ps.setString(1, customer.getCustomerId());
                ps.setString(2, customer.getFullName());
                ps.setString(3, customer.getPhoneNumber());
                ps.setString(4, customer.getEmail());
                ps.setString(5, customer.getCustomerType().name());
                ps.setDouble(6, discountOf(customer));
                ps.setString(7, customer.getPromotionPolicy());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void saveFields(List<SportField> fields) throws SQLException {
        String sql = """
                INSERT INTO fields (id, name, type, base_price_per_hour, status, racket_fee, mat_fee)
                VALUES (?, ?, ?, ?, ?, ?, ?)""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (SportField field : fields) {
                ps.setString(1, field.getFieldId());
                ps.setString(2, field.getFieldName());
                ps.setString(3, typeOf(field));
                ps.setDouble(4, field.getBasePricePerHour());
                ps.setString(5, field.getStatus().name());
                ps.setDouble(6, field instanceof BadmintonField racket ? racket.getRacketFee() : 0);
                ps.setDouble(7, field instanceof BadmintonField badminton ? badminton.getMatFee() : 0);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void saveServices(List<Service> services) throws SQLException {
        String sql = "INSERT INTO services (id, name, category, unit_price, active) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Service service : services) {
                ps.setString(1, service.getServiceId());
                ps.setString(2, service.getServiceName());
                ps.setString(3, service.getCategory());
                ps.setDouble(4, service.getUnitPrice());
                ps.setInt(5, service.isActive() ? 1 : 0);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void saveBookings(BookingManager manager) throws SQLException {
        String sql = """
                INSERT INTO bookings (id, customer_id, field_id, service_id, start_time, end_time,
                                      status, peak_hour, hours, field_price, service_fee,
                                      discount, total_amount)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Booking booking : manager.getAllBookings()) {
                ps.setString(1, booking.getBookingId());
                ps.setString(2, booking.getCustomer().getCustomerId());
                ps.setString(3, booking.getField().getFieldId());
                ps.setString(4, booking.getService() == null ? null : booking.getService().getServiceId());
                ps.setString(5, booking.getStartTime().toString());
                ps.setString(6, booking.getEndTime().toString());
                ps.setString(7, booking.getStatus().name());
                ps.setInt(8, booking.isPeakHourBooking() ? 1 : 0);
                ps.setDouble(9, booking.getHours());
                ps.setDouble(10, booking.getRentalFee());
                ps.setDouble(11, booking.getServiceFee());
                ps.setDouble(12, booking.getDiscount());
                ps.setDouble(13, booking.getTotalAmount());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void saveInvoices(InvoiceManager manager) throws SQLException {
        String sql = """
                INSERT INTO invoices (id, booking_id, issue_date, status, amount)
                VALUES (?, ?, ?, ?, ?)""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Invoice invoice : manager.getAllInvoices()) {
                ps.setString(1, invoice.getInvoiceId());
                ps.setString(2, invoice.getBookingId());
                ps.setString(3, invoice.getIssueDate().toString());
                ps.setString(4, invoice.getStatus().name());
                ps.setDouble(5, invoice.getAmount());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ---------------------------------------------------------------- đọc

    /** Nạp dữ liệu từ DB vào các manager. Trả về false nếu DB rỗng. */
    public boolean loadInto(FieldManager fields, CustomerManager customers, ServiceManager services,
            BookingManager bookings, InvoiceManager invoices) throws SQLException {
        Map<String, Customer> customerMap = readCustomers(customers);
        Map<String, Service> serviceMap = readServices(services);
        Map<String, SportField> fieldMap = readFields(fields);
        Map<String, Booking> bookingMap = readBookings(bookings, customerMap, fieldMap, serviceMap);
        readInvoices(invoices, bookingMap);
        return !customerMap.isEmpty();
    }

    private Map<String, Customer> readCustomers(CustomerManager manager) throws SQLException {
        Map<String, Customer> result = new HashMap<>();
        String sql = "SELECT id, full_name, phone_number, email, customer_type, discount_rate FROM customers ORDER BY id";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("full_name");
                String phone = rs.getString("phone_number");
                String email = rs.getString("email");
                double discount = rs.getDouble("discount_rate");
                boolean vip = "VIP".equals(rs.getString("customer_type"));
                Customer customer = vip
                        ? new VipCustomer(id, name, phone, email, discount, 1)
                        : new StandardCustomer(id, name, phone, email);
                manager.addCustomer(customer);
                result.put(id, customer);
            }
        }
        return result;
    }

    private Map<String, Service> readServices(ServiceManager manager) throws SQLException {
        Map<String, Service> result = new HashMap<>();
        String sql = "SELECT id, name, category, unit_price, active FROM services ORDER BY id";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Service service = new Service(rs.getString("id"), rs.getString("name"),
                        rs.getString("category"), rs.getDouble("unit_price"), rs.getInt("active") == 1);
                manager.addService(service);
                result.put(service.getServiceId(), service);
            }
        }
        return result;
    }

    private Map<String, SportField> readFields(FieldManager manager) throws SQLException {
        Map<String, SportField> result = new HashMap<>();
        String sql = "SELECT id, name, type, base_price_per_hour, status, racket_fee, mat_fee FROM fields ORDER BY id";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                double price = rs.getDouble("base_price_per_hour");
                FieldStatus status = FieldStatus.valueOf(rs.getString("status"));
                SportField field = createField(id, name, rs.getString("type"), price, status,
                        rs.getDouble("racket_fee"), rs.getDouble("mat_fee"));
                manager.addField(field);
                result.put(id, field);
            }
        }
        return result;
    }

    private SportField createField(String id, String name, String type, double price,
            FieldStatus status, double racketFee, double matFee) {
        return switch (type) {
            case "BADMINTON" -> new BadmintonField(id, name, price, status, racketFee, matFee);
            case "TENNIS" -> new TennisField(id, name, price, status);
            case "PICKLEBALL" -> new PickleballField(id, name, price, status);
            default -> new FootballField(id, name, price, status);
        };
    }

    private Map<String, Booking> readBookings(BookingManager manager, Map<String, Customer> customers,
            Map<String, SportField> fields, Map<String, Service> services) throws SQLException {
        Map<String, Booking> result = new HashMap<>();
        String sql = """
                SELECT id, customer_id, field_id, service_id, start_time, end_time, status
                FROM bookings ORDER BY id""";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                Customer customer = customers.get(rs.getString("customer_id"));
                SportField field = fields.get(rs.getString("field_id"));
                String serviceId = rs.getString("service_id");
                Service service = serviceId == null ? null : services.get(serviceId);
                if (customer == null || field == null) {
                    // Khoa ngoai chặn truong hop nay; bo qua de mot ban ghi hong khong lam hong ca nhan.
                    continue;
                }
                Booking booking = new Booking(id, customer, field, service,
                        LocalDateTime.parse(rs.getString("start_time")),
                        LocalDateTime.parse(rs.getString("end_time")),
                        BookingStatus.valueOf(rs.getString("status")));
                manager.addBooking(booking);
                result.put(id, booking);
            }
        }
        return result;
    }

    private void readInvoices(InvoiceManager manager, Map<String, Booking> bookings) throws SQLException {
        String sql = "SELECT id, booking_id, issue_date, status FROM invoices ORDER BY id";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Booking booking = bookings.get(rs.getString("booking_id"));
                if (booking == null) {
                    continue;
                }
                manager.addInvoice(new Invoice(rs.getString("id"), booking,
                        LocalDate.parse(rs.getString("issue_date")),
                        InvoiceStatus.valueOf(rs.getString("status"))));
            }
        }
    }

    // ---------------------------------------------------------------- tiện ích

    private static double discountOf(Customer customer) {
        return customer instanceof VipCustomer vip ? vip.getDiscountRate() : 0;
    }

    private static String typeOf(SportField field) {
        if (field instanceof BadmintonField) {
            return "BADMINTON";
        }
        if (field instanceof TennisField) {
            return "TENNIS";
        }
        if (field instanceof PickleballField) {
            return "PICKLEBALL";
        }
        return "FOOTBALL";
    }

    /** Đếm số bản ghi mỗi bảng, dùng cho kiểm thử. */
    public Map<String, Integer> countRows() throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        for (String table : List.of("customers", "fields", "services", "bookings", "invoices")) {
            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
                counts.put(table, rs.next() ? rs.getInt(1) : 0);
            }
        }
        return counts;
    }

    /**
     * Khung giờ đang chiếm của một sân.
     *
     * @return danh sách rỗng nếu sân trống
     */
    public List<TimeSlot> loadBookedSlotsOfField(String fieldId) throws SQLException {
        List<TimeSlot> result = new ArrayList<>();
        String sql = """
                SELECT id, start_time, end_time FROM bookings
                WHERE field_id = ? AND status IN ('PENDING', 'CONFIRMED')
                ORDER BY start_time""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fieldId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new TimeSlot(rs.getString(1),
                            LocalDateTime.parse(rs.getString(2)),
                            LocalDateTime.parse(rs.getString(3))));
                }
            }
        }
        return result;
    }

    /**
     * Khung giờ đã bị chiếm của một sân.
     *
     * @param bookingId mã booking chiếm chỗ, dùng để bỏ qua chính nó khi đổi lịch
     * @param start bắt đầu
     * @param end kết thúc
     */
    public record TimeSlot(String bookingId, LocalDateTime start, LocalDateTime end) {
        /** Hai khung giờ chồng nhau khi khoảng này bắt đầu trước khi khoảng kia kết thúc và ngược lại. */
        public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
            return start.isBefore(otherEnd) && otherStart.isBefore(end);
        }
    }
}