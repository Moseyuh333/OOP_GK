package smartsporthub.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import smartsporthub.model.Customer;
import smartsporthub.model.CustomerType;

/** Quản lý danh sách khách hàng. */
public class CustomerManager {

    private final List<Customer> customers;

    public CustomerManager() {
        customers = new ArrayList<>();
    }

    public boolean addCustomer(Customer customer) {
        if (customer == null || findCustomerById(customer.getCustomerId()) != null) {
            return false;
        }
        return customers.add(customer);
    }

    public Customer findCustomerById(String customerId) {
        if (customerId == null) {
            return null;
        }
        String normalizedId = customerId.trim();
        for (Customer customer : customers) {
            if (customer.getCustomerId().equals(normalizedId)) {
                return customer;
            }
        }
        return null;
    }

    public List<Customer> findByType(CustomerType type) {
        List<Customer> result = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.getCustomerType() == type) {
                result.add(customer);
            }
        }
        return result;
    }

    public List<Customer> searchByName(String keyword) {
        List<Customer> result = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return result;
        }
        String normalized = keyword.trim().toLowerCase();
        for (Customer customer : customers) {
            if (customer.getFullName().toLowerCase().contains(normalized)) {
                result.add(customer);
            }
        }
        return result;
    }

    public boolean removeCustomer(String customerId) {
        Customer customer = findCustomerById(customerId);
        return customer != null && customers.remove(customer);
    }

    public List<Customer> getAllCustomers() {
        return Collections.unmodifiableList(new ArrayList<>(customers));
    }
}