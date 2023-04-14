package main.java.database;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/*
 * Handles the csv file separately to generate the
 * respective entities from its content.
 */
public class CSVParser {
    /*
     * There's no reason for the source file to be provided
     * by the user.
     */
   private static final String csvPathRelease = "src/main/resources/data.csv";
   private static final String csvPathTest = "src/test/resources/test.csv";
    
    public static Record[] parse(int version) {
        
        try {
            List<String> lines = Files
                .readAllLines(
                    Paths.get((version == 2) ? csvPathTest : csvPathRelease),
                    Charset.forName("UTF-8"));
            
            Record[] records = new Record[lines.size()];
            
            int i = 0;
            for (String line : lines)
                records[i++] = buildFrom(line);
            
            return records;

        } catch (IOException e) {
            System.err.println(e.getMessage());
            
        } catch (ParseException e) {
            System.err.println(
                "Unable to parse csv lines: " + e.getLocalizedMessage());
        }
        
        return null;
    }
   
    // Returns the object generated from a raw file line.
    public static Record buildFrom(String line) throws ParseException {
        /*
         * Separates the line's content by commas ignoring the
         * ones inside double quotes.
         */
        String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        
        if (fields.length == 6)
            return buildFromInput(fields);
        
        // At this moment, all records generated are valid ones.
        boolean valid = true;
        
        int id = Integer.parseInt(fields[0]);
        
        /*
         * After the previous regular expression, textual
         * fields remain as "foo" or "foo, bar".
         */
        String name = fields[1].replace("\"", "");
        float score = toNumber(fields[2]);
        
        String[] genres = fields[3]
            .replace("\"", "")
            .split(",");

        int episodes = (int)toNumber(fields[4]);

        String[] producers = fields[5]
            .replace("\"", "")
            .split(",");

        Date date = toDate(fields[6]);

        return new Record(
            valid,
            id,
            name,
            score,
            genres,
            episodes,
            producers,
            date
        );
    }
    
    private static Record buildFromInput(String[] fields)
        throws ParseException {
        
        boolean valid = true;
        
        String name = fields[0].replace("\"", "");
        float score = toNumber(fields[1]);
        
        String[] genres = fields[2]
            .replace("\"", "")
            .split(",");

        int episodes = (int)toNumber(fields[3]);

        String[] producers = fields[4]
            .replace("\"", "")
            .split(",");

        Date date = toDate(fields[5]);

        return new Record(
            valid,
            0,
            name,
            score,
            genres,
            episodes,
            producers,
            date
        );
    }

    /*
     * Fields that should only contain numeric values don't
     * always provide one, except for the "id" column.
     */
    private static float toNumber(String field) throws ParseException {
        return (field.matches("[0-9]+|[0-9]+.[0-9]+"))
            ? Float.parseFloat(field)
            : 0;
    }
    
    // Dates are stored in the format year-month-day.
    private static Date toDate(String field) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(field);
    }
}

