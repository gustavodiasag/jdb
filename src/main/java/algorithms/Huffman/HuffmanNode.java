package main.java.algorithms.Huffman;

public class HuffmanNode extends HuffmanTree {
    public final HuffmanTree left, right; // sub-�rvores
 
    public HuffmanNode(HuffmanTree l, HuffmanTree r) {
        super(l.frequency + r.frequency);
        left = l;
        right = r;
    }
}
