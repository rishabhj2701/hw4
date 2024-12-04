/**
 * Archived Message Reconstruction Program
 *
 * This program reconstructs a compressed message that has been archived using a binary-tree-based encoding scheme.
 * The program reads an archive file (.arch) containing a tree structure and a binary message.
 * It decodes the message by traversing the binary tree and outputs the decoded message to the console.
 *
 * The encoding is based on a binary tree where each leaf node represents a character, and the path from the root
 * to each leaf represents the character's binary code (0 for left, 1 for right).
 * The program also calculates and prints statistics including:
 * - Average bits per character
 * - Total characters in the message
 * - Space savings compared to an uncompressed format (16 bits per character).
 * 
 * @author Rishabh Jain
 */

 package src.edu.iastate.cs2280.hw4;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.util.Scanner;
 import java.util.Stack;
 
 public class MsgTree {
     private char payloadChar;
     private MsgTree left;
     private MsgTree right;
 
     private static int currentBitIndex = 0;
 
     /**
      * Constructs a binary tree using the given encoding string (iterative approach).
      *
      * @param encoding The string representing the preorder traversal of the tree.
      */
     public MsgTree(String encoding) {
         if (encoding == null || encoding.isEmpty()) {
             throw new IllegalArgumentException("Invalid encoding string");
         }
 
         MsgTree root = constructTreeIterative(encoding.toCharArray());
         this.payloadChar = root.payloadChar;
         this.left = root.left;
         this.right = root.right;
     }
 
     /**
      * Helper method to construct a tree iteratively.
      *
      * @param encoding The encoding characters.
      * @return The constructed root of the tree.
      */
     private int CharIdx = 0;
 
     private MsgTree constructTreeIterative(char[] encoding) {
         Stack<MsgTree> stack = new Stack<>();
         MsgTree root = new MsgTree(encoding[CharIdx++]);
         stack.push(root);
 
         while (!stack.isEmpty() && CharIdx < encoding.length) {
             MsgTree current = stack.peek();
 
             if (current.payloadChar == '^' && current.left == null) {
                 MsgTree leftNode = new MsgTree(encoding[CharIdx++]);
                 current.left = leftNode;
                 if (leftNode.payloadChar == '^') {
                     stack.push(leftNode);
                 }
             } else if (current.payloadChar == '^' && current.right == null) {
                 MsgTree rightNode = new MsgTree(encoding[CharIdx++]);
                 current.right = rightNode;
                 if (rightNode.payloadChar == '^') {
                     stack.push(rightNode);
                 }
             } else {
                 stack.pop();
             }
         }
 
         return root;
     }
 
     /**
      * Constructor for a single node.
      *
      * @param payloadChar Character for the node.
      */
     public MsgTree(char payloadChar) {
         this.payloadChar = payloadChar;
         this.left = null;
         this.right = null;
     }
 
     /**
      * Prints binary codes for characters in the binary tree.
      *
      * @param root The root node of the tree.
      * @param path The current binary path to the node.
      */
     public static void printCodes(MsgTree root, String path) {
         if (root == null) {
             return;
         }
 
         if (root.payloadChar != '^') {
             System.out.println(root.payloadChar + "\t" + path);
         }
 
         printCodes(root.left, path + "0");
         printCodes(root.right, path + "1");
     }
 
     /**
      * Decodes a given binary-encoded message using the binary tree.
      *
      * @param root The root node of the binary tree.
      * @param msg  The binary-encoded message as a string of '0's and '1's.
      */
     public void decode(MsgTree root, String msg) {
         if (root == null || msg == null || msg.isEmpty()) {
             throw new IllegalArgumentException("Invalid input for decoding");
         }
 
         System.out.println("\nDecoded MESSAGE:");
         StringBuilder decodedMessage = new StringBuilder();
         MsgTree currentNode = root;
         int totalBits = msg.length();

         currentBitIndex = 0;
 
         while (currentBitIndex < totalBits) {
             char bit = msg.charAt(currentBitIndex++);
             currentNode = (bit == '0') ? currentNode.left : currentNode.right;
 
             if (currentNode == null) {
                 throw new IllegalStateException("Invalid bit sequence in the encoded message.");
             }
 
             if (currentNode.payloadChar != '^') {
                 decodedMessage.append(currentNode.payloadChar);
                 currentNode = root;
             }
         }
 
         System.out.println(decodedMessage);
 
         Statistics(totalBits, decodedMessage.length());
     }
 
     /**
      * Prints the statistics for the message decoding process.
      *
      * @param totalBits       Total number of bits in the encoded message.
      * @param totalCharacters Total number of decoded characters.
      */
     private void Statistics(int totalBits, int totalCharacters) {
         int uncompressedBits = totalCharacters * 16;
         double spaceSaving = (1 - (double) totalBits / uncompressedBits) * 100;
         double avgBitsPerChar = (double) totalBits / totalCharacters;
 
         System.out.println("\nSTATISTICS:");
         System.out.printf("Avg bits/char: %.1f\n", avgBitsPerChar);
         System.out.printf("Total characters: %d\n", totalCharacters);
         System.out.printf("Space savings: %.1f%%\n", spaceSaving);
     }
 
     /**
      * Main method for reading input, constructing the tree, and decoding messages.
      *
      * @param args Command-line arguments (not used).
      */
     public static void main(String[] args) {
         try {
             String filename = getFilename();
 
             String content = readFileContent(filename);
             String[] parts = parseFileContent(content);
 
             String pattern = parts[0];
             String binaryMessage = parts[1];
 
             MsgTree root = new MsgTree(pattern);
             displayCharacterCodes(root);
             root.decode(root, binaryMessage);
 
         } catch (IOException e) {
             System.err.println("File error: " + e.getMessage());
         } catch (IllegalArgumentException | IllegalStateException e) {
             System.err.println("Processing error: " + e.getMessage());
         } catch (Exception e) {
             System.err.println("Unexpected error: " + e.getMessage());
         }
     }
 
     /**
      * Prompts the user to enter the filename.
      *
      * @return The filename entered by the user.
      */
     private static String getFilename() {
         try (Scanner scanner = new Scanner(System.in)) {
             System.out.println("Please enter the filename to decode:");
             return scanner.nextLine().trim();
         }
     }
 
     /**
      * Reads the content of the specified file.
      *
      * @param filename The name of the file to read.
      * @return The file content as a string.
      * @throws IOException If an error occurs during file reading.
      */
     private static String readFileContent(String filename) throws IOException {
         return new String(Files.readAllBytes(Paths.get(filename))).trim();
     }
 
     /**
      * Splits the file content into the tree encoding pattern and the binary message.
      *
      * @param content The raw content of the file.
      * @return A string array where the first element is the tree encoding and the second is the binary message.
      */
     private static String[] parseFileContent(String content) {
         int pos = content.lastIndexOf('\n');
         if (pos == -1) {
             throw new IllegalArgumentException("Invalid file format: Missing binary message.");
         }
         return new String[]{content.substring(0, pos).trim(), content.substring(pos + 1).trim()};
     }
 
     /**
      * Displays the character codes by traversing the binary tree.
      *
      * @param root The root node of the tree.
      */
     private static void displayCharacterCodes(MsgTree root) {
         System.out.println("\nCharacter Code\n-------------------------");
         MsgTree.printCodes(root, "");
     }
 }
