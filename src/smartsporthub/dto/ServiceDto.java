package smartsporthub.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import smartsporthub.model.Service;

/** DTO phẳng cho dịch vụ bổ sung. */
public final class ServiceDto implements JsonDto {

    private final String id;
    private final String name;
    private final String category;
    private final double unitPrice;
    private final boolean active;

    public ServiceDto(Service service) {
        id = service.getServiceId();
        name = service.getServiceName();
        category = service.getCategory();
        unitPrice = service.getUnitPrice();
        active = service.isActive();
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("category", category);
        map.put("unitPrice", unitPrice);
        map.put("active", active);
        return map;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public boolean isActive() {
        return active;
    }
}