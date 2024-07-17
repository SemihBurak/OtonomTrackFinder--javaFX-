import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<int[]> starts = new ArrayList<>();
    private List<int[]> destinations = new ArrayList<>();
    private int currentVehicleIndex = 0;

    private void printShortestPath(List<int[]> path, GridPane grid, Vehicle vehicle, int vehicleIndex, boolean[][] occupiedCells, int[][] map) {
        if (path.isEmpty()) {
            System.out.println("\nNo paths found.");
            return;
        }
    
        Timeline timeline = new Timeline();
        for (int i = 1; i < path.size(); i++) { // Skip last point
            int[] point = path.get(i);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(i * 500), event -> { // 500 milliseconds per cell
                int[] currentPos = vehicle.getCurrentPosition();
                if (occupiedCells[point[0]][point[1]]) {
                    System.out.println("Collision detected at: [" + point[1] + ", " + point[0] + "]");
                    List<int[]> newPath = AStarAlgorithm.aStar(map, currentPos[0], currentPos[1], destinations.get(vehicleIndex)[0], destinations.get(vehicleIndex)[1], occupiedCells);
                    printShortestPath(newPath, grid, vehicle, vehicleIndex, occupiedCells, map);
                    return;
                }
    
                Rectangle pathRectangle = new Rectangle();
                pathRectangle.setWidth(63);
                pathRectangle.setHeight(63);
                pathRectangle.setFill(vehicleIndex == 1 ? Color.RED : Color.rgb(255, 255, 0, 0.5));
                pathRectangle.setMouseTransparent(true); // Make the rectangle non-interactive
                grid.add(pathRectangle, point[1], point[0]); // Add the path rectangle
                vehicle.move(grid, point); // Move the vehicle image
    
                occupiedCells[currentPos[0]][currentPos[1]] = false; // Free previous cell
                occupiedCells[point[0]][point[1]] = true; // Mark cell as occupied
                shortestPathRectangles.add(pathRectangle); // Add the path rectangle to the list
            });
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.play();
    
        System.out.println("\nShortest path for vehicle " + vehicleIndex + ":");
        for (int[] point : path) {
            System.out.print("[" + point[1] + ", " + point[0] + "] ");
        }
        System.out.println(" Time: " + (path.size() * 0.5) + " seconds"); // Adjusted to 0.5 seconds per step
    }
    
    @Override
    public void start(Stage stage) {
        TrackGenerator generator = new TrackGenerator(10, 10);
        generator.printTrack();
        int[][] map = generator.getTrack();

        Button startButton = new Button("Start");

        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);

        AtomicBoolean isSettingStart = new AtomicBoolean(true);

        Image obstacleImage = new Image("file:/Users/semihburakatilgan/Desktop/OTONOMTRACKFINDER/Assets/obstacle.jpeg");
        Image groundImage = new Image("file:/Users/semihburakatilgan/Desktop/OTONOMTRACKFINDER/Assets/ground.png");
        
        Vehicle car1 = new Car("file:/Users/semihburakatilgan/Desktop/OTONOMTRACKFINDER/Assets/pngegg.png");
        Vehicle car2 = new Car("file:/Users/semihburakatilgan/Desktop/OTONOMTRACKFINDER/Assets/car_red.png");
        vehicles.add(car1);
        vehicles.add(car2);

        for (int i = 0; i < vehicles.size(); i++) {
            starts.add(new int[2]);
            destinations.add(new int[2]);
        }

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
                                    (columnIndex == starts.get(currentVehicleIndex)[1] && rowIndex == starts.get(currentVehicleIndex)[0]) ||
                                    (columnIndex == destinations.get(currentVehicleIndex)[1] && rowIndex == destinations.get(currentVehicleIndex)[0])
                                ) && node instanceof Rectangle;
                            });

                            // Remove shortest path rectangles
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

                            grid.getChildren().remove(vehicles.get(currentVehicleIndex).getVehicleImageView());
                            grid.add(vehicles.get(currentVehicleIndex).getVehicleImageView(), x, y); // Add the car image
                            starts.get(currentVehicleIndex)[0] = y;
                            starts.get(currentVehicleIndex)[1] = x;

                            isSettingStart.set(false);
                        } else {
                            // Set new destination point
                            Rectangle destRectangle = new Rectangle();
                            destRectangle.setWidth(64); // Set to a fixed value
                            destRectangle.setHeight(64); // Set to a fixed value
                            destRectangle.setFill(Color.BLUE); // Destination color
                            grid.add(destRectangle, x, y); // Add the destination rectangle

                            destinations.get(currentVehicleIndex)[0] = y;
                            destinations.get(currentVehicleIndex)[1] = x;
                            isSettingStart.set(true);

                            currentVehicleIndex++;
                            
                            if (currentVehicleIndex >= vehicles.size()) {
                                currentVehicleIndex = 0; // Reset index
                            }
                        }
                    });
                }
            }
        }

        startButton.setOnAction(event -> {
            boolean[][] occupiedCells = new boolean[10][10];
        
            for (int i = 0; i < vehicles.size(); i++) {
                int[] start = starts.get(i);
                int[] dest = destinations.get(i);
        
                map[start[0]][start[1]] = -1;
                map[dest[0]][dest[1]] = 2;
        
                generator.printTrack();
        
                List<int[]> path = AStarAlgorithm.aStar(map, start[0], start[1], dest[0], dest[1], occupiedCells);
                printShortestPath(path, grid, vehicles.get(i), i, occupiedCells, map);
        
                // Reset the start and destination cells to be empty again
                map[start[0]][start[1]] = 0;
                map[dest[0]][dest[1]] = 0;
            }
       
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