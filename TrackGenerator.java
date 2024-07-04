import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Here is a Java class that generates an N x M array with random obstructions. In this array,
    0s represent clear path cells and 1s represent obstructions
 */


public class TrackGenerator {

    private int[][] track;
    private int rows;
    private int cols;

    public TrackGenerator(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.track = new int[rows][cols];
        generateTrack();
    }

    private void generateTrack() {
    Random rand = new Random();
    int onesCount = 25 + rand.nextInt(6); // Random number between 25 and 30
    List<Integer> positions = new ArrayList<>();
    
    // Initialize positions
    for (int i = 0; i < rows * cols; i++) {
        positions.add(i);
    }
    
    // Shuffle positions
    Collections.shuffle(positions);
    
    // Set onesCount positions to 1
    for (int i = 0; i < onesCount; i++) {
        int pos = positions.get(i);
        track[pos / cols][pos % cols] = 1;
    }
}

    public int[][] getTrack() {
        return track;
    }

    public void printTrack() {
        // ANSI escape code for red text
        final String ANSI_RED = "\u001B[31m";
        // ANSI escape code for green text
        final String ANSI_GREEN = "\u001B[32m";
        // ANSI escape code to reset to default text color
        final String ANSI_RESET = "\u001B[0m";
    
        // Print column headers
        System.out.print("  ");
        for (int j = 0; j < cols; j++) {
            System.out.print(j + " ");
        }
        System.out.println();
    
        // Print rows with row headers
        for (int i = 0; i < rows; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < cols; j++) {
                if (track[i][j] == 1) {
                    System.out.print(ANSI_RED + track[i][j] + ANSI_RESET + " ");
                } else if (track[i][j] == 0) {
                    System.out.print(ANSI_GREEN + track[i][j] + ANSI_RESET + " ");
                } else {
                    System.out.print(track[i][j] + " ");
                }
            }
            System.out.println();
        }
    }

   
}

