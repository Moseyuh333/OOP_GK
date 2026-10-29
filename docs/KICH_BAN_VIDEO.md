# KỊCH BẢN QUAY VIDEO — SmartSportHub (chỉ code + web, không slide)

**Cách dùng:** mỗi phân đoạn có:
- `[MÀN HÌNH]` = bật cái gì lên (VS Code / trình duyệt / PowerShell)
- `[LỜI NÓI]` = đọc đúng ý, nói tự nhiên, không cần thuộc lòng từng chữ
- `(HÀNH ĐỘNG)` = click / gõ phím

Tổng thời lượng dự kiến: **~11–13 phút** (A ~5.5–6.5 phút, B ~5.5–6.5 phút).

---

## 0. TRƯỚC KHI QUAY (làm sẵn, không quay phần này)

1. Mở **VS Code** tại `D:\New folder\OOP_GK` (Explorer hiện cây thư mục).
2. Mở **cửa sổ PowerShell 1** → gõ:
   ```powershell
   cd "D:\New folder\OOP_GK"
   .\run-video.ps1 -Role Api
   ```
   Đợi dòng `SmartSportHub API đang chạy tại http://localhost:18083/api/data`. Giữ mở.
3. Mở **cửa sổ PowerShell 2** → gõ:
   ```powershell
   cd "D:\New folder\OOP_GK"
   .\run-video.ps1 -Role Web
   ```
   Đợi dòng `Now listening on: http://localhost:5080`. Giữ mở.
4. Mở trình duyệt, gõ `http://localhost:5080/` → thấy trang chủ. Giữ tab đó, đừng tắt.

> Lần đầu PowerShell chặn script thì gõ trước: `Set-ExecutionPolicy -Scope Process Bypass`

---

# PHẦN DEV A — Java Core, ERD, Q01–Q10

## A1. Giới thiệu đề tài (~40s)
- `[MÀN HÌNH]` VS Code: cây thư mục repo (Explorer, cuộn từ từ từ trên xuống).
- `[LỜI NÓI]` "Em chào thầy/cô. Em là Nguyễn Minh Khôi. Đề tài nhóm là **SmartSportHub — hệ thống quản lý đặt sân thể thao và thanh toán**. Đây là hệ thống thông tin quản lý loại MIS: không chỉ lưu giao dịch đặt sân, mà còn xử lý dữ liệu thành báo cáo để người quản lý nhà thi đấu ra quyết định — chẳng hạn sân nào ít khách, loại sân nào doanh thu cao, khách nào chi tiêu nhiều."

## A2. Khảo sát hiện trạng (~60s)
- `[MÀN HÌNH]` VS Code: mở `src/smartsporthub/data/SeedData.java`, cuộn qua phần khách hàng, sân, dịch vụ.
- `[LỜI NÓI]` "Nhóm khảo sát hiện trạng theo **kịch bản mô phỏng** một nhà thi đấu nhỏ — đặt tên là Minh Phát: 6 sân gồm 2 bóng đá, 2 cầu lông, 1 tennis, 1 pickleball; trong tháng có 12 lượt khách đặt kèm dịch vụ thuê vợt, dụng cụ, nước, huấn luyện viên. Từ kịch bản đó, nhóm rút ra 4 vấn đề của cách quản lý thủ công bằng sổ tay: một là không biết sân nào đang trống; hai là tính tiền tay dễ sai, nhất là phí giờ cao điểm từ 17h đến 20h, mỗi loại sân một phí riêng; ba là lịch sử giao dịch rải rác, không lập được báo cáo doanh thu; bốn là không lưu hồ sơ khách nên không biết ai là VIP để áp ưu đãi. Dữ liệu mẫu trong SeedData phản ánh đúng các tình huống đó."

