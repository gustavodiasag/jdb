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
    
    // Writes the root to the tree file
    public void build() throws IOException {
        try {
            BTreePage root = new BTreePage(order);
            root.serialize(tree);
            
        } catch (IOException e) {
            throw new IOException("Error while initializing the tree", e);
        }
    }
    
    public long search(int id) throws IOException {
        BTreeKey key = new BTreeKey(id, -1);
        BTreePage page = new BTreePage(order);
        
        long pagePos = getPage(key, rootPos);
        page.deserialize(tree, pagePos);
        
        for (BTreeKey k : page.getKeys()) {
            if (key.getId() == id) {
                return k.getDbPtr();
            }
        }
        
        return -1;
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
            boolean control = true;
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
            
            if (parent.getElements() >= order - 1) {
                split(parent, pivot);
                
                long pagePos = getPage(pivot, rootPos);
                parent.deserialize(tree, pagePos);
                
                control = false;
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
            
            if (control)
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
    
    private long getPage(BTreeKey key, long pos) throws IOException {
        try {
            BTreePage page = new BTreePage(order);
            page.deserialize(tree, pos);
            
            int i = 0, m = 0, n = page.getElements() - 1;
            
            while (i <= n) {
                m = (i + n)/2;
                
                if (page.getKey(m).getId() == key.getId())
                    return page.getPos();
                
                if (page.getKey(m).getId() < key.getId()) {
                    i = m + 1;
                    
                } else {
                    i = m - 1;
                }
            }
            
            n = (n == -1) ? 0 : n;
            
            if (n == 0) {
                if (page.getKey(0).getId() > key.getId()) {
                    return getPage(key, page.getTreePtr());
                } else {
                    return getPage(key, page.getKey(0).getTreePtr());
                }
            } else {
                if (page.getKey(m).getId() < key.getId()) {
                    return getPage(key, page.getKey(m).getTreePtr());
                } else {
                    return getPage(key, page.getKey(m - 1).getTreePtr());
                }
            }
                    
        } catch (IOException e) {
            throw new IOException("Unable to get page", e);
        }
    }
    
    public void show() throws IOException {
        tree.seek(rootPos);
        
        while(tree.getFilePointer() < tree.length()) {
            System.out.println();
            System.out.println("==============");
            System.out.println("Posicao da Pagina: " + tree.getFilePointer());
            System.out.println("Ponteiro Pai: " + tree.readLong());
            int n = tree.read();
            System.out.println("Folha: " + tree.readBoolean());
            System.out.println("Total de Chaves: " + n);
            System.out.println("Primeiro Ponteiro da Pagina: " + tree.readLong());
            for(int i = 0; i < 7; i++) {
               if(i < n) {
                  System.out.println("Id: " + tree.readInt() + " Position: " + tree.readLong() + " Pointer: " + tree.readLong());
               } else {
                  tree.readInt(); tree.readLong(); tree.readLong();
               }
            }
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
}