using SmartSportHub.Web.Services;

var builder = WebApplication.CreateBuilder(args);
builder.Logging.ClearProviders();
builder.Logging.AddConsole();
builder.Services.AddControllersWithViews();
builder.Services.AddHttpClient<SmartSportHubApiClient>(client =>
{
    var baseUrl = builder.Configuration["SmartSportHubApi:BaseUrl"]
        ?? throw new InvalidOperationException("SmartSportHubApi:BaseUrl is required.");
    client.BaseAddress = new Uri(baseUrl);
    client.Timeout = TimeSpan.FromSeconds(10);
});
builder.Services.AddScoped<SmartSportHubQueryService>();

var app = builder.Build();
app.UseStaticFiles();
app.UseRouting();
app.MapControllerRoute("default", "{controller=Home}/{action=Index}/{id?}");
app.Run();
