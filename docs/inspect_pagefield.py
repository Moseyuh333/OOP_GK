# -*- coding: utf-8 -*-
import sys
sys.path.insert(0, r'C:/Users/MINH KHOI/AppData/Local/hermes/tools')
from docx import Document
p = r'C:/Users/MINH KHOI/AppData/Local/hermes/attachments/DeCuongChiTiet_Nhom5_23162026_23162046_NoiDung_v4_CoHinhAnh_APA.docx'
d = Document(p)
for s in d.sections:
    x = s.footer._element.xml
    print('PAGE' in x, 'NUMPAGES' in x)
    import re
    print(re.findall(r'w:instr="[^"]*"', x)[:5])
    print(re.findall(r'<w:instrText[^>]*>(.*?)</w:instrText>', x)[:5])