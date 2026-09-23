package smartsporthub.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import smartsporthub.model.FieldStatus;
import smartsporthub.model.SportField;

/** Quản lý trực tiếp danh sách các sân trong Field Domain. */
public class FieldManager {

    private final List<SportField> fields;

    public FieldManager() {
        fields = new ArrayList<>();
    }

    public boolean addField(SportField field) {
        if (field == null || findFieldById(field.getFieldId()) != null) {
            return false;
        }
        return fields.add(field);
    }

    public SportField findFieldById(String fieldId) {
        if (fieldId == null) {
            return null;
        }
        String normalizedId = fieldId.trim();
        for (SportField field : fields) {
            if (field.getFieldId().equals(normalizedId)) {
                return field;
            }
        }
        return null;
    }

    public List<SportField> findAvailableFields() {
        List<SportField> result = new ArrayList<>();
        for (SportField field : fields) {
            if (field.getStatus() == FieldStatus.AVAILABLE) {
                result.add(field);
            }
        }
        return result;
    }

    public boolean updateFieldStatus(String fieldId, FieldStatus newStatus) {
        SportField field = findFieldById(fieldId);
        if (field == null || newStatus == null) {
            return false;
        }
        field.setStatus(newStatus);
        return true;
    }

    public void displayAllFields() {
        for (SportField field : fields) {
            System.out.println(field.displayInfo());
        }
    }

    public boolean removeField(String fieldId) {
        SportField field = findFieldById(fieldId);
        return field != null && fields.remove(field);
    }

    public List<SportField> searchByType(Class<? extends SportField> type) {
        if (type == null) {
            return new ArrayList<>();
        }
        List<SportField> result = new ArrayList<>();
        for (SportField field : fields) {
            if (type.isInstance(field)) {
                result.add(field);
            }
        }
        return result;
    }

    /** Trả về bản sao để bên ngoài không thể sửa danh sách nội bộ trực tiếp. */
    public List<SportField> getAllFields() {
        return Collections.unmodifiableList(new ArrayList<>(fields));
    }
}
