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

    public Task<OverviewDto> GetOverviewAsync(CancellationToken cancellationToken = default) =>
        GetAsync<OverviewDto>("api/overview", cancellationToken);

    public Task<BookingDto> CreateBookingAsync(string customerId, string fieldId,
        DateTime startTime, DateTime endTime, string? serviceId = null,
        CancellationToken cancellationToken = default)
    {
        var body = new
        {
            customerId,
            fieldId,
            startTime = startTime.ToString("yyyy-MM-ddTHH:mm"),
            endTime = endTime.ToString("yyyy-MM-ddTHH:mm"),
            serviceId
        };
        return PostAsync<object, BookingDto>("api/bookings", body, cancellationToken);
    }

    public Task<BookingDto> CancelBookingAsync(string bookingId, CancellationToken cancellationToken = default) =>
        PostAsync<object, BookingDto>($"api/bookings/{bookingId}/cancel", new { }, cancellationToken);

    public Task<BookingDto> CompleteBookingAsync(string bookingId, CancellationToken cancellationToken = default) =>
        PostAsync<object, BookingDto>($"api/bookings/{bookingId}/complete", new { }, cancellationToken);

    public Task<BookingDto> RescheduleBookingAsync(string bookingId, DateTime newStartTime, DateTime newEndTime,
        CancellationToken cancellationToken = default)
    {
        var body = new
        {
            newStartTime = newStartTime.ToString("yyyy-MM-ddTHH:mm"),
            newEndTime = newEndTime.ToString("yyyy-MM-ddTHH:mm")
        };
        return PostAsync<object, BookingDto>($"api/bookings/{bookingId}/reschedule", body, cancellationToken);
    }

    public Task<InvoiceDto> PayInvoiceAsync(string invoiceId, CancellationToken cancellationToken = default) =>
        PostAsync<object, InvoiceDto>($"api/invoices/{invoiceId}/pay", new { }, cancellationToken);

    public Task<InvoiceDto> RefundInvoiceAsync(string invoiceId, CancellationToken cancellationToken = default) =>
        PostAsync<object, InvoiceDto>($"api/invoices/{invoiceId}/refund", new { }, cancellationToken);

    public Task<FieldDto> SetFieldStatusAsync(string fieldId, string status, CancellationToken cancellationToken = default)
    {
        var body = new { status };
        return PatchAsync<object, FieldDto>($"api/fields/{fieldId}/status", body, cancellationToken);
    }

    private async Task<T> GetAsync<T>(string path, CancellationToken cancellationToken)
    {
        using var response = await http.GetAsync(path, cancellationToken);
        if (!response.IsSuccessStatusCode)
        {
            var err = await response.Content.ReadAsStringAsync(cancellationToken);
            throw new HttpRequestException($"{(int)response.StatusCode} {response.ReasonPhrase}: {err}");
        }
        return await response.Content.ReadFromJsonAsync<T>(JsonOptions, cancellationToken)
            ?? throw new JsonException($"Empty response from {path}.");
    }

    private async Task<TRes> PostAsync<TReq, TRes>(string path, TReq body, CancellationToken cancellationToken)
    {
        using var response = await http.PostAsJsonAsync(path, body, JsonOptions, cancellationToken);
        if (!response.IsSuccessStatusCode)
        {
            var err = await response.Content.ReadAsStringAsync(cancellationToken);
            throw new HttpRequestException($"{(int)response.StatusCode} {response.ReasonPhrase}: {err}");
        }
        return await response.Content.ReadFromJsonAsync<TRes>(JsonOptions, cancellationToken)
            ?? throw new JsonException($"Empty response from {path}.");
    }

    private async Task<TRes> PatchAsync<TReq, TRes>(string path, TReq body, CancellationToken cancellationToken)
    {
        using var request = new HttpRequestMessage(HttpMethod.Patch, path)
        {
            Content = JsonContent.Create(body, options: JsonOptions)
        };
        using var response = await http.SendAsync(request, cancellationToken);
        if (!response.IsSuccessStatusCode)
        {
            var err = await response.Content.ReadAsStringAsync(cancellationToken);
            throw new HttpRequestException($"{(int)response.StatusCode} {response.ReasonPhrase}: {err}");
        }
        return await response.Content.ReadFromJsonAsync<TRes>(JsonOptions, cancellationToken)
            ?? throw new JsonException($"Empty response from {path}.");
    }
}
