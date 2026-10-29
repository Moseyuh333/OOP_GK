# -*- coding: utf-8 -*-
"""Build SmartSportHub APA report docx from scratch (not a md conversion)."""
import sys
sys.path.insert(0, r'C:/Users/MINH KHOI/AppData/Local/hermes/tools')
from docx import Document
from docx.shared import Pt, RGBColor, Inches, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

OUT = r'D:/New folder/OOP_GK/docs/BAO_CAO_DU_AN_APA.docx'

ndoc = Document()
for s in ndoc.sections:
    s.page_width = Cm(21.0); s.page_height = Cm(29.7)
    s.left_margin = Cm(3.0); s.right_margin = Cm(2.0)
    s.top_margin = Cm(2.5); s.bottom_margin = Cm(2.5)

normal = ndoc.styles['Normal']
normal.font.name = 'Times New Roman'; normal.font.size = Pt(13)
normal.paragraph_format.line_spacing = 1.5
normal.paragraph_format.space_after = Pt(6)
normal.paragraph_format.first_line_indent = Cm(1.0)

def style_heading(name, size, italic=False, left=None, sb=Pt(14), sa=Pt(6)):
    st = ndoc.styles[name]
    st.font.name = 'Times New Roman'; st.font.size = Pt(size); st.font.bold = True
    st.font.italic = italic; st.font.color.rgb = RGBColor(0,0,0)
    pf = st.paragraph_format; pf.line_spacing = 1.3
    pf.space_before = sb; pf.space_after = sa; pf.first_line_indent = Cm(0)
    if left is not None: pf.left_indent = left

style_heading('Heading 1', 16)
style_heading('Heading 2', 14)
style_heading('Heading 3', 14, italic=True, left=Cm(1.0))
style_heading('Heading 4', 13, italic=True)
style_heading('Title', 24)

def shade(cell, hexcolor):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement('w:shd'); shd.set(qn('w:val'),'clear'); shd.set(qn('w:color'),'auto'); shd.set(qn('w:fill'),hexcolor)
    tcPr.append(shd)

def set_cell(cell, text, bold=False, size=12):
    cell.text = ''
    p = cell.paragraphs[0]; p.paragraph_format.first_line_indent = Cm(0)
    p.paragraph_format.space_after = Pt(2); p.paragraph_format.space_before = Pt(2)
    run = p.add_run(text); run.bold = bold; run.font.size = Pt(size); run.font.name = 'Times New Roman'

def add_table(headers, rows):
    ncol = len(headers)
    t = ndoc.add_table(rows=0, cols=ncol); t.style = 'Table Grid'
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    tr = t.add_row()
    for i,h in enumerate(headers):
        set_cell(tr.cells[i], h, bold=True); shade(tr.cells[i], 'D9E2F3')
    for r in rows:
        tr = t.add_row()
        for i in range(ncol):
            set_cell(tr.cells[i], r[i] if i < len(r) else '')
    return t

def para(text, bold=False, italic=False, align=None, size=13, fi=Cm(1.0)):
    p = ndoc.add_paragraph()
    if align: p.alignment = align
    p.paragraph_format.first_line_indent = fi
    run = p.add_run(text); run.bold = bold; run.italic = italic
    run.font.size = Pt(size); run.font.name = 'Times New Roman'
    return p

def plain_para(text, size=13):
    p = ndoc.add_paragraph(); p.paragraph_format.first_line_indent = Cm(0)
    run = p.add_run(text); run.font.size = Pt(size); run.font.name = 'Times New Roman'
    return p

def caption(text):
    p = ndoc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.first_line_indent = Cm(0); p.paragraph_format.space_before = Pt(6); p.paragraph_format.space_after = Pt(6)
    run = p.add_run(text); run.bold = True; run.font.size = Pt(12); run.font.name = 'Times New Roman'
    return p

def add_pagebreak(): ndoc.add_page_break()

# ================= COVER =================
cover = [
    ("BỘ GIÁO DỤC VÀ ĐÀO TẠO", 13, True),
    ("TRƯỜNG ĐẠI HỌC CÔNG NGHỆ KỸ THUẬT TP. HỒ CHÍ MINH", 13, True),
    ("KHOA CÔNG NGHỆ THÔNG TIN", 13, True),
    ("", 12, False), ("", 12, False),
    ("TIỂU LUẬN CHUYÊN NGÀNH", 16, True),
    ("ĐỀ TÀI:", 15, True),
    ("HỆ THỐNG QUẢN LÝ ĐẶT SÂN THỂ THAO — SMARTSPORTHUB", 15, True),
    ("", 12, False), ("", 12, False),
    ("Sinh viên thực hiện:", 13, True),
    ("Trần Doãn Hòa  –  MSSV: 23162026", 13, False),
    ("Nguyễn Minh Khôi  -  MSSV: 23162046", 13, False),
    ("", 12, False),
    ("Giảng viên hướng dẫn:  Th.S Nguyễn Thị Thanh Vân", 13, False),
    ("", 12, False), ("", 12, False),
    ("TP. Hồ Chí Minh, tháng 10 năm 2026", 13, False),
]
for t, sz, bold in cover:
    p = ndoc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER; p.paragraph_format.first_line_indent = Cm(0)
    run = p.add_run(t); run.font.name = 'Times New Roman'; run.font.size = Pt(sz); run.bold = bold
add_pagebreak()

# ================= MỤC LỤC (field) =================
plain_para("MỤC LỤC", size=16)
p = ndoc.add_paragraph(); p.paragraph_format.first_line_indent = Cm(0)
fldBegin = OxmlElement('w:fldChar'); fldBegin.set(qn('w:fldCharType'),'begin')
instr = OxmlElement('w:instrText'); instr.set(qn('xml:space'),'preserve'); instr.text = 'TOC \\\\o "1-3" \\\\h \\\\z \\\\u'
fldSep = OxmlElement('w:fldChar'); fldSep.set(qn('w:fldCharType'),'separate')
t = OxmlElement('w:t'); t.text = "Nhấn chuột phải và chọn 'Update Field' để cập nhật mục lục."
fldEnd = OxmlElement('w:fldChar'); fldEnd.set(qn('w:fldCharType'),'end')
run = p.add_run(); run._r.append(fldBegin); run._r.append(instr); run._r.append(fldSep); run._r.append(t); run._r.append(fldEnd)
add_pagebreak()

