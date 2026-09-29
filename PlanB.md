Bạn là DEVELOPER B trong dự án Java Console Application môn Lập trình hướng đối tượng (OOPR240279).

# 1. BỐI CẢNH

Dự án:

SmartSportHub

Ứng dụng Console quản lý đặt sân thể thao.

Repository chỉ có MỘT repo Git.

Hai developer làm song song:

- `main`
- `feature/field-domain` — Developer A
- `feature/booking-billing` — bạn

Bạn là Developer B.

Bạn phụ trách:

Customer + Service + Booking + Billing + Reporting.

Developer A phụ trách SportField và FieldManager.

# 2. MỤC TIÊU

Xây dựng hoàn chỉnh:

- Customer
- StandardCustomer
- VipCustomer
- Service
- IPromotional
- Booking
- BookingManager
- Invoice/billing logic
- Reporting logic

Code phải tương thích với Field Domain của Developer A.

KHÔNG tự viết lại SportField nếu chưa cần thiết.

# 3. YÊU CẦU OOP

Phải thể hiện rõ:

## Abstraction

Customer là abstract class.

Ví dụ:

```java
public abstract class Customer {
    private String customerId;
    private String name;
    private String phone;

    public abstract double calculateDiscount(double amount);
}
```

## Encapsulation

Toàn bộ field phải private.

Có getter/setter.

Validation:

- ID không rỗng
- name không rỗng
- phone hợp lệ ở mức đơn giản
- discount không âm
- quantity > 0
- price không âm

Không để object tồn tại với dữ liệu vô lý.

## Inheritance

Tạo:

- StandardCustomer
- VipCustomer

cùng extends Customer.

## Polymorphism

Không kiểm tra kiểu khách hàng bằng hàng loạt if/else ở Booking.

Ví dụ KHÔNG làm:

```java
if (customer instanceof VipCustomer) {
   ...
} else {
   ...
}
```

để tính discount.

Thay vào đó gọi:

```java
customer.calculateDiscount(amount);
```

Runtime polymorphism sẽ chọn implementation phù hợp.

# 4. INTERFACE IPROMOTIONAL

Tạo:

```java
public interface IPromotional {
    double calculateDiscount(double amount);
}
```

Customer hoặc lớp phù hợp có thể implement interface này.

VIP customer có discount riêng.

Standard customer có thể discount 0 hoặc chính sách cơ bản.

Mục tiêu là thể hiện interface rõ ràng, dễ giải thích khi thi.

# 5. CUSTOMER

## Customer

Các thuộc tính đề xuất:

- customerId
- fullName
- phoneNumber

Có thể thêm email nếu cần nhưng không được over-engineer.

## StandardCustomer

Discount mặc định:

0%

hoặc mức nhỏ do nhóm thống nhất.

## VipCustomer

Có tỷ lệ discount rõ ràng.

Ví dụ:

10%

Không hard-code 10% ở nhiều nơi.

Nên để trong class hoặc constant rõ ràng.

# 6. SERVICE

Tạo class:

`Service`

Đây là dịch vụ đi kèm sân.

Ví dụ:

- Nước uống
- Thuê vợt
- Chiếu sáng

Thuộc tính:

- serviceId
- serviceName
- price

Validation:

- id không rỗng
- name không rỗng
- price >= 0

Không cần tạo inheritance cho Service nếu không cần thiết.

# 7. BOOKING

Booking đại diện cho một lượt đặt sân.

Nó phải kết nối:

Customer + SportField + thời gian + Service.

Các thuộc tính đề xuất:

```text
bookingId
customer
field
startTime
endTime
services
status
```

Có thể dùng:

```java
LocalDate
LocalTime
```

hoặc `LocalDateTime` nếu phù hợp.

Không sử dụng String để xử lý thời gian nếu có thể dùng Java Time API.

BookingStatus có thể gồm:

- PENDING
- CONFIRMED
- CANCELLED
- COMPLETED

Không cần workflow phức tạp.

# 8. TÍNH GIỜ

Booking phải tính được:

```text
duration = endTime - startTime
```

Phải validate:

- end > start
- duration > 0

Nếu đề bài chỉ yêu cầu số giờ nguyên thì có thể đơn giản hóa.

Nếu hỗ trợ số giờ lẻ, phải thống nhất cách tính với SportField.

# 9. TÍNH TIỀN

Tổng hóa đơn phải thể hiện:

```text
Tiền sân
+ Phụ phí giờ cao điểm
+ Dịch vụ
- Giảm giá
= Tổng tiền
```

Không được chỉ trả về một con số mà không thể hiện thành phần.

Thiết kế có thể là:

```text
fieldFee
peakHourSurcharge
serviceFee
subtotal
discount
total
```

Ưu tiên tạo một object Invoice nếu cần để giữ logic rõ ràng.

# 10. PEAK HOUR

Developer A chịu trách nhiệm chính cho:

`IPeakHourCalculable`

