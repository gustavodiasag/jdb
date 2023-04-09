package main.java.database;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class Hash {
  private int depth;
  private int initialEle;
  private byte bucketLength = 87;
  private int maxEle = 7;
  public ArrayList<Long> directory;
  private final RandomAccessFile raf;
  private final RandomAccessFile buckets; //profundidade - n° elementos - caves e endereços
  /*
   * Cada bucket pode armazenar até 7 chaves
   * Estrutura:
   * Profundidade + n° de elementos + id + chave ...
   * tam = short + byte + int + long ....
   *     = 1 + 2 + 7 * (4 + 8) = 87
   */

  public Hash(File file, int version) throws IOException { // armazena no maximo 7 chaves
    this.directory = new ArrayList<Long>();
    this.depth = 2;
    this.raf = new RandomAccessFile(file, "r");
    this.buckets = new RandomAccessFile("buckets.bin", "rw");
    this.initialEle = (version == 2) ? 400 : 17560;
    this.initialize(version);
  }

  private int hash(int id) {
    return (int)(id % Math.pow(2, depth));
  }

  // public void imprimir() throws IOException {
  // raf.seek(0);
  // System.out.println("last id = " + raf.readInt());
  // raf.seek(4);
  // System.out.println("posição = " + raf.getFilePointer());
  // Record record = Record.deserialize(raf);
  // System.out.println(raf.length());
  // System.out.println(record.getId());
  // System.out.println(record.getName());
  // }

  public void initialize(int version) throws IOException {
    try {
      raf.seek(0);
      raf.seek(4);

      while(!eof(raf)) {
        Long pointer = raf.getFilePointer();
        Record record = Record.deserialize(raf);
        int id = record.getId();
        System.out.println("ponteiro = " + pointer + " id: " + id);
      }

  } catch (IOException e) {
      throw new IOException(
              "Error while initializing the database", e);
  }
  }

  public void add(int id, long pointer) throws IOException {
    //pegar a posição do bucket
    int pos = hash(id);
    long seek = directory.get(pos);
    buckets.seek(seek);
    Bucket bucket = Bucket.deserialize(buckets); // pego as informaçoes do meu bucket
    if(bucket.getEle() <= maxEle){ // bucket ainda não está cheio
      BucketNode node = new BucketNode(id, pointer);
      bucket.setNodes(node, bucket.getEle());
      bucket.setNodes(bucket.sort(bucket.getNodes()));
      buckets.seek(seek); // volto o ponteiro para o início do bucket e reescrevo ordenadamente
      bucket.serialize(buckets);
    } else if(bucket.getDepth() < depth) { // bucket esta cheio mas a profundidade local é menor do que a global
      //realoc keys in buckets
    } else { // bucket esta cheio e sua profundidade local é igual a global
      //realoc keys in buckets
    }
  }

  private boolean eof(RandomAccessFile raf) throws IOException {
    try {
        return raf.getFilePointer() == raf.length();

    } catch (IOException e) {
        throw new IOException(
                "Error while checking for EOF", e);
    }
}

}
