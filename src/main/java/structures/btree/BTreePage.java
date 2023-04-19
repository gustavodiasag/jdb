package main.java.structures.btree;

import java.io.IOException;
import java.io.RandomAccessFile;

public class BTreePage {
    private final int order;
    private long pos;
    private long parent;
    private byte elements;
    private boolean leaf;
    private long treePtr;
    private BTreeKey[] keys;
    
    public BTreePage(int order) {
        this(order, 0);
    }
    
    public BTreePage(int order, long pos) {
        this(order, pos, -1);
    }
    
    public BTreePage(int order, long pos, long parent) {
        this(order, pos, parent, (byte)0, true, -1);
    }
    
    public BTreePage(int order, long pos, long parent, byte elements, boolean leaf, long treePtr) {
        this.order = order;
        this.pos = pos;
        this.parent = parent;
        this.elements = elements;
        this.leaf = leaf;
        this.treePtr = treePtr;
        this.keys = new BTreeKey[order - 1];
        
        for (int i = 0; i < keys.length; i++)
            keys[i] = new BTreeKey();
    }
    
    public long getPos() {
        return this.pos;
    }
    
    public void setPos(long pos) {
        this.pos = pos;
    }
    
    public long getParent() {
        return this.parent;
    }
    
    public void setParent(long parent) {
        this.parent = parent;
    }
    
    public byte getElements() {
        return this.elements;
    }
    
    public void setElements(byte elements) {
        this.elements = elements;
    }
    
    public boolean getLeaf() {
        return this.leaf;
    }
    
    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }
    
    public long getTreePtr() {
        return this.treePtr;
    }
    
    public void setTreePtr(long treePtr) {
        this.treePtr = treePtr;
    }
    
    public BTreeKey[] getKeys() {
        return this.keys;
    }
    
    public BTreeKey getKey(int index) {
        return this.keys[index];
    }
    
    public void setKeys(BTreeKey[] keys) {
        this.keys = keys;
    }
    
    public void insertKey(BTreeKey key) {
        int i = elements - 1;
        while (i >= 0 && key.getId() < keys[i].getId()) {
            keys[i + 1] = keys[i];
            i--;
        }
        keys[i + 1] = key;
        
        elements++;
    }
    
    public void serialize(RandomAccessFile raf) throws IOException {
        try {
            this.pos = raf.getFilePointer();
            
            raf.writeLong(parent);
            raf.write(elements);
            raf.writeBoolean(leaf);
            raf.writeLong(treePtr);
            
            for (int i = 0; i < order - 1; i++) {
                if (i < elements) {
                    keys[i].serialize(raf);
                    
                } else {
                    raf.writeInt(-1);
                    raf.writeLong(-1);
                    raf.writeLong(-1);
                }
                    
            }
            
        } catch (IOException e) {
            throw new IOException("Unable to write page to file");
        }
    }
    
    public void deserialize(RandomAccessFile raf) throws IOException {
        deserialize(raf, this.pos);
    }
    
    public void deserialize(RandomAccessFile raf, long pos) throws IOException {
        try {
            raf.seek(pos);
            
            this.pos = pos;
            this.parent = raf.readLong();
            this.elements = raf.readByte();
            this.leaf = raf.readBoolean();
            this.treePtr = raf.readLong();
            this.keys = new BTreeKey[order - 1];
            
            for (int i = 0; i < order - 1; i++) {
                keys[i] = new BTreeKey();
                
                if (i < elements)
                    keys[i].deserialize(raf);
            }
            
        } catch (IOException e) {
            throw new IOException("Unable to read page from file", e);
        }
    }
}