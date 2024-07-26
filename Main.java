import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends Application {

    private List<Rectangle> shortestPathRectangles = new ArrayList<>();
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<int[]> starts = new ArrayList<>();
    private List<int[]> destinations = new ArrayList<>();
    private int currentVehicleIndex = 0;
    private int tankcount = 0;
    private int helicoptercount = 0;
    private boolean isHelicopterMode = false;
    private boolean vehicleTypeChosen = false;
    private boolean isEnemyTankMode = false;
    private boolean isEnemyHelicopterMode = false;
    private final Semaphore moveSemaphore = new Semaphore(20);

    private void printShortestPath(List<int[]> path, GridPane grid, Vehicle vehicle, int vehicleIndex, boolean[][] occupiedCells, int[][] map, CountDownLatch startLatch) {
        if (path.isEmpty()) {
            System.out.println("\nNo paths found.");
            return;
        }

        AtomicBoolean collisionDetected = new AtomicBoolean(false);

        new Thread(() -> {
            try {
                startLatch.await(); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            for (int i = 1; i < path.size(); i++) {
                int[] point = path.get(i);
                try {
                    moveSemaphore.acquire();  

                    if (collisionDetected.get()) {
                        break;
                    }

                    int[] currentPos = vehicle.getCurrentPosition();

                    Vehicle blockingVehicle = getVehicleAtPosition(point);
                    boolean isEnemy = blockingVehicle != null && (blockingVehicle instanceof EnemyTank || blockingVehicle instanceof EnemyHelicopter);
                    boolean isSameType = blockingVehicle != null && blockingVehicle.getType().equals(vehicle.getType());

                    if (isSameType) {
                        collisionDetected.set(true);
                        vehicle.setCurrentPosition(currentPos);

                        List<int[]> newPath = AStarAlgorithm.aStar(map, currentPos[0], currentPos[1], destinations.get(vehicleIndex)[0], destinations.get(vehicleIndex)[1], occupiedCells, vehicle.canFly());
                        printShortestPath(newPath, grid, vehicle, vehicleIndex, occupiedCells, map, startLatch);
                        System.out.println("Collision detected, recalculating path.");
                        break;
                    }

                    if ((vehicle instanceof Tank || vehicle instanceof Helicopter) && isEnemy) {
                        collisionDetected.set(true);
                        vehicle.setCurrentPosition(currentPos);

                        List<int[]> newPath = AStarAlgorithm.aStar(map, currentPos[0], currentPos[1], destinations.get(vehicleIndex)[0], destinations.get(vehicleIndex)[1], occupiedCells, vehicle.canFly());
                        printShortestPath(newPath, grid, vehicle, vehicleIndex, occupiedCells, map, startLatch);
                        System.out.println("Collision detected, recalculating path.");
                        break;
                    }
                    
                    Platform.runLater(() -> vehicle.move(grid, point));
                    vehicle.setCurrentPosition(point);
                    occupiedCells[currentPos[0]][currentPos[1]] = false;
                    occupiedCells[point[0]][point[1]] = true;

                    Platform.runLater(() -> {
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
                        shortestPathRectangles.add(pathRectangle);
                    });

                    Thread.sleep(400);  

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    moveSemaphore.release();  
                }
            }

            System.out.println("\nShortest path for vehicle " + vehicleIndex + ":");
            for (int[] point : path) {
                System.out.print("[" + point[1] + ", " + point[0] + "] ");
            }
            System.out.println(" Time: " + (path.size()) + " seconds");

        }).start();
    }

    private Vehicle getVehicleAtPosition(int[] position) {
        for (Vehicle vehicle : vehicles) {
            int[] vehiclePosition = vehicle.getCurrentPosition();
            if (vehiclePosition[0] == position[0] && vehiclePosition[1] == position[1]) {
                return vehicle;
            }
        }
        return null;
    }

    private void setupGrid(GridPane grid, int[][] map, Image obstacleImage, Image airobstacleImage, Image waterImage, Image groundImage, AtomicBoolean isSettingStart) {
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                if (map[i][j] == 1) {
                ImageView obstacleImageView = new ImageView(obstacleImage);
                obstacleImageView.setFitWidth(64);
                obstacleImageView.setFitHeight(64);
                grid.add(obstacleImageView, j, i);
                } else if (map[i][j] == 3) {
                ImageView airobstacleImageView = new ImageView(airobstacleImage);
                airobstacleImageView.setFitWidth(64);
                airobstacleImageView.setFitHeight(64);
                grid.add(airobstacleImageView, j, i);
                } else if (map[i][j] == 4) {
                ImageView waterImageView = new ImageView(waterImage);
                waterImageView.setFitWidth(64);
                waterImageView.setFitHeight(64);
                grid.add(waterImageView, j, i);
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
                     
                            switch (helicoptercount) {
                                case 0:
                                    vehicle = new Helicopter("file:Assets/helicopter01.png");
                                    break;
                                case 1:
                                    vehicle = new Helicopter("file:Assets/helicopter02.png");
                                    break;
                                case 2:
                                    vehicle = new Helicopter("file:Assets/helicopter03.png");
                                    break;
                                case 3:
                                    vehicle = new Helicopter("file:Assets/helicopter04.png");
                                    break;
                                default:
                                    vehicle = new Helicopter("file:Assets/helicopter01.png");
                                    break;
                            }
                            helicoptercount++;
                        } else if (isEnemyTankMode) {
                            vehicle = new EnemyTank("file:Assets/enemytank.png");
                        } else if (isEnemyHelicopterMode) {
                            vehicle = new EnemyHelicopter("file:Assets/enemyheli.png");
                        } else {
                      
                            switch (tankcount) {
                                case 0:
                                    vehicle = new Tank("file:Assets/tank01.png");
                                    break;
                                case 1:
                                    vehicle = new Tank("file:Assets/tank02.png");
                                    break;
                                case 2:
                                    vehicle = new Tank("file:Assets/tank03.png");
                                    break;
                                case 3:
                                    vehicle = new Tank("file:Assets/tank04.png");
                                    break;
                                default:
                                    vehicle = new Tank("file:Assets/tank01.png");
                                    break;
                            }
                            tankcount++;
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

                        vehicles.get(currentVehicleIndex).rotateTowards(destinations.get(currentVehicleIndex)); // Rotate the vehicle towards the destination

                        isSettingStart.set(true);
                        vehicleTypeChosen = false;
                        isHelicopterMode = false;
                        isEnemyTankMode = false;
                        isEnemyHelicopterMode = false;
                        currentVehicleIndex++;

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
    TrackGenerator generator = new TrackGenerator(12, 12);
    generator.printTrack();
    int[][] map = generator.getTrack();

    Button startButton = new Button("Start");
    Button addTankButton = new Button("Add Tank");
    Button addEnemyTankButton = new Button("Add Enemy Tank");
    Button addEnemyHelicopterButton = new Button("Add Enemy Helicopter");
    Button addHelicopterButton = new Button("Add Helicopter");
    Button resetButton = new Button("Reset");

    GridPane grid = new GridPane();
    grid.setGridLinesVisible(true);

    Label messageLabel = new Label("Choose a vehicle type:");

    AtomicBoolean isSettingStart = new AtomicBoolean(true);

    Image obstacleImage = new Image("file:Assets/obstacle.jpeg");
    Image groundImage = new Image("file:Assets/ground.png");
    Image airobstacleImage = new Image("file:Assets/mountainnew.png");
    Image waterImage = new Image("file:Assets/water.gif");

    setupGrid(grid, map, obstacleImage, airobstacleImage, waterImage, groundImage, isSettingStart);

    startButton.setOnAction(event -> {
        // Clear existing path rectangles from the grid
        for (Rectangle rect : shortestPathRectangles) {
            grid.getChildren().remove(rect);
        }
        shortestPathRectangles.clear();  // Clear the list of path rectangles

        boolean[][] occupiedCells = new boolean[12][12];  // Reset occupied cells

        CountDownLatch startLatch = new CountDownLatch(1); // Latch to synchronize start

        for (int i = 0; i < vehicles.size(); i++) {
            int[] start = starts.get(i);
            int[] dest = destinations.get(i);

            map[start[0]][start[1]] = -1;  // Mark start point on map
            map[dest[0]][dest[1]] = 2;     // Mark destination point on map

           
            List<int[]> path = AStarAlgorithm.aStar(map, start[0], start[1], dest[0], dest[1], occupiedCells, vehicles.get(i).canFly());
            vehicles.get(i).setCurrentPosition(new int[]{start[0], start[1]});

            // Print and animate the shortest path
            printShortestPath(path, grid, vehicles.get(i), i, occupiedCells, map, startLatch);
        }

        startLatch.countDown();  // Release all vehicles to start moving
    });

    addTankButton.setOnAction(event -> {
        isHelicopterMode = false;
        isEnemyHelicopterMode = false;
        isEnemyTankMode = false;
        vehicleTypeChosen = true;
        messageLabel.setText("Place the tank on the grid.");
    });

    addEnemyTankButton.setOnAction(event -> {
        isEnemyTankMode = true;
        isHelicopterMode = false;
        isEnemyHelicopterMode = false;
        vehicleTypeChosen = true;
        messageLabel.setText("Place the enemy tank on the grid.");
    });

    addEnemyHelicopterButton.setOnAction(event -> {
        isHelicopterMode = false;
        isEnemyTankMode = false;
        isEnemyHelicopterMode = true;
        vehicleTypeChosen = true;
        messageLabel.setText("Place the enemy helicopter on the grid.");
    });

    addHelicopterButton.setOnAction(event -> {
        isHelicopterMode = true;
        isEnemyTankMode = false;
        isEnemyHelicopterMode = false;
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
        setupGrid(grid, map, obstacleImage, airobstacleImage, waterImage, groundImage, isSettingStart);
    });

    HBox hbox = new HBox(startButton, addTankButton, addEnemyTankButton, addHelicopterButton, addEnemyHelicopterButton, resetButton);
    VBox vbox = new VBox(messageLabel, hbox, grid);
    VBox.setVgrow(grid, Priority.ALWAYS);

    Scene scene = new Scene(vbox, 766, 818);
    stage.setScene(scene);
    stage.setTitle("OtonomTrackFinder");
    stage.show();
}

public static void main(String[] args) {
    launch(args);
}
}