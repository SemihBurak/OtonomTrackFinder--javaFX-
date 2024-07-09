import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

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
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

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


private void printShortestPath(List<List<int[]>> allPaths, GridPane grid, int[][] map) {
    List<int[]> shortestPath = allPaths.get(0);
    for (List<int[]> path : allPaths) {
        if (path.size() < shortestPath.size()) {
            shortestPath = path;
        }
    }

    Timeline timeline = new Timeline();
    for (int i = 1; i < shortestPath.size() - 1; i++) { // Skip first and last point
        int[] point = shortestPath.get(i);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(i * 1000), event -> { // 100 milliseconds per cell
            for (javafx.scene.Node node : grid.getChildren()) {
                Rectangle rectangle = (Rectangle) node;
                int x = GridPane.getColumnIndex(rectangle);
                int y = GridPane.getRowIndex(rectangle);
                if (y == point[0] && x == point[1]) {
                    rectangle.setFill(Color.YELLOW);
                }
            }
        });
        timeline.getKeyFrames().add(keyFrame);
    }
    timeline.play();

    System.out.println("\nShortest path:");
    for (int[] point : shortestPath) {
        System.out.print("[" + point[1] + ", " + point[0] + "] ");
        try {
            Thread.sleep(0); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    System.out.println(" Time: " + (shortestPath.size()) + " seconds");
}

    
    
 @Override
public void start(Stage stage) {
    TrackGenerator generator = new TrackGenerator(10, 10);
    generator.printTrack();
    int[][] map = generator.getTrack();

    Button startButton = new Button("Start");

    GridPane grid = new GridPane();
    int[] start = new int[2];
    int[] dest = new int[2];
    AtomicBoolean isSettingStart = new AtomicBoolean(true);

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
            rectangle.setOnMouseClicked(event -> {
                int x = GridPane.getColumnIndex(rectangle);
                int y = GridPane.getRowIndex(rectangle);
                if (map[y][x] == 1) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Dialog");
                    alert.setHeaderText(null);
                    alert.setContentText("Cannot place starting or ending position on an obstacle.");
                    alert.showAndWait();
                } else if (isSettingStart.get()) {
                    rectangle.setFill(Color.GREEN); // Car
                    start[0] = y;
                    start[1] = x;
                    isSettingStart.set(false);
                } else {
                    rectangle.setFill(Color.BLUE); // Destination
                    dest[0] = y;
                    dest[1] = x;
                    isSettingStart.set(true);
                }
            });
            grid.add(rectangle, j, i);
        }
    }

    GridPane.setHgrow(grid, Priority.ALWAYS);
    GridPane.setVgrow(grid, Priority.ALWAYS);

    startButton.setOnAction(event -> {
        map[start[0]][start[1]] = -1;
        map[dest[0]][dest[1]] = 2;

        generator.printTrack();
        List<List<int[]>> allPaths = findPaths(map, start[0], start[1], dest[0], dest[1]);
        printPaths(allPaths);
        System.out.println("\nNumber of total paths:"+allPaths.size());
        printShortestPath(allPaths, grid, map);
    });

    VBox root = new VBox(startButton, grid);
    Scene scene = new Scene(root, 540, 540);
    stage.setScene(scene);
    stage.show();
}

    public static void main(String[] args){
        launch(args);
    }
}
