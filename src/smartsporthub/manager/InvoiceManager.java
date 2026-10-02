package smartsporthub.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import smartsporthub.model.Invoice;
import smartsporthub.model.InvoiceStatus;

/** Quản lý danh sách hóa đơn. */
public class InvoiceManager {

    private final List<Invoice> invoices;

    public InvoiceManager() {
        invoices = new ArrayList<>();
    }

    public boolean addInvoice(Invoice invoice) {
        if (invoice == null || findInvoiceById(invoice.getInvoiceId()) != null) {
            return false;
        }
        return invoices.add(invoice);
    }

    public Invoice findInvoiceById(String invoiceId) {
        if (invoiceId == null) {
            return null;
        }
        String normalizedId = invoiceId.trim();
        for (Invoice invoice : invoices) {
            if (invoice.getInvoiceId().equals(normalizedId)) {
                return invoice;
            }
        }
        return null;
    }

    public List<Invoice> findByStatus(InvoiceStatus status) {
        return filter(i -> i.getStatus() == status);
    }

    public List<Invoice> findByCustomer(String customerId) {
        return filter(i -> i.getCustomerId().equals(customerId));
    }

    /** Tổng tiền của các hóa đơn đã thanh toán. */
    public double getPaidTotal() {
        double total = 0;
        for (Invoice invoice : invoices) {
            if (invoice.getStatus() == InvoiceStatus.PAID) {
                total += invoice.getAmount();
            }
        }
        return total;
    }

    public boolean removeInvoice(String invoiceId) {
        Invoice invoice = findInvoiceById(invoiceId);
        return invoice != null && invoices.remove(invoice);
    }

    public List<Invoice> getAllInvoices() {
        return Collections.unmodifiableList(new ArrayList<>(invoices));
    }

    private List<Invoice> filter(java.util.function.Predicate<Invoice> predicate) {
        List<Invoice> result = new ArrayList<>();
        for (Invoice invoice : invoices) {
            if (predicate.test(invoice)) {
                result.add(invoice);
            }
        }
        return result;
    }
}