và logic phụ phí sân.

Bạn chỉ nên gọi API của A.

Không copy lại logic peak hour vào BookingManager.

Ví dụ:

```java
double fieldFee = booking.getField().calculateRentalFee(hours);
```

Nếu API của Developer A cung cấp riêng surcharge:

```java
double surcharge =
    ((IPeakHourCalculable) field).calculatePeakHourSurcharge(...);
```

Phải kiểm tra khả năng tương thích với implementation thực tế trước khi tích hợp.

# 11. BOOKINGMANAGER

Tạo:

`BookingManager`

Quản lý:

```java
List<Booking>
```

Các chức năng tối thiểu:

- createBooking()
- findBookingById()
- cancelBooking()
- changeBooking()
- isTimeSlotAvailable()
- completeBooking()
- calculateBookingTotal()
- displayBooking()

# 12. CHỐNG TRÙNG LỊCH

Đây là requirement bắt buộc.

Không được đặt cùng sân trong thời gian bị overlap.

Ví dụ:

Booking A:

18:00 → 20:00

Booking B:

19:00 → 21:00

=> từ chối Booking B.

Nhưng:

Booking A:

18:00 → 20:00

Booking B:

20:00 → 22:00

=> có thể cho phép.

Phải bỏ qua booking CANCELLED khi kiểm tra conflict.

Pseudo logic:

```text
same field
AND existing booking is not CANCELLED
AND time ranges overlap
=> conflict
```

# 13. SÂN BẢO TRÌ

Nếu:

```java
field.getStatus() == MAINTENANCE
```

thì không được tạo booking.

Phải trả về thông báo lỗi rõ ràng.

Ví dụ:

```text
Đặt sân thất bại: sân F003 đang bảo trì.
```

Không throw exception cho mọi lỗi business thông thường nếu Console app có thể xử lý bằng boolean/result message đơn giản.

# 14. HỦY BOOKING

Phải hỗ trợ:

```text
cancelBooking(bookingId)
```

Chỉ cho phép hủy trước giờ bắt đầu.

Ví dụ:

Booking bắt đầu 18:00.

Nếu hiện tại 17:00:

=> cho phép hủy.

Nếu đã 18:30:

=> từ chối.

Nếu demo không phụ thuộc current time để tránh test khó, có thể thiết kế method nhận `LocalDateTime currentTime`.

# 15. ĐỔI LỊCH / ĐỔI SÂN

Phải có ít nhất một chức năng:

- đổi thời gian
hoặc
- đổi sân.

Khi đổi phải kiểm tra conflict lại.

Không được bỏ qua validation.

# 16. PAYMENT / INVOICE

Tạo logic thanh toán.

Invoice phải in:

```text
========================================
             SMARTSPORTHUB
========================================
Booking ID:
Customer:
Customer Type:
Sport Field:
Start:
End:

Tiền sân:
Phụ phí cao điểm:
Dịch vụ:

Tạm tính:
Giảm giá VIP:
----------------------------------------
TỔNG THANH TOÁN:
========================================
```

Các số tiền phải tính từ object thực tế.

Không hard-code output.

# 17. REPORTING

Tạo manager hoặc service phù hợp để thực hiện:

## Tổng doanh thu

Chỉ tính các booking:

`COMPLETED`

Không tính:

- CANCELLED
- PENDING
- CONFIRMED chưa hoàn tất

## Top 3 loại sân được đặt nhiều nhất

Đếm theo loại:

- Football
- Badminton
- Tennis
- Pickleball

Không cần Stream API.

Có thể dùng vòng lặp và Map đơn giản.

Ví dụ:

```java
Map<String, Integer>
```

Sau đó sort bằng logic đơn giản hoặc cấu trúc dễ giải thích.

Mục tiêu là code dễ bảo vệ trong bài thi.

## Danh sách sân còn trống

Nhận:

- thời gian bắt đầu
- thời gian kết thúc

Sau đó gọi logic conflict.

Trả về các sân:

- AVAILABLE
- không bị booking conflict
- không phải MAINTENANCE

# 18. KHÔNG ĐƯỢC LÀM

Không sử dụng:

- Spring
- Database
- GUI
- REST API
- Generics Repository phức tạp
- Observer Pattern
- Event Pattern
- Stream API phức tạp
- Dependency Injection
- Framework không cần thiết

Đây là bài OOP Console giữa kỳ.

Ưu tiên:

Readable code
+
OOP rõ ràng
+
Dễ demo
+
Dễ giải thích.

# 19. API PHỤ THUỘC DEVELOPER A

Bạn phải coi SportField là external dependency trong domain của mình.

Dự kiến:

```java
SportField field;

field.getFieldId();
field.getFieldName();
field.getStatus();
field.calculateRentalFee(hours);
```

Không tự tạo một `MySportField`.

Không duplicate các subclass:

- FootballField
- BadmintonField
- TennisField
- PickleballField