## A3. Các bước phân tích để ra ERD (~90s)
- `[MÀN HÌNH]` VS Code: mở `docs/DAC_TA_THIET_KE.md`, cuộn xuống **phần 3 — danh mục thực thể** (bảng 9 thực thể), rồi **phần 4 — lược đồ quan hệ dạng chữ** (khối CUSTOMER/SPORTFIELD/BOOKING/SERVICE/INVOICE).
- `[LỜI NÓI]` "Cách nhóm làm, theo đúng trình tự: bước một, đọc mô tả nghiệp vụ, gạch chân các danh từ chính — ra 5 thực thể gốc: khách hàng, sân, dịch vụ, lượt đặt, hóa đơn. Bước hai, xác định thuộc tính: khách có mã, họ tên, số điện thoại, email, loại khách, tỉ lệ giảm; sân có mã, tên, loại, giá giờ, trạng thái... Bước ba, chọn khóa chính và khóa ngoại: lượt đặt giữ khóa ngoại trỏ tới khách, sân, dịch vụ. Bước bốn, xác định quan hệ: khách đặt nhiều lượt — một-nhiều; sân có nhiều lượt — một-nhiều; mỗi lượt đặt có một hóa đơn — một-một. Bước năm, chuẩn hóa: vì 4 loại sân có 4 bộ phí riêng — bóng đá phí đèn, cầu lông phí vợt và thảm, tennis phí dụng cụ, pickleball phí dụng cụ tính theo giờ — ERD tách thành các bảng chuyên biệt, tổng cộng 9 bảng. Bước sáu, vẽ lại bằng draw.io và rà khóa."
- `[LỜI NÓI thêm]` "Một điểm nhóm muốn nói rõ: ERD có 9 bảng, nhưng khi triển khai lưu trữ bằng SQLite, nhóm gộp về 5 bảng vật lý: customers, fields, services, bookings, invoices — thông tin riêng của từng loại sân nằm trong cột field_type và cột phí tương ứng. Trong Java vẫn giữ 4 lớp con để thể hiện đầy đủ tính hướng đối tượng."

## A4. Cấu trúc project — từng folder chức năng gì (~90s)
- `[MÀN HÌNH]` VS Code Explorer, bấm mở từng thư mục theo thứ tự, để con trỏ ở tên thư mục.
- `[LỜI NÓI]` (nói theo từng folder, mỗi folder 1–2 câu)
  - "`model` — 11 lớp thực thể: Customer trừu tượng với hai lớp con Standard và Vip; SportField trừu tượng với 4 lớp con Football, Badminton, Tennis, Pickleball; rồi Service, Booking, Invoice và các enum trạng thái."
  - "`interfaces` — hai interface: IPromotional cho giảm giá, IPeakHourCalculable cho phí giờ cao điểm."
  - "`manager` — 5 lớp quản lý danh sách: thêm, tìm, cập nhật khách, sân, dịch vụ, booking, hóa đơn."
  - "`data` — dữ liệu mẫu SeedData và SmartSportHubData giữ toàn bộ dữ liệu trong bộ nhớ."
  - "`dto` — các lớp DTO để xuất JSON ra API; `json` — JsonWriter."
  - "`service` — BookingService chứa nghiệp vụ ghi: đặt sân, hủy, đổi lịch, hoàn thành, thu tiền, hoàn tiền; BusinessException ánh xạ lỗi nghiệp vụ ra mã HTTP."
  - "`db` — Database kết nối JDBC với SQLite; Repository đọc ghi, giao dịch, chặn đặt trùng lịch."
  - "`api` — SmartSportHubApi là server HTTP cung cấp endpoint đọc; WriteApi nhận các thao tác ghi."
  - "`test` — 8 bộ kiểm thử, tổng cộng 113 phép thử, chạy bằng build.ps1."

## A5. Các bước làm ra project (~40s)
- `[MÀN HÌNH]` VS Code: mở `README.md`, cuộn nhanh từ đầu; hoặc gõ `git log --oneline` ở PowerShell 3.
- `[LỜI NÓI]` "Trình tự làm: một, phân tích bài toán và hiện trạng; hai, xác định thực thể, thuộc tính, quan hệ và vẽ ERD; ba, chuyển ERD sang các lớp Java theo hướng đối tượng; bốn, viết các lớp quản lý và dữ liệu mẫu; năm, dựng DTO và API JSON cho Developer B; sáu, thêm lưu trữ SQLite qua JDBC; bảy, Developer B làm giao diện web và 20 truy vấn LINQ; tám, kiểm thử cả hai phía. Kiến trúc cuối cùng là trình duyệt gọi C#, C# gọi API Java, Java giữ toàn bộ nghiệp vụ và dữ liệu."

