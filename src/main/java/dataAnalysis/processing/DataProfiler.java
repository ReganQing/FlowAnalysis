package dataAnalysis.processing;

import dataAnalysis.model.ColumnProfile;
import dataAnalysis.model.DataProfile;
import tech.tablesaw.api.Table;
import tech.tablesaw.api.NumberColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.DateColumn;
import tech.tablesaw.columns.Column;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据概况扫描器
 * 轻量级扫描，生成数据概况摘要
 */
public class DataProfiler {

    public DataProfile profile(Table table) {
        List<ColumnProfile> columnProfiles = new ArrayList<>();
        List<String> numericCols = new ArrayList<>();
        List<String> categoricalCols = new ArrayList<>();
        List<String> dateCols = new ArrayList<>();

        for (Column<?> col : table.columns()) {
            String name = col.name();
            String type = col.type().name();
            int missing = col.countMissing();
            int unique = (col instanceof StringColumn sc) ? sc.unique().size() : 0;
            double missingRate = table.rowCount() > 0 ? (double) missing / table.rowCount() : 0;

            columnProfiles.add(new ColumnProfile(name, type, missing, unique, missingRate));

            if (col instanceof NumberColumn) {
                numericCols.add(name);
            } else if (col instanceof DateColumn) {
                dateCols.add(name);
            } else {
                categoricalCols.add(name);
            }
        }

        return new DataProfile(
            table.rowCount(),
            table.columnCount(),
            columnProfiles,
            numericCols,
            categoricalCols,
            dateCols
        );
    }
}
