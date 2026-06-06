package desktop.view.component;

import org.apache.commons.csv.CSVFormat;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class CsvPreviewDataReader {

    private CsvPreviewDataReader() {
    }

    static List<String[]> read(Path path, int maxRecords) throws Exception {
        List<String[]> rows = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             var parser = CSVFormat.DEFAULT.parse(reader)) {
            for (var record : parser) {
                rows.add(record.values());
                if (rows.size() >= maxRecords) {
                    break;
                }
            }
        }
        return rows;
    }
}
