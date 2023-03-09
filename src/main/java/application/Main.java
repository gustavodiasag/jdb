package main.java.application;

import main.java.database.Database;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        // Database file.
        File file = new File("db.bin");
        
        try {
            Database db = new Database(file);
            
            db.sort(1000);
            
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
