package main.java.structures.btree;

import java.io.IOException;
import java.io.RandomAccessFile;

import main.java.database.Record;

public class BTree {
    private final int order;
    private RandomAccessFile tree;
    private final int rootPos;
    
    public BTree(int order) throws IOException {
        this.rootPos = 0;
        this.order = order;
        this.tree = new RandomAccessFile("tree.bin", "rw");
    }
    
    public void build() throws IOException {
        try {
            BTreePage root = new BTreePage(order);
            root.serialize(tree);
            
        } catch (IOException e) {
            throw new IOException("Error while initializing the tree", e);
        }
    }
    
    public void insert(Record record, long dbPtr) throws IOException {
        BTreeKey key = new BTreeKey(record.getId(), dbPtr);
        
        insert(rootPos, key);
    }
    
    private void insert(long pagePos, BTreeKey key) throws IOException {
        try {
            BTreePage page = new BTreePage(order);
            page.deserialize(tree, pagePos);
            
            if (page.getLeaf()) {
                if (page.getElements() < order - 1) {
                    page.insertKey(key);
                    tree.seek(pagePos);
                    page.serialize(tree);
                } else {
                    split(page, key);
                }
            } else {
                long childPos = getChildPointer(page, key);
                
                insert(childPos, key);
            }
            
        } catch (IOException e) {
            throw new IOException("Unable to insert key", e);
        }
    }
    
    private void split(BTreePage page, BTreeKey key) throws IOException {
        try {
            BTreePage parent, right = new BTreePage(order);
            
            int splitPos = (order - 1)/2;
            
            BTreeKey pivot = page.getKey(splitPos);
            
            for (int i = splitPos + 1; i < order - 1; i++)
                right.insertKey(page.getKey(i));
            
            page.setElements((byte)splitPos);
            
            if (key.getId() < pivot.getId()) {
                page.insertKey(key);
            } else {
                right.insertKey(key);
            }
            
            parent = new BTreePage(order);
            
            if (page.getParent() != -1)
                parent.deserialize(tree, page.getParent());
            
            right.setParent(parent.getPos());
            right.setTreePtr(pivot.getTreePtr());
            right.setLeaf((right.getTreePtr() == -1));
            
            tree.seek(tree.length());
            right.serialize(tree);
            
            pivot.setTreePtr(right.getPos());
            
            if (parent.getElements() == order - 1) {
                split(parent, pivot);
                
            }
            
            right.setParent(parent.getPos());
            
            tree.seek(right.getPos());
            right.serialize(tree);
            
            page.setParent(parent.getPos());
            page.setLeaf((page.getTreePtr() == -1));
            
            if (page.getPos() != 0) {
                tree.seek(page.getPos());
            } else {
                tree.seek(tree.length());
            }
            
            page.serialize(tree);
            
            parent.insertKey(pivot);
            parent.setLeaf(false);
            
            if (page.getParent() == rootPos && parent.getTreePtr() == -1)
                parent.setTreePtr(page.getPos());
            
            tree.seek(parent.getPos());
            parent.serialize(tree);
            
        } catch (IOException e) {
            throw new IOException("Unable to split page", e);
        }
    }
    
    private long getChildPointer(BTreePage page, BTreeKey key) {
        if (key.getId() < page.getKey(0).getId())
            return page.getTreePtr();
        
        int i = 1;
        for (; i < page.getElements(); i++) {
            if (key.getId() < page.getKey(i).getId())
                return page.getKey(i - 1).getTreePtr();
        }
        
        return page.getKey(i - 1).getTreePtr();
    }
    
    public void show() throws IOException {
        tree.seek(rootPos);
        
        while (tree.getFilePointer() < tree.length()) {
            System.out.println();
            System.out.println("==========================");
            System.out.println("Pos: " + tree.getFilePointer());
            System.out.println("Pai: " + tree.readLong());
            System.out.println("Tamanho: " + tree.readByte());
            System.out.println("Folha: " + tree.readBoolean());
            System.out.println("Primeiro ptr: " + tree.readLong());
            
            for (int i = 0; i < 7; i++) {
                System.out.println("Id: " + tree.readInt() + " Ptr arq: " + tree.readLong() + " Ptr arv: " + tree.readLong());
            }
        }
    }
}