import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class PredictiveCoding2D {
    private int[][] imageMatrix;
    private JFrame frame;
    private JTextArea textArea;

    PredictiveCoding2D() {
        frame = new JFrame("2D Predictive Coding Compression and Decompression");
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea();
        frame.add(textArea);

        JButton compressButton = new JButton("Compress");
        compressButton.addActionListener(e -> compressAction());

        JButton decompressButton = new JButton("Decompress");
//        decompressButton.addActionListener(e -> decompressAction());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(compressButton);
        buttonPanel.add(decompressButton);

        frame.add(buttonPanel, "South");
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PredictiveCoding2D());
    }

//    private void decompressAction() {
//        JFileChooser fileChooser = new JFileChooser();
//
//        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
//            File selectedFile = fileChooser.getSelectedFile();
//
//            try {
//                decompress(selectedFile);
//                textArea.append("Image has been decompressed successfully.\n");
//            } catch (IOException e) {
//                e.printStackTrace();
//                textArea.append("Error occurred during decompression.\n");
//            }
//        }
//    }

    private void compressAction() {
        JFileChooser fileChooser = new JFileChooser();

        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try {
                compress(selectedFile);
                textArea.append("Image has been compressed successfully.\n");
            } catch (IOException ex) {
                ex.printStackTrace();
                textArea.append("Error occurred during compression.\n");
            }
        }
    }

    private void compress(File inputFile) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputFile);
        int height = originalImage.getHeight();
        int width = originalImage.getWidth();
        imageMatrix = new int[height][width];
        WritableRaster raster = originalImage.getRaster();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                imageMatrix[i][j] = raster.getSample(j, i, 0); // Get the intensity value of each pixel
            }
        }

        int[][] compressedMatrix = new int[height][width];

        for (int i = 0; i < height; i++) { // Apply the adaptive 2D prediction rules
            for (int j = 0; j < width; j++) {
                int A = (j - 1 >= 0) ? imageMatrix[i][j - 1] : 0;  // Check if j - 1 is a valid index
                int B = (i - 1 >= 0 && j - 1 >= 0) ? imageMatrix[i - 1][j - 1] : 0;  // Check if i - 1 and j - 1 are valid indices
                int C = (i - 1 >= 0) ? compressedMatrix[i - 1][j] : 0;  // Check if i - 1 is a valid index

                if (B <= Math.min(A, C)) {
                    compressedMatrix[i][j] = Math.max(A, C);
                } else if (B >= Math.max(A, C)) {
                    compressedMatrix[i][j] = Math.min(A, C);
                } else {
                    compressedMatrix[i][j] = A + C - B;
                }
            }
        }

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("compressed.bin"))) {
            dos.writeInt(height);
            dos.writeInt(width); // Write the height and width

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    dos.writeByte(compressedMatrix[i][j]); // Write the compressed value for each original pixel
                }
            }
        }
    }
}
