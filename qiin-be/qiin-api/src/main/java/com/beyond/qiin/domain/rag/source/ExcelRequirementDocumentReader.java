package com.beyond.qiin.domain.rag.source;

import com.beyond.qiin.domain.rag.dto.ParsedDocument;
import com.beyond.qiin.domain.rag.dto.RequirementRow;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

@Component
public class ExcelRequirementDocumentReader {

    private static final int HEADER_ROW_INDEX = 0;
    private static final String REQUIREMENT_SHEET_NAME = "요구사항정의서";
    private static final String FALLBACK_FINAL_REQUIREMENT_HEADER = "변경사항";

    private final DataFormatter dataFormatter = new DataFormatter();

    public ParsedDocument read(String title, String sourceUrl, Path excelPath) {
        List<RequirementRow> rows = readRows(excelPath);

        return new ParsedDocument(title, sourceUrl, rows);
    }

    private List<RequirementRow> readRows(Path excelPath) {
        try (InputStream inputStream = Files.newInputStream(excelPath);
                Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheet(REQUIREMENT_SHEET_NAME);
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            Map<String, Integer> headerIndexes = readHeaderIndexes(sheet.getRow(HEADER_ROW_INDEX));
            List<RequirementRow> rows = new ArrayList<>();

            for (int rowIndex = HEADER_ROW_INDEX + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row excelRow = sheet.getRow(rowIndex);
                if (excelRow == null) {
                    continue;
                }

                RequirementRow row = toRequirementRow(excelRow, headerIndexes);
                if (!isEmptyRow(row)) {
                    rows.add(row);
                }
            }

            return rows;
        } catch (Exception e) {
            throw new IllegalStateException("엑셀 요구사항 문서를 읽지 못했습니다: " + excelPath, e);
        }
    }

    private Map<String, Integer> readHeaderIndexes(Row headerRow) {
        if (headerRow == null) {
            throw new IllegalStateException("엑셀 요구사항 문서의 헤더 행이 없습니다.");
        }

        Map<String, Integer> headerIndexes = new HashMap<>();

        for (Cell cell : headerRow) {
            String header = normalize(readCell(cell));
            if (!header.isBlank()) {
                headerIndexes.put(header, cell.getColumnIndex());
            }
        }

        return headerIndexes;
    }

    private RequirementRow toRequirementRow(Row row, Map<String, Integer> headerIndexes) {
        return new RequirementRow(
                get(row, headerIndexes, "업무그룹"),
                get(row, headerIndexes, "권한"),
                get(row, headerIndexes, "요구사항 ID"),
                get(row, headerIndexes, "요구사항명"),
                get(row, headerIndexes, "기능 요구사항"),
                getFinalRequirement(row, headerIndexes),
                get(row, headerIndexes, "화면 요구사항"),
                get(row, headerIndexes, "보안/기술 요구사항"));
    }

    private String getFinalRequirement(Row row, Map<String, Integer> headerIndexes) {
        String finalRequirement = get(row, headerIndexes, "최종 요구사항");
        if (!finalRequirement.isBlank()) {
            return finalRequirement;
        }

        return get(row, headerIndexes, FALLBACK_FINAL_REQUIREMENT_HEADER);
    }

    private String get(Row row, Map<String, Integer> headerIndexes, String header) {
        Integer cellIndex = headerIndexes.get(normalize(header));
        if (cellIndex == null) {
            return "";
        }

        return normalizeValue(readCell(row.getCell(cellIndex)));
    }

    private String readCell(Cell cell) {
        return cell == null ? "" : dataFormatter.formatCellValue(cell);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeValue(String value) {
        String normalized = normalize(value);
        return "-".equals(normalized) ? "" : normalized;
    }

    private boolean isEmptyRow(RequirementRow row) {
        return isBlank(row.requirementId()) && isBlank(row.requirementName()) && isBlank(row.resolvedRequirement());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