## A6. Chạy Main — khởi động API (~50s)
- `[MÀN HÌNH]` Cửa sổ PowerShell 1 đã chạy sẵn: hiện dòng `SmartSportHub API đang chạy tại http://localhost:18083/api/data` và `Dữ liệu: customers=6, fields=6, bookings=12...`
- `[MÀN HÌNH]` Trình duyệt: mở tab `http://localhost:18083/api/health` (hiện JSON).
- `[LỜI NÓI]` "Đây là hàm main của SmartSportHubApi: nó mở cơ sở dữ liệu SQLite, tạo bảng nếu chưa có, nạp dữ liệu mẫu nếu database trống — như đang thấy, 6 khách, 6 sân, 12 lượt đặt — rồi khởi động HTTP server ở cổng 18083. Trình duyệt gọi thẳng /api/health để kiểm tra server sống và xem số lượng dữ liệu."

## A7. Trình bày Q01–Q10 (~5 phút, mỗi câu ~30–35s)
Với mỗi câu: `[MÀN HÌNH]` mở `http://localhost:5080/queries?id=N` → rồi VS Code mở `web/SmartSportHub.Web/Services/SmartSportHubQueryService.cs`, cuộn đến đúng hàm `Q0N` → quay lại trang web. Nói theo khuôn: **câu hỏi → dùng LINQ gì → kết quả → ý nghĩa**.

- **Q01** — "Câu 1: sân AVAILABLE nào có giá thuê thấp nhất. Em dùng Where lọc sân đang mở, OrderBy theo giá. Kết quả là [sân ... giá ...]. Ý nghĩa: nhân viên tư vấn ngay sân rẻ còn trống cho khách."
- **Q02** — "Câu 2: sân nào đắt hơn mức trung bình. Em dùng Average tính mặt bằng chung rồi Where so sánh. Kết quả giúp phân khúc giá: sân nào nên đắt hơn, sân nào cần điều chỉnh."
- **Q03** — "Câu 3: mỗi loại có bao nhiêu sân. GroupBy theo loại rồi Count. Kết quả: 2 bóng đá, 2 cầu lông, 1 tennis, 1 pickleball — nhìn được cơ cấu sân đang vận hành."
- **Q04** — "Câu 4: loại sân nào giá trung bình cao nhất. GroupBy, Average từng loại rồi lấy Max. Khác Q02 vì so theo nhóm loại, không theo từng sân — cho biết loại sân chủ lực cao cấp."
- **Q05** — "Câu 5: sân nào đã có lượt hoàn thành và bao nhiêu lần. Dùng GroupJoin nối sân với booking COMPLETED rồi Count. Kết quả cho thấy sân nào thực sự sinh ra doanh thu."
- **Q06** — "Câu 6: sân nào chưa từng có lượt hoàn thành. Dùng Where kết hợp Any kiểm tra không tồn tại booking COMPLETED. Kết quả là sân ít được khai thác — gợi ý cho kế hoạch kinh doanh."
- **Q07** — "Câu 7: sân nào có tổng giờ thuê cao nhất. GroupBy, Sum tổng giờ, Max tìm đỉnh, Join lấy tên sân. Khác Q05 vì đếm theo giờ, không theo số lượt — đo mức độ sử dụng thực tế."
- **Q08** — "Câu 8: booking hoàn thành mang lại bao nhiêu doanh thu theo loại sân. Where lọc COMPLETED, GroupBy loại, Sum tiền. Đây là báo cáo doanh thu quản trị cơ bản."
- **Q09** — "Câu 9: sân nào có doanh thu cao hơn trung bình của tất cả sân, kể cả sân chưa có doanh thu. Dùng GroupJoin để sân không có booking vẫn được tính vào mức trung bình. Nhìn được sân nào vượt mặt bằng."
- **Q10** — "Câu 10: sân nào từng được đặt cả giờ thường lẫn giờ cao điểm. GroupBy theo sân, kiểm tra bằng Any và All trên hai nhóm giờ. Kết quả phản ánh nhu cầu phân bố theo khung giờ trong ngày."

## A8. Bàn giao cho Dev B (~15s)
- `[LỜI NÓI]` "Phần của em đã trình bày xong: đề tài, hiện trạng, ERD, cấu trúc Java Core, cách chạy Main và 10 câu truy vấn đầu. Em xin chuyển cho bạn [TÊN B] trình bày phần web và 10 câu còn lại."

---

# PHẦN DEV B — C# Web, LINQ Q11–Q20