# ================= DANH MỤC TỪ VIẾT TẮT =================
ndoc.add_heading("DANH MỤC TỪ VIẾT TẮT", level=1)
add_table(["Từ viết tắt", "Ý nghĩa"], [
    ["APA", "American Psychological Association — chuẩn trích dẫn tài liệu"],
    ["API", "Application Programming Interface"],
    ["CRUD", "Create, Read, Update, Delete"],
    ["CWE", "Common Weakness Enumeration"],
    ["DB", "Database"],
    ["DTO", "Data Transfer Object"],
    ["ERD", "Entity Relationship Diagram"],
    ["FK", "Foreign Key"],
    ["JDBC", "Java Database Connectivity"],
    ["JSON", "JavaScript Object Notation"],
    ["LINQ", "Language Integrated Query"],
    ["MIS", "Management Information System"],
    ["MVC", "Model–View–Controller"],
    ["OOP", "Object-Oriented Programming"],
    ["PK", "Primary Key"],
    ["SQL", "Structured Query Language"],
    ["WAL", "Write-Ahead Logging"],
])
add_pagebreak()

# ================= DANH MỤC HÌNH ẢNH VÀ BẢNG BIỂU =================
ndoc.add_heading("DANH MỤC HÌNH ẢNH VÀ BẢNG BIỂU", level=1)
for s in [
    "Hình 1. Sơ đồ kiến trúc tổng thể hệ thống SmartSportHub",
    "Hình 2. Sơ đồ ERD các thực thể của hệ thống",
    "Bảng 1. Bảng định nghĩa các thực thể và thuộc tính",
    "Bảng 2. Bảng các mối liên kết giữa các thực thể",
    "Bảng 3. Bảng các ràng buộc toàn vẹn",
    "Bảng 4. Bảng ánh xạ ERD sang các lớp Java",
    "Bảng 5. Bảng các truy vấn LINQ Q01–Q20",
    "Bảng 6. Bảng tổng kết kết quả kiểm thử Java",
    "Bảng 7. Bảng kết quả kiểm thử C#",
    "Bảng 8. Bảng mô tả các endpoint API",
    "Bảng 9. Thuộc tính bảng CUSTOMER",
    "Bảng 10. Thuộc tính bảng SPORTFIELD và 4 bảng loại sân",
    "Bảng 11. Thuộc tính bảng SERVICE",
    "Bảng 12. Thuộc tính bảng BOOKING",
    "Bảng 13. Thuộc tính bảng INVOICE",
]:
    plain_para(s, size=12)
add_pagebreak()

# ================= MỞ ĐẦU =================
ndoc.add_heading("MỞ ĐẦU", level=1)

ndoc.add_heading("1. Lý do chọn đề tài", level=2)
para("Trong bối cảnh chuyển đổi số, các loại hình dịch vụ giải trí và thể thao tại Việt Nam tăng trưởng nhanh nhưng công tác quản lý tại nhiều cơ sở nhỏ vẫn mang tính thủ công, dựa trên sổ sách và kinh nghiệm của nhân viên. Tại một nhà thi đấu tiêu biểu, việc đặt sân vẫn thường được thực hiện bằng gọi điện thoại hoặc đến trực tiếp quầy; lịch sử giao dịch được ghi lại bằng tay; việc tính tiền thuê sân, phụ phí giờ cao điểm và phí dịch vụ kèm theo phụ thuộc hoàn toàn vào người trực quầy. Cách làm này phát sinh nhiều vấn đề như sai sót khi tính tiền, khó kiểm soát sân trống, và không thể tổng hợp báo cáo doanh thu, đặc biệt khi cơ sở có nhiều loại sân với nhiều công thức tính phí khác nhau (Nguyen, 2026).")
para("SmartSportHub là hệ thống thông tin quản lý (Management Information System – MIS) được xây dựng nhằm giải quyết bài toán nêu trên. Hệ thống không chỉ lưu trữ giao dịch đặt sân mà còn xử lý dữ liệu thành thông tin hỗ trợ ra quyết định quản trị. Điểm đặc trưng của bài toán là sự đa dạng của các loại sân, mỗi loại có một công thức tính phí riêng, và chính sách ưu đãi dành cho hai nhóm khách hàng Standard và VIP. Vì vậy, việc ứng dụng phương pháp phân tích và thiết kế hướng đối tượng (Object-Oriented Programming – OOP) là lựa chọn phù hợp để mô hình hoá chính xác các thực thể và quan hệ của nghiệp vụ.")

ndoc.add_heading("2. Mục tiêu nghiên cứu", level=2)
para("Mục tiêu chính của đề tài là xây dựng một hệ thống thông tin quản lý việc đặt sân thể thao, trong đó tầng mô hình hoá nghiệp vụ, xử lý dữ liệu và cung cấp giao diện lập trình (Application Programming Interface – API) được hiện thực bằng ngôn ngữ Java; còn phần giao diện người dùng và các câu truy vấn thống kê được hiện thực bằng C# ASP.NET Core MVC. Cụ thể, hệ thống cần thực hiện được sáu mục tiêu sau:")
for t in [
    "M1 — Khoản tra cứu sân trống: quản lý trạng thái AVAILABLE / OCCUPIED / MAINTENANCE của từng sân, giúp nhân viên biết chính xác sân nào còn trống.",
    "M2 — Tính tiền chính xác: tính tiền thuê sân theo công thức riêng của từng loại sân, cộng phụ phí giờ cao điểm và phí dịch vụ, rồi trừ đi chiết khấu theo chính sách khách hàng.",
    "M3 — Lập báo cáo đa chiều: tổng hợp doanh thu theo loại sân, theo khách hàng, theo giờ, theo ngày và theo dịch vụ.",
    "M4 — Quản lý hồ sơ khách hàng: lưu thông tin khách, phân loại Standard/VIP với tỷ lệ chiết khấu tương ứng.",
    "M5 — Theo dõi thanh toán: phát hành hóa đơn và quản lý trạng thái PAID / UNPAID / REFUNDED.",
    "M6 — Chuyển dữ liệu sang phần giao diện web: cung cấp dữ liệu dạng JSON qua REST API để phần C# hiển thị và thống kê.",
]:
    para(t, fi=Cm(1.0))

ndoc.add_heading("3. Câu hỏi và giả thuyết nghiên cứu", level=2)
para("Để đạt được các mục tiêu trên, nghiên cứu đặt ra bốn câu hỏi định hướng:")
for t in [
    "CH1 — Làm thế nào để áp dụng phương pháp phân tích và thiết kế hướng đối tượng nhằm mô hình hoá đúng quy trình nghiệp vụ đặt sân thể thao gồm nhiều loại sân và nhiều công thức tính phí?",
    "CH2 — Làm thế nào để đảm bảo chính xác việc tính tiền thuê sân khi mỗi loại sân có công thức riêng, có giờ cao điểm và có chính sách ưu đãi khác nhau?",
    "CH3 — Làm thế nào để thiết kế cơ sở dữ liệu và lớp lưu trữ sao cho vừa đảm bảo tính toàn vẹn, vừa phục vụ được các câu truy vấn thống kê đa chiều?",
    "CH4 — Làm thế nào để phân chia trách nhiệm giữa Java Core và C# Web sao cho Java giữ toàn bộ nghiệp vụ, còn C# chỉ đọc dữ liệu và thống kê?",
]:
    para(t, fi=Cm(1.0))
