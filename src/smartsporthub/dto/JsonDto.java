package smartsporthub.dto;

import java.util.Map;

/**
 * Mọi DTO đều tự biết cách trở thành map để {@code JsonWriter} ghi ra JSON.
 * <p>
 * Nhờ interface này tầng DTO không cần {@code instanceof} khi gom danh sách,
 * và thêm DTO mới chỉ cần implement là dùng được.
 */
public interface JsonDto {

    /** Trả về map với thứ tự key cố định, giá trị chỉ gồm kiểu JSON cơ bản. */
    Map<String, Object> toMap();
}