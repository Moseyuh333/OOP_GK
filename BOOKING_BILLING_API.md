# SmartSportHub — Booking/Billing (Developer B)

Phần B dùng trực tiếp Field Domain hiện có. Không có dependency, build tool hoặc cấu hình mới.

## File được thêm

```text
src/smartsporthub/
├── interfaces/
│   └── IPromotional.java
├── model/
│   ├── Customer.java
│   ├── StandardCustomer.java
│   ├── VipCustomer.java
│   ├── Service.java
│   ├── Booking.java          # Có nested class Booking.ServiceItem
│   ├── BookingStatus.java
│   └── Invoice.java
└── manager/
    ├── BookingManager.java
    └── ReportManager.java
test/smartsporthub/test/
└── BookingBillingTest.java
BOOKING_BILLING_API.md
```

Không chỉnh sửa file cũ, `Main.java`, `PlanB.md`, `RuleB.md` hoặc các file Developer A.

## Thiết kế OOP

- `Customer` là abstract class, implements `IPromotional`.
- `StandardCustomer` và `VipCustomer` kế thừa `Customer`, override `calculateDiscount(amount)`.
- Invoice gọi `customer.calculateDiscount(subtotal)` để lấy **số tiền giảm**, không phân nhánh theo loại khách hàng.
- Mọi thuộc tính đều private. Customer/Service có setter với validation; ID bất biến.
- `Booking` và `Invoice` có getter, không có setter thay lịch/trạng thái trực tiếp.
- `Booking.ServiceItem` giữ mã, tên, đơn giá và số lượng dịch vụ; đơn giá được lưu tại thời điểm tạo dòng dịch vụ.

## Quy ước nghiệp vụ

1. Khách thường giảm 0%; VIP mặc định giảm 10%, có thể đặt tỷ lệ trong `[0, 1]`.
2. Giảm giá áp dụng trên toàn bộ tạm tính: tiền sân + phụ phí cao điểm + dịch vụ.
3. Số điện thoại gồm 9–15 chữ số, tùy chọn dấu `+` ở đầu.
4. Dùng `LocalDateTime` theo giờ địa phương; hỗ trợ giờ lẻ và booking qua ngày. Giờ kết thúc phải sau giờ bắt đầu.
5. Phụ phí cao điểm dựa vào **giờ bắt đầu**, áp dụng cho toàn bộ thời lượng theo contract của A. Không chia nhỏ booking theo từng đoạn giờ cao điểm.
6. Sân phải là chính object đã đăng ký trong `FieldManager` và có trạng thái `AVAILABLE`.
7. Đặt lịch không tự chuyển sân sang `OCCUPIED`. Trạng thái sân phản ánh khả năng sử dụng; lịch được kiểm tra riêng trong `BookingManager` để hai lượt nối tiếp vẫn đặt được.
8. Hai khoảng `[start, end)` trùng khi `start < existingEnd && existingStart < end`. Booking `CANCELLED` được bỏ qua; các trạng thái còn lại vẫn giữ lịch, kể cả lịch sử `COMPLETED`.
9. Constructor tạo booking `PENDING`. `createBooking()` lưu một phiên bản `CONFIRMED`. Bản nháp chưa đăng ký không xuất hiện trong báo cáo.
10. Chỉ hủy/đổi booking đang hoạt động trước giờ bắt đầu; lịch mới cũng phải bắt đầu sau `currentTime`.
11. `completeBooking()` chỉ thành công khi booking `CONFIRMED` đã đến giờ kết thúc. Trong bài này hoàn tất đồng nghĩa thanh toán, không có cổng thanh toán hay trạng thái thanh toán riêng.
12. Báo giá tính theo giá sân/chính sách khách hàng hiện tại. Khi hoàn tất, hóa đơn lưu cả thông tin hiển thị lẫn số tiền; cập nhật giá sân hoặc VIP sau đó không làm đổi hóa đơn/doanh thu cũ.
13. Top 3 đếm booking chưa hủy theo loại sân; sắp số lượt giảm dần, hòa thì tên loại tăng dần. Nếu có ít hơn 3 loại, trả đúng số loại có booking.
14. Tiền dùng `double` để tương thích API của A; hiển thị 2 chữ số thập phân, không làm tròn trung gian. Giá trị không hữu hạn hoặc giảm giá vượt tạm tính bị từ chối.
15. Dữ liệu lưu trong bộ nhớ; không lưu lại sau khi kết thúc chương trình.