Nếu API của A khác với dự kiến:

Đọc code thật của A và adapt Booking domain.

Không sửa code A nếu không cần thiết.

# 20. PACKAGE

Nếu repo chưa có convention:

```text
model/
    Customer.java
    StandardCustomer.java
    VipCustomer.java
    Service.java
    Booking.java
    Invoice.java
    BookingStatus.java

interfaces/
    IPromotional.java

manager/
    BookingManager.java
    ReportManager.java
```

Nếu project đã có cấu trúc package thì tuân theo cấu trúc hiện tại.

# 21. TEST CASE BẮT BUỘC

Phải test ít nhất:

### Test 1

StandardCustomer tạo thành công.

### Test 2

VipCustomer tạo thành công.

### Test 3

VIP discount hoạt động.

### Test 4

Booking thành công với sân AVAILABLE.

### Test 5

Booking thất bại do sân MAINTENANCE.

### Test 6

Booking thất bại do trùng giờ.

### Test 7

Booking có service.

### Test 8

Booking có peak-hour surcharge.

### Test 9

Invoice hiển thị:

- field fee
- surcharge
- services
- discount
- total

### Test 10

Cancel booking thành công trước giờ chơi.

### Test 11

Cancel booking thất bại sau giờ bắt đầu.

### Test 12

Change booking kiểm tra conflict.

### Test 13

Revenue chỉ tính COMPLETED.

### Test 14

Top 3 field type.

### Test 15

Available field search.

# 22. MAIN INTEGRATION

Bạn KHÔNG tự viết toàn bộ Main nếu Developer A cũng đang sửa Main.

Để tránh conflict:

- tạo các method demo riêng;
- hoặc ghi rõ phần code Main mà bạn muốn Developer A tích hợp.

Ví dụ:

```java
static void demoBookingScenarios(...) {
    ...
}
```

Developer A sẽ gọi method này khi tích hợp.

Nếu nhóm quyết định chỉ một người sở hữu Main thì Developer A sẽ giữ quyền merge Main cuối cùng.

# 23. GIT WORKFLOW

Branch:

`feature/booking-billing`

Trước khi bắt đầu:

```bash
git checkout feature/booking-billing
git pull origin main
```

Commit nhỏ:

```bash
git commit -m "feat(customer): add customer hierarchy"
```

```bash
git commit -m "feat(booking): add booking model"
```

```bash
git commit -m "feat(booking): add conflict validation"
```

```bash
git commit -m "feat(billing): add invoice calculation"
```

```bash
git commit -m "feat(report): add booking statistics"
```

Không commit trực tiếp vào main.

# 24. MERGE SAFETY

Đặc biệt tránh sửa:

- SportField.java
- FootballField.java
- BadmintonField.java
- TennisField.java
- PickleballField.java
- FieldManager.java

trừ khi thực sự cần.

Các file này thuộc Developer A.

Tương tự, Developer A tránh sửa:

- Customer.java
- StandardCustomer.java
- VipCustomer.java
- Service.java
- Booking.java
- BookingManager.java
- Invoice.java
- ReportManager.java

trừ khi hai bên thống nhất.

# 25. DEFINITION OF DONE

[ ] Customer abstract.

[ ] StandardCustomer.

[ ] VipCustomer.

[ ] Inheritance đúng.

[ ] Encapsulation đúng.

[ ] Polymorphism đúng.

[ ] IPromotional tồn tại.

[ ] VIP discount hoạt động.

[ ] Service hoạt động.

[ ] Booking hoạt động.

[ ] Booking kiểm tra time conflict.

[ ] Booking từ chối sân MAINTENANCE.

[ ] Cancel booking.

[ ] Change booking.

[ ] Invoice.

[ ] Peak-hour surcharge được tính thông qua Field Domain.

[ ] Service fee được tính.

[ ] Discount được tính.

[ ] Final total chính xác.

[ ] Revenue report.

[ ] Top 3 field type.

[ ] Available fields.

[ ] Code compile.

[ ] Test pass.

[ ] Không có dependency vào GUI/database/framework.

[ ] Commit rõ ràng.

# 26. OUTPUT CUỐI CÙNG

Sau khi hoàn thành, cung cấp:

1. Cây thư mục phần bạn phụ trách.
2. Danh sách class/interface.
3. API public mà Main cần gọi.
4. Các test đã chạy.
5. Các commit.
6. Những file Developer A cần biết.
7. Những dependency/API bạn đang sử dụng từ Developer A.
8. Hướng dẫn merge branch.

Đặc biệt ghi rõ:

```text
DEPENDENCY FROM DEVELOPER A
---------------------------
SportField API:
...

FieldStatus:
...

Peak-hour API:
...

Assumptions:
...
```

Sau đó đảm bảo branch của bạn có thể merge vào main mà không cần rewrite architecture.

Hãy bắt đầu bằng việc kiểm tra cấu trúc repository hiện tại và branch hiện tại trước khi code.