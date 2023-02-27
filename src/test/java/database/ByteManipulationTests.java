package test.java.database;

import static org.junit.jupiter.api.Assertions.*;

import main.java.database.*;
import main.java.database.Record;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

// Related to all the methods implemented within the Database class
public class ByteManipulationTests {
    // For the sake of tests, destination file is already defined
    private final String binPath = "src/test/test.bin";

    @Test
    public void testDeletion() throws IOException {
        try {
            File file = new File(binPath);
            Database db = new Database(file);
            
            assertEquals(true, db.delete(18));
            
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    @Test
    public void testSelection() throws IOException {
        try {
            File file = new File(binPath);
            Database db = new Database(file);
            
            assertEquals("Ring ni Kakero 1", db.get(23).getName());
            
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    @Test 
    public void testInsertion() throws IOException {
        try {
            File file = new File(binPath);
            Database db = new Database(file);
            
            Record record = CSVParser.buildFrom(
                "23729,foo,Unknown,\"Comedy, Fantasy, Kids\",1,Unknown,2023-02-17"
            );
            
            db.insert(record);
                
            assertEquals(
                record.getName(),
                db.get(record.getId()).getName());
            
        } catch (IOException e) {
            System.out.println(e.getMessage());
            
        } catch (ParseException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }
}
