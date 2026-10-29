# -*- coding: utf-8 -*-
import re
lines = open(r'D:/New folder/OOP_GK/docs/BAO_CAO_DU_AN.md', encoding='utf-8').read().splitlines()
for l in lines:
    if 'Tổng quan hệ thống' in l and l.strip().startswith('-'):
        print(repr(l))
        print(re.sub(r'^- \[([^\]]+)\]\([^)]*\)$', r'- \1', l))