para("Giả thuyết đặt ra là: nếu xây dựng đúng mô hình hướng đối tượng với lớp trừu tượng SportField và Customer, dùng đa hình để tính phí, và lưu trữ bền vững qua JDBC + SQLite, thì hệ thống sẽ đáp ứng được toàn bộ sáu mục tiêu M1–M6 với sai số tính tiền bằng không.")

ndoc.add_heading("4. Đối tượng và phạm vi nghiên cứu", level=2)
para("Đối tượng nghiên cứu là quy trình nghiệp vụ đặt sân thể thao tại một nhà thi đấu nhỏ, bao gồm các thực thể: khách hàng, sân thể thao, dịch vụ kèm theo, lượt đặt và hóa đơn. Phạm vi nghiên cứu giới hạn ở một cơ sở duy nhất với sáu sân thuộc bốn loại (bóng đá, cầu lông, tennis, pickleball) và sáu dịch vụ. Hệ thống không bao gồm thanh toán trực tuyến qua cổng ngân hàng, quản lý nhân sự, nhiều chi nhánh hay tích hợp thiết bị đọc thẻ thành viên.")

ndoc.add_heading("5. Phương pháp nghiên cứu", level=2)
para("Nghiên cứu sử dụng phương pháp phân tích và thiết kế hệ thống theo hướng đối tượng (Object-Oriented Analysis and Design – OOAD). Quá trình gồm năm bước: (1) khảo sát hiện trạng và phát biểu vấn đề; (2) xác định các thực thể, thuộc tính và ràng buộc; (3) xác định các mối liên kết và xây dựng sơ đồ thực thể–quan hệ (Entity Relationship Diagram – ERD); (4) chuyển ERD thành các lớp Java theo sáu quy tắc chuyển đổi; (5) kiểm thử cả hai phía Java và C#.")

ndoc.add_heading("6. Ý nghĩa khoa học và thực tiễn", level=2)
para("Về mặt khoa học, nghiên cứu đóng góp ở ba điểm. Thứ nhất, đề xuất cơ chế mô hình hoá đa hình cho bài toán có nhiều công thức tính phí, trong đó mỗi loại sân ghi đè phương thức tính phí riêng. Thứ hai, xử lý tường minh bài toán chấm mức độ nghiêm trọng khi thiếu bối cảnh triển khai bằng cách công bố bộ quy ước giả định kèm căn cứ cho từng chỉ số. Thứ ba, xây dựng giao thức đánh giá tách riêng đóng góp của công cụ tất định, của lớp hỗ trợ mô hình và của lớp kiểm chứng.")
para("Về mặt thực tiễn, hệ thống hướng tới việc giảm khối lượng công việc quản lý thủ công, tính tiền chính xác và cung cấp báo cáo đa chiều cho nhà quản lý, đồng thời cho phép nhân viên thao tác đặt sân, hủy, hoàn thành, thanh toán, hoàn tiền và đổi trạng thái sân ngay trên giao diện web.")

ndoc.add_heading("7. Bố cục tiểu luận", level=2)
para("Ngoài phần mở đầu, kết luận, tài liệu tham khảo và phụ lục, nội dung tiểu luận được tổ chức thành bốn chương. Chương 1 trình bày cơ sở lý thuyết và quá trình phân tích hiện trạng, xác định thực thể và xây dựng ERD. Chương 2 mô tả các bước chuyển ERD thành lớp Java và thiết kế cơ sở dữ liệu. Chương 3 trình bày kiến trúc tổng thể, các thành phần mã nguồn và các câu truy vấn LINQ. Chương 4 trình bày kết quả kiểm thử và đánh giá. Phụ lục chứa công thức tính phí, danh mục file và hướng dẫn chạy.")

# ================= CHƯƠNG 1 =================
add_pagebreak()
ndoc.add_heading("CHƯƠNG 1. PHÂN TÍCH HIỆN TRẠNG VÀ XÂY DỰNG ERD", level=1)

ndoc.add_heading("1.1. Khảo sát hiện trạng và phát biểu vấn đề", level=2)
para("Nhà thi đấu Minh Phát đang vận hành 6 sân thể thao: 2 sân bóng đá, 2 sân cầu lông, 1 sân tennis, 1 sân pickleball. Trong tháng 2/2026 có 12 lượt khách đặt sân, kèm theo dịch vụ phát sinh như thuê vợt, thuê dụng cụ, nước uống, thuê huấn luyện viên. Qua khảo sát, xác định 4 vấn đề của quản lý thủ công bằng sổ tay:")
add_table(["Mã", "Vấn đề", "Hậu quả"], [
    ["V1", "Không biết chính xác sân nào đang trống", "Khách đến nơi mới biết sân có trống hay không; nhiều khách phải chờ"],
    ["V2", "Tính tiền bằng tay, dễ tính nhầm phí giờ cao điểm", "Bất đồng giữa tiền sân và tiền khách phải trả; mất uy tín"],
    ["V3", "Lịch sử giao dịch nằm rải rác trong sổ, không tra cứu được", "Không lập được báo cáo doanh thu theo khách, theo loại sân, theo giờ"],
    ["V4", "Không lưu hồ sơ khách hàng", "Không biết khách nào là VIP, không áp dụng được chính sách ưu đãi"],
])
para("Điểm cốt lõi của V2: phí giờ cao điểm chỉ áp dụng trong khung 17:00 ≤ giờ bắt đầu < 20:00, và mỗi loại sân có phí riêng khác nhau. Với sáu loại thuộc tính tính tiền cộng dồn, tính tay rất dễ quên bớt một khoản.")

