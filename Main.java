import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
//import same javafx 
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class Main extends Application{

    public static void dfs(int[][] map, boolean[][] visited, int i, int j, int destX, int destY, List<int[]> path, List<List<int[]>> allPaths) {
        if (i < 0 || i >= 10 || j < 0 || j >= 10 || map[i][j] == 1 || visited[i][j]) {
            return;
        }

        int[] coordinates = new int[]{i,j};
        path.add(coordinates);

        if (i == destX && j == destY) {
            allPaths.add(new ArrayList<>(path));
            path.remove(path.size() - 1); // Remove destination cell before backtracking 
            return;
        } 

        visited[i][j] = true;
        dfs(map, visited, i - 1, j, destX, destY, path, allPaths); // down
        dfs(map, visited, i + 1, j, destX, destY, path, allPaths); // up
        dfs(map, visited, i, j - 1, destX, destY, path, allPaths); // left
        dfs(map, visited, i, j + 1, destX, destY, path, allPaths); // right
        visited[i][j] = false; // unmark the cell as visited that other paths can use it

        path.remove(path.size() - 1); // Remove the current cell because all paths from it have been explored
    }

    public static List<List<int[]>> findPaths(int[][] map, int startX, int startY, int destX, int destY) {
        List<List<int[]>> allPaths = new ArrayList<>();
        List<int[]> path = new ArrayList<>();
        boolean[][] visited = new boolean[10][10];
        dfs(map, visited, startX, startY, destX, destY, path, allPaths);
        return allPaths;
    }

    public static void printPaths(List<List<int[]>> allPaths) {
    System.out.println("\nAll paths: ");
    for (List<int[]> path : allPaths) {
        int time = path.size();
        for (int[] cell : path) {
            System.out.print("[" + cell[1] + ", " + cell[0] + "] ");
            try {
                Thread.sleep(0); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Time: " + time + " seconds");
    }
}


public static void printShortestPath(List<List<int[]>> allPaths) {
    int shortestTime = Integer.MAX_VALUE;
    List<int[]> shortestPath = null;

    for (List<int[]> path : allPaths) {
        int time = path.size();
        if (time < shortestTime) {
            shortestTime = time;
            shortestPath = path;
        }
    }

    if (shortestPath != null) {
        System.out.println("\nShortest path: ");
        for (int[] cell : shortestPath) {
            System.out.print("[" + cell[1] + ", " + cell[0] + "] ");
        }
        System.out.println("Time: " + shortestTime + " seconds");
    } else {
        System.out.println("No path found");
    }
}

    
    
  @Override
public void start(Stage stage) {
    TrackGenerator generator = new TrackGenerator(10, 10);
    generator.printTrack();
    int[][] map = generator.getTrack();

    // Create input fields and button
    TextField startXField = new TextField();
    TextField startYField = new TextField();
    TextField destXField = new TextField();
    TextField destYField = new TextField();
    Button startButton = new Button("Start");

    GridPane grid = new GridPane();
    for (int i = 0; i < 10; i++) {
        for (int j = 0; j < 10; j++) {
            Rectangle rectangle = new Rectangle();
            rectangle.widthProperty().bind(grid.widthProperty().divide(10.2));
            rectangle.heightProperty().bind(grid.heightProperty().divide(10.2));
            rectangle.setStroke(Color.BLACK);
            if (map[i][j] == 1) {
                rectangle.setFill(Color.DARKRED); // Obstacle
            } else {
                rectangle.setFill(Color.WHITE); // Empty cell
            }
            grid.add(rectangle, j, i);
        }
    }

    GridPane.setHgrow(grid, Priority.ALWAYS);
    GridPane.setVgrow(grid, Priority.ALWAYS);

    startButton.setOnAction(event -> {
    int startX = Integer.parseInt(startXField.getText());
    int startY = Integer.parseInt(startYField.getText());
    int destX = Integer.parseInt(destXField.getText());
    int destY = Integer.parseInt(destYField.getText());

    // Check if starting or ending position is on an obstacle
    if (map[startY][startX] == 1 || map[destY][destX] == 1) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Cannot place starting or ending position on an obstacle.");
        alert.showAndWait();
        return;
    }

    map[startY][startX] = -1;
    map[destY][destX] = 2;

    // Update the grid
    for (javafx.scene.Node node : grid.getChildren()) {
        Rectangle rectangle = (Rectangle) node;
        int x = GridPane.getColumnIndex(rectangle);
        int y = GridPane.getRowIndex(rectangle);
        if (map[y][x] == -1) {
            rectangle.setFill(Color.GREEN); // Car
        } else if (map[y][x] == 2) {
            rectangle.setFill(Color.BLUE); // Destination
        }
    }

    generator.printTrack();
    List<List<int[]>> allPaths = findPaths(map, startY, startX, destY, destX);
    printPaths(allPaths);
    System.out.println("Number of total paths:"+allPaths.size());
    printShortestPath(allPaths);
    });

    VBox inputBox = new VBox(startXField, startYField, destXField, destYField, startButton);

    VBox root = new VBox(inputBox, grid);
    Scene scene = new Scene(root, 660, 660);
    stage.setScene(scene);
    stage.show();
}

    public static void main(String[] args){
        launch(args);
    }
}
