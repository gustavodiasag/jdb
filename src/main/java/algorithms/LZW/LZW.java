package main.java.algorithms.LZW;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class LZW {
  private static final short MAX_DICTIONARY_SIZE = 32767; // Maximum dictionary size 2^16, only positive numbers.
  private static int instances;
  private long executionTime;
    /**
   * Compresses the given input file using the LZW algorithm and writes the 
   * compressed data to the output file.
   *
   * @param  inputFile   the file to compress
   * @param  outputFile  the file to write the compressed data to
   */

   public LZW () {
      LZW.instances ++;
   }
  public void compress(File inputFile) {
    long startTime = System.currentTimeMillis();
    File outputFile = new File(inputFile.getName() + "LZW" + instances + ".bin");
    try (RandomAccessFile inputRAF = new RandomAccessFile(inputFile, "r");
        RandomAccessFile outputRAF = new RandomAccessFile(outputFile, "rw")) {

      LZWDictionary dictionary = new LZWDictionary();
      short currentByte;
      StringBuilder currentSequence = new StringBuilder();

      while ((currentByte = (short) inputRAF.read()) != -1) {
        currentSequence.append((char) currentByte);

        if (!dictionary.containsSequence(currentSequence.toString())) {
          dictionary.addSequence(currentSequence.toString());
          short prefixCode = (short) dictionary.getCode(currentSequence.substring(0, currentSequence.length() - 1));
          if(prefixCode > -1) { 
            outputRAF.writeShort(prefixCode );
          }
          currentSequence.delete(0, currentSequence.length() - 1);
        }
      }

      if (currentSequence.length() > 0) {
        short code = (short) dictionary.getCode(currentSequence.toString());
        if(code > -1) { 
          outputRAF.writeShort(code );
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    long endTime = System.currentTimeMillis();
    this.executionTime = endTime - startTime;
    System.out.println("Compressing archive");
    this.printLength(inputFile, outputFile);
  }

    /**
   * Decompresses the input file using LZWDictionary and writes to the output file.
   *
   * @param inputFile  the input file to decompress
   * @param outputFile the output file to write the decompressed data
   */

  public void decompress(int version) {
    long startTime = System.currentTimeMillis();
    File inputFile = new File("db.binLZW" + version + ".bin");
    File outputFile = new File("db.bin");
    if(outputFile.exists()) {
      outputFile.delete();
    }
    try (RandomAccessFile inputRAF = new RandomAccessFile(inputFile, "r");
        RandomAccessFile outputRAF = new RandomAccessFile(outputFile, "rw")) {

      LZWDictionary dictionary = new LZWDictionary();
      short currentCode = inputRAF.readShort();
      StringBuilder currentSequence = new StringBuilder(dictionary.getSequence(currentCode));
      outputRAF.writeBytes(currentSequence.toString());

      while (inputRAF.getFilePointer() < inputRAF.length()) {
        currentCode = inputRAF.readShort();

        if (!dictionary.containsCode(currentCode)) {
          String sequence = currentSequence.toString() + currentSequence.charAt(0);
          dictionary.addSequence(sequence);
          outputRAF.writeBytes(sequence);
          currentSequence = new StringBuilder(sequence);
        } else {
          String sequence = dictionary.getSequence(currentCode);
          outputRAF.writeBytes(sequence);
          currentSequence.append(sequence.charAt(0));
          dictionary.addSequence(currentSequence.toString());
          currentSequence = new StringBuilder(sequence);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    long endTime = System.currentTimeMillis();
    this.executionTime = endTime - startTime;
    System.out.println("\nDecompressing archive");
    System.out.println("Execution time: " + this.executionTime + " ms");
    System.out.println("Decompacted file" + outputFile.getName() + " size: " + outputFile.length() + " bytes");
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

  private static class LZWDictionary {
    private final String[] dictionary; // Array to store the dictionary entries
    private int nextCode; // Next available code in the dictionary
  
    public LZWDictionary() {
      dictionary = new String[MAX_DICTIONARY_SIZE]; // Initialize the dictionary array
      nextCode = 0; // Initialize the nextCode to 0
  
      // Initialize the dictionary with all possible  numbers with 1 byte
      for (int i = 0; i < 256; i++) {
        dictionary[i] = Integer.toBinaryString(i);
        nextCode++;
      }
    }
  
    // Check if the dictionary contains a given sequence
    public boolean containsSequence(String sequence) {
      return getCode(sequence) != -1;
    }
  
    // Check if the dictionary contains a given code
    public boolean containsCode(short code) {
      return code >= 0 && code < nextCode;
    }
  
    // Add a new sequence to the dictionary
    public void addSequence(String sequence) {
      if (nextCode < MAX_DICTIONARY_SIZE) {
        dictionary[nextCode++] = sequence;
      }
    }
  
    // Get the code for a given sequence in the dictionary
    public short getCode(String sequence) {
      for (short i = 0; i < nextCode; i++) {
        if (dictionary[i].equals(sequence)) {
          return i;
        }
      }
      return -1; // Sequence not found in the dictionary
    }
  
    // Get the sequence for a given code in the dictionary
    public String getSequence(int code) {
      return dictionary[code];
    }
  }
}