## B1. Kiến trúc + cấu trúc phần C# (~70s)
- `[MÀN HÌNH]` VS Code: bấm mở lần lượt `web/SmartSportHub.Web` → `Controllers` → `Services` → `Dtos` → `Views` → `SmartSportHub.Web.Tests`.
- `[LỜI NÓI]` "Em là [TÊN B], phụ trách Developer B. Phần C# của nhóm nằm trong web/SmartSportHub.Web. Điểm quan trọng: **C# không có nghiệp vụ riêng** — không mở database, không tự tính tiền; nó chỉ gọi API Java, nhận JSON, hiển thị và chạy các câu truy vấn thống kê bằng LINQ trên dữ liệu trong bộ nhớ. Controllers xử lý request; Services có ApiClient gọi Java và QueryService chứa 20 câu LINQ; Dtos mô tả cấu trúc JSON nhận về; Views là các trang hiển thị; còn dự án SmartSportHub.Web.Tests có 43 kiểm thử."

## B2. Cách C# lấy dữ liệu từ Java (~40s)
- `[MÀN HÌNH]` VS Code: mở `web/SmartSportHub.Web/Services/SmartSportHubApiClient.cs` (cuộn qua các lệnh gọi HTTP) rồi `web/SmartSportHub.Web/Dtos/ApiData.cs`.
- `[LỜI NÓI]` "Đây là hợp đồng tích hợp: ApiClient gửi request HTTP đến Java API ở cổng 18083, nhận JSON, rồi đổ vào các DTO. Toàn bộ 20 câu truy vấn sau đây chạy trên List trong bộ nhớ — không dùng SQL, không dùng Entity Framework thay cho Java."

## B3. Chạy web (~30s)
- `[MÀN HÌNH]` Cửa sổ PowerShell 2: hiện `Now listening on: http://localhost:5080`.
- `[MÀN HÌNH]` Trình duyệt: `http://localhost:5080/` (trang chủ).
- `[LỜI NÓI]` "Web chạy ở cổng 5080 và gọi Java API ở 18083. Đây là trang chủ của ứng dụng, dữ liệu hiển thị đều lấy trực tiếp từ Java."

## B4. Trình bày Q11–Q20 (~5 phút, mỗi câu ~30–35s)
Khuôn như phần A: mở `queries?id=N` → mở `SmartSportHubQueryService.cs` cuộn đến `Q0N` → nói.

- **Q11** — "Câu 11: mỗi khách có bao nhiêu lượt hoàn thành và chi bao nhiêu, kể cả khách chưa từng đặt. Dùng GroupJoin và DefaultIfEmpty để khách không có booking vẫn xuất hiện với số 0. Đây là báo cáo đầy đủ toàn bộ khách hàng, không bỏ sót ai."
- **Q12** — "Câu 12: khách nào có tổng giờ thuê cao nhất. GroupBy, Sum giờ, Max rồi Join lấy tên khách. Kết quả cho biết khách sử dụng dịch vụ nhiều nhất — cơ sở để chăm sóc khách thân thiết."
- **Q13** — "Câu 13: khách nào đã hoàn thành lượt thuê ở ít nhất hai loại sân. GroupBy khách, Distinct đếm loại sân khác nhau, Count rồi Join. Đây là nhóm khách đa dạng nhu cầu, thích hợp cho chính sách ưu đãi chéo."
- **Q14** — "Câu 14: khách nào đã thuê dụng cụ nhưng chưa từng đặt sân tennis. Dùng Any kiểm tra có booking dùng dịch vụ nhóm Thiết bị, và All kiểm tra mọi booking đều không phải tennis. Kết quả là nhóm tiềm năng để giới thiệu sân tennis."
- **Q15** — "Câu 15: có bao nhiêu booking hoàn thành bắt đầu ở mỗi giờ. GroupBy theo giờ bắt đầu rồi Count. Báo cáo này cho thấy khung giờ nào đông khách — để bố trí nhân sự và xem xét giá cao điểm."
- **Q16** — "Câu 16: ngày nào nhiều booking hoàn thành nhất. GroupBy ngày, Count, lấy Max; nếu nhiều ngày bằng nhau thì chọn ngày sớm nhất bằng Min để kết quả luôn duy nhất. Kết quả là ngày bận nhất trong kỳ."
- **Q17** — "Câu 17: dịch vụ nào được dùng nhiều nhất trong các booking hoàn thành. SelectMany trải dịch vụ ra, GroupBy, Count, Max. Cho biết dịch vụ phát sinh chủ lực."
- **Q18** — "Câu 18: booking nào có giá trị cao hơn trung bình của chính khách đó. GroupBy khách, tính Average trong từng nhóm rồi lọc. Điểm đáng chú ý: so với trung bình của từng khách, không phải trung bình toàn hệ thống — phát hiện lượt đặt bất thường của từng người."
- **Q19** — "Câu 19: trang 2, mỗi trang 3 booking hoàn thành. Dùng OrderBy rồi Skip(3).Take(3). Đây là cách nhóm minh họa phân trang: dữ liệu nhận về từ API dưới dạng collection, sau đó phân trang ngay trong bộ nhớ."
- **Q20** — "Câu 20: khách nào từng đặt nhưng chưa có booking hoàn thành nào. Dùng Any và Where: có booking, nhưng không tồn tại booking COMPLETED. Nhóm khách này cần được nhắc lịch hoặc chăm sóc để quay lại."

