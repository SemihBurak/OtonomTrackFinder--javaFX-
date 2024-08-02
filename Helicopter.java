import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import java.util.LinkedList;
import java.util.Queue;

public class Helicopter extends Vehicle {
    private Queue<int[]> positionHistory = new LinkedList<>();

    public Helicopter(String imagePath) {
        super(imagePath);
    }

    @Override
    public void move(GridPane grid, int[] position) {
        if (positionHistory.size() >= 2) {
            positionHistory.poll();
        }
        positionHistory.offer(getCurrentPosition().clone());

        Platform.runLater(() -> {
            grid.getChildren().remove(vehicleImageView);
            grid.add(vehicleImageView, position[1], position[0]);
            setCurrentPosition(position);

            if (positionHistory.size() >= 2) {
                int[] oldPosition = positionHistory.peek();
                double angle = calculateRotationAngle(oldPosition, position);
                vehicleImageView.setRotate(angle);
            }
        });
    }

    @Override
    public String getType() {
        return "Helicopter";
    }

    @Override
    public boolean isEnemy() {
        return false;
    }

    @Override
    public boolean canFly() {
        return true;
    }
}