package main.java.structures.btree;

import java.io.IOException;
import java.io.RandomAccessFile;

public class BTreeKey {
    private int id;
    private long dbPtr;
    private long treePtr;
    
    public BTreeKey() {
        this(-1, -1, -1);
    }
    
    public BTreeKey(int id, long dbPtr) {
        this(id, dbPtr, -1);
    }
    
    public BTreeKey(int id, long dbPtr, long treePtr) {
        this.id = id;
        this.dbPtr = dbPtr;
        this.treePtr = treePtr;
    }
    
    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public long getDbPtr() {
        return this.dbPtr;
    }
    
    public void setDbPtr(long dbPtr) {
        this.dbPtr = dbPtr;
    }
    
    public long getTreePtr() {
        return this.treePtr;
    }
    
    public void setTreePtr(long treePtr) {
        this.treePtr = treePtr;
    }
    
    public void serialize(RandomAccessFile raf) throws IOException {
        try {
            raf.writeInt(id);
            raf.writeLong(dbPtr);
            raf.writeLong(treePtr);
            
        } catch (IOException e) {
            throw new IOException("Unable to write key to file", e);
        }
    }
    
    public void deserialize(RandomAccessFile raf) throws IOException {
        try {
            int id = raf.readInt();
            long dbPtr = raf.readLong();
            long treePtr = raf.readLong();
            
            this.setId(id);
            this.setDbPtr(dbPtr);
            this.setTreePtr(treePtr);
            
        } catch (IOException e) {
            throw new IOException("Unable to read key from file",e);
        }
    }
}