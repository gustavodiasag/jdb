package main.java.algorithms.bm;

import java.io.RandomAccessFile;
import java.io.IOException;

public class BoyerMoore {
    private final int MAX_VAL = 256;
    private final byte[] text;

    public BoyerMoore(RandomAccessFile raf) throws IOException {
        this.text = new byte[(int)raf.length()];

        raf.seek(0);
        raf.readFully(text);
    }

    public void search(String input) throws IOException {
        try {
            byte[] pattern = input.getBytes("UTF-8");

            long start = System.currentTimeMillis();

            int comparisons = match(text, pattern);

            long end = System.currentTimeMillis();
            long time = end - start;

            System.out.println("\nComparisons: " + comparisons);

            System.out.println("\nExecution time: " + time + " ms");

        } catch (IOException e) {
            throw new IOException("Error while search for pattern", e);
        }
    }

    public int match(byte[] text, byte[] pattern) {
        int m = pattern.length, comparisons = 0;
        int movements[] = new int[MAX_VAL];

        for (int i = 0; i < MAX_VAL; i++)
            movements[i] = m +1;
        
        for (int i = 0; i < m; i++)
            movements[pattern[i]] = m - i;

        int i = m - 1;

        while (i < text.length - 1) {
            int j = i, k = m - 1;

            comparisons++;
            while (k >= 0 && text[j] == pattern[k]) {
                j--;
                k--;
            }

            if (k < 0)
                System.out.println("\nPattern found at " + (j + 1));
            
            i = i + movements[text[i + 1]];
        }

        return comparisons;
    }
}
