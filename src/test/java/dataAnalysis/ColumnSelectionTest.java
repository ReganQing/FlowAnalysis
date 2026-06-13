package dataAnalysis;

import dataAnalysis.model.DataProfile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ColumnSelectionTest {

    private DataProfile profile() {
        return new DataProfile(
            10, 5,
            List.of(),
            List.of("amount", "quantity"),          // numericColumns
            List.of("region"),                       // categoricalColumns
            List.of("date")                          // dateColumns
        );
    }

    @Test
    void firstDateReturnsFirstDateColumn() {
        assertEquals("date", ColumnSelection.firstDate(profile()));
    }

    @Test
    void firstNumericReturnsFirstNumericColumn() {
        assertEquals("amount", ColumnSelection.firstNumeric(profile()));
    }

    @Test
    void secondNumericReturnsSecondNumericColumn() {
        assertEquals("quantity", ColumnSelection.secondNumeric(profile()));
    }

    @Test
    void firstCategoricalReturnsFirstCategoricalColumn() {
        assertEquals("region", ColumnSelection.firstCategorical(profile()));
    }

    @Test
    void helpersReturnNullWhenEmpty() {
        DataProfile empty = new DataProfile(0, 0, List.of(), List.of(), List.of(), List.of());
        assertNull(ColumnSelection.firstDate(empty));
        assertNull(ColumnSelection.firstNumeric(empty));
        assertNull(ColumnSelection.secondNumeric(empty));
        assertNull(ColumnSelection.firstCategorical(empty));
    }
}
