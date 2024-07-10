import java.util.concurrent.atomic.AtomicBoolean;

//import same javafx 
import java.util.*;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Main extends Application {

    private List<Rectangle> shortestPathRectangles = new ArrayList<>();

    static class Node implements Comparable<Node> {
        int x, y;
        double g, h;
        Node parent;

        public Node(int x, int y, double g, double h, Node parent) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.h = h;
            this.parent = parent;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.g + this.h, other.g + other.h);
        }
    }

    public static List<int[]> aStar(int[][] map, int startX, int startY, int destX, int destY) {
        PriorityQueue<Node> openList = new PriorityQueue<>();
        boolean[][] closedList = new boolean[10][10];

        Node startNode = new Node(startX, startY, 0, Math.abs(destX - startX) + Math.abs(destY - startY), null);
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node current = openList.poll();

            if (current.x == destX && current.y == destY) {
                List<int[]> path = new ArrayList<>();
                while (current != null) {
                    path.add(new int[]{current.x, current.y});
                    current = current.parent;
                }
                Collections.reverse(path);
                return path;
            }

            closedList[current.x][current.y] = true;

            int[][] directions = {
                { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 },
                { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }
            };

            for (int[] direction : directions) {
                int newX = current.x + direction[0];
                int newY = current.y + direction[1];
                if (newX >= 0 && newX < 10 && newY >= 0 && newY < 10 && map[newX][newY] != 1 && !closedList[newX][newY]) {
                    double g = current.g;
                    if (direction[0] != 0 && direction[1] != 0) {
                        g += Math.sqrt(2) + 0.001; // Diagonal movement cost with penalty
                    } else {
                        g += 1; // Horizontal/vertical movement cost
                    }
                    int dx = Math.abs(destX - newX);
                    int dy = Math.abs(destY - newY);
                    int h = (int) (Math.sqrt(2) * Math.min(dx, dy) + Math.max(dx, dy) - Math.min(dx, dy));
                    Node neighbor = new Node(newX, newY, g, h, current);
                    openList.add(neighbor);
                }
            }
        }

        return new ArrayList<>(); // No path found
    }

    private void printShortestPath(List<int[]> path, GridPane grid, ImageView carImageView) {
        if (path.isEmpty()) {
            System.out.println("\nNo paths found.");
            return;
        }
    
        Timeline timeline = new Timeline();
        for (int i = 1; i < path.size(); i++) { // Skip first and last point
            int[] point = path.get(i);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(i * 100), event -> { // 100 milliseconds per cell
                Rectangle pathRectangle = new Rectangle();
                pathRectangle.widthProperty().bind(grid.widthProperty().divide(10.2));
                pathRectangle.heightProperty().bind(grid.heightProperty().divide(10.2));
                pathRectangle.setFill(Color.YELLOW); // Path color
                pathRectangle.setMouseTransparent(true); // Make the rectangle non-interactive
                grid.add(pathRectangle, point[1], point[0]); // Add the path rectangle
                grid.getChildren().remove(carImageView);
                grid.add(carImageView, point[1], point[0]); // Move the car image
    
                shortestPathRectangles.add(pathRectangle); // Add the path rectangle to the list
            });
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.play();
    
        System.out.println("\nShortest path:");
        for (int[] point : path) {
            System.out.print("[" + point[1] + ", " + point[0] + "] ");
        }
        System.out.println(" Time: " + (path.size()) + " seconds");
    }
    
    @Override
    public void start(Stage stage) {
        TrackGenerator generator = new TrackGenerator(10, 10);
        generator.printTrack();
        int[][] map = generator.getTrack();

        Button startButton = new Button("Start");

        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);
 
        int[] start = new int[2];
        int[] dest = new int[2];

        AtomicBoolean isSettingStart = new AtomicBoolean(true);

        Image obstacleImage = new Image("file:/Users/semihburakatilgan/Desktop/obstacle.jpeg");
        Image groundImage = new Image("file:/Users/semihburakatilgan/Desktop/ground.png");
        Image carImage = new Image("file:/Users/semihburakatilgan/Desktop/pngegg.png");
        ImageView carImageView = new ImageView(carImage);
        carImageView.setFitWidth(50); // Adjust the size as needed
        carImageView.setFitHeight(50); // Adjust the size as needed

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (map[i][j] == 1) {
                    ImageView obstacleImageView = new ImageView(obstacleImage);
                    obstacleImageView.setFitWidth(64); // Adjust the size as needed
                    obstacleImageView.setFitHeight(61); // Adjust the size as needed
                    grid.add(obstacleImageView, j, i);

                    obstacleImageView.setOnMouseClicked(event -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("You cannot set the start or destination on an obstacle.");
                        alert.showAndWait();
                    });
                    
                } else {
                    ImageView groundImageView = new ImageView(groundImage);
                    groundImageView.setFitWidth(64); // Adjust the size as needed
                    groundImageView.setFitHeight(61); // Adjust the size as needed
                    
                    groundImageView.setOnMouseClicked(event -> {
                        int x = GridPane.getColumnIndex(groundImageView);
                        int y = GridPane.getRowIndex(groundImageView);
                    
                        if (isSettingStart.get()) {
                            
                            // Clear previous start and destination points
                            if (start[0] != 0 || start[1] != 0 || dest[0] != 0 || dest[1] != 0) {
                                
                                grid.getChildren().removeIf(node -> {
                                    Integer columnIndex = GridPane.getColumnIndex(node);
                                    Integer rowIndex = GridPane.getRowIndex(node);
                                    return columnIndex != null && rowIndex != null && (
                                        (columnIndex == start[1] && rowIndex == start[0]) ||
                                        (columnIndex == dest[1] && rowIndex == dest[0])
                                    ) && node instanceof Rectangle;
                                });
                    
                                // Reset map values to ensure cells are clickable again
                                map[start[0]][start[1]] = 0;
                                map[dest[0]][dest[1]] = 0;
                    
                                // Remove shortest path rectangles and
                                for (Rectangle rectangle : shortestPathRectangles) {
                                    grid.getChildren().remove(rectangle);
                                }
                                shortestPathRectangles.clear();
                            }
                    
                            // Set new start point
                            Rectangle startRectangle = new Rectangle();
                            startRectangle.widthProperty().bind(grid.widthProperty().divide(10));
                            startRectangle.heightProperty().bind(grid.heightProperty().divide(10));
                            startRectangle.setFill(Color.GREEN); // Start color
                            grid.add(startRectangle, x, y); // Add the start rectangle
                    
                            grid.getChildren().remove(carImageView);
                            grid.add(carImageView, x, y); // Add the car image
                            start[0] = y;
                            start[1] = x;
                            isSettingStart.set(false);
                        } else {
                            // Set new destination point
                            Rectangle destRectangle = new Rectangle();
                            destRectangle.widthProperty().bind(grid.widthProperty().divide(10));
                            destRectangle.heightProperty().bind(grid.heightProperty().divide(10));
                            destRectangle.setFill(Color.BLUE); // Destination color
                            grid.add(destRectangle, x, y); // Add the destination rectangle
                    
                            dest[0] = y;
                            dest[1] = x;
                            isSettingStart.set(true);
                        }
                    });
                    
                    grid.add(groundImageView, j, i);
                }
            }
        }

        startButton.setOnAction(event -> {
            map[start[0]][start[1]] = -1;
            map[dest[0]][dest[1]] = 2;
        
            generator.printTrack();
            List<int[]> path = aStar(map, start[0], start[1], dest[0], dest[1]);
            printShortestPath(path, grid, carImageView);
        
            // Reset the start and destination cells to be empty again
            map[start[0]][start[1]] = 0;
            map[dest[0]][dest[1]] = 0;
        
            // Clear shortest path rectangles
            for (Rectangle rectangle : shortestPathRectangles) {
                grid.getChildren().remove(rectangle);
            }
            shortestPathRectangles.clear();
        });
        

        VBox root = new VBox(startButton, grid);
        VBox.setVgrow(grid, Priority.ALWAYS); 

        Scene scene = new Scene(root, 640, 640);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
