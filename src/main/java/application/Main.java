package main.java.application;

import main.java.database.Database;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        File file = new File(args[0]);
        
        try {
            Database db = new Database(file);
            
            db.sort(1000);
            
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}