import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.*;

public class VectorQuantization {
    private static int imageHeight;
    private static int imageWidth;
    private JFrame frame;
    private JTextArea textArea;

    VectorQuantization() {
        // Create the main frame
        frame = new JFrame("Vector Quantization Compression and Decompression");
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
            }
        });

        // Create a button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(compressButton);
        buttonPanel.add(decompressButton);

        // Add the button panel to the bottom of the frame
        frame.add(buttonPanel, "South");
        frame.setVisible(true);
    }

    private void compressAction() {
        // Create a file chooser dialog to allow the user to select a file and store the user's selection
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(frame);

        // Check if the user selected a file (clicked "Open" and did not close the window)
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile(); // Get the selected file to be decompressed

            try {
                Compress(2, 2, 4, selectedFile.getAbsolutePath());
                textArea.append("Image has been compressed successfully.\n");
            } catch (IOException ex) {
                ex.printStackTrace();
                textArea.append("Error occurred during compression.\n");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new VectorQuantization();
            }
        });
    }

    private static int[][] readImage(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        imageHeight = image.getHeight();
        imageWidth = image.getWidth();
        int[][] imagePixels = new int[imageHeight][imageWidth]; // To store the intensity values of each pixel in the image

        for (int i = 0; i < imageWidth; i++) {
            for (int j = 0; j < imageHeight; j++) {
                /* Get the intensity value of the current pixel
                   'getRaster()' returns the image pixel data (raster)
                   'getSample()' gets the intensity of that pixel
                   The third argument specifies which color channel to retrieve
                   It's 0 because a grayscale image has only one channel at index 0 */
                imagePixels[j][i] = image.getRaster().getSample(i, j, 0);
            }
        }

        return imagePixels;
    }

    // Generate the representative vectors based on the average values of the original image vectors
    private static Vector<Integer> calculateAverageVector(Vector<Vector<Integer>> Vectors) {
        int[] sums = new int[Vectors.get(0).size()];
        Vector<Integer> average = new Vector<>();

        for (Vector<Integer> vector : Vectors) {
            for (int i = 0; i < vector.size(); i++) {
                sums[i] += vector.get(i); // Calculate the sum of each element of each vector
            }
        }

        for (int sum : sums) {
            average.add(sum / Vectors.size()); // Calculate the average of each element by dividing the sum by the number of vectors
        }

        return average;
    }

    // Calculate the Euclidean distance between two vectors to select the nearest vector in the codebook
    private static int getEuclideanDistance(Vector<Integer> vector1, Vector<Integer> vector2, int incrementFactor) {
        int sumOfSquaredDifferences = 0;

        for (int i = 0; i < vector1.size(); i++) {
            int squaredDifference = (vector1.get(i) - vector2.get(i) + incrementFactor) * (vector1.get(i) - vector2.get(i));
            sumOfSquaredDifferences += squaredDifference;
        }

        return (int) Math.sqrt(sumOfSquaredDifferences);
    }

    // Recursively split and average the vectors to form the codebook
    private static void quantizeVectors(int level, Vector<Vector<Integer>> vectors, Vector<Vector<Integer>> codebook) {
        if (level == 1 || vectors.isEmpty()) {
            if (!vectors.isEmpty()) { // If the vector level is 1
                codebook.add(calculateAverageVector(vectors));
            }

            return; // If there are no vectors
        }

        // Split vectors into left and right
        Vector<Vector<Integer>> leftVectors = new Vector<>();
        Vector<Vector<Integer>> rightVectors = new Vector<>();
        Vector<Integer> averageVector = calculateAverageVector(vectors); // Calculate the average vector for the current set of vectors

        // Calculate Euclidean Distance for each vector to either add it to the leftVectors or the rightVectors
        for (Vector<Integer> vector : vectors) {
            int euclideanDistance1 = getEuclideanDistance(vector, averageVector, 1); // Consider the vector with a slight decrease to be sufficient to distinguish between the two effectively
            int euclideanDistance2 = getEuclideanDistance(vector, averageVector, -1); // Also consider the vector with a slight increase

            if (euclideanDistance1 >= euclideanDistance2) {
                leftVectors.add(vector);
            } else {
                rightVectors.add(vector);
            }
        }

        // Keep splitting and averaging the leftVectors and rightVectors with the reduced Level
        quantizeVectors(level / 2, leftVectors, codebook);
        quantizeVectors(level / 2, rightVectors, codebook);
    }

    private static Vector<Integer> mapVectorsToLabels(Vector<Vector<Integer>> vectors, Vector<Vector<Integer>> codebook) {
        Vector<Integer> labels = new Vector<>(); // To store the label of the closest quantized vector in the codebook for each input vector

        for (Vector<Integer> vector : vectors) {
            int smallestDistance = getEuclideanDistance(vector, codebook.get(0), 0);
            int smallestLabel = 0;

            // Calculate the distance between the current vector and the i-th vector in the codebook
            for (int i = 1; i < codebook.size(); i++) {
                int tempDistance = getEuclideanDistance(vector, codebook.get(i), 0);

                if (tempDistance < smallestDistance) {
                    smallestDistance = tempDistance;
                    smallestLabel = i; //  If the distance is smaller update smallestIndex to the index of the i-th vector in the codebook
                }
            }

            labels.add(smallestLabel);
        }

        return labels;
    }

    private void Compress(int vectorHeight, int vectorWidth, int codeBookSize, String Path) throws IOException {
        int[][] image = readImage(Path);
        int originalHeight = imageHeight;
        int originalWidth = imageWidth;

        // Calculate new dimensions ensuring they are multiples of vectorHeight and vectorWidth and create the new image
        int newHeight = originalHeight % vectorHeight == 0 ? originalHeight : ((originalHeight / vectorHeight) + 1) * vectorHeight;
        int newWidth = originalWidth % vectorWidth == 0 ? originalWidth : ((originalWidth / vectorWidth) + 1) * vectorWidth;
        int[][] newImage = new int[newHeight][newWidth];

        // Copy pixels from the original image to the new image, applying padding if needed
        for (int i = 0; i < newHeight; i++) {
            int x = i >= originalHeight ? originalHeight - 1 : i;

            for (int j = 0; j < newWidth; j++) {
                int y = j >= originalWidth ? originalWidth - 1 : j;
                newImage[i][j] = image[x][y];
            }
        }

        Vector<Vector<Integer>> vectors = new Vector<>(); // Divide the image into vectors

        for (int i = 0; i < newHeight; i += vectorHeight) {
            for (int j = 0; j < newWidth; j += vectorWidth) {
                vectors.add(new Vector<>());

                for (int x = i; x < i + vectorHeight; x++) {
                    for (int y = j; y < j + vectorWidth; y++) {
                        vectors.lastElement().add(newImage[x][y]);
                    }
                }
            }
        }

        Vector<Vector<Integer>> codebook = new Vector<>();

        // Fill the codebook using after quantizing the vectors
        quantizeVectors(codeBookSize, vectors, codebook);

        // Save the mapped label of each vector
        Vector<Integer> mappedVectorsToLabels = mapVectorsToLabels(vectors, codebook);

        // Create the compressed file and write the needed data into it
        FileOutputStream fileOutputStream = new FileOutputStream(Path.substring(0, Path.lastIndexOf('.')) + ".bin");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(originalWidth);
        objectOutputStream.writeObject(originalHeight);
        objectOutputStream.writeObject(newWidth);
        objectOutputStream.writeObject(newHeight);
        objectOutputStream.writeObject(vectorWidth);
        objectOutputStream.writeObject(vectorHeight);
        objectOutputStream.writeObject(mappedVectorsToLabels);
        objectOutputStream.writeObject(codebook);
        objectOutputStream.close();
    }
}
