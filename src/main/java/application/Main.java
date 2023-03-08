package main.java.application;

import main.java.database.Database;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {

		// Database file.
        File file = new File(args[0]);
        
        try {
        	Database db = new Database(file);
        	
			// Simple test.
        	System.out.println(db.get(1).toString());
        	
        } catch (IOException e) {
        	System.err.println(e.getLocalizedMessage());
        }
    }
}
