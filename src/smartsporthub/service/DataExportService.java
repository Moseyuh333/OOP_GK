package smartsporthub.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import smartsporthub.data.SmartSportHubData;
import smartsporthub.dto.DtoMapper;
import smartsporthub.dto.JsonDto;
import smartsporthub.json.JsonWriter;

/**
 * Chuyển kho dữ liệu in-memory thành JSON.
 * <p>
 * Tách riêng khỏi tầng API để dùng được cả khi ghi file cho Dev B
 * mà không cần bật server.
 */
public final class DataExportService {

    /** Đường dẫn file xuất mặc định, tương đối so với thư mục chạy chương trình. */
    public static final String DEFAULT_EXPORT_PATH = "data/dataset.json";

    private DataExportService() {
        // Utility class
    }

    /** Toàn bộ dữ liệu thành một JSON object gồm 5 mảng. */
    public static String toSnapshotJson(SmartSportHubData data) {
        Map<String, Object> snapshot = DtoMapper.snapshot(
                data.getCustomers(),
                data.getFields(),
                data.getServices(),
                data.getBookings(),
                data.getInvoices());
        return JsonWriter.toJson(snapshot);
    }

    /** Một danh sách DTO bất kỳ thành mảng JSON. */
    public static String toArrayJson(List<? extends JsonDto> dtos) {
        return JsonWriter.toJsonArray(DtoMapper.toMaps(dtos));
    }

    /**
     * Ghi dữ liệu ra file UTF-8 để Dev B có thể nạp trực tiếp nếu không bật API.
     *
     * @return đường dẫn file đã ghi
     */
    public static Path writeSnapshotToFile(SmartSportHubData data, String targetPath) throws IOException {
        Path file = Path.of(targetPath);
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        Files.writeString(file, toSnapshotJson(data), StandardCharsets.UTF_8);
        return file.toAbsolutePath();
    }
}