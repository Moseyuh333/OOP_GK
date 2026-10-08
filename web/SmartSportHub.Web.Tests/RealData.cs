using System.Text.Json;
using SmartSportHub.Web.Dtos;

namespace SmartSportHub.Web.Tests;

/// Tải đúng dữ liệu Java Core xuất ra, không tự chế dataset cho test.
/// File nằm ở data/dataset.json, đi từ thư mục test lên gốc repo.
internal static class RealData
{
    private static readonly JsonSerializerOptions Options = new(JsonSerializerDefaults.Web);

    public static ApiData Load()
    {
        var path = FindRepoFile("data", "dataset.json");
        var json = File.ReadAllText(path);
        var data = JsonSerializer.Deserialize<ApiData>(json, Options)
            ?? throw new InvalidOperationException($"Không đọc được {path}");
        return data;
    }

    private static string FindRepoFile(params string[] parts)
    {
        var dir = new DirectoryInfo(AppContext.BaseDirectory);
        while (dir is not null)
        {
            var candidate = Path.Combine([dir.FullName, .. parts]);
            if (File.Exists(candidate))
            {
                return candidate;
            }

            dir = dir.Parent;
        }

        throw new FileNotFoundException($"Không tìm thấy {Path.Combine(parts)} từ {AppContext.BaseDirectory}");
    }
}