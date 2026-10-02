package smartsporthub.data;

import smartsporthub.manager.BookingManager;
import smartsporthub.manager.CustomerManager;
import smartsporthub.manager.FieldManager;
import smartsporthub.manager.InvoiceManager;
import smartsporthub.manager.ServiceManager;
import smartsporthub.model.Booking;
import smartsporthub.model.Customer;
import smartsporthub.model.Invoice;
import smartsporthub.model.Service;
import smartsporthub.model.SportField;

import java.util.List;

/**
 * Kho dữ liệu in-memory của SmartSportHub.
 * <p>
 * Gom 5 manager lại thành một đơn vị để API và test cùng truy cập một nguồn dữ liệu duy nhất.
 */
public class SmartSportHubData {

    private final FieldManager fieldManager;
    private final CustomerManager customerManager;
    private final ServiceManager serviceManager;
    private final BookingManager bookingManager;
    private final InvoiceManager invoiceManager;

    public SmartSportHubData() {
        fieldManager = new FieldManager();
        customerManager = new CustomerManager();
        serviceManager = new ServiceManager();
        bookingManager = new BookingManager();
        invoiceManager = new InvoiceManager();
    }

    public FieldManager getFieldManager() {
        return fieldManager;
    }

    public CustomerManager getCustomerManager() {
        return customerManager;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public BookingManager getBookingManager() {
        return bookingManager;
    }

    public InvoiceManager getInvoiceManager() {
        return invoiceManager;
    }

    public List<SportField> getFields() {
        return fieldManager.getAllFields();
    }

    public List<Customer> getCustomers() {
        return customerManager.getAllCustomers();
    }

    public List<Service> getServices() {
        return serviceManager.getAllServices();
    }

    public List<Booking> getBookings() {
        return bookingManager.getAllBookings();
    }

    public List<Invoice> getInvoices() {
        return invoiceManager.getAllInvoices();
    }

    /** Tổng doanh thu của các booking chưa bị hủy. */
    public double getTotalRevenue() {
        return bookingManager.getTotalRevenue();
    }

    /** Thống kê nhanh phục vụ endpoint {@code /api/health}. */
    public String summary() {
        return String.format("customers=%d, fields=%d, bookings=%d, services=%d, invoices=%d",
                getCustomers().size(), getFields().size(), getBookings().size(),
                getServices().size(), getInvoices().size());
    }
}