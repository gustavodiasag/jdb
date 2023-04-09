package main.java.database;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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
      nodes[i] = new BucketNode(-1, -1);
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
  }

  public BucketNode getNodes(int pos) {
    return nodes[pos];
  }

  public void setNodes(BucketNode node, int pos) {
    this.nodes[pos] = node;
  }

  public BucketNode[] sort(BucketNode[] nos) {
    for (int i = 0; i < (nos.length - 1); i++) {
      int menor = i;
      for(int j = (i + 1); j < nos.length; j++) {
        if(nos[menor].key > nos[j].key) {
          menor = j;
        }
      }
      swap(menor, i, nos);
    }
    return nos;
  }

  public void swap(int i, int j, BucketNode[] nos) {
    BucketNode no = new BucketNode(nos[i].key, nos[i].pointer);
    nos[i].key = nos[j].key;
    nos[i].pointer = nos[j].pointer;
    nos[j].key = no.key;
    nos[j].pointer = no.pointer;
 }

  public void serialize(RandomAccessFile raf) throws IOException {
    byte[] recordAsBytes = this.toByteArray();
    raf.write(recordAsBytes);
  }

  public byte[] toByteArray() throws IOException {
    // Byte stream is closed even when an exception is thrown.
    try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
      DataOutputStream stream = new DataOutputStream(byteStream);

      stream.writeByte(ele);
      stream.writeShort(depth);

      for (int i = 0; i < size; i++) {
        stream.writeInt(nodes[i].key);
        stream.writeLong(nodes[i].pointer);
      }

      return byteStream.toByteArray();

    } catch (IOException e) {
      throw new IOException(
          "Could not transfer data to byte file", e);
    }
  }

  public static Bucket deserialize(RandomAccessFile raf) throws IOException {
    try {
      byte ele = raf.readByte();
      short depth = raf.readShort();

      BucketNode[] nodes = new BucketNode[size];

      for(int i = 0; i < size; i++) {
        nodes[i].key = raf.readInt();
        nodes[i].pointer = raf.readLong();
      }

      return new Bucket(ele, depth, nodes);
     

    } catch (IOException e) {
      throw new IOException(
          "Error while reading data from file", e);
    }
  }

}