ndoc.add_heading("1.2. Xác định các thực thể và thuộc tính", level=2)
para("Quá trình phân tích nghiệp vụ dẫn đến 5 thực thể gốc và 4 bảng chuyên biệt hoá của thực thể sân:")
add_table(["STT", "Thực thể", "Ý nghĩa nghiệp vụ", "Loại"], [
    ["1", "CUSTOMER", "Khách hàng của nhà thi đấu", "Thực thể gốc"],
    ["2", "SPORTFIELD", "Sân thể thao", "Thực thể gốc, có phân loại"],
    ["3", "FOOTBALL_FIELD", "Riêng biệt của sân bóng đá", "Bảng chuyên biệt hoá"],
    ["4", "BADMINTON_FIELD", "Riêng biệt của sân cầu lông", "Bảng chuyên biệt hoá"],
    ["5", "TENNIS_FIELD", "Riêng biệt của sân tennis", "Bảng chuyên biệt hoá"],
    ["6", "PICKLEBALL_FIELD", "Riêng biệt của sân pickleball", "Bảng chuyên biệt hoá"],
    ["7", "SERVICE", "Dịch vụ phát sinh kèm theo", "Thực thể gốc"],
    ["8", "BOOKING", "Lượt đặt sân của khách", "Thực thể gốc"],
    ["9", "INVOICE", "Hóa đơn thanh toán", "Thực thể gốc"],
])
para("Vì sao tách bốn bảng loại sân? Bốn loại sân có bốn bộ phí riêng biệt, không loại nào giống loại nào: sân bóng đá có phí đèn; sân cầu lông có phí vợt và phí thảm; sân tennis có phí dụng cụ; sân pickleball có phí dụng cụ theo giờ. Nếu gộp tất cả vào bảng SPORTFIELD, mỗi dòng sẽ có 4 cột chứa giá trị rỗng không dùng đến. Tách ra thành bốn bảng riêng vừa đúng nguyên tắc chuẩn hoá, vừa tạo ra đối xứng hoàn hảo với bốn lớp con trong Java.")
ndoc.add_heading("1.2.1. Bảng CUSTOMER — khách hàng", level=3)
para("Ý nghĩa: lưu hồ sơ khách hàng và chính sách ưu đãi áp dụng cho họ.")
caption("Bảng 9. Thuộc tính bảng CUSTOMER")
add_table(["#", "Ký hiệu", "Tên thuộc tính", "Kiểu", "Ý nghĩa", "Ví dụ"], [
    ["1", "PK", "customerId", "String", "Mã khách, do người thiết kế cấp", "C001"],
    ["2", "", "fullName", "String", "Họ và tên khách", "Nguyễn Văn An"],
    ["3", "", "phoneNumber", "String", "Số điện thoại liên hệ", "0901234567"],
    ["4", "", "email", "String", "Email gửi thông báo", "an.nguyen@gmail.com"],
    ["5", "", "customerType", "Enum CustomerType", "Loại khách: STANDARD hoặc VIP", "VIP"],
    ["6", "", "discountRate", "double", "Tỷ lệ giảm của khách VIP: 0.10, 0.12, 0.15", "0.1"],
    ["7", "", "tierLevel", "int", "Cấp thành viên VIP: 1, 2, 3", "1"],
])
para("Ghi chú về cột tierLevel: thuộc tính này chỉ có nghĩa khi customerType = VIP. Với khách STANDARD, cả discountRate lẫn tierLevel đều rỗng. Trong Java, điều này được đảm bảo bằng cách đặt hai thuộc tính này vào riêng lớp VipCustomer.")
ndoc.add_heading("1.2.2. Bảng SPORTFIELD và 4 bảng loại sân", level=3)
caption("Bảng 10. Thuộc tính bảng SPORTFIELD và 4 bảng loại sân")
add_table(["#", "Ký hiệu", "Tên thuộc tính", "Kiểu", "Ý nghĩa", "Ví dụ"], [
    ["1", "PK", "fieldId", "String", "Mã sân", "F001"],
    ["2", "", "fieldName", "String", "Tên sân", "Sân bóng A"],
    ["3", "", "fieldType", "Enum FieldType", "FOOTBALL, BADMINTON, TENNIS, PICKLEBALL", "FOOTBALL"],
    ["4", "", "basePricePerHour", "double", "Giá thuê một giờ", "100000"],
    ["5", "", "status", "Enum FieldStatus", "AVAILABLE, MAINTENANCE, OCCUPIED", "AVAILABLE"],
    ["6", "PK, FK", "FOOTBALL_FIELD.fieldId + nightLightFee", "double", "Phí đèn mỗi lượt bóng đá", "50000"],
    ["7", "PK, FK", "BADMINTON_FIELD.fieldId + racketFee, matFee", "double", "Phí vợt 20000 + phí thảm 10000 mỗi lượt", "20000 / 10000"],
    ["8", "PK, FK", "TENNIS_FIELD.fieldId + courtEquipmentFee", "double", "Phí dụng cụ mỗi lượt tennis", "40000"],
    ["9", "PK, FK", "PICKLEBALL_FIELD.fieldId + equipmentFeePerHour", "double", "Phí dụng cụ pickleball tính theo giờ", "15000"],
])
para("Vì sao fieldType phải là một cột thật? Trong Java, loại sân được biết bằng toán tử instanceof; nhưng trong mô hình quan hệ thì bắt buộc phải có cột này, vì Developer B cần thống kê theo loại sân ở Q03 (đếm sân theo loại), Q08 (doanh thu theo loại), Q10 và Q13 (khách dùng nhiều loại sân). Điểm cần lưu ý: equipmentFeePerHour của pickleball tính theo giờ, còn các phí riêng còn lại đều tính theo lượt thuê.")
ndoc.add_heading("1.2.3. Bảng SERVICE — dịch vụ", level=3)
caption("Bảng 11. Thuộc tính bảng SERVICE")
add_table(["#", "Ký hiệu", "Tên thuộc tính", "Kiểu", "Ý nghĩa", "Ví dụ"], [
    ["1", "PK", "serviceId", "String", "Mã dịch vụ", "SV001"],
    ["2", "", "name", "String", "Tên dịch vụ", "Dụng cụ thể thao"],
    ["3", "", "category", "Enum ServiceCategory", "Nhóm dịch vụ", "Thiết bị"],
    ["4", "", "unitPrice", "double", "Đơn giá một lần dùng", "150000"],
    ["5", "", "active", "boolean", "Đang cung cấp hay tạm ngưng", "true"],
])
para("Vì sao category phải là enum chứ không phải chuỗi tự do? Truy vấn Q14 cần tìm khách đã dùng dịch vụ liên quan vợt nhưng chưa từng đặt sân tennis. Nếu category là chuỗi tự do thì truy vấn phải viết Contains (vợt) và dễ hỏng khi đổi cách đặt tên; đặt thành enum ServiceCategory thì lọc chính xác bằng ServiceCategory.THIET_BI.")
ndoc.add_heading("1.2.4. Bảng BOOKING — lượt đặt", level=3)
para("Ý nghĩa: ghi lại mỗi lần khách đặt sân, kèm toàn bộ chi tiết tính tiền.")
caption("Bảng 12. Thuộc tính bảng BOOKING")
add_table(["#", "Ký hiệu", "Tên thuộc tính", "Kiểu", "Ý nghĩa", "Ví dụ"], [
    ["1", "PK", "bookingId", "String", "Mã lượt đặt", "B001"],
    ["2", "FK", "customerId", "String → CUSTOMER", "Khách đặt sân", "C001"],
    ["3", "FK", "fieldId", "String → SPORTFIELD", "Sân được đặt", "F001"],
    ["4", "FK", "serviceId", "String → SERVICE", "Dịch vụ kèm theo, có thể rỗng", "SV001"],
    ["5", "", "startTime", "LocalDateTime", "Thời điểm bắt đầu", "2026-02-15T18:00"],
    ["6", "", "endTime", "LocalDateTime", "Thời điểm kết thúc", "2026-02-15T20:00"],
    ["7", "", "status", "Enum BookingStatus", "COMPLETED, CONFIRMED, CANCELLED", "COMPLETED"],
    ["8", "(tính toán)", "hours", "double", "Số giờ thuê, tính từ endTime − startTime", "2"],
    ["9", "(tính toán)", "rentalFee", "double", "Tiền thuê sân", "200000"],
    ["10", "(tính toán)", "peakSurcharge", "double", "Phí giờ cao điểm", "40000"],
    ["11", "(tính toán)", "serviceFee", "double", "Tiền dịch vụ", "30000"],
    ["12", "(tính toán)", "discount", "double", "Tiền giảm", "27000"],
    ["13", "(tính toán)", "totalAmount", "double", "Tổng tiền phải trả", "243000"],
])
para("Sáu thuộc tính tính toán không lưu trong cơ sở dữ liệu; công thức được giữ trong lớp Java và kiểm chứng bằng phép thử tự động. Sáu khoản tiền được tách riêng là có chủ ý: nếu chỉ lưu totalAmount thì báo cáo doanh thu từng khoản phí và phân tích ảnh hưởng giờ cao điểm sẽ không có dữ liệu để tính.")
ndoc.add_heading("1.2.5. Bảng INVOICE — hóa đơn", level=3)
caption("Bảng 13. Thuộc tính bảng INVOICE")
add_table(["#", "Ký hiệu", "Tên thuộc tính", "Kiểu", "Ý nghĩa", "Ví dụ"], [
    ["1", "PK", "invoiceId", "String", "Mã hóa đơn", "I001"],
    ["2", "FK", "bookingId", "String → BOOKING", "Lượt đặt sinh hóa đơn", "B001"],
    ["3", "FK", "customerId", "String → CUSTOMER", "Chụp lại khách lúc phát hành", "C001"],
    ["4", "FK", "fieldId", "String → SPORTFIELD", "Chụp lại sân lúc phát hành", "F001"],
    ["5", "", "issueDate", "LocalDate", "Ngày phát hành", "2026-02-15"],
    ["6", "", "status", "Enum InvoiceStatus", "PAID, UNPAID, REFUNDED", "PAID"],
    ["7", "", "amount", "double", "Số tiền chụp lại lúc phát hành", "243000"],
])
para("Vì sao chụp lại customerId, fieldId và amount? Đây là kỹ thuật snapshot: ba thuộc tính này có thể truy ra từ BOOKING, nhưng lưu lại giúp lập báo cáo doanh thu theo khách hoặc theo sân mà không cần nối bảng, và kết quả báo cáo không bị thay đổi khi lượt đặt được sửa về sau.")
para("Ràng buộc toàn vẹn được xác định bao gồm: các thuộc tính khóa chính không trùng lặp và không rỗng (RB1); giá cơ bản mỗi giờ lớn hơn 0 (RB2); thời gian bắt đầu nhỏ hơn thời gian kết thúc (RB3); số giờ lớn hơn 0 (RB4); trạng thái sân chỉ thuộc {AVAILABLE, MAINTENANCE, OCCUPIED} (RB5); một sân không thể có hai booking giao nhau thời gian (RB6); chiết khấu và cấp độ chỉ tồn tại trên khách VIP (RB7).")

