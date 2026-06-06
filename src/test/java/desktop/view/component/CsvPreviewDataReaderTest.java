package desktop.view.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvPreviewDataReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void readsQuotedMultilineFieldsAndStopsAtPreviewLimit() throws Exception {
        Path csv = tempDir.resolve("preview.csv");
        Files.writeString(csv, "name,notes\n"
            + "North,\"line one\nline two\"\n"
            + "South,\"quoted \"\"value\"\"\"\n");

        List<String[]> rows = CsvPreviewDataReader.read(csv, 2);

        assertEquals(2, rows.size());
        assertEquals(List.of("name", "notes"), List.of(rows.get(0)));
        assertEquals(List.of("North", "line one\nline two"), List.of(rows.get(1)));
    }
}
