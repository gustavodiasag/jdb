package main.java.algorithms.OTP;

import java.nio.file.Files;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class Otp {
    private static final String keyPath = "key.txt";
    private static final int KEY_SIZE = 64;

    public static void encrypt(RandomAccessFile raf) throws IOException {
        try {
            byte[] db = new byte[(int)raf.length()];
            byte[] encrypted = new byte[db.length];
            byte[] key = generateKey(KEY_SIZE);
    
            FileOutputStream writeKey = new FileOutputStream(new File(keyPath));
            writeKey.write(key);
            writeKey.close();
            
            raf.seek(0);
            raf.readFully(db);

            int index = 0;
            for (int i = 0; i < db.length; i++) {
                if (index == key.length)
                    index = 0;

                encrypted[i] = (byte)(key[index] ^ db[i]);
                index++;
            }

            raf.seek(0);
            raf.write(encrypted);
                
        } catch (IOException e) {
            throw new IOException("Unable to encrypt file", e);
        }
    }      
        
    public static void decrypt(RandomAccessFile raf) throws IOException {
        try {
            byte[] encrypted = new byte[(int)raf.length()];
            byte[] key = Files.readAllBytes(new File(keyPath).toPath());
            byte[] decrypted = new byte[encrypted.length];

            raf.seek(0);
            raf.readFully(encrypted);

            int index = 0;
            for (int i = 0; i < encrypted.length; i++) {
                if (index == key.length)
                    index = 0;

                decrypted[i] = (byte)(key[index] ^ encrypted[i]);
                index++;
            }
            
            raf.seek(0);
            raf.write(decrypted);

        } catch (IOException e) {
            throw new IOException("Unable to decrypt database", e);
        }
    }

    private static byte[] generateKey(int n) {
        Random r = new Random();
        byte[] keyBytes = new byte[n];
        r.nextBytes(keyBytes);

        return keyBytes;
    }
}