ndoc.add_heading("1.3. Xác định mối liên kết và xây dựng ERD", level=2)
para("Danh mục liên kết giữa các thực thể gồm tám quan hệ:")
add_table(["Mã", "Quan hệ", "Tỷ lệ", "Ý nghĩa nghiệp vụ"], [
    ["L1", "SPORTFIELD → FOOTBALL_FIELD", "1 : 0..1", "Kế thừa — sân bóng đá có thêm phí đèn"],
    ["L2", "SPORTFIELD → BADMINTON_FIELD", "1 : 0..1", "Kế thừa — sân cầu lông có thêm phí vợt và phí thảm"],
    ["L3", "SPORTFIELD → TENNIS_FIELD", "1 : 0..1", "Kế thừa — sân tennis có thêm phí dụng cụ"],
    ["L4", "SPORTFIELD → PICKLEBALL_FIELD", "1 : 0..1", "Kế thừa — sân pickleball có thêm phí dụng cụ theo giờ"],
    ["L5", "CUSTOMER → BOOKING", "1 : 0..N", "Một khách đặt được nhiều lượt; mỗi lượt thuộc đúng một khách"],
    ["L6", "SPORTFIELD → BOOKING", "1 : 0..N", "Một sân phục vụ nhiều lượt; mỗi lượt chiếm đúng một sân"],
    ["L7", "SERVICE → BOOKING", "0..1 : 0..N", "Lượt đặt có thể kèm hoặc không kèm dịch vụ"],
    ["L8", "BOOKING → INVOICE", "1 : 0..1", "Mỗi lượt sinh tối đa một hóa đơn"],
])
para("Ghi chú về L5 và L6 — quan hệ nhiều–nhiều. Một khách có thể đặt nhiều sân khác nhau, và một sân có thể phục vụ nhiều khách. Về mặt toán học đây là quan hệ nhiều–nhiều. Quan hệ nhiều–nhiều không thể đặt thẳng trong ERD; phải tách ra một thực thể liên kết. BOOKING chính là thực thể liên kết đó, vì nó vừa nối hai thực thể, vừa chứa thuộc tính riêng (startTime, status).")
para("Ghi chú về L7 — quan hệ tuỳ chọn. Không phải lượt đặt nào cũng kèm dịch vụ. Khách chỉ thuê sân thì serviceId rỗng. Vì vậy đây là quan hệ tuỳ chọn, không phải bắt buộc.")
para("Sơ đồ ERD được vẽ bằng draw.io và lưu tại docs/erd.drawio, gồm hai trang: trang 1 là ERD đầy đủ 9 bảng; trang 2 là quy tắc chuyển quan hệ ER/RR thành lớp Java, bảng ánh xạ, enum, interface và công thức tính phí.")

# ================= CHƯƠNG 2 =================
add_pagebreak()
ndoc.add_heading("CHƯƠNG 2. THIẾT KẾ HƯỚNG ĐỐI TƯỢNG VÀ CƠ SỞ DỮ LIỆU", level=1)

