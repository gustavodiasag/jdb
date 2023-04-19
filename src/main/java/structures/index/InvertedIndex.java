package main.java.structures.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.database.Record;

public class InvertedIndex {
    private RandomAccessFile index;
    private Map<Object, Long> map;
    
    public InvertedIndex() throws IOException {
        try {
            this.index = new RandomAccessFile("index.bin","rw");
            this.map = new HashMap<Object, Long>();
            
        } catch (IOException e) {
            throw new IOException("Error while initializing index file", e);
        }
    }
    
    public void insert(Record record, long dbPtr) throws IOException {
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
    
    private <K> void insert(K key, long dbPtr) throws IOException {
        try {
            long indexPtr;
            
            if (map.containsKey(key)) {
                indexPtr = map.get(key);
                fileWrite(indexPtr, dbPtr);
            } else {
                index.seek(index.length());
                
                indexPtr = index.getFilePointer();
                map.put(key, indexPtr);
                
                index.writeLong(dbPtr);
                index.writeLong(-1);
            }
        } catch (IOException e) {
            throw new IOException("Unable to insert genres from record", e);
        }
    }
    
    public <K> List<Long> get(K key) throws IOException {
        try {
            List<Long> recordPtrs = new ArrayList<Long>(); 
            long indexPtr = map.get(key);
            
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
    
    public <K> List<Long> get(K firstKey, K secondKey) throws IOException {
        List<Long> firstList = get(firstKey);
        List<Long> secondList = get(secondKey);
        
        firstList.retainAll(secondList);

        return firstList;
    }
    
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
