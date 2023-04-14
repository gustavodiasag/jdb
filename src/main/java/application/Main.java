package main.java.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

import main.java.database.*;

public class Main {
    public static void main(String[] args) {

        // Database file.
        File file = new File("db.bin");
        
        try {
            Database db = new Database(file, 1);
            
            System.out.println("\nDatabase successfully initialized.");
            
            prompt(db);

        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
    
    private static void prompt(Database db) throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        
        while (true) {
            System.out.print("\n"
                + "Choose an operation:\n"
                + "\n1) Select record by ID"
                + "\n2) Insert record"
                + "\n3) Update record by ID"
                + "\n4) Delete record by ID"
                + "\n5) Sort records"
                + "\n6) Quit"
                + "\n\n");
            
            String line = "";
            
            while (!validChoice(line)) {
                System.out.print("> ");
                line = reader.readLine();
            }
            
            switch (line) {
                case "1":
                    selectionHelper(db, reader);
                    break;
                case "2":
                    insertionHelper(db, reader);
                    break;
                case "3":
                    updationHelper(db, reader);
                case "4":
                    deletionHelper(db, reader);
                    break;
                case "5":
                    sortHelper(db, reader);
                    break;
                case "6":
                    return;
            }
        }
    }
    
    private static void selectionHelper(Database db, BufferedReader reader)
        throws IOException {
        
        System.out.println("\nDefine the ID to be retrieved:\n");
        
        String line = "";

        while (!validInt(line)) {
            System.out.print("> ");
            line = reader.readLine();
        }
        
        Record record = db.get(Integer.parseInt(line));
        
        if (record == null) {
            System.out.println("\nRecord with ID " + line + " does not exist!");
            return;
        }
            
        System.out.println("\nRecord successfuly found:\n" + record.toString());
    }
    
    private static void insertionHelper(Database db, BufferedReader reader)
        throws IOException {
        
        System.out.println("\nDefine the record to be inserted:\n"
            + "\n\"name,score,\"genres (list)\",episodes,\"producers (list)\",date (year-month-day)\"\n");
        
        String line = "";
        Record record = null;
        
        while (record == null) {
            System.out.print("> ");
            line = reader.readLine();
            record = validRecord(line);
        }
        
        boolean result = db.insert(record);

        if (!result) {
            System.out.println("\nRecord could not be inserted to the database!");
            return;
        }

        System.out.println("\nRecord successfully inserted to the database!");
    }

    private static void updationHelper(Database db, BufferedReader reader)
        throws IOException {

        System.out.println("\nDefine the ID to be updated:\n");

        String line = "";

        while (!validInt(line)) {
            System.out.print("> ");
            line = reader.readLine();
        }

        int id = Integer.parseInt(line);

        System.out.println("\n"
            + "Update:\n"
            + "\n\"name,score,\"genres (list)\",episodes,\"producers (list)\",date (year-month-day)\""
            + "\n");

        Record record = null;

        while (record == null) {
            System.out.print("> ");
            line = reader.readLine();
            record = validRecord(line);
        }

        record.setId(id);

        boolean result = db.update(record);

        if (!result) {
            System.out.println("\nUnable to update record with ID " + id);
            return;
        }

        System.out.println("\nRecord successfully updated!");
    }
    
    private static void deletionHelper(Database db, BufferedReader reader)
        throws IOException {
        
        System.out.println("\nDefine the ID to be deleted:\n");
        
        String line = "";
        
        while (!validInt(line)) {
            System.out.print("> ");
            line = reader.readLine();
        }
        
        boolean result = db.delete(Integer.parseInt(line));
        
        if (!result) {
            System.out.println("\nUnable to delete record with ID " + line);
            return;
        }
            
        System.out.println("\nRecord successfully deleted!");
    }
    
    private static void sortHelper(Database db, BufferedReader reader)
        throws IOException {
        
        System.out.println("\nDefine the number of blocks:\n");
        
        String line = "";
        
        while (!validInt(line)) {
            System.out.print("> ");
            line = reader.readLine();
        }
        
        System.out.println("\nUse the optimized version? (y/n)\n");
        
        String version = "";
        
        while (!validAnswer(version)) {
            System.out.print("> ");
            version = reader.readLine();
        }
        
        db.sort(Integer.parseInt(line), (version.compareTo("y") == 0) ? true : false);
        
        db.show();
    }
    
    private static boolean validChoice(String input) {
        return input.length() == 1 && input.matches("[1-6]");
    }
    
    private static Record validRecord(String input) {
        try {
            return CSVParser.buildFrom(input);
            
        } catch (ParseException e) {
            return null;
        }
    }
    
    private static boolean validAnswer(String input) {
        return input.compareTo("y") == 0 || input.compareTo("n") == 0;
    }
    
    private static boolean validInt(String attribute) {
        return attribute.matches("[0-9]+");
    }
}