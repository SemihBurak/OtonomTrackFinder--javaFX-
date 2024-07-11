
import java.util.ArrayList;
import java.util.List;

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
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends Application {

    private List<Rectangle> shortestPathRectangles = new ArrayList<>();

    private void printShortestPath(List<int[]> path, GridPane grid, ImageView carImageView) {
        if (path.isEmpty()) {
            System.out.println("\nNo paths found.");
            return;
        }
    
        Timeline timeline = new Timeline();
        for (int i = 1; i < path.size(); i++) { // Skip last point
            int[] point = path.get(i);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(i * 100), event -> { // 100 milliseconds per cell
                Rectangle pathRectangle = new Rectangle();
                pathRectangle.setWidth(63);
                pathRectangle.setHeight(63);
                pathRectangle.setFill(Color.rgb(255, 255, 0, 0.5)); // Semi-transparent yellow
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
                    obstacleImageView.setFitHeight(64); // Adjust the size as needed
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
                    groundImageView.setFitHeight(64); // Adjust the size as needed
                    grid.add(groundImageView, j, i);

                    groundImageView.setOnMouseClicked(event -> {
                        int x = GridPane.getColumnIndex(groundImageView);
                        int y = GridPane.getRowIndex(groundImageView);
                    
                        if (isSettingStart.get()) {
                            
                            // Clear previous start and destination points
                                grid.getChildren().removeIf(node -> {
                                    Integer columnIndex = GridPane.getColumnIndex(node);
                                    Integer rowIndex = GridPane.getRowIndex(node);
                                    return columnIndex != null && rowIndex != null && (
                                        (columnIndex == start[1] && rowIndex == start[0]) ||
                                        (columnIndex == dest[1] && rowIndex == dest[0])
                                    ) && node instanceof Rectangle;
                                });
                    
                                // Remove shortest path rectangles and
                                for (Rectangle rectangle : shortestPathRectangles) {
                                    grid.getChildren().remove(rectangle);
                                }
                                shortestPathRectangles.clear();
                            
                    
                            // Set new start point
                            Rectangle startRectangle = new Rectangle();
                            startRectangle.setWidth(64); // Set to a fixed value
                            startRectangle.setHeight(64); // Set to a fixed value
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
                            destRectangle.setWidth(64); // Set to a fixed value
                            destRectangle.setHeight(64); // Set to a fixed value
                            destRectangle.setFill(Color.BLUE); // Destination color
                            grid.add(destRectangle, x, y); // Add the destination rectangle
                    
                            dest[0] = y;
                            dest[1] = x;
                            isSettingStart.set(true);
                        }
                    });
                }
            }
        }

        startButton.setOnAction(event -> {
            map[start[0]][start[1]] = -1;
            map[dest[0]][dest[1]] = 2;
        
            generator.printTrack();

            List<int[]> path = AStarAlgorithm.aStar(map, start[0], start[1], dest[0], dest[1]);
            printShortestPath(path, grid, carImageView);
        
            // Reset the start and destination cells to be empty again
            map[start[0]][start[1]] = 0;
            map[dest[0]][dest[1]] = 0;
        
        });
        

        VBox root = new VBox(startButton, grid);
        VBox.setVgrow(grid, Priority.ALWAYS);

        Scene scene = new Scene(root, 640, 670);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
