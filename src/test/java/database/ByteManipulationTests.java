package test.java.database;


import main.java.database.*;
import main.java.database.Record;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

// Related to all the methods implemented within the Database class.
public class ByteManipulationTests {
    // For the sake of tests, destination file is already defined.
    private final String binPath = "src/test/test.bin";

    @Test
    public void testDeletion() {
        try {
            Database db = new Database(new File(binPath));
            
            assertEquals(true, db.delete(15));
            
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    @Test
    public void testSelection() {
        try {
            Database db = new Database(new File(binPath));
            
            // To be validated with the expected values.
            Record actual = db.get(23);
            
            assertEquals(true, actual.getValid());
            assertEquals(23, actual.getId());
            assertEquals("Ring ni Kakero 1", actual.getName());
            assertEquals(6.38F, actual.getScore(), 0.00f);
            
            // Arrays cannot be directly compared.
            assertArrayEquals(
                new String[] {"Action", " Shounen", " Sports"},
                actual.getGenres()
            );
            assertEquals(12, actual.getEpisodes());            
            assertArrayEquals(new String[] {"Unknown"}, actual.getProducers());
            
            assertEquals(
                new SimpleDateFormat("yyyy-MM-dd").parse("2023-02-17"),
                actual.getDate()
            );
            
        } catch (IOException e) {
            System.err.println(e.getMessage());
            
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        }
    }
    
    
    @Test 
    public void testInsertion() {
        try {
            Database db = new Database(new File(binPath));
            
            // Random record to be inserted
            Record record = CSVParser
                .buildFrom("23,foo,bar,\"baz, qux, quuz\",1,corge,2023-02-17");
            
            assertEquals(true, db.insert(record));
            
        } catch (IOException e) {
            System.out.println(e.getMessage());
            
        } catch (ParseException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }
}
