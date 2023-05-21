package main.java.algorithms.Huffman;

import java.util.*;
import java.io.IOException;
import java.io.File;
import java.io.RandomAccessFile;

public class Huffman {
    private int[] charFreqs;
    private static int instances;
    private HuffmanTree tree;
    private RandomAccessFile compressed, decompressed;
    private long executionTime;

    public Huffman() {
        instances++;
        charFreqs = new int[256];
    }

	public void compress(File inputFile) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(inputFile, "rw")) {
            raf.seek(0);

            while(raf.getFilePointer() < raf.length()) {
                char character = (char)raf.read();
                charFreqs[character]++;
            }
            
            this.tree = buildTree();
            
            long startTime = System.currentTimeMillis();
            File outputFile = new File(inputFile.getName() + "Huff" + instances + ".bin");
            
            this.compressed = encode(tree, raf, outputFile);
            
            long endTime = System.currentTimeMillis();
            this.executionTime = endTime - startTime;

            System.out.println("Compressing archive");
            printLength(inputFile, outputFile);
        }
    }

    public void decompress(int version) throws IOException {
        File inputFile = new File("db.binHuff" + version + ".bin");
        File outputFile = new File("db.bin");

        try (RandomAccessFile raf = new RandomAccessFile(inputFile, "rw")) {
            long startTime = System.currentTimeMillis();
    
            this.decompressed = decode(tree, raf, outputFile);
    
            long endTime = System.currentTimeMillis();
            this.executionTime = endTime - startTime;
            System.out.println("\nDecompressing archive");
            System.out.println("Execution time: " + this.executionTime + " ms");
            System.out.println("Decompacted file" + outputFile.getName() + " size: " + outputFile.length() + " bytes");
        }
    }

    public HuffmanTree buildTree() {
        PriorityQueue<HuffmanTree> trees = new PriorityQueue<HuffmanTree>();
        
        for (int i = 0; i < charFreqs.length; i++){
            if (charFreqs[i] > 0)
                trees.offer(new HuffmanLeaf(charFreqs[i], (char)i));
        }

        while (trees.size() > 1) {
            HuffmanTree a = trees.poll(); 
            HuffmanTree b = trees.poll();
 
            trees.offer(new HuffmanNode(a, b)); 
        }

        return trees.poll();
    }
 
    public RandomAccessFile encode(HuffmanTree tree, RandomAccessFile encode, File outputFile) throws IOException {
    	assert tree != null;

        encode.seek(0);
        RandomAccessFile compressedFile = new RandomAccessFile(outputFile, "rw");

        while(encode.getFilePointer() < encode.length()) {
            char character = (char) encode.read();
            compressedFile.writeUTF((getCodes(tree, new StringBuffer(), character)));
        }

    	return compressedFile;
    }
    
    public RandomAccessFile decode(HuffmanTree tree, RandomAccessFile decode, File outputFile) throws IOException {
    	assert tree != null;

        RandomAccessFile decompression = new RandomAccessFile(outputFile, "rw");
        HuffmanNode node = (HuffmanNode)tree;
        decode.seek(0);

        while(decode.getFilePointer() < decode.length()) {
            String code = decode.readUTF();

            for(char c : code.toCharArray()) {
                if (c == '0'){
                    if (node.left instanceof HuffmanLeaf) { 
                        decompression.writeChar(((HuffmanLeaf)node.left).value);
                        node = (HuffmanNode)tree;
                    }else{
                        node = (HuffmanNode) node.left;
                    }
                }else if (c == '1'){
                    if (node.right instanceof HuffmanLeaf) {
                        decompression.writeChar(((HuffmanLeaf)node.right).value);
                        node = (HuffmanNode)tree;
                    }else{
                        node = (HuffmanNode) node.right;
                    }
                }
            }
        }

    	return decompression;
    }
    
    public String getCodes(HuffmanTree tree, StringBuffer prefix, char w) {
        assert tree != null;
        
        if (tree instanceof HuffmanLeaf) {
            HuffmanLeaf leaf = (HuffmanLeaf)tree;
            
            if (leaf.value == w ){
            	return prefix.toString();
            }
            
        } else if (tree instanceof HuffmanNode) {
            HuffmanNode node = (HuffmanNode)tree;
 
            prefix.append('0');
            String left = getCodes(node.left, prefix, w);
            prefix.deleteCharAt(prefix.length()-1);
 
            prefix.append('1');
            String right = getCodes(node.right, prefix,w);
            prefix.deleteCharAt(prefix.length()-1);
            
            if (left==null) return right; else return left;
        }
		return null;
    }

    public void printLength (File inputFile, File outputFile) {
    long inputSize = inputFile.length();
    long outputSize = outputFile.length();
    double ratio = (double) outputSize / inputSize;
    double factor = (double) inputSize / outputSize;
    double gain = 100 * Math.log(factor);
    System.out.print("\nOriginal file: ");
    System.out.println(inputFile.getName() + " size: " + inputSize + " bytes");
    System.out.print("\nCompacted file: ");
    System.out.println(outputFile.getName() + " size: " + outputSize + " bytes");
    System.out.println("\nCompression ratio: " + ratio + " bytes");
    System.out.println("\nCompression Factor:" + factor + " bytes");
    System.out.println("\nCompression Gain: " + gain + " %");
    System.out.println("\nExecution time: " + this.executionTime + " ms");
  }
}
