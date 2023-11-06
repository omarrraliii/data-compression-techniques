/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

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
    
    public static void compress (String data) throws IOException{
        HashMap<String,Integer> tableMap=new HashMap<>();
        File compressedFile=new File("Compressed.txt");
        compressedFile.createNewFile();
        try (FileWriter myWriter = new FileWriter("Compressed.txt")) {
            int index,found = 0;
            String bufferString="";
            for (index=0;index<128;index++){                                                //creating the table dictionary into table map
                String c;
                c=String.valueOf((char)index);
                tableMap.put(c, index);
            }   for (int i=0;i<data.length();i++){
                boolean isFound=false;
                bufferString+=data.charAt(i);
                for(String j : tableMap.keySet()){
                    if (bufferString.equals(j)){
                        isFound=true;
                        found=tableMap.get(j);
                    }
                }
                if (!isFound){
                    tableMap.put(bufferString, index);
                    index++;
                    i--;
                    bufferString="";
                    myWriter.write(Integer.toString(found)+" ");
                }
            }   myWriter.write(Integer.toString(found));
        }
    }

    public static void main(String[] args) {
        String data="";
        try {
            File myFile = new File ("read.txt");
            try (Scanner dataScanner = new Scanner(myFile)) {
                while (dataScanner.hasNextLine()) {
                    data = dataScanner.nextLine();                               
                }
                System.out.println(data);
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
