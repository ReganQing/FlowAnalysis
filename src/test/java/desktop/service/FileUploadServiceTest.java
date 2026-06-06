package desktop.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUploadServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void convertsExcelWithoutCollapsingEmptyColumnsAndUsesDisplayedValues() throws Exception {
        Path excelPath = tempDir.resolve("sales.xlsx");
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("sales");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("name");
            header.createCell(1).setCellValue("empty");
            header.createCell(2).setCellValue("amount");

            var row = sheet.createRow(1);
            row.createCell(0).setCellValue("North");
            row.createCell(2).setCellFormula("20+22");

            try (var output = Files.newOutputStream(excelPath)) {
                workbook.write(output);
            }
        }

        FileUploadService service = new FileUploadService(tempDir.resolve("uploads"));
        Path csvPath = service.uploadFile(excelPath);

        try (Reader reader = Files.newBufferedReader(csvPath)) {
            List<CSVRecord> records = CSVFormat.DEFAULT.parse(reader).getRecords();
            assertEquals(List.of("name", "empty", "amount"), records.get(0).toList());
            assertEquals(List.of("North", "", "42"), records.get(1).toList());
        }
    }
}
