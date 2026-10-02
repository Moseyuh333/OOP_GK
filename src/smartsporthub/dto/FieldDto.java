package smartsporthub.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import smartsporthub.model.SportField;

/** DTO phẳng cho sân thể thao. */
public final class FieldDto implements JsonDto {

    private final String id;
    private final String name;
    private final String type;
    private final String status;
    private final double basePricePerHour;

    public FieldDto(SportField field) {
        id = field.getFieldId();
        name = field.getFieldName();
        type = FieldType.of(field).name();
        status = field.getStatus().name();
        basePricePerHour = field.getBasePricePerHour();
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("type", type);
        map.put("status", status);
        map.put("basePricePerHour", basePricePerHour);
        return map;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public double getBasePricePerHour() {
        return basePricePerHour;
    }
}