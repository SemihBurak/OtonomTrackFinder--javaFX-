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
import javafx.scene.layout.HBox;
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
    private boolean isHelicopterMode = false;

    private void printShortestPath(List<int[]> path, GridPane grid, Vehicle vehicle, int vehicleIndex, boolean[][] occupiedCells, int[][] map) {
        if (path.isEmpty()) {
            System.out.println("\nNo paths found.");
            return;
        }

        Timeline timeline = new Timeline();
        AtomicBoolean collisionDetected = new AtomicBoolean(false);

        for (int i = 1; i < path.size(); i++) {
            int[] point = path.get(i);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(i * 500), event -> {
                if (collisionDetected.get()) {
                    return;
                }

                int[] currentPos = vehicle.getCurrentPosition();
                if (!vehicle.canFly() && occupiedCells[point[0]][point[1]]) {
                    collisionDetected.set(true);

                    // Update the current position before recalculating the path
                    vehicle.setCurrentPosition(currentPos);

                    List<int[]> newPath = AStarAlgorithm.aStar(map, currentPos[0], currentPos[1], destinations.get(vehicleIndex)[0], destinations.get(vehicleIndex)[1], occupiedCells, vehicle.canFly());
                    printShortestPath(newPath, grid, vehicle, vehicleIndex, occupiedCells, map);
                    System.out.println("Collision detected at: [" + point[1] + ", " + point[0] + "]");
                    return;
                }

                Rectangle pathRectangle = new Rectangle();
                pathRectangle.setWidth(63);
                pathRectangle.setHeight(63);
                if (vehicleIndex == 0) {
                    pathRectangle.setFill(Color.rgb(250, 250, 0, 0.4));
                } else if (vehicleIndex == 1) {
                    pathRectangle.setFill(Color.rgb(250, 0, 0, 0.4));
                } else {
                    pathRectangle.setFill(Color.rgb(50, 50, 50, 0.4));
                }
                pathRectangle.setMouseTransparent(true);
                grid.add(pathRectangle, point[1], point[0]);

                vehicle.move(grid, point);
                vehicle.setCurrentPosition(point);  // Update the current position of the vehicle

                if (!vehicle.canFly()) {
                    occupiedCells[currentPos[0]][currentPos[1]] = false;
                    occupiedCells[point[0]][point[1]] = true;
                }
                shortestPathRectangles.add(pathRectangle);
            });
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.play();

        System.out.println("\nShortest path for vehicle " + vehicleIndex + ":");
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
        Button addHelicopterButton = new Button("Add Helicopter");

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
                    obstacleImageView.setFitWidth(64);
                    obstacleImageView.setFitHeight(64);
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
                    groundImageView.setFitWidth(64);
                    groundImageView.setFitHeight(64);
                    grid.add(groundImageView, j, i);
    
                    groundImageView.setOnMouseClicked(event -> {
                        int x = GridPane.getColumnIndex(groundImageView);
                        int y = GridPane.getRowIndex(groundImageView);
    
                        if (isSettingStart.get()) {
                            if (isHelicopterMode) {
                                Vehicle helicopter = new Helicopter("file:/Users/semihburakatilgan/Desktop/OTONOMTRACKFINDER/Assets/helicopter.png");
                                vehicles.add(helicopter);
                                starts.add(new int[]{y, x});
                                destinations.add(new int[2]);
                    
                                // Set new start point for helicopter
                                Rectangle startRectangle = new Rectangle();
                                startRectangle.setWidth(63);
                                startRectangle.setHeight(63);
                                startRectangle.setFill(Color.GREEN);
                                grid.add(startRectangle, x, y);
                    
                                // Add helicopter image to the grid
                                ImageView helicopterImageView = helicopter.getVehicleImageView();
                                helicopterImageView.setFitWidth(63);
                                helicopterImageView.setFitHeight(63);
                                grid.add(helicopterImageView, x, y);
                    
                                helicopter.setCurrentPosition(new int[]{y, x});
                    
                                currentVehicleIndex = vehicles.size() - 1;
                                isSettingStart.set(false);
                            } else {
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
                                startRectangle.setWidth(63);
                                startRectangle.setHeight(63);
                                startRectangle.setFill(Color.GREEN);
                                grid.add(startRectangle, x, y);
    
                                grid.getChildren().remove(vehicles.get(currentVehicleIndex).getVehicleImageView());
                                grid.add(vehicles.get(currentVehicleIndex).getVehicleImageView(), x, y);
                                starts.get(currentVehicleIndex)[0] = y;
                                starts.get(currentVehicleIndex)[1] = x;
    
                                isSettingStart.set(false);
                            }
                        } else {
                            // Set new destination point
                            Rectangle destRectangle = new Rectangle();
                            destRectangle.setWidth(63);
                            destRectangle.setHeight(63);
                            destRectangle.setFill(Color.BLUE);
                            grid.add(destRectangle, x, y);
    
                            destinations.get(currentVehicleIndex)[0] = y;
                            destinations.get(currentVehicleIndex)[1] = x;
                            isSettingStart.set(true);
    
                            currentVehicleIndex++;
                            isHelicopterMode = false;
    
                            if (currentVehicleIndex >= vehicles.size()) {
                                currentVehicleIndex = 0;
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
                List<int[]> path = AStarAlgorithm.aStar(map, start[0], start[1], dest[0], dest[1], occupiedCells, vehicles.get(i).canFly());
                vehicles.get(i).setCurrentPosition(new int[]{start[0], start[1]}); // Set initial position
                printShortestPath(path, grid, vehicles.get(i), i, occupiedCells, map);
            }
        });

        addHelicopterButton.setOnAction(event -> {
            isHelicopterMode = true;
        });
    
        HBox hbox = new HBox(startButton, addHelicopterButton);
VBox vbox = new VBox(hbox, grid);
VBox.setVgrow(grid, Priority.ALWAYS);
    
        Scene scene = new Scene(vbox, 640, 670);
        stage.setScene(scene);
        stage.setTitle("OtonomTrackFinder");
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}