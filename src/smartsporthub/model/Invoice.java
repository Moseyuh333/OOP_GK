package smartsporthub.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import smartsporthub.interfaces.IPeakHourCalculable;

/** Hóa đơn bất biến, giữ chi tiết và số tiền tại thời điểm lập. */
public final class Invoice {

    private final String bookingId;
    private final String customerName;
    private final String customerType;
    private final String fieldName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final List<Booking.ServiceItem> services;
    private final double fieldFee;
    private final double peakHourSurcharge;
    private final double serviceFee;
    private final double subtotal;
    private final double discount;
    private final double total;

    public Invoice(Booking booking) {
        if (booking == null || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Không thể lập hóa đơn cho booking null hoặc đã hủy.");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Dùng booking.getInvoice() để lấy hóa đơn đã thanh toán.");
        }
        bookingId = booking.getBookingId();
        customerName = booking.getCustomer().getFullName();
        customerType = booking.getCustomer().getClass().getSimpleName();
        fieldName = booking.getField().getFieldId() + " - " + booking.getField().getFieldName();
        startTime = booking.getStartTime();
        endTime = booking.getEndTime();
        services = booking.getServices();
        SportField field = booking.getField();
        double hours = booking.getDurationHours();
        if (field instanceof FootballField && booking.isUseNightLight()) {
            fieldFee = ((FootballField) field).calculateRentalFee(hours, true);
        } else {
            fieldFee = field.calculateRentalFee(hours);
        }
        double peakFee = 0;
        if (field instanceof IPeakHourCalculable) {
            IPeakHourCalculable peak = (IPeakHourCalculable) field;
            if (peak.isPeakHour(startTime.toLocalTime())) {
                peakFee = peak.calculatePeakHourSurcharge(hours);
            }
        }
        peakHourSurcharge = peakFee;
        double servicesTotal = 0;
        for (Booking.ServiceItem item : services) {
            servicesTotal += item.getTotal();
        }
        serviceFee = servicesTotal;
        subtotal = fieldFee + peakHourSurcharge + serviceFee;
        requireAmount(fieldFee);
        requireAmount(peakHourSurcharge);
        requireAmount(serviceFee);
        requireAmount(subtotal);
        discount = booking.getCustomer().calculateDiscount(subtotal);
        requireAmount(discount);
        if (discount > subtotal) {
            throw new IllegalArgumentException("Giảm giá không được vượt tạm tính.");
        }
        total = subtotal - discount;
    }

    public String getBookingId() { return bookingId; }
    public double getFieldFee() { return fieldFee; }
    public double getPeakHourSurcharge() { return peakHourSurcharge; }
    public double getServiceFee() { return serviceFee; }
    public double getSubtotal() { return subtotal; }
    public double getDiscount() { return discount; }
    public double getTotal() { return total; }

    public void printInvoice() {
        System.out.println(toString());
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        text.append("========================================\n")
                .append("             SMARTSPORTHUB\n")
                .append("========================================\n")
                .append("Booking ID: ").append(bookingId).append('\n')
                .append("Customer: ").append(customerName).append('\n')
                .append("Customer Type: ").append(customerType).append('\n')
                .append("Sport Field: ").append(fieldName).append('\n')
                .append("Start: ").append(startTime).append('\n')
                .append("End: ").append(endTime).append('\n');
        appendMoney(text, "Tiền sân", fieldFee);
        appendMoney(text, "Phụ phí cao điểm", peakHourSurcharge);
        appendMoney(text, "Dịch vụ", serviceFee);
        for (Booking.ServiceItem item : services) {
            appendMoney(text, "  " + item.getServiceName() + " x " + item.getQuantity(), item.getTotal());
        }
        appendMoney(text, "Tạm tính", subtotal);
        appendMoney(text, "Giảm giá", discount);
        text.append("----------------------------------------\n");
        appendMoney(text, "TỔNG THANH TOÁN", total);
        return text.append("========================================").toString();
    }

    private static void appendMoney(StringBuilder text, String label, double amount) {
        text.append(String.format(Locale.ROOT, "%s: %,.2f VND%n", label, amount));
    }

    private static void requireAmount(double amount) {
        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Số tiền hóa đơn phải hữu hạn và không âm.");
        }
    }
}
