package smartsporthub.dto;

/**
 * Ánh xạ từ class sân sang mã loại sân dùng trong JSON contract.
 * <p>
 * Tách riêng ở tầng DTO để không phải sửa {@code SportField} đã hoàn thành từ Part A.
 */
public enum FieldType {
    FOOTBALL,
    BADMINTON,
    TENNIS,
    PICKLEBALL;

    /** Xác định loại sân từ instance thật. */
    public static FieldType of(smartsporthub.model.SportField field) {
        if (field instanceof smartsporthub.model.FootballField) {
            return FOOTBALL;
        }
        if (field instanceof smartsporthub.model.BadmintonField) {
            return BADMINTON;
        }
        if (field instanceof smartsporthub.model.TennisField) {
            return TENNIS;
        }
        if (field instanceof smartsporthub.model.PickleballField) {
            return PICKLEBALL;
        }
        throw new IllegalArgumentException("Không nhận diện được loại sân: " + field.getClass().getName());
    }
}