package assignment.lzw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author omara
 */
public class LZW {

    public static void compress(String data) throws IOException {
        HashMap<String, Integer> tableMap = new HashMap<>();
        File compressedFile = new File("Compressed.txt");
        compressedFile.createNewFile();
        try ( FileWriter myWriter = new FileWriter("Compressed.txt")) {
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

    public static void main(String[] args) {
        String data = "";
        try {
            File myFile = new File("read.txt");
            try ( Scanner dataScanner = new Scanner(myFile)) {
                while (dataScanner.hasNextLine()) {
                    data = dataScanner.nextLine();
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found ");
        }
        try {
            compress(data);
        } catch (IOException ex) {
            System.out.println("something wrong happend ");
        }
    }
}
