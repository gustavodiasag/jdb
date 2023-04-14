package main.java.database;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Bucket {
  private byte ele;
  private short depth;
  private static short size = 7;
  private BucketNode[] nodes = new BucketNode[size];

  public Bucket(short depth) { // raf e seek
    this.ele = 0;
    this.depth = depth;

    for (int i = 0; i < size; i++) {
      nodes[i] = new BucketNode(Integer.MAX_VALUE, Long.MAX_VALUE);
    }

  }

  public Bucket(byte ele, short depth, BucketNode[] nodes) {
    this.ele = ele;
    this.depth = depth;
    this.nodes = nodes;
  }

  public byte getEle() {
    return ele;
  }

  public void setEle(byte ele) {
    this.ele = ele;
  }

  public short getDepth() {
    return depth;
  }

  public void setDepth(short depth) {
    this.depth = depth;
  }

  public static short getSize() {
    return size;
  }

  public static void setSize(short size) {
    Bucket.size = size;
  }

  public BucketNode[] getNodes() {
    return nodes;
  }

  public void setNodes(BucketNode[] nodes) {
    this.nodes = nodes;
    this.ele = (byte) nodes.length;
  }

  public BucketNode getNode(int pos) {
    return nodes[pos];
  }

  public void setNode(BucketNode node, int pos) {
    this.nodes[pos] = node;
    this.ele += 1;
  }

  public void setNode(BucketNode node) {
    this.nodes[ele] = node;
    this.ele += 1;
  }

  public void sort() {
    for (int i = 0; i < (nodes.length - 1); i++) {
      int menor = i;
      for(int j = (i + 1); j < nodes.length; j++) {
        if(nodes[menor].key > nodes[j].key) {
          menor = j;
        }
      }
      swap(menor, i, nodes);
    }
  }

  public void sort(BucketNode[] nos) {
    for (int i = 0; i < (nos.length - 1); i++) {
      int menor = i;
      for(int j = (i + 1); j < nos.length; j++) {
        if(nos[menor].key > nos[j].key) {
          menor = j;
        }
      }
      swap(menor, i, nos);
    }
  }

  public static void swap(int i, int j, BucketNode[] nos) {
    BucketNode no = new BucketNode(nos[i].key, nos[i].pointer);
    nos[i].key = nos[j].key;
    nos[i].pointer = nos[j].pointer;
    nos[j].key = no.key;
    nos[j].pointer = no.pointer;
 }

  public void serialize(RandomAccessFile raf) throws IOException {
    raf.writeByte(ele);
    raf.writeShort(depth);
    for (int i = 0; i < size; i++) {
      raf.writeInt(nodes[i].key);
      raf.writeLong(nodes[i].pointer);
    }
  }

  public static Bucket deserialize(RandomAccessFile raf) throws IOException {
    try {
      byte ele = raf.readByte();
      short depth = raf.readShort();

      BucketNode[] nodes = new BucketNode[size];

      for(int i = 0; i < size; i++) {
        nodes[i] = new BucketNode(raf.readInt(), raf.readLong());        
      }

      return new Bucket(ele, depth, nodes);
     

    } catch (IOException e) {
      throw new IOException(
          "Error while reading bucket from file", e);
    }
  }

  public void print() {
    System.out.println("Elementos = " + getEle());
    System.out.println("Profundidade = " + getDepth());
    for(BucketNode node : nodes) {
      System.out.println("Chave: " + node.key + " - ponteiro: " + node.pointer);
    }
  }

}