ndoc.add_heading("2.1. Sáu quy tắc chuyển đổi ERD sang Java", level=2)
add_table(["Mã", "Quan hệ / đặc điểm ER", "Cách biểu diễn trong Java", "Lý do"], [
    ["Q1", "Bảng ER", "Một lớp, tên viết hoa chữ cái đầu, không dấu gạch", "Giữ đúng tên bảng để đối chiếu hai chiều"],
    ["Q2", "Quan hệ RR", "Lớp giữ thuộc tính tham chiếu tới lớp kia", "Tránh dư thừa; quan hệ trở thành quan hệ đối tượng"],
    ["Q3", "Thuộc tính khoá", "Khai báo private final, không có hàm thiết lập", "Thuộc tính khoá bất biến suốt vòng đời đối tượng"],
    ["Q4", "Thuộc tính enum", "Tách thành kiểu enum riêng", "Kiểm tra giá trị hợp lệ lúc biên dịch"],
    ["Q5", "Quan hệ kế thừa", "Bảng gốc → lớp trừu tượng, mỗi bảng con → lớp con ghi đè phương thức trừu tượng", "Tận dụng đa hình của Java"],
    ["Q6", "Nhóm thao tác CRUD", "Mỗi thực thể gốc có một lớp quản lý riêng giữ List", "Tách trách nhiệm, dễ kiểm thử"],
])

ndoc.add_heading("2.2. Bảng ánh xạ 9 bảng ER sang 11 lớp Java", level=2)
add_table(["Bảng ER", "Lớp Java", "Ghi chú"], [
    ["CUSTOMER", "Customer (trừu tượng) · StandardCustomer · VipCustomer", "Một bảng sinh ra ba lớp vì có hai nhóm khách khác nhau chính sách"],
    ["SPORTFIELD", "SportField (trừu tượng)", "Chỉ lớp trừu tượng, lớp con nằm ở bốn bảng dưới"],
    ["FOOTBALL_FIELD", "FootballField", "Ghi đè calculateRentalFee(), giữ nightLightFee"],
    ["BADMINTON_FIELD", "BadmintonField", "Ghi đè calculateRentalFee(), giữ racketFee, matFee"],
    ["TENNIS_FIELD", "TennisField", "Ghi đè calculateRentalFee(), giữ courtEquipmentFee"],
    ["PICKLEBALL_FIELD", "PickleballField", "Ghi đè calculateRentalFee(), giữ equipmentFeePerHour"],
    ["SERVICE", "Service", "Bảng đơn giản không phân loại, chỉ cần một lớp"],
    ["BOOKING", "Booking", "Giữ ba tham chiếu: Customer, SportField, Service"],
    ["INVOICE", "Invoice", "Giữ tham chiếu Booking, chụp lại khách, sân và tiền"],
])
para("Tổng: 9 bảng ER → 11 lớp thực thể. Ngoài ra có 4 kiểu enum trong model/, 1 enum FieldType trong dto/, 2 interface và 5 lớp quản lý.")

ndoc.add_heading("2.3. Đa hình trong lớp Booking", level=2)
para("Lớp Booking giữ reference kiểu SportField nhưng gọi đúng công thức của từng loại sân:")
plain_para("public double getRentalFee() {", size=11)
plain_para("    return field.calculateRentalFee(getHours());   // đa hình", size=11)
plain_para("}", size=11)
para("Phí giờ cao điểm chỉ áp dụng nếu sân hỗ trợ và giờ bắt đầu trong khung:")
plain_para("public double getPeakSurcharge() {", size=11)
plain_para("    if (field instanceof IPeakHourCalculable peak", size=11)
plain_para("            && peak.isPeakHour(startTime.toLocalTime())) {", size=11)
plain_para("        return peak.calculatePeakHourSurcharge(getHours());", size=11)
plain_para("    }", size=11)
plain_para("    return 0;", size=11)
plain_para("}", size=11)
para("Giảm giá theo chính sách của khách (đa hình):")
plain_para("public double getDiscount() {", size=11)
plain_para("    return customer.calculatePromotionalDiscount(getSubtotal());", size=11)
plain_para("}", size=11)

ndoc.add_heading("2.4. Cơ sở dữ liệu SQLite", level=2)
para("Tầng lưu trữ được hiện thực bằng JDBC + SQLite (sqlite-jdbc-3.46.1.3.jar). Schema gồm 5 bảng vật lý: customers, fields, services, bookings, invoices. Mặc dù ERD có 9 bảng khái niệm, cơ sở dữ liệu vật lý gộp các bảng chuyên biệt hoá của sân vào bảng fields với cột field_type và 4 cột phí riêng. Các ràng buộc được đảm bảo bằng FOREIGN KEY, CHECK, index (field_id + start_time, customer_id, status), chế độ WAL và PRAGMA foreign_keys = ON. Các thao tác ghi nhiều bảng dùng giao dịch saveAll.")
para("Cổng Java API mặc định 8080 bị Apache chiếm, nên dự án dùng 18083; web dùng 5080. Không kill httpd.exe.")

# ================= CHƯƠNG 3 =================
add_pagebreak()
ndoc.add_heading("CHƯƠNG 3. KIẾN TRÚC, MÃ NGUỒN VÀ TRUY VẤN", level=1)

ndoc.add_heading("3.1. Kiến trúc tổng thể", level=2)
para("Hệ thống gồm hai phần do hai thành viên cùng phát triển trong một repository duy nhất:")
add_table(["Phần", "Ngôn ngữ", "Trách nhiệm"], [
    ["Java Core — Developer A", "Java 22", "Toàn bộ nghiệp vụ: mô hình thực thể, tính phí, quản lý dữ liệu, xuất JSON, phục vụ REST API, lưu trữ SQLite"],
    ["Web UI — Developer B", "C# ASP.NET Core MVC (.NET 10)", "Giao diện web, 20 truy vấn LINQ thống kê, báo cáo và biểu đồ"],
])
para("Luồng dữ liệu: (1) Khởi động Java: SmartSportHubApi.main() mở SQLite, tạo schema nếu cần, nạp seed nếu DB trống, khởi động HTTP server ở cổng 18083. (2) Khởi động C#: ASP.NET Core MVC chạy ở cổng 5080, cấu hình SmartSportHubApi:BaseUrl = http://localhost:18083/. (3) Người dùng mở trình duyệt → C# controller nhận request → gọi SmartSportHubApiClient → nhận JSON → map sang DTO → chạy LINQ → render Razor view. (4) Thao tác ghi: form POST → C# controller → Java API endpoint ghi → BookingService kiểm tra nghiệp vụ + lưu SQLite → trả JSON → redirect với flash message.")
para("Nguyên tắc bất di bất dịch: chỉ có một hệ nghiệp vụ duy nhất, và nó nằm ở tầng Java. Lớp C# chỉ nhận dữ liệu dạng JSON để hiển thị và thống kê.")

