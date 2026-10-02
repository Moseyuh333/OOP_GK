package smartsporthub.json;

import java.util.List;
import java.util.Map;

/**
 * Bộ ghi JSON tự viết, không phụ thuộc thư viện ngoài.
 * <p>
 * Hỗ trợ đúng các kiểu mà SmartSportHub cần: {@link Map} (object),
 * {@link List} (array), {@link String}, {@link Number}, {@link Boolean},
 * {@link Enum} (ghi theo tên) và {@code null}.
 */
public final class JsonWriter {

    private static final String INDENT = "  ";

    private JsonWriter() {
        // Utility class
    }

    /** Chuyển đối tượng bất kỳ thành chuỗi JSON định dạng đẹp. */
    public static String toJson(Object value) {
        StringBuilder builder = new StringBuilder();
        writeValue(builder, value, 0);
        return builder.toString();
    }

    /** Chuyển danh sách object thành mảng JSON. */
    public static String toJsonArray(List<?> values) {
        return toJson(values);
    }

    private static void writeValue(StringBuilder builder, Object value, int depth) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof String text) {
            writeString(builder, text);
        } else if (value instanceof Boolean flag) {
            builder.append(flag.booleanValue());
        } else if (value instanceof Number number) {
            writeNumber(builder, number.doubleValue());
        } else if (value instanceof Enum<?> type) {
            writeString(builder, type.name());
        } else if (value instanceof Map<?, ?> map) {
            writeObject(builder, map, depth);
        } else if (value instanceof Iterable<?> list) {
            writeArray(builder, list, depth);
        } else {
            // Fallback: đối tượng phức tạp được ghi bằng toString() dạng chuỗi.
            writeString(builder, String.valueOf(value));
        }
    }

    private static void writeObject(StringBuilder builder, Map<?, ?> map, int depth) {
        if (map.isEmpty()) {
            builder.append("{}");
            return;
        }
        builder.append("{\n");
        int remaining = map.size();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            appendIndent(builder, depth + 1);
            writeString(builder, String.valueOf(entry.getKey()));
            builder.append(": ");
            writeValue(builder, entry.getValue(), depth + 1);
            if (--remaining > 0) {
                builder.append(',');
            }
            builder.append('\n');
        }
        appendIndent(builder, depth);
        builder.append('}');
    }

    private static void writeArray(StringBuilder builder, Iterable<?> list, int depth) {
        builder.append('[');
        boolean first = true;
        for (Object item : list) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('\n');
            appendIndent(builder, depth + 1);
            writeValue(builder, item, depth + 1);
        }
        if (!first) {
            builder.append('\n');
            appendIndent(builder, depth);
        }
        builder.append(']');
    }

    private static void appendIndent(StringBuilder builder, int depth) {
        builder.append(INDENT.repeat(Math.max(0, depth)));
    }

    private static void writeNumber(StringBuilder builder, double number) {
        if (Double.isNaN(number) || Double.isInfinite(number)) {
            builder.append('0');
        } else if (number == Math.floor(number) && Math.abs(number) < 1e15) {
            builder.append((long) number);
        } else {
            builder.append(number);
        }
    }

    /** Escape ký tự đặc biệt theo RFC 8259 để JSON luôn hợp lệ. */
    private static void writeString(StringBuilder builder, String text) {
        builder.append('"');
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                default -> {
                    if (ch < 0x20) {
                        builder.append(String.format("\\u%04x", (int) ch));
                    } else {
                        builder.append(ch);
                    }
                }
            }
        }
        builder.append('"');
    }
}