package main.java.structures.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.database.Record;

/*
 * Allows searching operations based on attributes
 * other than the records' id value.
 */
public class InvertedIndex {
    private RandomAccessFile index;
    /*
     * The dictionary structure is used to support a considerably
     * large amount of keys and be flexible towards their types.
     */
    private Map<Object, Long> map;
    
    public InvertedIndex() throws IOException {
        try {
            this.index = new RandomAccessFile("index.bin","rw");
            this.map = new HashMap<Object, Long>();
            
        } catch (IOException e) {
            throw new IOException("Error while initializing index file", e);
        }
    }
    
    /*
     * Inserts a pointer for the record in the database file for each
     * attribute presented by it.
     */
    public void insert(Record record, long dbPtr) throws IOException {
        /*
         * Genres and producers were selected for being the two
         * most descriptive fields and also the ones with a
         * decent record intersection.
         */
        String[] genres = record.getGenres();
        String[] producers = record.getProducers();
        
        for (String genre : genres) {
            genre = genre.trim();
            
            insert(genre, dbPtr);
        }
        
        for (String producer : producers) {
            producer = producer.trim();
            insert(producer, dbPtr);
        }
    }
    
    /*
     * Inserts a new entry in the dictionary or a new "node" in
     * the linked-list file.
     */
    private <K> void insert(K key, long dbPtr) throws IOException {
        try {
            long indexPtr;
            
            if (map.containsKey(key)) {
                indexPtr = map.get(key);
                fileWrite(indexPtr, dbPtr);
            } else {
                index.seek(index.length());
                
                indexPtr = index.getFilePointer();
                /*
                 * When the first record containing the key specified
                 * is inserted, it becomes the "head" of that key's
                 * linked-list. 
                 */
                map.put(key, indexPtr);
                
                /*
                 * The information stored in the index file present
                 * the following structure:
                 * 
                 * <database-pointer><next-node-pointer>
                 */
                index.writeLong(dbPtr);
                index.writeLong(-1);
            }
        } catch (IOException e) {
            throw new IOException("Unable to insert genres from record", e);
        }
    }
    
    /*
     * Returns a list of pointers to the database file,
     * each representing the position of a record that
     * contains that key.
     */
    public <K> List<Long> get(K key) throws IOException {
        try {
            List<Long> recordPtrs = new ArrayList<Long>();
            
            // Head pointer
            long indexPtr = map.get(key);
            
            /*
             * The value -1 is used to represent the end
             * of the linked-list.
             */
            while (indexPtr != -1) {
                index.seek(indexPtr);
                recordPtrs.add(index.readLong());
                indexPtr = index.readLong();
            }
            
            return recordPtrs;
            
        } catch (IOException e) {
            throw new IOException(
                "Unable to get records with the genre specified", e);
        }
    }
    
    /*
     * Returns the records that contain both of the keys specified.
     * 
     * Later on, the implementation should support multiple keys,
     * so that generics can be really explored.
     */
    public <K> List<Long> get(K firstKey, K secondKey) throws IOException {
        List<Long> firstList = get(firstKey);
        List<Long> secondList = get(secondKey);
        
        firstList.retainAll(secondList);

        return firstList;
    }
    
    // Inserts a new record pointer to the linked-list.
    private void fileWrite(long indexPtr, long dbPtr) throws IOException {
        try {
            long prevPtr = -1, currPtr = indexPtr;
            
            while (currPtr != -1) {
                index.seek(currPtr + Long.BYTES);
                
                prevPtr = index.getFilePointer();
                currPtr = index.readLong();
            }
            
            index.seek(index.length());
            
            currPtr = index.getFilePointer();
            
            index.writeLong(dbPtr);
            index.writeLong(-1);
            
            index.seek(prevPtr);
            index.writeLong(currPtr);
            
        } catch (IOException e) {
            throw new IOException(
                "Unable to insert new record with the specified token", e);
        }
    }
}