## B5. Demo thao tác ghi trên web (~90s)
- `[MÀN HÌNH]` Trình duyệt: mở `http://localhost:5080/bookings`, điền form **Đặt sân mới** (chọn sân còn AVAILABLE, thời gian, có hoặc không dịch vụ) → bấm Đặt.
- `[MÀN HÌNH]` Sau khi redirect: thấy thông báo xanh thành công, booking mới xuất hiện cuối danh sách.
- `[MÀN HÌNH]` PowerShell 1: không cần làm gì (Java in log nếu có); có thể mở lại `/api/health` thấy số booking tăng lên.
- `[MÀN HÌNH]` Trình duyệt: thử hủy booking vừa đặt (nút Hủy) → thấy trạng thái CANCELLED; hoặc thử đặt vào sân đang bảo trì → thấy thông báo đỏ "sân đang bảo trì, chưa thể đặt".
- `[LỜI NÓI]` "Đây là luồng ghi thật: trang web gửi yêu cầu sang Java API; Java kiểm tra sân còn trống không, thời gian có hợp lệ không, tính phí sân cộng phí giờ cao điểm cộng dịch vụ, trừ giảm giá, tạo hóa đơn rồi lưu xuống SQLite. Khi hủy, Java đổi trạng thái booking và hóa đơn theo nghiệp vụ. Sân đang bảo trì bị Java chặn với mã 409 — lỗi nghiệp vụ nằm ở tầng Java, web chỉ hiển thị thông báo."

## B6. Kiểm thử + kết luận (~60s)
- `[MÀN HÌNH]` Cửa sổ PowerShell 3: gõ `.\run-video.ps1 -Role Build` → cuộn lên thấy `===== KẾT QUẢ ... PASS =====` cho 8 bộ, kết thúc bằng dòng tổng; rồi `.\run-video.ps1 -Role Test` → thấy `Passed! 43/43`.
- `[MÀN HÌNH]` Trình duyệt: mở `http://localhost:5080/reports` (báo cáo tổng quan).
- `[LỜI NÓI]` "Kiểm thử hai phía: Java 113 phép thử, C# 43 phép thử, tất cả đạt. Tổng kết: SmartSportHub là một hệ thống thông tin quản lý hoàn chỉnh ở mức bài tập lớn — Java đảm nhiệm mô hình hóa, nghiệp vụ, lưu trữ và API; C# đảm nhiệm giao diện và 20 truy vấn LINQ phục vụ báo cáo. Sản phẩm không chỉ quản lý việc đặt sân mà còn cung cấp thông tin ra quyết định: doanh thu theo loại sân, mức độ sử dụng sân, hành vi khách hàng. Nhóm xin cảm ơn thầy/cô đã lắng nghe."

---

# CÂU HỎI THƯỜNG GẶP

- **Có khảo sát thực tế không?** — Khảo sát theo kịch bản mô phỏng nhà thi đấu Minh Phát, chưa có biên bản phỏng vấn doanh nghiệp thật; dữ liệu mẫu phục vụ học tập.
- **ERD 9 bảng mà SQLite 5 bảng?** — ERD là mô hình khái niệm tách 4 loại sân; khi triển khai SQLite gộp về fields + field_type + cột phí; lớp con Java vẫn giữ để thể hiện OOP.
- **20 câu chạy bằng SQL?** — Không: Java dùng SQLite để lưu trữ; C# nhận JSON và chạy 20 câu bằng LINQ trên collection trong bộ nhớ.
- **Cổng nào?** — Java API 18083 (8080 trên máy bị Apache chiếm), web 5080; cấu hình ở appsettings.json.
