package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * DO NOT EDIT
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) { 
        fileName = f; 
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by frequency
     */
    public void makeSortedList() {
        StdIn.setFile(fileName);
	/* Your code goes here */
        double total=0; //make sure total is double for probability to be correct
        int[] charArray = new int[128]; //array of 128 for each ASCII value
        while (StdIn.hasNextChar()) {
            char c = StdIn.readChar();
            charArray[c]+=1; //stores frequency at ASII value index
            total++;
        }
        double probability;
        sortedCharFreqList = new ArrayList<>();
        for (int i=0;i<charArray.length;i++){
            if (charArray[i]>0){
                probability = charArray[i]/total;
                CharFreq character = new CharFreq((char)i,probability);
                sortedCharFreqList.add(character);
            }
        }
        // add extraChar if file just one distinct character
        if (sortedCharFreqList.size()==1) {
            if ((char)sortedCharFreqList.get(0).getCharacter()==127) {
                CharFreq extraChar = new CharFreq((char)0, 0.0);
                sortedCharFreqList.add(extraChar);
            } else {
                CharFreq extraChar = new CharFreq((char)(sortedCharFreqList.get(0).getCharacter()+1), 0.0);
                sortedCharFreqList.add(extraChar);
            }
        }
        Collections.sort(sortedCharFreqList);
    }
    

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    public void makeTree() {
	/* Your code goes here */
        Queue<CharFreq> source = new Queue<CharFreq>();
        Queue<TreeNode> target = new Queue<TreeNode>();

        //enqueue source with increasing order of probability
        for (int i = 0; i<sortedCharFreqList.size(); i++){
            source.enqueue(sortedCharFreqList.get(i));
        }

        CharFreq firstSmallest = new CharFreq();
        CharFreq secSmallest = new CharFreq();
        double totalProb;
        CharFreq newNode = new CharFreq();
        TreeNode leftNode = new TreeNode();
        TreeNode rightNode = new TreeNode();
        while (!source.isEmpty()||target.size()>1){
            double sourceProb;
            double targetProb;
            int count = 0;   
            while (count<2){
                if (target.isEmpty()&&count==0){ //first iteration bc target is empty
                    firstSmallest= source.dequeue(); 
                    leftNode = new TreeNode(firstSmallest, null, null);
                    secSmallest = source.dequeue();
                    rightNode = new TreeNode(secSmallest, null, null);
                    break;
                }
                if (source.isEmpty()&&count==0) { 
                    firstSmallest = target.peek().getData();//this will only get the CharFreq (char,prob)
                    leftNode = target.dequeue(); //this will take the whole tree
                    secSmallest = target.peek().getData();
                    rightNode = target.dequeue(); //this will take the whole tree
                    break;
                }
                if (source.isEmpty()&&count==1){ 
                    secSmallest=target.peek().getData();
                    rightNode = target.dequeue();
                    break;
                }
                if (target.isEmpty()&&count==1){
                    secSmallest=source.dequeue();
                    rightNode = new TreeNode (secSmallest, null,null);
                    break;
                }
                if (count==0){
                    sourceProb=source.peek().getProbOcc();
                    targetProb = target.peek().getData().getProbOcc();
                    if (sourceProb<=targetProb) {
                        firstSmallest = source.dequeue();
                        leftNode = new TreeNode(firstSmallest, null, null);
                    } else {
                        firstSmallest = target.peek().getData();
                        leftNode = target.dequeue();
                    }
                }
                if (count==1){
                    sourceProb = source.peek().getProbOcc();
                    targetProb = target.peek().getData().getProbOcc();
                    if (sourceProb<=targetProb){
                        secSmallest = source.dequeue();
                        rightNode = new TreeNode(secSmallest,null,null);
                    } else {
                        secSmallest = target.peek().getData();
                        rightNode = target.dequeue();
                    }
                } 
                count++;
            }
            totalProb = firstSmallest.getProbOcc() + secSmallest.getProbOcc();
            newNode = new CharFreq(null,totalProb);
            huffmanRoot = new TreeNode(newNode, leftNode, rightNode);
            target.enqueue(huffmanRoot);
        }
    }

    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null.
     * Set encodings to this array.
     */
    public void makeEncodings() {
	/* Your code goes here */
        encodings = new String[128];
        String binary="";
        helper(huffmanRoot, binary);
    }
    private void helper(TreeNode tree, String binary) {
        if (tree == null) {
            return;
        }
        if (tree.getLeft() == null && tree.getRight() == null) {
            int index = (int) tree.getData().getCharacter();
            encodings[index] = binary;
        } else {
            helper(tree.getLeft(), binary + "0");
            helper(tree.getRight(), binary + "1");
        }
    }

    /**
     * Using encodings and filename, this method makes use of the writeBitString method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        /* Your code goes here */
        StdIn.setFile(fileName);
        String bitString = "";
        while (StdIn.hasNextChar()){
            int nextChar = (int)StdIn.readChar();
            bitString += encodings[nextChar];
        }
        writeBitString(encodedFile, bitString);
    }
    
    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * DO NOT EDIT
     * 
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding-1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }

            if (c == '1') currentByte += 1 << (7-byteIndex);
            byteIndex++;
            
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        
        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString method 
     * to convert the file into a bit string, then decodes the bit string using the 
     * tree, and writes it to a decoded file. 
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    public void decode(String encodedFile, String decodedFile) {
        StdOut.setFile(decodedFile);
	    /* Your code goes here */
        String bit = readBitString(encodedFile);
        TreeNode tree = huffmanRoot;
        for (int i = 0; i<bit.length();i++){
            char c = bit.charAt(i);
            if (c=='1'){
                tree=tree.getRight();
            }
            if (c=='0'){
                tree=tree.getLeft();
            }
            if (tree.getRight()==null&&tree.getLeft()==null){
                StdOut.print(tree.getData().getCharacter());
                tree=huffmanRoot;
            }
        }
    }

    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * DO NOT EDIT
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString + 
                String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1') return bitString.substring(i+1);
            }
            
            return bitString.substring(8);
        }
        catch(Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver. 
     * DO NOT EDIT or REMOVE
     */

    public String getFileName() { 
        return fileName; 
    }

    public ArrayList<CharFreq> getSortedCharFreqList() { 
        return sortedCharFreqList; 
    }

    public TreeNode getHuffmanRoot() { 
        return huffmanRoot; 
    }

    public String[] getEncodings() { 
        return encodings; 
    }
}

