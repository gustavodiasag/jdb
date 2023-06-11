package main.java.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

import main.java.algorithms.KMP.KMP;
import main.java.database.*;

public class Main {
    static File file;
    public static void main(String[] args) {

        // Database file.
        Main.file = new File("db.bin");
        
        try {
            Database db = new Database(file);
            
            db.build();

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
                + "\n6) B-tree search"
                + "\n7) Hash search"
                + "\n8) Select record by attributes"
                + "\n9) Compress DB"
                + "\n10) Pattern match"
                + "\n11) Quit"
                + "\n\n");
            
            String line = "";
            
            while (!validChoice(line, 10)) {
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
                    break;
                case "4":
                    deletionHelper(db, reader);
                    break;
                case "5":
                    sortHelper(db, reader);
                    break;
                case "6":
                    treeHelper(db, reader);
                    break;
                case "7":
                    hashHelper(db, reader);
                    break;
                case "8":
                    indexHelper(db, reader);
                    break;
                case "9":
                    String option;
                    System.out.println("Select algorithm to compress: ");
                    System.out.println("1) Huffman");
                    System.out.println("2) LZW");
                    System.out.print("> ");
                    option = reader.readLine();
                    db.compress((Integer.parseInt(option)), file);
                    break;
                case "10":
                    String patter;
                    System.out.println("Enter pattern to match: ");
                    System.out.print("> ");
                    patter = reader.readLine();
                    KMP.search(patter);
                    break;
                case "11":
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
            record = getRecord(line);
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
            record = getRecord(line);
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
    
    private static void treeHelper(Database db, BufferedReader reader)
        throws IOException {
        
        System.out.println("\nDefine the ID to be retrieved:\n");
        
        String line = "";
        
        while (!validInt(line)) {
            System.out.print("> ");
            line = reader.readLine();
        }
        
        Record record = db.treeSearch(Integer.parseInt(line));
        
        if (record == null) {
            System.out.println("\nRecord with ID " + line + " does not exist!");
            return;
        }
            
        System.out.println("\nRecord successfuly found:\n" + record.toString());
    }

    private static void hashHelper(Database db, BufferedReader reader) throws IOException {
        
        System.out.println("\nDefine the ID to be retrieved:\n");
        
        String line = "";
        
        while (!validInt(line)) {
            System.out.print("> ");
            line = reader.readLine();
        }
        
        Record record = db.hashSearch(Integer.parseInt(line));
        
        if (record == null) {
            System.out.println("\nRecord with ID " + line + " does not exist!");
            return;
        }
            
        System.out.println("\nRecord successfuly found:\n" + record.toString());
    }
    
    private static void indexHelper(Database db, BufferedReader reader)
        throws IOException {
        
        System.out.println("\n"
            + "Search by:\n"
            + "\n1) Genre"
            + "\n2) Producer"
            + "\n3) Both"
            + "\n");
        
        String line = "";
        
        while (!validChoice(line, 3)) {
            System.out.print("> ");
            line = reader.readLine();
        }
        
        switch (line) {
            case "1":
                System.out.print("Genre: ");
                break;
            case "2":
                System.out.print("Producer: ");
                break;
            case "3":
                System.out.print("Genre and producer (genre,producer): ");
                break;
        }
        
        line = reader.readLine();
        
        if (line.contains(",")) {
            String[] options = line.split(",");
            
            db.get(options[0], options[1]);
            
        } else {
            db.get(line);
        }
    }
    
    private static boolean validChoice(String input, int range) {
        if(range < 10) {
            return input.length() == 1 && input.matches("[1-" + range + "]");
        } else {
            return input.matches("[1-9]|1[0" + range + "]");
        }
    }
    
    private static Record getRecord(String input) {
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
        return attribute.matches("[0-10]+");
    }
}