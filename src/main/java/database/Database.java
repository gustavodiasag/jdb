package main.java.database;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import main.java.algorithms.Huffman.Huffman;
import main.java.algorithms.KMP.KMP;
import main.java.algorithms.LZW.LZW;
import main.java.algorithms.bm.BoyerMoore;
import main.java.algorithms.OTP.Otp;
import main.java.structures.btree.BTree;
import main.java.structures.hash.Hash;
import main.java.structures.index.InvertedIndex;

/*
 * Responsible for managing all operations and
 * manipulations that may occur in the "database".
 */
public class Database implements Sorting {
    /*
     * Provides the reading and writing operations
     * in the file specified.
     */
    private final RandomAccessFile raf;
    
    // Index structures.
    private final BTree tree;
    private final Hash hash;
    private final InvertedIndex index;

    // Data compression algorithms
    private final Huffman huffman;
    private final LZW lzw;

    // Pattern matching algorithms
    private BoyerMoore bm;

    public Database(File file) throws IOException {
        this.raf = new RandomAccessFile(file, "rw");
        this.tree = new BTree(8);
        this.hash = new Hash();
        this.index = new InvertedIndex();
        this.huffman = new Huffman();
        this.lzw = new LZW();
    }
    
    public void build() throws IOException {
        try {
            Record[] records = CSVParser.parse();
            /*
             * Storing the highest id value before the records is clever
             * for this attribute should not be user-defined and new
             * values are easily generated by incrementing that one.
             */
            int lastId = records[records.length - 1].getId();
            raf.writeInt(lastId);
      
            tree.build();
            
            for (Record record : records) {
                long pos = record.serialize(raf);
                
                index.insert(record, pos);
                tree.insert(record, pos);
                hash.add(record.getId(), pos);
            }

            bm = new BoyerMoore(raf);

        } catch (IOException e) {
            throw new IOException("Error while initializing the database", e);
        }
    }

    public void match(String option, String pattern) throws IOException {
        switch (option) {
            case "1":
                bm.search(pattern);
                break;
            case "2":
                KMP.search(pattern);
                break;
        }
    }

    public void compress(int option, File inputFile) throws IOException {
        switch(option) {
            case 1:
                huffman.compress(inputFile);
                break;
            case 2:
                lzw.compress(inputFile);
        }
    }
    
    public Record treeSearch(int id) throws IOException {
        try {
            long dbPtr = tree.search(id);
            
            if (dbPtr == -1)
                return null;
            
            raf.seek(dbPtr);
            
            return Record.deserialize(raf);
                
        } catch (IOException e) {
            throw new IOException(
                "Error while retrieving record with id: " + id, e);
        }
    }

    public Record hashSearch(int id) throws IOException {
        try {
            long hashPtr = hash.search(id);
            
            if (hashPtr == -1)
                return null;
            
            raf.seek(hashPtr);
            
            return Record.deserialize(raf);
                
        } catch (IOException e) {
            throw new IOException(
                "Error while retrieving record with id: " + id, e);
        }
    }
    
    public <K> void get(K key) throws IOException {
        List<Long> recordPtrs = index.get(key);
        
        for (long recordPtr : recordPtrs) {
            raf.seek(recordPtr);
            Record record = Record.deserialize(raf);
            System.out.println(record.toString());
        }
    }
    
    public <K> void get(K firstKey, K secondKey) throws IOException {
        List<Long> recordPtrs = index.get(firstKey, secondKey);
        
        for (long recordPtr : recordPtrs) {
            raf.seek(recordPtr);
            Record record = Record.deserialize(raf);
            System.out.println(record.toString());
        }
    }
    
    /*
     * Returns the entity with the corresponding id
     * and null if it isn't found in the database.
     */
    public Record get(int id) throws IOException {
        try {
            // Header is not useful for this operation.
            raf.seek(Integer.BYTES);

            while (!eof(raf)) {
                long pos = raf.getFilePointer();
                boolean valid = raf.readBoolean();
                int recordSize = raf.readInt();

                if (valid) {
                    raf.seek(pos);
                    Record r = Record.deserialize(raf);

                    if (r.getId() == id)
                        return r;
                } else
                    // Deleted records must not be read.
                    raf.skipBytes(recordSize);
            }

        } catch (IOException e) {
            throw new IOException(
                "Error while retrieving record with id: " + id, e);
        }

        return null;
    }

