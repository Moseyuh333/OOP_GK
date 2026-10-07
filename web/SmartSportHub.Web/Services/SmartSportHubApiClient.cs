using System.Net.Http.Json;
using System.Text.Json;
using SmartSportHub.Web.Dtos;

namespace SmartSportHub.Web.Services;

public sealed class SmartSportHubApiClient(HttpClient http)
{
    private static readonly JsonSerializerOptions JsonOptions = new(JsonSerializerDefaults.Web);

    public Task<ApiData> GetAllDataAsync(CancellationToken cancellationToken = default) =>
        GetAsync<ApiData>("api/data", cancellationToken);

    public Task<List<CustomerDto>> GetCustomersAsync(CancellationToken cancellationToken = default) =>
        GetAsync<List<CustomerDto>>("api/customers", cancellationToken);

    public Task<List<FieldDto>> GetFieldsAsync(CancellationToken cancellationToken = default) =>
        GetAsync<List<FieldDto>>("api/fields", cancellationToken);

    public Task<List<ServiceDto>> GetServicesAsync(CancellationToken cancellationToken = default) =>
        GetAsync<List<ServiceDto>>("api/services", cancellationToken);

    public Task<List<BookingDto>> GetBookingsAsync(CancellationToken cancellationToken = default) =>
        GetAsync<List<BookingDto>>("api/bookings", cancellationToken);

    public Task<List<InvoiceDto>> GetInvoicesAsync(CancellationToken cancellationToken = default) =>
        GetAsync<List<InvoiceDto>>("api/invoices", cancellationToken);

    private async Task<T> GetAsync<T>(string path, CancellationToken cancellationToken)
    {
        using var response = await http.GetAsync(path, cancellationToken);
        response.EnsureSuccessStatusCode();
        return await response.Content.ReadFromJsonAsync<T>(JsonOptions, cancellationToken)
            ?? throw new JsonException($"Empty response from {path}.");
    }
}