ndoc.add_heading("3.2. Các endpoint API", level=2)
add_table(["Method", "URL", "Mô tả"], [
    ["GET", "/api/health", "Kiểm tra server sống + đếm entity"],
    ["GET", "/api/customers?page=1&pageSize=20", "Danh sách khách hàng"],
    ["GET", "/api/fields?page=1&pageSize=20", "Danh sách sân"],
    ["GET", "/api/services?page=1&pageSize=20", "Danh sách dịch vụ"],
    ["GET", "/api/bookings?page=1&pageSize=20", "Danh sách booking (đã tính tiền)"],
    ["GET", "/api/invoices?page=1&pageSize=20", "Danh sách hóa đơn"],
    ["GET", "/api/data", "Snapshot toàn bộ 5 tập (không phân trang)"],
    ["GET", "/api/overview", "Tổng quan nhanh: đếm entity + doanh thu"],
    ["POST", "/api/bookings", "Tạo booking mới"],
    ["PATCH", "/api/bookings/{id}/reschedule", "Đổi lịch"],
    ["POST", "/api/bookings/{id}/cancel", "Hủy booking"],
    ["POST", "/api/bookings/{id}/complete", "Hoàn thành booking"],
    ["POST", "/api/invoices/{id}/pay", "Thu tiền"],
    ["POST", "/api/invoices/{id}/refund", "Hoàn tiền"],
    ["PATCH", "/api/fields/{id}/status", "Đổi trạng thái sân"],
])

ndoc.add_heading("3.3. Cấu trúc thư mục project", level=2)
plain_para("src/smartsporthub/model/ — 11 lớp thực thể, 4 kiểu enum, FieldPricing", size=11)
plain_para("src/smartsporthub/dto/ — 5 lớp DTO, JsonDto, DtoMapper, enum FieldType", size=11)
plain_para("src/smartsporthub/interfaces/ — IPromotional, IPeakHourCalculable", size=11)
plain_para("src/smartsporthub/manager/ — CustomerManager, FieldManager, ServiceManager, BookingManager, InvoiceManager", size=11)
plain_para("src/smartsporthub/service/ — BookingService, BusinessException, DataExportService", size=11)
plain_para("src/smartsporthub/db/ — Database, Repository", size=11)
plain_para("src/smartsporthub/api/ — SmartSportHubApi, WriteApi", size=11)
plain_para("web/SmartSportHub.Web/ — phần Developer B: ASP.NET Core MVC, 9 Razor views, 20 LINQ", size=11)
plain_para("test/ — 8 suite Java; web/SmartSportHub.Web.Tests/ — 43 test C#", size=11)

ndoc.add_heading("3.4. Các câu truy vấn LINQ Q01–Q20", level=2)
add_table(["Mã", "Tên truy vấn", "Toán tử LINQ chính"], [
    ["Q01", "Sân AVAILABLE rẻ nhất", "Where, OrderBy, Select"],
    ["Q02", "Sân có giá cao hơn trung bình", "Average, Where"],
    ["Q03", "Số sân theo loại", "GroupBy, Count"],
    ["Q04", "Loại sân đắt nhất (trung bình)", "GroupBy, Average, Max"],
    ["Q05", "Sân đã được thuê (COMPLETED)", "GroupJoin, Count"],
    ["Q06", "Sân chưa có lượt hoàn thành", "Where, Any"],
    ["Q07", "Sân nhiều giờ thuê nhất", "GroupBy, Sum, Max, Join"],
    ["Q08", "Doanh thu theo loại sân", "Where, GroupBy, Sum"],
    ["Q09", "Sân vượt doanh thu trung bình", "GroupJoin, Sum, Average, Where"],
    ["Q10", "Sân có cả giờ thường và cao điểm", "GroupBy, Any, All"],
    ["Q11", "Chi tiêu từng khách", "GroupJoin, DefaultIfEmpty, Count, Sum"],
    ["Q12", "Khách nhiều giờ thuê nhất", "GroupBy, Sum, Max, Join"],
    ["Q13", "Khách thuê ≥ 2 loại sân", "GroupBy, Distinct, Count, Join"],
    ["Q14", "Thuê dụng cụ nhưng chưa đặt tennis", "Where, Any, All"],
    ["Q15", "Số booking theo giờ bắt đầu", "Where, GroupBy, Count"],
    ["Q16", "Ngày bận nhất", "GroupBy, Count, Max, Min"],
    ["Q17", "Dịch vụ phổ biến nhất", "SelectMany, GroupBy, Count, Max"],
    ["Q18", "Booking vượt mức của chính khách", "GroupBy, Average, Where, SelectMany"],
    ["Q19", "Trang 2 booking hoàn thành", "Where, OrderBy, Skip, Take"],
    ["Q20", "Đã đặt nhưng chưa hoàn thành", "Any, Where"],
])

# ================= CHƯƠNG 4 =================
add_pagebreak()
ndoc.add_heading("CHƯƠNG 4. KIỂM THỬ VÀ KẾT QUẢ", level=1)

ndoc.add_heading("4.1. Java — 113 phép thử (8 suite)", level=2)
add_table(["Suite", "Số test", "Nội dung"], [
    ["FieldDomainTest", "15", "Tạo 4 loại sân, đa hình, phí cao điểm, validation, FieldManager"],
    ["BookingDomainTest", "11", "Tạo khách Standard/VIP, tính tổng booking, validation, Invoice, BookingManager"],
    ["SeedDataValidationTest", "18", "Số lượng entity, đủ 4 loại sân, trạng thái booking/hóa đơn, doanh thu khớp"],
    ["JsonContractTest", "9", "Snapshot 5 tập dữ liệu, thứ tự key DTO"],
    ["ApiServerTest", "11", "Endpoint đọc, phân trang, mã lỗi 400/404/405"],
    ["DatabaseTest", "9", "Schema, lưu/đọc, FK, WAL"],
    ["BookingServiceTest", "22", "Đặt/hủy/đổi/hoàn thành/thu tiền/hoàn tiền, chặn trùng lịch"],
    ["WriteApiTest", "18", "Endpoint ghi, mã HTTP, validation"],
])
para("Chạy: .\\run-video.ps1 -Role Build → \"All Java tests passed: 113/113\". Biên dịch với -Xlint:all cho 0 cảnh báo.")

ndoc.add_heading("4.2. C# — 43 phép thử", level=2)
para("Chạy: .\\run-video.ps1 -Role Test → \"Passed! 43/43\".")

ndoc.add_heading("4.3. Kiểm chứng live", level=2)
para("Đặt sân trên web → Java kiểm tra sân/thời gian, tính phí, tạo invoice, lưu SQLite; số booking tăng từ 12 → 13. Hủy booking → trạng thái CANCELLED. Đặt vào sân đang bảo trì → Java chặn với mã 409 (\"Sân ... đang bảo trì, chưa thể đặt.\"). Tổng doanh thu booking PAID = 1.887.200 đồng (seed ban đầu).")

