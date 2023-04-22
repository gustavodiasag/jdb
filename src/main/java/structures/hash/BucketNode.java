package main.java.structures.hash;

public class BucketNode {
      public int key;
      public long pointer;

      public BucketNode(int key, long pointer) {
        this.key = key;
        this.pointer = pointer;
      }
    }