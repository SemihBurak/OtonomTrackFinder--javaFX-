// File: Tank.java
import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import java.util.LinkedList;
import java.util.Queue;

public class Tank extends Vehicle {
    private Queue<int[]> positionHistory = new LinkedList<>(); // History of positions

    public Tank(String imagePath) {
        super(imagePath);
    }

    @Override
    public void move(GridPane grid, int[] position) {
        if (positionHistory.size() >= 2) {
            positionHistory.poll(); // Remove the oldest position if we have more than 2
        }
        positionHistory.offer(getCurrentPosition().clone()); // Add the current position to the history

        Platform.runLater(() -> {
            grid.getChildren().remove(vehicleImageView);
            grid.add(vehicleImageView, position[1], position[0]); // Move the vehicle image
            setCurrentPosition(position); // Update the current position

            if (positionHistory.size() >= 2) {
                int[] oldPosition = positionHistory.peek(); // Get the position two moves ago
                double angle = calculateRotationAngle(oldPosition, position); // Calculate rotation angle
                System.out.println("Old position: [" + oldPosition[0] + ", " + oldPosition[1] + "]");
                System.out.println("Current position: [" + position[0] + ", " + position[1] + "]");
                System.out.println("Rotating to angle: " + angle); // Debug statement to check angle
                vehicleImageView.setRotate(angle); // Rotate the image view
            }
        });
    }

    @Override
    public String getType() {
        return "Tank";
    }

    @Override
    public boolean canFly() {
        return false;
    }
}