import javafx.scene.layout.GridPane;

public class EnemyHelicopter extends Vehicle {

    public EnemyHelicopter(String imagePath) {
        super(imagePath);
    }

    @Override
    public void move(GridPane grid, int[] position) {
        grid.getChildren().remove(vehicleImageView);
        grid.add(vehicleImageView, position[1], position[0]); // Move the vehicle image
        currentPosition = new int[]{position[0], position[1]}; // Update the current position
    }

    @Override
    public String getType() {
        return "EnemyTank";
    }

    @Override
    public boolean canFly() {
        return true;
    }
    
}