## API public cho Main

```java
Customer standard = new StandardCustomer(id, fullName, phoneNumber);
VipCustomer vip = new VipCustomer(id, fullName, phoneNumber); // mặc định 10%
VipCustomer customVip = new VipCustomer(id, fullName, phoneNumber, 0.15);
Service water = new Service(serviceId, serviceName, price);
Booking.ServiceItem item = new Booking.ServiceItem(water, quantity);

Booking draft = new Booking(bookingId, customer, field, start, end);
// Hoặc kèm dịch vụ và tùy chọn đèn (đèn chỉ dành cho FootballField):
Booking draftWithServices = new Booking(bookingId, customer, field, start, end,
        services, useNightLight);

BookingManager bookings = new BookingManager(fields);
bookings.createBooking(draft);                                  // boolean
bookings.findBookingById(bookingId);                             // Booking hoặc null
bookings.getAllBookings();                                      // bản sao danh sách unmodifiable
bookings.isTimeSlotAvailable(fieldId, start, end);                // boolean
bookings.cancelBooking(bookingId, currentTime);                  // boolean
bookings.changeBooking(bookingId, newFieldId, start, end, currentTime);
bookings.completeBooking(bookingId, currentTime);                 // boolean
bookings.calculateBookingTotal(bookingId);                       // double
bookings.displayBooking(bookingId);                              // in ra console, trả boolean
bookings.getLastMessage();                                       // thông báo thao tác gần nhất

Invoice invoice = bookings.findBookingById(bookingId).getInvoice();
invoice.getFieldFee();
invoice.getPeakHourSurcharge();
invoice.getServiceFee();
invoice.getSubtotal();
invoice.getDiscount();
invoice.getTotal();
invoice.printInvoice();

ReportManager reports = new ReportManager(fields, bookings);
reports.calculateTotalRevenue();                                // chỉ COMPLETED
reports.getTop3FieldTypes();                                     // Map<String, Integer> có thứ tự
reports.findAvailableFields(start, end);                         // List<SportField>
```

Các overload hủy/đổi/hoàn tất không có `currentTime` dùng `LocalDateTime.now()`.
Dùng cùng một `FieldManager` khi tạo `BookingManager` và `ReportManager`.

Các lỗi nghiệp vụ thường gặp (không tìm thấy, trùng ID/lịch, sân bảo trì, thao tác sai trạng thái/thời điểm) trả `false` và cập nhật `getLastMessage()`.
Tham số không hợp lệ dùng `IllegalArgumentException`. Main nên kiểm tra kết quả tìm kiếm trước khi truy cập; `calculateBookingTotal()` báo lỗi nếu ID không tồn tại, `getInvoice()` từ chối booking đã hủy bằng `IllegalStateException`.

Các phương thức `Booking.confirm/cancel/reschedule/complete` trả về **object mới** và không thay đổi dữ liệu trong manager. Main thực hiện thao tác qua `BookingManager` để kiểm tra nghiệp vụ và lưu kết quả. Sau thao tác, lấy lại booking bằng ID; reference cũ vẫn là phiên bản cũ.
Với booking đã hoàn tất, gọi `getInvoice()` để lấy hóa đơn đã chốt; không tạo `new Invoice(completedBooking)`.

## Đoạn demo để Developer A tích hợp vào Main

Đây là method riêng, không cần chỉnh sửa Main khi phát triển phần B. Thêm các import tương ứng từ `java.time`, `java.util`, `smartsporthub.model` và `smartsporthub.manager` khi tích hợp.

