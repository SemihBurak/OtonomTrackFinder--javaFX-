import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import java.util.LinkedList;
import java.util.Queue;

public class EnemyTank extends Vehicle {
    private Queue<int[]> positionHistory = new LinkedList<>();

    public EnemyTank(String imagePath) {
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
        return "EnemyTank";
    }

    @Override
    public boolean isEnemy() {
        return true;
    }

    @Override
    public boolean canFly() {
        return false;
    }
}