    /*
     * Returns true if a new entity is successfully inserted
     * into the database and false otherwise.
     */
    public boolean insert(Record record) throws IOException {
        try {
            record.setId(getLastId() + 1);
            raf.seek(0);
            raf.writeInt(record.getId());
            
            // New records are always inserted at the end.
            long dbPtr = raf.length();
            raf.seek(dbPtr);
            record.serialize(raf);

            tree.insert(record, dbPtr);
            hash.add(record.getId(), dbPtr);
            index.insert(record, dbPtr);


            //tree.show();

            return true;

        } catch (IOException e) {
            System.err.println(
                    "Unable to insert record record:\n" + record.toString());
        }

        return false;
    }

    /*
     * Returns true if it was successfully able to update
     * such record and false otherwise.
     */
    public boolean update(Record record) throws IOException {
        try {
            raf.seek(Integer.BYTES);

            while (!eof(raf)) {
                long pos = raf.getFilePointer();
                boolean valid = raf.readBoolean();
                int recordSize = raf.readInt();

                if (valid) {
                    raf.seek(pos);
                    Record r = Record.deserialize(raf);

                    if (record.getId() == r.getId()) {
                        byte[] recordAsBytes = record.toByteArray();
                        raf.seek(pos);

                        if (recordAsBytes.length <= recordSize)
                            record.serialize(raf, recordSize);
                        else {
                            raf.writeBoolean(false);
                            raf.seek(raf.length());

                            record.serialize(raf);
                        }

                        return true;
                    }

                } else
                    raf.skipBytes(recordSize);
            }
        } catch (IOException e) {
            throw new IOException(
                    "Error while updating record with id: " + record.getId(), e);
        }

        return false;
    }

    /*
     * Returns true if the record with such id was successfully
     * removed from the "database", false otherwise.
     */
    public boolean delete(int id) throws IOException {
        try {
            // Header is not useful for this operation.
            raf.seek(Integer.BYTES);

            while (!eof(raf)) {
                // Position of the validation bit.
                long pos = raf.getFilePointer();
                boolean valid = raf.readBoolean();
                int recordSize = raf.readInt();

                if (valid) {
                    raf.seek(pos);
                    Record r = Record.deserialize(raf);

                    if (r.getId() == id) {
                        raf.seek(pos);
                        raf.writeBoolean(false);

                        return true;
                    }

                } else
                    // Deleted records must not be read.
                    raf.skipBytes(recordSize);
            }
        } catch (IOException e) {
            System.err.println(
                    "Error while deleting record with id: " + id);
        }

        return false;
    }

    public void sort(int limit, boolean optimize) throws IOException {
        try {
            // Temporarily used for the merging process.
            RandomAccessFile[] files = new RandomAccessFile[4];

            for (int i = 0; i < 4; i++)
                files[i] = new RandomAccessFile("tmp" + i, "rw");

            // Header remains the same.
            raf.seek(Integer.BYTES);

            // Initial distribution.
            while (!eof(raf)) {
                distribute(files[0], limit);
                distribute(files[1], limit);
            }
            // Used to alternate the source and destination files.
            boolean control = true;

            for (int i = limit; !singleDest(files); i *= 2, control = !control) {
                if (control)
                    merge(i, optimize, files[0], files[1], files[2], files[3]);
                else
                    merge(i, optimize, files[3], files[2], files[1], files[0]);
            }

            close(files);

        } catch (IOException e) {
            throw new IOException("Unable to sort", e);
        }
    }

    /*
     * Retrieves the specified amount of records from the database
     * file, sorts and writes them into the specified destination.
     */
    private void distribute(RandomAccessFile file, int limit)
            throws IOException {

        try {
            Record[] records = new Record[limit];

            int i = 0;
            // Prioritizes end-of-file considering the last iteration.
            for (; !eof(raf) && i < limit; i++)
                records[i] = Record.deserialize(raf);

            /*
             * Limiting the array like this guarantees that the sorting
             * operation will only consider existing objects (non null).
             */
            quickSort(records, 0, i - 1);

            for (int j = 0; j < i; j++)
                records[j].serialize(file);

        } catch (IOException e) {
            throw new IOException("Error while sorting", e);
        }
    }

