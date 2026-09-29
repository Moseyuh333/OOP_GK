package smartsporthub.manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import smartsporthub.model.Booking;
import smartsporthub.model.BookingStatus;
import smartsporthub.model.FieldStatus;
import smartsporthub.model.FootballField;
import smartsporthub.model.SportField;

/** Quản lý lịch và các chuyển trạng thái; lỗi nghiệp vụ trả false cùng thông báo. */
public class BookingManager {

    private final FieldManager fieldManager;
    private final List<Booking> bookings;
    private String lastMessage;

    public BookingManager(FieldManager fieldManager) {
        if (fieldManager == null) {
            throw new IllegalArgumentException("FieldManager không được null.");
        }
        this.fieldManager = fieldManager;
        this.bookings = new ArrayList<>();
        this.lastMessage = "";
    }

    /** Nhận bản nháp PENDING, lưu một phiên bản CONFIRMED nếu hợp lệ. */
    public boolean createBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking không được null.");
        }
        if (findBookingById(booking.getBookingId()) != null) {
            return fail("Mã booking đã tồn tại: " + booking.getBookingId());
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            return fail("Chỉ nhận booking mới ở trạng thái PENDING.");
        }
        SportField field = fieldManager.findFieldById(booking.getField().getFieldId());
        if (field != booking.getField()) {
            return fail("Hãy sử dụng sân được đăng ký trong FieldManager.");
        }
        if (!checkFieldAvailable(field)) {
            return false;
        }
        if (hasConflict(field.getFieldId(), booking.getStartTime(), booking.getEndTime(), null)) {
            return fail("Đặt sân thất bại: trùng lịch sân " + field.getFieldId() + ".");
        }
        bookings.add(booking.confirm());
        lastMessage = "Đặt sân thành công: " + booking.getBookingId();
        return true;
    }

    public Booking findBookingById(String bookingId) {
        if (bookingId == null) {
            return null;
        }
        for (Booking booking : bookings) {
            if (booking.getBookingId().equals(bookingId.trim())) {
                return booking;
            }
        }
        return null;
    }

    public List<Booking> getAllBookings() {
        return Collections.unmodifiableList(new ArrayList<>(bookings));
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public boolean isTimeSlotAvailable(String fieldId, LocalDateTime start, LocalDateTime end) {
        Booking.validateTimeRange(start, end);
        SportField field = fieldManager.findFieldById(fieldId);
        return field != null && field.getStatus() == FieldStatus.AVAILABLE
                && !hasConflict(field.getFieldId(), start, end, null);
    }

    public boolean cancelBooking(String bookingId) {
        return cancelBooking(bookingId, LocalDateTime.now());
    }

    public boolean cancelBooking(String bookingId, LocalDateTime currentTime) {
        requireCurrentTime(currentTime);
        Booking booking = findBookingById(bookingId);
        if (!canChange(booking, currentTime)) {
            return false;
        }
        replace(booking, booking.cancel());
        lastMessage = "Đã hủy booking " + booking.getBookingId();
        return true;
    }

    public boolean changeBooking(String bookingId, String newFieldId,
            LocalDateTime start, LocalDateTime end) {
        return changeBooking(bookingId, newFieldId, start, end, LocalDateTime.now());
    }

    /** Đổi sân và/hoặc lịch, bỏ qua chính booking này khi kiểm tra xung đột. */
    public boolean changeBooking(String bookingId, String newFieldId,
            LocalDateTime start, LocalDateTime end, LocalDateTime currentTime) {
        Booking.validateTimeRange(start, end);
        requireCurrentTime(currentTime);
        Booking booking = findBookingById(bookingId);
        if (!canChange(booking, currentTime)) {
            return false;
        }
        if (!start.isAfter(currentTime)) {
            return fail("Lịch mới phải bắt đầu sau thời điểm hiện tại.");
        }
        SportField field = fieldManager.findFieldById(newFieldId);
        if (!checkFieldAvailable(field)) {
            return false;
        }
        if (booking.isUseNightLight() && !(field instanceof FootballField)) {
            return fail("Booking có đèn sân bóng chỉ có thể đổi sang sân bóng đá.");
        }
        if (hasConflict(field.getFieldId(), start, end, booking.getBookingId())) {
            return fail("Đổi booking thất bại: trùng lịch sân " + field.getFieldId() + ".");
        }
        replace(booking, booking.reschedule(field, start, end));
        lastMessage = "Đã đổi booking " + booking.getBookingId();
        return true;
    }

    public boolean completeBooking(String bookingId) {
        return completeBooking(bookingId, LocalDateTime.now());
    }

    /** Thanh toán và chốt hóa đơn sau khi kết thúc lượt chơi. */
    public boolean completeBooking(String bookingId, LocalDateTime currentTime) {
        requireCurrentTime(currentTime);
        Booking booking = findBookingById(bookingId);
        if (booking == null) {
            return fail("Không tìm thấy booking.");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            return fail("Chỉ thanh toán booking đã xác nhận và chưa hoàn tất.");
        }
        if (currentTime.isBefore(booking.getEndTime())) {
            return fail("Chưa kết thúc lượt chơi, không thể hoàn tất booking.");
        }
        replace(booking, booking.complete());
        lastMessage = "Đã thanh toán và hoàn tất booking " + booking.getBookingId();
        return true;
    }

    public double calculateBookingTotal(String bookingId) {
        Booking booking = findBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Không tìm thấy booking: " + bookingId);
        }
        return booking.getInvoice().getTotal();
    }

    public boolean displayBooking(String bookingId) {
        Booking booking = findBookingById(bookingId);
        if (booking == null) {
            return fail("Không tìm thấy booking.");
        }
        System.out.println("Booking " + booking.getBookingId() + " | " + booking.getStatus());
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            System.out.println(booking.getField().getFieldName() + " | "
                    + booking.getStartTime() + " -> " + booking.getEndTime());
        } else {
            booking.getInvoice().printInvoice();
        }
        lastMessage = "Đã hiển thị booking " + booking.getBookingId();
        return true;
    }

    private boolean hasConflict(String fieldId, LocalDateTime start,
            LocalDateTime end, String excludedBookingId) {
        for (Booking booking : bookings) {
            if (!booking.getBookingId().equals(excludedBookingId)
                    && booking.getStatus() != BookingStatus.CANCELLED
                    && booking.getField().getFieldId().equals(fieldId)
                    && start.isBefore(booking.getEndTime())
                    && booking.getStartTime().isBefore(end)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkFieldAvailable(SportField field) {
        if (field == null) {
            return fail("Không tìm thấy sân.");
        }
        if (field.getStatus() == FieldStatus.MAINTENANCE) {
            return fail("Đặt sân thất bại: sân " + field.getFieldId() + " đang bảo trì.");
        }
        if (field.getStatus() != FieldStatus.AVAILABLE) {
            return fail("Sân " + field.getFieldId() + " không ở trạng thái AVAILABLE.");
        }
        return true;
    }

    private boolean canChange(Booking booking, LocalDateTime currentTime) {
        if (booking == null) {
            return fail("Không tìm thấy booking.");
        }
        if (booking.getStatus() != BookingStatus.PENDING
                && booking.getStatus() != BookingStatus.CONFIRMED) {
            return fail("Booking đã hủy hoặc hoàn tất không thể thay đổi.");
        }
        if (!currentTime.isBefore(booking.getStartTime())) {
            return fail("Chỉ được hủy/đổi trước giờ bắt đầu.");
        }
        return true;
    }

    private void replace(Booking previous, Booking next) {
        bookings.set(bookings.indexOf(previous), next);
    }

    private boolean fail(String message) {
        lastMessage = message;
        return false;
    }

    private static void requireCurrentTime(LocalDateTime currentTime) {
        if (currentTime == null) {
            throw new IllegalArgumentException("Thời điểm hiện tại không được null.");
        }
    }
}
