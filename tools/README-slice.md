# Slicing a sheet

    pip install pillow
    python3 tools/slice_sheet.py sheets-src/sheet01.png tools/sheets/sheet01.txt app/src/main/res/drawable-nodpi

Names per icon: `tools/sheets/sheetNN.txt` (row-major). Output: `<name>.webp`, 256 px, transparent.
Any flat background color is removed automatically. Total icon weight budget: 1 MB.
