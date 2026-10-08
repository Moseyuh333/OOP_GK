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
