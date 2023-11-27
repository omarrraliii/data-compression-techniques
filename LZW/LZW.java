package assignment.lzw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import static java.lang.System.exit;

/**
 * @author omara
 */
public class LZW {

    public static void compress(String data) throws IOException {
        HashMap<String, Integer> tableMap = new HashMap<>();
        File compressedFile = new File("Compressed.txt");
        compressedFile.createNewFile();
        try (FileWriter myWriter = new FileWriter("Compressed.txt")) {
            int index, found = 0;
            String bufferString = "";
            for (index = 0; index < 128; index++) {                             //creating the table dictionary into table map
                String c;
                c = String.valueOf((char) index);
                tableMap.put(c, index);
            }
            for (int i = 0; i < data.length(); i++) {                           //loop on data that is needed to be compressed
                boolean isFound = false;
                bufferString += data.charAt(i);                            //put characters in buffer to search within the dictionary table
                for (String j : tableMap.keySet()) {
                    if (bufferString.equals(j)) {
                        isFound = true;
                        found = tableMap.get(j);                            // save the index of the longest found sequence
                    }
                }
                if (!isFound) {                                                 //add the not founded sequence to the table and empty the buffer
                    tableMap.put(bufferString, index);
                    index++;
                    i--;
                    bufferString = "";
                    myWriter.write(Integer.toString(found) + " ");            //encode the compressed sequence
                }
            }
            myWriter.write(Integer.toString(found));                      //encode the sequence of the last characters in file
        }
    }

    public static void decompress(String compressedData) throws IOException {
        HashMap<Integer, String> dictionary = new HashMap<>();
        String decompressedData = "";

        // Initialize the dictionary with the basic characters from 0 to 127
        for (int index = 0; index < 128; index++) {
            dictionary.put(index, String.valueOf((char) index));
        }

        Scanner compressedScanner = new Scanner(compressedData);
        int currentCode, previousCode = 0;

        // Initialize the decompressed string with the taking the symbol corresponding to the first code
        if (compressedScanner.hasNextInt()) {
            currentCode = compressedScanner.nextInt();
            decompressedData += (dictionary.get(currentCode));
            previousCode = currentCode;
        }

        while (compressedScanner.hasNextInt()) {
            // If the current code is encoded in the dictionary, take its corresponding symbol
            currentCode = compressedScanner.nextInt();
            String currentString = dictionary.get(currentCode);

            // If the current code is not encoded yet, take the previous symbol + the first character of it
            if (currentString == null) {
                currentString = dictionary.get(previousCode) + dictionary.get(previousCode).charAt(0);
            }

            decompressedData += (currentString);
            dictionary.put(dictionary.size(), dictionary.get(previousCode) + currentString.charAt(0));
            previousCode = currentCode;
        }

        try (FileWriter decompressedFile = new FileWriter("Decompressed.txt")) {
            decompressedFile.write(decompressedData.toString());
        }
    }

    public static void main(String[] args) throws IOException {
        int choice;
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("--------------------------------------\n" +
                    "| LZW Compression and Decompression  |\n" +
                    "--------------------------------------\n" +
                    "| 1. Compress.                       |\n" +
                    "| 2. Decompress.                     |\n" +
                    "| 3. Exit.                           |\n" +
                    "--------------------------------------\n" +
                    "Enter your choice (1/2/3): ");

            choice = scanner.nextInt();

            if (choice == 1) {
                String data = "", filePath = "";
                try {
                    System.out.print("Enter the path of the file to be compressed: ");
                    scanner.nextLine();
                    filePath = scanner.nextLine();
                    File myFile = new File(filePath);
                    try (Scanner dataScanner = new Scanner(myFile)) {
                        while (dataScanner.hasNextLine()) {
                            data = dataScanner.nextLine();
                        }
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Please make sure the file exists.");
                }
                try {
                    compress(data);
                    System.out.println("File is compressed successfully in: Compressed.txt");
                } catch (IOException ex) {
                    System.out.println("Something wrong happened.");
                }

            } else if (choice == 2) {
                String compressedData = "";

                try {
                    System.out.print("Enter the path of the file to be decompressed: ");
                    scanner.nextLine();
                    String filePath = scanner.nextLine();

                    File decompressedFile = new File(filePath);

                    Scanner fileScanner = new Scanner(decompressedFile);
                    while (fileScanner.hasNextLine()) {
                        compressedData = fileScanner.nextLine();
                    }

                    decompress(compressedData);
                    System.out.println("File is decompressed successfully in: Decompressed.txt");

                } catch (FileNotFoundException e) {
                    System.out.println("Please make sure the file exists.");
                }

            } else if (choice == 3) {
                exit(0);

            } else {
                System.out.println("Invalid option.");
            }
        }
    }
}
