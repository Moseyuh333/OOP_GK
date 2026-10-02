package smartsporthub.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import smartsporthub.model.Service;

/** Quản lý danh sách dịch vụ bổ sung. */
public class ServiceManager {

    private final List<Service> services;

    public ServiceManager() {
        services = new ArrayList<>();
    }

    public boolean addService(Service service) {
        if (service == null || findServiceById(service.getServiceId()) != null) {
            return false;
        }
        return services.add(service);
    }

    public Service findServiceById(String serviceId) {
        if (serviceId == null) {
            return null;
        }
        String normalizedId = serviceId.trim();
        for (Service service : services) {
            if (service.getServiceId().equals(normalizedId)) {
                return service;
            }
        }
        return null;
    }

    public List<Service> findActiveServices() {
        List<Service> result = new ArrayList<>();
        for (Service service : services) {
            if (service.isActive()) {
                result.add(service);
            }
        }
        return result;
    }

    public boolean removeService(String serviceId) {
        Service service = findServiceById(serviceId);
        return service != null && services.remove(service);
    }

    public List<Service> getAllServices() {
        return Collections.unmodifiableList(new ArrayList<>(services));
    }
}