```java
static void demoBookingScenarios(FieldManager fields) {
    SportField field = fields.findFieldById("F001");
    if (field == null) {
        System.out.println("Cần đăng ký sân F001 trước khi demo.");
        return;
    }
    BookingManager bookings = new BookingManager(fields);
    ReportManager reports = new ReportManager(fields, bookings);
    Customer customer = new VipCustomer("V001", "Nguyễn An", "0901234567");
    Service water = new Service("W001", "Nước uống", 10_000);
    LocalDateTime start = LocalDateTime.of(2030, 1, 15, 18, 0);
    LocalDateTime end = start.plusHours(2);
    Booking draft = new Booking("BK001", customer, field, start, end,
            Arrays.asList(new Booking.ServiceItem(water, 2)), false);

    if (!bookings.createBooking(draft)) {
        System.out.println(bookings.getLastMessage());
        return;
    }
    // Truyền thời gian giả lập rõ ràng để demo không phụ thuộc đồng hồ máy.
    if (bookings.completeBooking("BK001", end)) {
        bookings.displayBooking("BK001");
        System.out.println("Doanh thu: " + reports.calculateTotalRevenue());
        System.out.println("Top 3: " + reports.getTop3FieldTypes());
        System.out.println("Sân trống sau lượt chơi: "
                + reports.findAvailableFields(end, end.plusHours(2)));
    } else {
        System.out.println(bookings.getLastMessage());
    }
}
```

## DEPENDENCY FROM DEVELOPER A

| API | Cách dùng trong phần B |
|---|---|
| `SportField.getFieldId/getFieldName/getStatus()` | Xác định sân, hiển thị hóa đơn, kiểm tra khả năng đặt |
| `SportField.calculateRentalFee(double hours)` | Tính tiền sân bằng đa hình, gồm phụ phí riêng của từng loại |
| `FootballField.calculateRentalFee(hours, useNightLight)` | Tính tiền sân bóng khi dùng đèn; không cộng lại phí đèn dưới dạng Service |
| `IPeakHourCalculable.isPeakHour(LocalTime)` | Kiểm tra giờ bắt đầu theo contract A |
| `IPeakHourCalculable.calculatePeakHourSurcharge(hours)` | Lấy phụ phí cao điểm, không sao chép công thức từ A |
| `FieldStatus.AVAILABLE/MAINTENANCE/OCCUPIED` | Chỉ AVAILABLE được tạo/đổi booking |
| `FieldManager.findFieldById(id)` | Lấy đúng object sân đã đăng ký |
| `FieldManager.findAvailableFields()` | Danh sách ứng viên cho tìm sân trống theo lịch |

Assumptions: API A giữ nguyên theo `FIELD_DOMAIN_API.md`; phí cao điểm dựa giờ bắt đầu; các phí đã nằm trong công thức sân (vợt/thảm/thiết bị/đèn) không thêm trùng thành dịch vụ bổ sung. Top 3 dùng tên class hiện có bỏ hậu tố `Field`.

Developer A cần biết `BookingManager`, `ReportManager`, các model mới, `IPromotional` và đoạn demo trên để nối vào Main. Không cần đổi kiến trúc/package hoặc API Field Domain.

## Compile và test

PowerShell, tại thư mục gốc repo:

```powershell
$taskSources = @(Get-ChildItem -LiteralPath src,test -Recurse -Filter *.java | ForEach-Object { $_.FullName })
javac -encoding UTF-8 -Xlint:all -d out @taskSources
java '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' -cp out smartsporthub.test.FieldDomainTest
java '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' -cp out smartsporthub.test.BookingBillingTest
```

Git Bash có thể dùng lệnh `javac` hiện tại trong `FIELD_DOMAIN_API.md`, rồi chạy hai lớp test trên.

Kết quả đã chạy: compile thành công với `-Xlint:all`, Field Domain **15/15 PASS**, Booking/Billing **28/28 PASS**.
Bộ test B có đủ 15 tình huống bắt buộc trong `PlanB.md`, cùng kiểm tra validation, lịch nối tiếp/qua ngày, trạng thái kết thúc, tính bất biến của hóa đơn, số lượng dịch vụ, và tính tương thích công thức bốn loại sân.

## Trạng thái Git

Làm việc trên nhánh hiện tại `B`. Chưa commit, pull, push hoặc merge theo yêu cầu tập trung hoàn thiện code. Các bước tích hợp Git sẽ thực hiện ở lượt riêng khi được yêu cầu.
