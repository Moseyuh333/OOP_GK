# -*- coding: utf-8 -*-
import sys, re
sys.path.insert(0, r'C:/Users/MINH KHOI/AppData/Local/hermes/tools')
from docx import Document
p = r'C:/Users/MINH KHOI/AppData/Local/hermes/attachments/DeCuongChiTiet_Nhom5_23162026_23162046_NoiDung_v4_CoHinhAnh_APA.docx'
d = Document(p)
tb = d.tables[0]
for c in tb.rows[0].cells:
    xml = c._tc.xml
    print('shd:', re.findall(r'<w:shd[^>]*/>', xml))
    print('sz:', re.findall(r'<w:sz w:val="[^"]*"/>', xml)[:3])
    print('---')