import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
        int totalCells = rows * cols;
        int numObstructions = (int) (totalCells * 0.04);
        int airobstacles = (int) (totalCells * 0.03);
        int waterobstacles = (int) (totalCells * 0.02);
        int friendlytower = (int) (totalCells * 0.02);
        int enemytower = (int) (totalCells * 0.02);

        List<Integer> cells = new ArrayList<>(totalCells);

        for (int i = 0; i < numObstructions; i++) {
            cells.add(1);
        }

        for (int i = 0; i < airobstacles; i++) {
            cells.add(3);
        }

        for (int i = 0; i < friendlytower; i++) {
            cells.add(6);
        }

        for (int i = 0; i < enemytower; i++) {
            cells.add(5);
        }

        for (int i = numObstructions + airobstacles + friendlytower + enemytower; i < totalCells - waterobstacles; i++) {
            cells.add(0);
        }

        Collections.shuffle(cells, new Random());

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!cells.isEmpty()) {
                    track[i][j] = cells.remove(0);
                } else {
                    track[i][j] = 0;
                }
            }
        }

        Random random = new Random();
        for (int i = 0; i < waterobstacles; i++) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            if (track[row][col] == 0) {
                spreadWater(row, col, random.nextInt(3) + 1);
            }
        }
    }

    private void spreadWater(int row, int col, int remaining) {
        if (row < 0 || row >= rows || col < 0 || col >= cols || track[row][col] != 0 || remaining <= 0) {
            return;
        }

        track[row][col] = 4;
        remaining--;

        spreadWater(row - 1, col, remaining);
        spreadWater(row, col + 1, remaining);
        spreadWater(row + 1, col, remaining);
        spreadWater(row, col - 1, remaining);
    }

    public int[][] getTrack() {
        return track;
    }

    public void printTrack() {
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_GREEN = "\u001B[32m";
        final String ANSI_BLUE = "\u001B[34m";
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_PINK = "\u001B[35m";
        final String ANSI_YELLOW = "\u001B[33m";

        System.out.print("  ");
        for (int j = 0; j < cols; j++) {
            System.out.print(j + " ");
        }
        System.out.println();

        for (int i = 0; i < rows; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < cols; j++) {
                switch (track[i][j]) {
                    case 1 -> System.out.print(ANSI_RED + track[i][j] + ANSI_RESET + " ");
                    case 0 -> System.out.print(ANSI_GREEN + track[i][j] + ANSI_RESET + " ");
                    case 4 -> System.out.print(ANSI_BLUE + track[i][j] + ANSI_RESET + " ");
                    case 5 -> System.out.print(ANSI_YELLOW + track[i][j] + ANSI_RESET + " ");
                    case 6 -> System.out.print(ANSI_PINK + track[i][j] + ANSI_RESET + " ");
                    default -> System.out.print(track[i][j] + " ");
                }
            }
            System.out.println();
        }
    }

    public void generateNewTrack() {
        generateTrack();
    }
}
