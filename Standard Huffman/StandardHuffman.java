import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class StandardHuffman {
    private JFrame frame;
    private JTextArea textArea;

    StandardHuffman() {
        // Create the main frame
        frame = new JFrame("Standard Huffman Compression and Decompression");
        frame.setSize(500, 300); // Frame size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close operation (the program terminates on clicking "X")

        // Create a text area to display messages
        textArea = new JTextArea();
        frame.add(textArea);

        // Create the compress button
        JButton compressButton = new JButton("Compress");
        compressButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                compressAction();
            }
        });

        // Create the decompress button
        JButton decompressButton = new JButton("Decompress");
        decompressButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                decompressAction();
            }
        });

        // Create a button panel to group the compress and the decompress buttons together
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(compressButton);
        buttonPanel.add(decompressButton);

        // Add the button panel to the bottom of the frame
        frame.add(buttonPanel, "South");
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new StandardHuffman();
            }
        });
    }
    private void decompressAction(){
        // Create a file chooser dialog to allow the user to select a file and store the user's selection
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(frame);
        // Check if the user selected a file (clicked "Open" and did not close the window)
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile(); // Get the selected file to be decompressed
            decompress (selectedFile);
        }
    }

    private void compressAction() {
        // Create a file chooser dialog to allow the user to select a file and store the user's selection
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(frame);

        // Check if the user selected a file (clicked "Open" and did not close the window)
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile(); // Get the selected file to be compressed
            compress(selectedFile);
        }
    }
    private void decompress(File file){
        try  {
            Scanner scanner = new Scanner(file);
            StringBuilder compressedBytes = new StringBuilder();
            compressedBytes.append(scanner.nextLine());
            String compressedBytesString=compressedBytes.toString();
            StringBuilder binaryStringBuilder= new StringBuilder();
            String huffmanCodes="";
            Map <String,String> huffmanTable=new HashMap<>();
            while (scanner.hasNext()){
                huffmanTable.put(scanner.next(),scanner.next());
            }
            for (int i=0;i<compressedBytesString.length();i++){
                if(i==compressedBytesString.length()-1){
                    int index=0;
                    String lastByteModifier=Integer.toBinaryString((int)compressedBytesString.charAt(i));
                    for(int j=0;j<lastByteModifier.length();j++){
                        if(lastByteModifier.charAt(j)=='1'){
                            index=j;
                        }
                    }
                    String finalOriginalValue="";
                    for(int j=0;j<=index;j++){
                        finalOriginalValue+=lastByteModifier.charAt(j);
                    }
                    binaryStringBuilder.append(finalOriginalValue);
                    break;
                }
                if(((int)compressedBytesString.charAt(i)>=64)&& ((int)compressedBytesString.charAt(i)<128)){
                    binaryStringBuilder.append("0");
                }
                else if((int)compressedBytesString.charAt(i)>=32){
                    binaryStringBuilder.append("00");
                }
                else if((int)compressedBytesString.charAt(i)>=16){
                    binaryStringBuilder.append("000");
                }
                else if((int)compressedBytesString.charAt(i)>=8){
                    binaryStringBuilder.append("0000");
                }
                else if((int)compressedBytesString.charAt(i)>=4){
                    binaryStringBuilder.append("00000");
                }
                else if((int)compressedBytesString.charAt(i)>=2){
                    binaryStringBuilder.append("000000");
                }
                else if((int)compressedBytesString.charAt(i)>=1){
                    binaryStringBuilder.append("0000000");
                }
                binaryStringBuilder.append(Integer.toBinaryString((int)compressedBytesString.charAt(i)));
            }
            huffmanCodes=binaryStringBuilder.toString();
            
            scanner.close();
            String searchString="";
            String originalData="";
            for(int i=0;i<huffmanCodes.length();i++){
                searchString+=huffmanCodes.charAt(i);
                for(Map.Entry<String,String> entry:huffmanTable.entrySet()){
                    if (searchString.equals(entry.getValue())){
                        originalData+=entry.getKey();
                        searchString="";
                    }
                }
            }
            try  {
                File decompressedFile=new File("decompressed.txt");
                decompressedFile.createNewFile();
                FileWriter fileWriter =new FileWriter("decompressed.txt");
                fileWriter.write(originalData);
                fileWriter.close();
                textArea.setText("File is decompressed successfully in decompressed.txt");
            } catch (Exception e) {
                System.out.println("Something went wrong.");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Please make sure the file exists.");
        }
    }
    private void compress(File file) {
        try {
            // Get the original text from the file
            Scanner scanner = new Scanner(file);
            StringBuilder originalData = new StringBuilder();
            while (scanner.hasNextLine()) {
                originalData.append(scanner.nextLine());
            }
            scanner.close();

            // Calculate character probabilities of the original text
            Map<Character, Integer> probabilities = new HashMap<>();
            for (char c : originalData.toString().toCharArray()) {
                 probabilities.put(c, probabilities.getOrDefault(c, 0) + 1); // Ensure that the default frequency is 0 if a character is encountered for the first time
            }

            // Create a priority queue (to handle the ordering of characters based on their probabilities)
            PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(node -> node.frequency));
            for (Map.Entry<Character, Integer> entry : probabilities.entrySet()) {
                Node newNode = new Node(entry.getKey(), entry.getValue()); // Create a new Node for the current character and its probability
                queue.add(newNode);
            }

            while (queue.size() > 1) { // Continue until there is only one node with one probability
                // The two nodes with the lowest probabilities are removed
                Node left = queue.poll();
                Node right = queue.poll();

                // Add a new combined node with a probability equal to the sum of the probabilities of the two nodes removed
                Node combined = new Node('\0', left.frequency + right.frequency);
                combined.left = left; // The left and right children of the new node are set to the nodes whose probabilities were added
                combined.right = right;
                queue.add(combined);
            }

            Map<Character, String> codes = new HashMap<>(); // To store the codes for each character
            buildCodes(queue.peek(), "", codes); // Pass the root to assign the other added characters their own code

            // Convert each character of the original text to its code
            StringBuilder compressedString = new StringBuilder();
            for (char c : originalData.toString().toCharArray()) {
                compressedString.append(codes.get(c));
            }

            // Convert the compressed string to bytes
            byte[] compressedBytes = new byte[(compressedString.length() + 7) / 8];
            for (int i = 0; i < compressedString.length(); i++) {
                if (compressedString.toString().charAt(i) == '1') {
                    // Set the corresponding bit in the byte
                    compressedBytes[i / 8] |= (byte) (1 << (7 - (i % 8)));
                }
            }

            try (FileOutputStream compressedFile = new FileOutputStream("compressed.bin")) {
                compressedFile.write(compressedBytes);
                for (Map.Entry<Character, String> code : codes.entrySet()) {
                    compressedFile.write(("\n" + code.getKey() + " " + code.getValue()).getBytes());
                }
                textArea.setText("File is compressed successfully in compressed.bin");
            } catch (IOException e) {
                System.out.println("Something went wrong.");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Please make sure the file exists.");
        }
    }
    private void buildCodes(Node root, String code, Map<Character, String> huffmanCodes) {
        // Check that the node is not null (have a character with its probability)
        if (root != null) {
            if (root.character != '\0') { // If the node is not an internal node and has a character with its probability
                huffmanCodes.put(root.character, code);
            }
            // Assign 0 to one of its children and 1 to the other until all nodes (characters) are assigned a code
            buildCodes(root.left, code + "0", huffmanCodes);
            buildCodes(root.right, code + "1", huffmanCodes);
        }
    }
    class Node {
        char character;
        int frequency;
        Node left, right;
        public Node(char character, int frequency) {
            this.character = character;
            this.frequency = frequency;
        }
    }
}