    /*
     * Merges the already sorted records from the first two files
     * to the last ones considering the established limit.
     */
    private void merge(
            int limit,
            boolean optimize,
            RandomAccessFile first,
            RandomAccessFile second,
            RandomAccessFile third,
            RandomAccessFile fourth

    ) throws IOException {
        // Used to switch between destination files.
        boolean destControl = false;
        first.seek(0);
        second.seek(0);

        int firstLimit = limit;
        int secondLimit = limit;

        // Deals with all the intervals except the last one.
        while (!eof(first) && !eof(second)) {
            int firstCounter = 0;
            int secondCounter = 0;
            // The function can support both the "static" and "dynamic" merges.
            if (optimize) {
                firstLimit = getLimit(first, limit);
                secondLimit = getLimit(second, limit);
            }

            while (firstCounter < firstLimit && secondCounter < secondLimit) {
                /*
                 * While reading record by record, EOF may get reached
                 * before the outer loop condition.
                 */
                if (eof(first) || eof(second))
                    break;
                /*
                 * Position to return to when a record has a higher id
                 * than the other, so new comparisons can happen.
                 */
                long firstPos = first.getFilePointer();
                long secondPos = second.getFilePointer();

                // Loaded for attribute comparison.
                Record fromFirst = Record.deserialize(first);
                Record fromSecond = Record.deserialize(second);

                if (fromFirst.getId() < fromSecond.getId()) {
                    fromFirst.serialize((destControl) ? fourth : third);
                    second.seek(secondPos);
                    firstCounter++;

                } else {
                    fromSecond.serialize((destControl) ? fourth : third);
                    first.seek(firstPos);
                    secondCounter++;
                }
            }

            while (!eof(first) && firstCounter < firstLimit) {
                Record.deserialize(first).serialize((destControl) ? fourth : third);
                firstCounter++;
            }

            while (!eof(second) && secondCounter < secondLimit) {
                Record.deserialize(second).serialize((destControl) ? fourth : third);
                secondCounter++;
            }

            destControl = !destControl;
        }

        while (!eof(first))
            fourth.write(first.read());

        while (!eof(second))
            fourth.write(second.read());

        /*
         * Once the two files have been merged, they must be
         * reinitialized for the next run.
         */
        first.setLength(0);
        second.setLength(0);
    }

    /*
     * Checks if the last element from a group and the first
     * from the next one are in ascending order, which
     * increases the merging limit, directly reducing the
     * amount of runs necessary to sort the database.
     */
    private int getLimit(RandomAccessFile file, int limit)
            throws IOException {

        long pos = file.getFilePointer();
        int newLimit = limit;

        while (true) {
            for (int i = 0; !eof(file) && i < limit - 1; i++) {
                // Ignores all records besides the last on the block.
                file.skipBytes(1);
                file.skipBytes(file.readInt());
            }

            if (eof(file))
                break;

            Record prev = Record.deserialize(file);
            Record next = Record.deserialize(file);

            if (prev.getId() > next.getId())
                return newLimit;

            newLimit += limit;
        }
        /*
         * After peeking plenty of records, the file pointer
         * must return to execute the merging process.
         */
        file.seek(pos);

        return newLimit;
    }

    /*
     * Checks if only one file contains data, which is the
     * condition for the merging operation to end.
     */
    private boolean singleDest(RandomAccessFile[] files) throws IOException {
        int zeroLen = 0;

        for (int i = 0; i < files.length; i++)
            if (files[i].length() == 0)
                zeroLen++;

        return zeroLen == files.length - 1;
    }

    // Deletes all the temporary files.
    private void close(RandomAccessFile[] files) throws IOException {
        for (int i = 0; i < files.length; i++) {
            if (files[i].length() > 0) {
                raf.seek(Integer.BYTES);

                while (!eof(files[i]))
                    raf.write(files[i].read());
            }
            files[i].close();
        }

        Files.delete(Paths.get("tmp0"));
        Files.delete(Paths.get("tmp1"));
        Files.delete(Paths.get("tmp2"));
        Files.delete(Paths.get("tmp3"));
    }

    // Returns the database file's first four bytes.
    private int getLastId() throws IOException {
        try {
            raf.seek(0);

            return raf.readInt();

        } catch (IOException e) {
            throw new IOException(
                    "Unable to retrieve file header", e);
        }
    }

    public void show() throws IOException {
        raf.seek(Integer.BYTES);

        while (!eof(raf))
            System.out.println(Record.deserialize(raf));

        raf.seek(0);
    }

    /*
     * Returns whether there's still an offset between
     * the file pointer and its length.
     */
    private boolean eof(RandomAccessFile raf) throws IOException {
        try {
            return raf.getFilePointer() == raf.length();

        } catch (IOException e) {
            throw new IOException(
                    "Error while checking for EOF", e);
        }
    }
}
