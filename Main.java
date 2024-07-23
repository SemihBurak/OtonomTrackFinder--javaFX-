import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    private boolean vehicleTypeChosen = false;

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
                vehicle.setCurrentPosition(point);

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

    // This method sets up the grid with the given map and images for obstacles and ground. It also handles the logic for placing vehicles on the grid.
    // We don't actually need this method. In fact, we can write the contents directly, but I wrote it this way to make the code more readable.
    private void setupGrid(GridPane grid, int[][] map, Image obstacleImage, Image airobstacleImage ,Image groundImage, AtomicBoolean isSettingStart) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (map[i][j] == 1) {
                    ImageView obstacleImageView = new ImageView(obstacleImage);
                    obstacleImageView.setFitWidth(64);
                    obstacleImageView.setFitHeight(64);
                    grid.add(obstacleImageView, j, i);
                } else if (map[i][j] == 3) {
                    ImageView airobtacleImageView = new ImageView(airobstacleImage);
                    airobtacleImageView.setFitWidth(64);
                    airobtacleImageView.setFitHeight(64);
                    grid.add(airobtacleImageView, j, i);
                   
                } else {
                    ImageView groundImageView = new ImageView(groundImage);
                    groundImageView.setFitWidth(64);
                    groundImageView.setFitHeight(64);
                    grid.add(groundImageView, j, i);

                    groundImageView.setOnMouseClicked(mouseEvent -> {
                        if (!vehicleTypeChosen) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Information");
                            alert.setHeaderText(null);
                            alert.setContentText("Please choose a vehicle type first.");
                            alert.showAndWait();
                            return;
                        }

                        int x = GridPane.getColumnIndex(groundImageView);
                        int y = GridPane.getRowIndex(groundImageView);

                        if (isSettingStart.get()) {
                            Vehicle vehicle;
                            if (isHelicopterMode) {
                                vehicle = new Helicopter("file:/Users/semihburakatilgan/Desktop/OTONOMTRACKFINDER/Assets/helicopter.png");
                            } else {
                                vehicle = new Tank("file:/Users/semihburakatilgan/Desktop/OTONOMTRACKFINDER/Assets/TANK.gif");
                            }
                            vehicles.add(vehicle);
                            starts.add(new int[]{y, x});
                            destinations.add(new int[2]);

                            Rectangle startRectangle = new Rectangle();
                            startRectangle.setWidth(63);
                            startRectangle.setHeight(63);
                            startRectangle.setFill(Color.GREEN);
                            grid.add(startRectangle, x, y);

                            ImageView vehicleImageView = vehicle.getVehicleImageView();
                            vehicleImageView.setFitWidth(63);
                            vehicleImageView.setFitHeight(63);
                            grid.add(vehicleImageView, x, y);

                            vehicle.setCurrentPosition(new int[]{y, x});

                            currentVehicleIndex = vehicles.size() - 1;
                            isSettingStart.set(false);
                        } else {
                            Rectangle destRectangle = new Rectangle();
                            destRectangle.setWidth(63);
                            destRectangle.setHeight(63);
                            destRectangle.setFill(Color.BLUE);
                            grid.add(destRectangle, x, y);

                            destinations.get(currentVehicleIndex)[0] = y;
                            destinations.get(currentVehicleIndex)[1] = x;
                            isSettingStart.set(true);
                            vehicleTypeChosen = false;
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
    }
    
    @Override
    public void start(Stage stage) {
        TrackGenerator generator = new TrackGenerator(10, 10);
        generator.printTrack();
        int[][] map = generator.getTrack();

        Button startButton = new Button("Start");
        Button addCarButton = new Button("Add Tank");
        Button addHelicopterButton = new Button("Add Helicopter");
        Button resetButton = new Button("Reset");

        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);

        Label messageLabel = new Label("Choose a vehicle type:");

        AtomicBoolean isSettingStart = new AtomicBoolean(true);

        Image obstacleImage = new Image("file:/Users/semihburakatilgan/Desktop/OTONOMTRACKFINDER/Assets/obstacle.jpeg");
        Image groundImage = new Image("file:/Users/semihburakatilgan/Desktop/OTONOMTRACKFINDER/Assets/ground.png");
        Image airobstacleImage = new Image("file:/Users/semihburakatilgan/Desktop/OTONOMTRACKFINDER/Assets/mountainnew.png");

        setupGrid(grid, map, obstacleImage, airobstacleImage, groundImage, isSettingStart);

        startButton.setOnAction(event -> {
            boolean[][] occupiedCells = new boolean[10][10];
        
            for (int i = 0; i < vehicles.size(); i++) {
                int[] start = starts.get(i);
                int[] dest = destinations.get(i);
        
                map[start[0]][start[1]] = -1;
                map[dest[0]][dest[1]] = 2;
                List<int[]> path = AStarAlgorithm.aStar(map, start[0], start[1], dest[0], dest[1], occupiedCells, vehicles.get(i).canFly());
                vehicles.get(i).setCurrentPosition(new int[]{start[0], start[1]});
                printShortestPath(path, grid, vehicles.get(i), i, occupiedCells, map);
            }
        });

        addCarButton.setOnAction(event -> {
            isHelicopterMode = false;
            vehicleTypeChosen = true;
            messageLabel.setText("Place the tank on the grid.");
        });

        addHelicopterButton.setOnAction(event -> {
            isHelicopterMode = true;
            vehicleTypeChosen = true;
            messageLabel.setText("Place the helicopter on the grid.");
        });

        resetButton.setOnAction(event -> {
            grid.getChildren().clear();
            vehicles.clear();
            starts.clear();
            destinations.clear();
            currentVehicleIndex = 0;
            vehicleTypeChosen = false;
            isSettingStart.set(true);
            messageLabel.setText("Choose a vehicle type:");
            setupGrid(grid, map, obstacleImage,airobstacleImage, groundImage, isSettingStart);
        });

        HBox hbox = new HBox(startButton, addCarButton, addHelicopterButton, resetButton);
        VBox vbox = new VBox(messageLabel, hbox, grid);
        VBox.setVgrow(grid, Priority.ALWAYS);

        Scene scene = new Scene(vbox, 640, 690);
        stage.setScene(scene);
        stage.setTitle("OtonomTrackFinder");
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}