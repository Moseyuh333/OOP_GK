# -*- coding: utf-8 -*-
import sys
sys.path.insert(0, r'C:/Users/MINH KHOI/AppData/Local/hermes/tools')
from docx import Document
p = r'C:/Users/MINH KHOI/AppData/Local/hermes/attachments/DeCuongChiTiet_Nhom5_23162026_23162046_NoiDung_v4_CoHinhAnh_APA.docx'
d = Document(p)
body = d.element.body
xml = body.xml
print('page breaks:', xml.count('w:br w:type="page"'))
# find index of first heading paragraph in body
import re
# print first 20 body child tags
from docx.oxml.ns import qn
children = list(body)
print('num body children:', len(children))
for c in children[:18]:
    print(c.tag.split('}')[-1])