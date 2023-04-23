package main.java.structures.hash;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import main.java.database.Record;

public class Hash {
  private int globalDepth;
  private byte bucketLength = 87;
  private int maxEle = 7;
  public ArrayList<Long> directory;
  private final RandomAccessFile buckets;
  /*
   * Cada bucket pode armazenar até 7 chaves
   * Estrutura:
   * Profundidade + n° de elementos + id + chave ...
   * tam = short + byte + int + long ....
   * = 1 + 2 + 7 * (4 + 8) = 87
   */

  public Hash() throws IOException {
    this.directory = new ArrayList<Long>();
    this.globalDepth = 1;

    this.buckets = new RandomAccessFile("buckets.bin", "rw");
    this.initialize();
  }

  private int hash(int id) {
    return (int) (id % Math.pow(2, globalDepth));
  }

  private int hash(int id, int depth) {
    return (int) (id % Math.pow(2, depth));
  }

  public void initialize() throws IOException {
    try {
      // crio o ponteiro para os dois primeiros buckets
      directory.add((long) 0);
      directory.add((long) bucketLength);

      // crio dois buckets vazios e escrevos eles no arquivo
      buckets.seek(0);
      for (int i = 0; i < 2; i++) {
        Bucket newBucket = new Bucket((short) globalDepth);
        newBucket.serialize(buckets);
      }

    } catch (IOException e) {
      throw new IOException("Error while initializing the hash", e);
    }
  }

  public void add(int id, long pointer) throws IOException {
    // monto meu novo "no" do bucket
    BucketNode newNode = new BucketNode(id, pointer);

    // pegar a posição do bucket
    int pos = hash(id);
    long seek = directory.get(pos);
    buckets.seek(seek);

    // pego as informaçoes do meu bucket
    Bucket bucket = Bucket.deserialize(buckets);
    if (bucket.getEle() < maxEle) { // bucket ainda não está cheio
      BucketNode node = new BucketNode(id, pointer);
      bucket.setNode(node);
      bucket.sort();
      buckets.seek(seek); // volto o ponteiro para o início do bucket e reescrevo ordenadamente
      bucket.serialize(buckets);
    } else if (bucket.getDepth() < globalDepth) { // bucket esta cheio mas a profundidade local é menor do que a global
      BucketNode[] allNodes = getAllNodes(bucket, newNode);
      splitBucket(allNodes, (bucket.getDepth() + 1), pos);

    } else { // bucket esta cheio e sua profundidade local é igual a global
      incraseDepth();
      BucketNode[] allNodes = getAllNodes(bucket, newNode);
      splitBucket(allNodes, globalDepth, pos);
    }
  }

  private BucketNode[] getAllNodes(Bucket bucket, BucketNode node) {
    BucketNode[] allNodes = new BucketNode[bucket.getEle() + 1];

    for (int i = 0; i < bucket.getEle(); i++) {
      allNodes[i] = new BucketNode(bucket.getNode(i).key, bucket.getNode(i).pointer);
    }

    allNodes[allNodes.length - 1] = new BucketNode(node.key, node.pointer);

    return allNodes;
  }

  private void incraseDepth() {
    globalDepth += 1;
    int length = directory.size();
    for (int i = 0; i < length; i++) {
      directory.add(directory.get(i));
    }
  }

  private int searchOtherPointer(BucketNode[] nodes, int localDepth, int pos) {
    int otherPointer = -1;

    for(int i = 0; i < nodes.length; i++) {
      int hash = hash(nodes[i].key, localDepth);
      if(hash != pos) {
        otherPointer = hash;
        break;
      }
    }
    return otherPointer;
  }

  private void splitBucket(BucketNode[] nodes, int localDepth, int pos) throws IOException {
    int newPos = searchOtherPointer(nodes, localDepth, pos);

    //Preciso saber se entrar no loop para poder voltar o profundidade local para o valor correto
    boolean loop = false;

    // Caso em que é necessário aumentar mais de uma vez a profundidade global para conseguir distribuir as chaves
    // Para entrar nesse while foi um caso muito especifico que me deparei testando 
    while((newPos == -1)) {
      newPos = searchOtherPointer(nodes, localDepth++, pos);
      if(newPos == -1) {
        incraseDepth();
      }
      loop = true;
    }

    if(loop) {      
      localDepth--;
    }

    // Preciso aumantar a profundidade local em 1 no caso em que o diretorio aponta para outro diretorio
    if(hash(pos, localDepth) == hash(newPos, localDepth)) {
      localDepth++;
    }

    if(pos > newPos) {
      int tempPos = pos;
      pos = newPos;
      newPos = tempPos;
    }

    // Adiciono a nova posição do bucket ao diretorio
    directory.remove(newPos);
    directory.add(newPos, (long) (newPos * bucketLength));
    Bucket newBucket = new Bucket((short) localDepth);
    Bucket oldBucket = new Bucket((short) localDepth);

    for (BucketNode node : nodes) {
      int hashPos = hash(node.key, localDepth);
      if (hashPos == pos) {
        oldBucket.setNode(node);
      } else {
        newBucket.setNode(node);
      }
    }

    buckets.seek(directory.get(pos));
    oldBucket.serialize(buckets);

    buckets.seek(directory.get(newPos));
    newBucket.serialize(buckets);
  }

  public long search(int id) throws IOException {
    int hash = hash(id);

    buckets.seek(directory.get(hash));
    Bucket bucket = Bucket.deserialize(buckets);

    return bucket.getPos(id);
  }

  private boolean eof(RandomAccessFile raf) throws IOException {
    try {
      return raf.getFilePointer() == raf.length();

    } catch (IOException e) {
      throw new IOException(
          "Error while checking for EOF", e);
    }
  }

  public void print() throws IOException {
    buckets.seek(0);
    System.out.println("\nProfundidade global = " + globalDepth);
    int count = 0;
    while(!eof(buckets)){
      System.out.println("");
      System.out.println("Bucket " + count);
      Bucket bucket = Bucket.deserialize(buckets);
      bucket.print();
      count ++;
    }
    System.out.println("\nDiretorios");
    for(int i = 0; i < directory.size(); i++) {
      System.out.println("Diretorio[" + i + "] = " + directory.get(i));
    }
  }

}