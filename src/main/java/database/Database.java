package main.java.database;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

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
    private int totalRecords; 

    public Database(File file) throws IOException {
        this.raf = new RandomAccessFile(file, "rw");
        this.initialize();
    }
    
    private void initialize() throws IOException {
        try {
            Record[] records = CSVParser.parse();
            /*
             * Storing the highest "id" value before the records is clever
             * for this attribute should not be user-defined and new
             * values are easily generated by incrementation.
             */
            int lastId = records[records.length - 1].getId();
            raf.writeInt(lastId);
            
            for (Record record : records)
                record.serialize(raf);
            
            this.totalRecords = records.length;
            
        } catch (IOException e) {
            throw new IOException(
                "Error while initializing the database", e);
        }
    }

    /*
     * Returns the entity with the corresponding "id" value
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
     * into the database and an exception otherwise.
     */
    public boolean insert(Record record) throws IOException {            
        try {
            record.setId(getLastId() + 1);
            raf.seek(0);
            raf.writeInt(record.getId());

            // New records are always inserted at the end.
            raf.seek(raf.length());
            record.serialize(raf);

            return true;
            
        } catch (IOException e) {
            throw new IOException(
                "Unable to insert record record:\n" + record.toString(), e);
        }
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
     * Returns true if the record with such "id" is successfully
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
            throw new IOException(
                "Error while deleting record with id: " + id, e);
        }

        return false;
    }
    
    public void sort(int limit) throws IOException {
        try {
            RandomAccessFile[] files = new RandomAccessFile[4];
            
            for (int i = 0; i < 4; i++)
                files[i] = new RandomAccessFile("tmp" + i, "rw");
            
            raf.seek(Integer.BYTES);
            
            // Initial distribution.
            while (!eof(raf)) {
                sort(files[0], limit);
                sort(files[1], limit);
            }
            
            int runs = getTotalRuns(limit);
            boolean control = true;
            
            for (int i = limit; runs >= 0; runs--, i *= 2, control = !control) {
                 if (control)
                	 intercalate(files[0], files[1], files[2], files[3], i);
                 else
                	 intercalate(files[3], files[2], files[1], files[0], i);
            }
            
//            files[3].seek(0);
//            while (!eof(files[3]))
//            	System.out.println(Record.deserialize(files[3]).toString());
            
        } catch (IOException e) {
            throw new IOException("Unable to sort", e);
        }
    }
    
    private void sort(RandomAccessFile tmp, int limit) throws IOException {
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
            quickSort(records, 0, i-1);
            
            for (int j = 0; j < i; j++)
                records[j].serialize(tmp);
            
        } catch (IOException e) {
            throw new IOException("Error while sorting", e);
        }
    }
    
    /*
     * Merges the already sorted records from the first two files
     * to the last ones considering the established limit.
     */
    private void intercalate
    (
        RandomAccessFile first,
        RandomAccessFile second,
        RandomAccessFile third,
        RandomAccessFile fourth,
        int limit	
    ) {
        try {
            // Used to switch between destination files.
            boolean destControl = false;
            first.seek(0);
            second.seek(0);
            
            // Deals with all the intervals except the last one.
            while (!eof(first) && !eof(second)) {
            	int firstCounter = 0;
            	int secondCounter = 0;
                
                while (firstCounter < limit && secondCounter < limit) {
                    /*
                     * While analyzing record by record, EOF may get
                     * reached before the outer loop condition.
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
                
            	while (!eof(first) && firstCounter < limit) {
            		Record.deserialize(first).serialize((destControl) ? fourth : third);
            		firstCounter++;
            	}
                
            	while(!eof(second) && secondCounter < limit) {
            		Record.deserialize(second).serialize((destControl) ? fourth : third);
	        		secondCounter++;
            	}
                
                destControl = !destControl;
            }
            
        	while (!eof(second)) {
        		Record record = Record.deserialize(second);
        		record.serialize(fourth);
        	}
            	
        	while (!eof(first)) {
        		Record record = Record.deserialize(first);
        		record.serialize(fourth);
        	}
            
            first.setLength(0);
            second.setLength(0);
            
        } catch (IOException e) {
            System.err.println("Error while intercalating");
            e.printStackTrace();
        }
    }
    
    // Returns the file's first four bytes.
    private int getLastId() throws IOException {
        try {
            raf.seek(0);
            
            return raf.readInt();
            
        } catch (IOException e) {
            throw new IOException(
                "Unable to retrieve file header", e);
        }
    }
    
    private int getTotalRuns(int limit) {
    	return (int)Math.ceil(Math.log(totalRecords/limit)/Math.log(2));
    }

    /*
     * Returns whether there's still an offset between
     * the file pointer and the file's length.
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
