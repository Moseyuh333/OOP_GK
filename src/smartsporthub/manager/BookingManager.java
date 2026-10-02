package smartsporthub.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import smartsporthub.model.Booking;
import smartsporthub.model.BookingStatus;
import smartsporthub.model.Customer;
import smartsporthub.model.Service;
import smartsporthub.model.SportField;

/** Quản lý danh sách booking và các truy vấn phân tích phía server. */
public class BookingManager {

    private final List<Booking> bookings;

    public BookingManager() {
        bookings = new ArrayList<>();
    }

    public boolean addBooking(Booking booking) {
        if (booking == null || findBookingById(booking.getBookingId()) != null) {
            return false;
        }
        return bookings.add(booking);
    }

    public Booking findBookingById(String bookingId) {
        if (bookingId == null) {
            return null;
        }
        String normalizedId = bookingId.trim();
        for (Booking booking : bookings) {
            if (booking.getBookingId().equals(normalizedId)) {
                return booking;
            }
        }
        return null;
    }

    public List<Booking> findByCustomer(String customerId) {
        return filter(b -> b.getCustomer().getCustomerId().equals(customerId));
    }

    public List<Booking> findByField(String fieldId) {
        return filter(b -> b.getField().getFieldId().equals(fieldId));
    }

    public List<Booking> findByStatus(BookingStatus status) {
        return filter(b -> b.getStatus() == status);
    }

    public List<Booking> findByService(String serviceId) {
        return filter(b -> b.getService() != null && b.getService().getServiceId().equals(serviceId));
    }

    public List<Booking> findPeakHourBookings() {
        return filter(Booking::isPeakHourBooking);
    }

    /** Tổng doanh thu của các booking không bị hủy. */
    public double getTotalRevenue() {
        double total = 0;
        for (Booking booking : bookings) {
            if (booking.getStatus() != BookingStatus.CANCELLED) {
                total += booking.getTotalAmount();
            }
        }
        return total;
    }

    public boolean updateStatus(String bookingId, BookingStatus status) {
        Booking booking = findBookingById(bookingId);
        if (booking == null || status == null) {
            return false;
        }
        booking.setStatus(status);
        return true;
    }

    public boolean removeBooking(String bookingId) {
        Booking booking = findBookingById(bookingId);
        return booking != null && bookings.remove(booking);
    }

    public List<Booking> getAllBookings() {
        return Collections.unmodifiableList(new ArrayList<>(bookings));
    }

    public Customer findCustomerOf(String bookingId) {
        Booking booking = findBookingById(bookingId);
        return booking == null ? null : booking.getCustomer();
    }

    public SportField findFieldOf(String bookingId) {
        Booking booking = findBookingById(bookingId);
        return booking == null ? null : booking.getField();
    }

    public Service findServiceOf(String bookingId) {
        Booking booking = findBookingById(bookingId);
        return booking == null ? null : booking.getService();
    }

    private List<Booking> filter(java.util.function.Predicate<Booking> predicate) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : bookings) {
            if (predicate.test(booking)) {
                result.add(booking);
            }
        }
        return result;
    }
}