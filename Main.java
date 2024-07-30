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


    private void printShortestPath(List<int[]> path, GridPane grid, Vehicle vehicle, int vehicleIndex, boolean[][] occupiedCells, int[][]map, CountDownLatch startLatch) {
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
                    System.out.println("Vehicle " + vehicleIndex + " current position: [" + currentPos[1] + ", " + currentPos[0] + "]");
    
                    synchronized (occupiedCells) {
                        // Check if the target cell is occupied before moving
                        if (occupiedCells[point[0]][point[1]]) {
                            collisionDetected.set(true);
                            vehicle.setCurrentPosition(currentPos);
    
                            // Recalculate path if the target cell is occupied
                            List<int[]> newPath = AStarAlgorithm.aStar(map, currentPos[0], currentPos[1], destinations.get(vehicleIndex)[0], destinations.get(vehicleIndex)[1], occupiedCells, vehicle.canFly(), vehicle.getType());
                            printShortestPath(newPath, grid, vehicle, vehicleIndex, occupiedCells, map, startLatch);
                            System.out.println("Collision detected at [" + point[1] + ", " + point[0] + "], recalculating path for vehicle " + vehicleIndex + ".");
                            break;
                        }
    
                        // Mark cells as occupied
                        occupiedCells[currentPos[0]][currentPos[1]] = false;
                        occupiedCells[point[0]][point[1]] = true;
                    }
    
                    // Move vehicle to new position
                    Platform.runLater(() -> vehicle.move(grid, point));
                    vehicle.setCurrentPosition(point);
    
                    Platform.runLater(() -> {
                        Rectangle pathRectangle = new Rectangle();
                        pathRectangle.setWidth(63);
                        pathRectangle.setHeight(63);
                        switch (vehicleIndex) {
                            case 0 -> pathRectangle.setFill(Color.rgb(250, 250, 0, 0.4));
                            case 1 -> pathRectangle.setFill(Color.rgb(0, 0, 250, 0.4));
                            case 2 -> pathRectangle.setFill(Color.rgb(0, 250, 250, 0.4));
                            case 3 -> pathRectangle.setFill(Color.rgb(250, 0, 0, 0.4));
                            case 4 -> pathRectangle.setFill(Color.rgb(100, 100, 100, 0.4));
                            default -> pathRectangle.setFill(Color.rgb(200, 150, 100, 0.4));
                        }
                        pathRectangle.setMouseTransparent(true);
                        grid.add(pathRectangle, point[1], point[0]);
                        shortestPathRectangles.add(pathRectangle);
                    });
    
                    Thread.sleep(250);
    
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

    private void setupGrid(GridPane grid, int[][] map, Image obstacleImage, Image airobstacleImage, Image waterImage, Image groundImage,Image FriendlyTower, Image EnemyTower, AtomicBoolean isSettingStart) {
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 23; j++) {
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
                } 
                else if (map[i][j] == 5) {
                ImageView FrindlyTowerImageView = new ImageView(FriendlyTower);
                FrindlyTowerImageView.setFitWidth(64);
                FrindlyTowerImageView.setFitHeight(64);
                grid.add(FrindlyTowerImageView, j, i);
                } 
                else if (map[i][j] == 6) {
                ImageView EnemyTowerImageView = new ImageView(EnemyTower);
                EnemyTowerImageView.setFitWidth(64);
                EnemyTowerImageView.setFitHeight(64);
                grid.add(EnemyTowerImageView, j, i);

                }
                else {
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

                        vehicles.get(currentVehicleIndex).rotateTowards(destinations.get(currentVehicleIndex)); // Rotate the vehicle towards the destination at first.

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
    TrackGenerator generator = new TrackGenerator(12, 23);
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
    Image FriendlyTower = new Image("file:Assets/friendlytower.png");
    Image EnemyTower = new Image("file:Assets/enemytower.png");

    setupGrid(grid, map, obstacleImage, airobstacleImage, waterImage, groundImage,FriendlyTower,EnemyTower,isSettingStart);

    startButton.setOnAction(event -> {
        
        for (Rectangle rect : shortestPathRectangles) {
            grid.getChildren().remove(rect);
        }
        shortestPathRectangles.clear();  

        boolean[][] occupiedCells = new boolean[13][23];  

        CountDownLatch startLatch = new CountDownLatch(1);

        for (int i = 0; i < vehicles.size(); i++) {
            int[] start = starts.get(i);
            int[] dest = destinations.get(i);

            map[start[0]][start[1]] = -1; 
            map[dest[0]][dest[1]] = 2;     

           
            List<int[]> path = AStarAlgorithm.aStar(map, start[0], start[1], dest[0], dest[1], occupiedCells, vehicles.get(i).canFly(), vehicles.get(i).getType());
            vehicles.get(i).setCurrentPosition(new int[]{start[0], start[1]});

            
            printShortestPath(path, grid, vehicles.get(i), i, occupiedCells, map, startLatch);
        }

        startLatch.countDown();  
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
        tankcount = 0;
        helicoptercount = 0;
        vehicleTypeChosen = false;
        isSettingStart.set(true);
        messageLabel.setText("Choose a vehicle type:");
        setupGrid(grid, map, obstacleImage, airobstacleImage, waterImage, groundImage, FriendlyTower,EnemyTower,isSettingStart);
    });

    HBox hbox = new HBox(startButton, addTankButton, addEnemyTankButton, addHelicopterButton, addEnemyHelicopterButton, resetButton);
    VBox vbox = new VBox(messageLabel, hbox, grid);
    VBox.setVgrow(grid, Priority.ALWAYS);

    Scene scene = new Scene(vbox, 1472, 818);
    stage.setScene(scene);
    stage.setTitle("OtonomTrackFinder");
    stage.show();
}

public static void main(String[] args) {
    launch(args);
}
}