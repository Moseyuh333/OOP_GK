using System.Text.Json;
using Microsoft.AspNetCore.Mvc;
using SmartSportHub.Web.Dtos;
using SmartSportHub.Web.Services;
using SmartSportHub.Web.ViewModels;

namespace SmartSportHub.Web.Controllers;

public sealed class HomeController(
    SmartSportHubApiClient api,
    SmartSportHubQueryService queries,
    ILogger<HomeController> logger) : Controller
{
    [HttpGet("/")]
    public Task<IActionResult> Index(CancellationToken cancellationToken) =>
        Render(d => View(queries.Dashboard(d)), cancellationToken);

    [HttpGet("/fields")]
    public Task<IActionResult> Fields(string? type, string? status, int page = 1,
        CancellationToken cancellationToken = default) =>
        Render(d => View(queries.Fields(d, type, status, page)), cancellationToken);

    [HttpGet("/customers")]
    public Task<IActionResult> Customers(CancellationToken cancellationToken) =>
        Render(d => View(queries.Customers(d)), cancellationToken);

    [HttpGet("/bookings")]
    public Task<IActionResult> Bookings(string? status, string? fieldType,
        string? customerId, DateOnly? date, int page = 1,
        CancellationToken cancellationToken = default) =>
        Render(d => View(queries.Bookings(d, status, fieldType, customerId, date, page)), cancellationToken);

    [HttpGet("/invoices")]
    public Task<IActionResult> Invoices(string? status, string? customerId, DateOnly? date,
        int page = 1, CancellationToken cancellationToken = default) =>
        Render(d => View(queries.Invoices(d, status, customerId, date, page, 10)),
            cancellationToken);

    [HttpGet("/services")]
    public Task<IActionResult> Services(CancellationToken cancellationToken) =>
        Render(d => View(queries.Services(d)), cancellationToken);

    [HttpGet("/reports")]
    public Task<IActionResult> Reports(CancellationToken cancellationToken) =>
        Render(d => View(queries.Reports(d)), cancellationToken);

    [HttpGet("/queries")]
        public Task<IActionResult> Queries(int id = 1, CancellationToken cancellationToken = default) =>
            Render(d =>
            {
                var all = queries.RunAll(d);
                return View(new QueriesModel(all, all[Math.Clamp(id, 1, 20) - 1]));
            }, cancellationToken);

    // ---- Thao tác ghi: mọi thay đổi đi qua Java Core API ----

    [HttpPost("/bookings/create")]
    public async Task<IActionResult> CreateBooking(string customerId, string fieldId,
        string? serviceId, DateTime startTime, DateTime endTime,
        CancellationToken cancellationToken = default)
    {
        try
        {
            await api.CreateBookingAsync(customerId, fieldId, startTime, endTime, serviceId, cancellationToken);
            SetFlash($"Đã đặt sân {fieldId} cho {startTime:dd/MM/yyyy HH:mm}.", isError: false);
        }
        catch (HttpRequestException error)
        {
            SetFlash(error.Message);
        }
        return RedirectToAction(nameof(Bookings), new { customerId, date = DateOnly.FromDateTime(startTime) });
    }

    [HttpPost("/bookings/{id}/cancel")]
    public async Task<IActionResult> CancelBooking(string id, CancellationToken cancellationToken = default)
    {
        try
        {
            await api.CancelBookingAsync(id, cancellationToken);
            SetFlash($"Đã hủy booking {id}.", isError: false);
        }
        catch (HttpRequestException error)
        {
            SetFlash(error.Message);
        }
        return RedirectToAction(nameof(Bookings));
    }

    [HttpPost("/bookings/{id}/complete")]
    public async Task<IActionResult> CompleteBooking(string id, CancellationToken cancellationToken = default)
    {
        try
        {
            await api.CompleteBookingAsync(id, cancellationToken);
            SetFlash($"Đã hoàn thành booking {id}.", isError: false);
        }
        catch (HttpRequestException error)
        {
            SetFlash(error.Message);
        }
        return RedirectToAction(nameof(Bookings));
    }

    [HttpPost("/bookings/{id}/reschedule")]
    public async Task<IActionResult> RescheduleBooking(string id, DateTime newStartTime, DateTime newEndTime,
        CancellationToken cancellationToken = default)
    {
        try
        {
            await api.RescheduleBookingAsync(id, newStartTime, newEndTime, cancellationToken);
            SetFlash($"Đã đổi lịch booking {id} sang {newStartTime:dd/MM/yyyy HH:mm}.", isError: false);
        }
        catch (HttpRequestException error)
        {
            SetFlash(error.Message);
        }
        return RedirectToAction(nameof(Bookings));
    }

    [HttpPost("/invoices/{id}/pay")]
    public async Task<IActionResult> PayInvoice(string id, CancellationToken cancellationToken = default)
    {
        try
        {
            await api.PayInvoiceAsync(id, cancellationToken);
            SetFlash($"Đã thu tiền hóa đơn {id}.", isError: false);
        }
        catch (HttpRequestException error)
        {
            SetFlash(error.Message);
        }
        return RedirectToAction(nameof(Invoices));
    }

    [HttpPost("/invoices/{id}/refund")]
    public async Task<IActionResult> RefundInvoice(string id, CancellationToken cancellationToken = default)
    {
        try
        {
            await api.RefundInvoiceAsync(id, cancellationToken);
            SetFlash($"Đã hoàn tiền hóa đơn {id}.", isError: false);
        }
        catch (HttpRequestException error)
        {
            SetFlash(error.Message);
        }
        return RedirectToAction(nameof(Invoices));
    }

    [HttpPost("/fields/{id}/status")]
    public async Task<IActionResult> SetFieldStatus(string id, string status,
        CancellationToken cancellationToken = default)
    {
        try
        {
            await api.SetFieldStatusAsync(id, status, cancellationToken);
            SetFlash($"Sân {id} chuyển sang {status}.", isError: false);
        }
        catch (HttpRequestException error)
        {
            SetFlash(error.Message);
        }
        return RedirectToAction(nameof(Fields));
    }

    private void SetFlash(string message, bool isError = true)
    {
        TempData[isError ? "FlashError" : "FlashOk"] = message;
    }


    private async Task<IActionResult> Render(Func<ApiData, IActionResult> build,
        CancellationToken cancellationToken)
    {
        try
        {
            return build(await api.GetAllDataAsync(cancellationToken));
        }
        catch (Exception error) when (error is HttpRequestException or JsonException or TaskCanceledException)
        {
            logger.LogError(error, "Could not load SmartSportHub Core API data");
            Response.StatusCode = 503;
            return View("Unavailable");
        }
    }
}
