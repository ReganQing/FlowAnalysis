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
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUploadServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void uploadedCsvPathShouldBeAbsolute(@TempDir Path absoluteTempDir) throws Exception {
        // 用相对目录构造，模拟生产中 Path.of("output","uploads")
        Path sourceCsv = Files.createTempFile(absoluteTempDir, "sample", ".csv");
        Files.writeString(sourceCsv, "category,value\nA,1\n");

        FileUploadService service = new FileUploadService(Path.of("output", "uploads"));

        Path result = service.uploadFile(sourceCsv);

        assertTrue(result.isAbsolute(),
            "上传返回路径必须为绝对路径，否则在不同工作目录下无法被预览面板定位；实际: " + result);
    }

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
