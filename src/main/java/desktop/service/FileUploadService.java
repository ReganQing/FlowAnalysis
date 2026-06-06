package desktop.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * 文件上传服务 — 验证、存储上传的数据文件。
 * <p>
 * 支持 .csv / .xls / .xlsx 格式，最大 50MB。
 * 文件被复制到 {@code output/uploads/} 工作目录。
 * Excel 文件自动转换为 CSV 以便管线处理。
 */
public class FileUploadService {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(".csv", ".xls", ".xlsx");
    private final Path uploadDir;

    public FileUploadService() {
        this(Path.of("output", "uploads"));
    }

    FileUploadService(Path uploadDir) {
        this.uploadDir = uploadDir;
    }

    /**
     * 验证并存储上传的文件。
     * <p>
     * 对于 .xls/.xlsx 文件，自动转换为 .csv 格式。
     *
     * @param sourcePath 原始文件路径
     * @return 工作文件路径（在 output/uploads/ 下，始终为 .csv）
     * @throws IOException 文件操作失败
     * @throws IllegalArgumentException 文件不合法（格式/大小）
     */
    public Path uploadFile(Path sourcePath) throws IOException {
        // 验证扩展名
        String fileName = sourcePath.getFileName().toString();
        String extension = getExtension(fileName).toLowerCase();
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                "不支持的文件格式: " + extension + "。仅支持 .csv .xls .xlsx 文件。");
        }

        // 验证文件存在
        if (!Files.exists(sourcePath)) {
            throw new IOException("文件不存在: " + sourcePath);
        }

        // 验证大小
        long fileSize = Files.size(sourcePath);
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("文件过大 (%.1fMB)，最大支持 50MB。", fileSize / (1024.0 * 1024.0)));
        }

        // 确保上传目录存在
        Files.createDirectories(uploadDir);

        // 生成唯一文件名
        long timestamp = System.currentTimeMillis();

        // Excel 文件需要转换为 CSV
        if (extension.equals(".xls") || extension.equals(".xlsx")) {
            String csvName = timestamp + "_" + getBaseName(fileName) + ".csv";
            Path csvPath = uploadDir.resolve(csvName);
            convertExcelToCsv(sourcePath, csvPath, extension);
            return csvPath;
        }

        // CSV 直接复制
        String uniqueName = timestamp + "_" + fileName;
        Path targetPath = uploadDir.resolve(uniqueName);
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath;
    }

    /**
     * 判断文件扩展名是否受支持。
     */
    public boolean isSupported(Path filePath) {
        String ext = getExtension(filePath.getFileName().toString()).toLowerCase();
        return SUPPORTED_EXTENSIONS.contains(ext);
    }

    /**
     * 将 Excel 文件转换为 CSV。
     */
    private void convertExcelToCsv(Path excelPath, Path csvPath, String extension) throws IOException {
        try (InputStream is = Files.newInputStream(excelPath);
             Workbook workbook = extension.equals(".xlsx")
                 ? new XSSFWorkbook(is)
                 : new HSSFWorkbook(is);
             Writer writer = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

            // 只转换第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            int columnCount = findColumnCount(sheet);

            for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                java.util.List<String> values = new java.util.ArrayList<>(columnCount);
                for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                    Cell cell = row == null ? null : row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    values.add(cell == null ? "" : formatter.formatCellValue(cell, evaluator));
                }
                printer.printRecord(values);
            }
        }
    }

    private static int findColumnCount(Sheet sheet) {
        int columnCount = 0;
        for (Row row : sheet) {
            columnCount = Math.max(columnCount, row.getLastCellNum());
        }
        return columnCount;
    }

    private static String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex >= 0 ? fileName.substring(dotIndex) : "";
    }

    private static String getBaseName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex >= 0 ? fileName.substring(0, dotIndex) : fileName;
    }
}