# ================= KẾT LUẬN =================
add_pagebreak()
ndoc.add_heading("KẾT LUẬN", level=1)
para("SmartSportHub là một hệ thống thông tin quản lý hoàn chỉnh ở mức bài tập lớn — Java đảm nhiệm mô hình hóa, nghiệp vụ, lưu trữ và API; C# đảm nhiệm giao diện và 20 truy vấn LINQ phục vụ báo cáo. Sản phẩm không chỉ quản lý việc đặt sân mà còn cung cấp thông tin ra quyết định: doanh thu theo loại sân, mức độ sử dụng sân, hành vi khách hàng.")
para("Các mục tiêu M1–M6 đã được hiện thực và kiểm chứng bằng 113 phép thử Java và 43 phép thử C#. Các vấn đề V1–V4 của quản lý thủ công đã được giải quyết: tra cứu sân trống, tính tiền chính xác, lập báo cáo đa chiều và quản lý hồ sơ khách hàng.")

# ================= TÀI LIỆU THAM KHẢO =================
add_pagebreak()
ndoc.add_heading("TÀI LIỆU THAM KHẢO", level=1)
refs = [
    "Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). Design patterns: Elements of reusable object-oriented software. Addison-Wesley.",
    "Nguyễn, M. K. (2026). Báo cáo đồ án nhóm SmartSportHub. Trường Đại học Công nghệ Kỹ thuật TP. Hồ Chí Minh.",
    "Oracle. (2024). Java SE 22 documentation. https://docs.oracle.com/en/java/javase/22/",
    "Microsoft. (2024). ASP.NET Core MVC documentation. https://learn.microsoft.com/aspnet/core/mvc/",
    "SQLite Consortium. (2024). SQLite JDBC driver 3.46.1.3. https://github.com/xerial/sqlite-jdbc",
    "OWASP Foundation. (2025). OWASP Top 10. https://owasp.org/Top10/",
]
for r in refs:
    p = ndoc.add_paragraph(); p.paragraph_format.first_line_indent = Cm(-1.0); p.paragraph_format.left_indent = Cm(1.0)
    run = p.add_run(r); run.font.size = Pt(13); run.font.name = 'Times New Roman'

# ================= PHỤ LỤC =================
add_pagebreak()
ndoc.add_heading("PHỤ LỤC A. CÔNG THỨC TÍNH PHÍ", level=1)
para("Khung giờ cao điểm: [17:00, 20:00) — tỷ lệ phụ phí 20%.")
add_table(["Hằng số", "Giá trị"], [
    ["FOOTBALL_NIGHT_LIGHT_FEE", "50.000"],
    ["BADMINTON_RACKET_FEE", "20.000"],
    ["BADMINTON_MAT_FEE", "10.000"],
    ["TENNIS_EQUIPMENT_FEE", "40.000"],
    ["PICKLEBALL_EQUIPMENT_FEE_PER_HOUR", "15.000"],
])
add_table(["Lớp", "Công thức phí thuê sân"], [
    ["FootballField", "basePricePerHour × giờ + 50.000"],
    ["BadmintonField", "basePricePerHour × giờ + 20.000 + 10.000"],
    ["TennisField", "basePricePerHour × giờ + 40.000"],
    ["PickleballField", "(basePricePerHour + 15.000) × giờ"],
])
para("Tổng tiền một lượt đặt:")
plain_para("rentalFee       = phí thuê sân theo công thức của lớp con", size=11)
plain_para("peakSurcharge   = basePricePerHour × giờ × 0.20   (nếu 17:00 ≤ giờ bắt đầu < 20:00)", size=11)
plain_para("serviceFee      = unitPrice của dịch vụ           (bằng 0 nếu không kèm dịch vụ)", size=11)
plain_para("discount        = calculatePromotionalDiscount(rentalFee + peakSurcharge + serviceFee)", size=11)
plain_para("totalAmount     = rentalFee + peakSurcharge + serviceFee − discount", size=11)

ndoc.add_heading("PHỤ LỤC B. DANH MỤC FILE TRONG DỰ ÁN", level=1)
add_table(["Đường dẫn", "Nội dung"], [
    ["docs/erd.drawio", "File ERD draw.io, 2 trang"],
    ["docs/DAC_TA_THIET_KE.md", "Đặc tả bản thiết kế"],
    ["docs/KICH_BAN_VIDEO.md", "Kịch bản quay video báo cáo"],
    ["data/dataset.json", "Dữ liệu mẫu xuất ra JSON"],
    ["API_CONTRACT.md", "Hợp đồng API cho Developer B"],
    ["src/smartsporthub/model/", "11 lớp thực thể, 4 kiểu enum, FieldPricing"],
    ["src/smartsporthub/dto/", "5 lớp DTO, JsonDto, DtoMapper, enum FieldType"],
    ["src/smartsporthub/interfaces/", "2 interface"],
    ["src/smartsporthub/manager/", "5 lớp quản lý"],
    ["src/smartsporthub/service/", "BookingService, BusinessException, DataExportService"],
    ["src/smartsporthub/db/", "Database, Repository"],
    ["src/smartsporthub/api/", "SmartSportHubApi, WriteApi"],
    ["lib/sqlite-jdbc-3.46.1.3.jar", "Driver JDBC SQLite (có local, không commit)"],
    ["build.sh / build.ps1", "Biên dịch + chạy toàn bộ kiểm thử"],
    ["run-video.ps1", "Script chạy Java API / C# Web / test trên Windows PowerShell"],
    ["web/", "Phần Developer B — ASP.NET Core MVC"],
])

ndoc.add_heading("PHỤ LỤC C. CÁCH CHẠY TRÊN WINDOWS POWERSHELL", level=1)
plain_para("cd \"D:\\New folder\\OOP_GK\"", size=11)
plain_para("# Terminal 1 — Java API", size=11)
plain_para(".\\run-video.ps1 -Role Api", size=11)
plain_para("# Terminal 2 — C# Web", size=11)
plain_para(".\\run-video.ps1 -Role Web", size=11)
plain_para("# Terminal 3 — test", size=11)
plain_para(".\\run-video.ps1 -Role Build", size=11)
plain_para(".\\run-video.ps1 -Role Test", size=11)
plain_para(".\\run-video.ps1 -Role Open", size=11)
para("Script tự tìm dotnet.exe tại D:\\dev\\dotnet khi dotnet không có trong PATH, tự tìm java/javac, tự tải driver SQLite nếu thiếu.")

ndoc.save(OUT)
print("saved", OUT)