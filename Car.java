import javafx.scene.layout.GridPane;

public class Car extends Vehicle {
    public Car(String imagePath) {
        super(imagePath);
    }

    @Override
    public void move(GridPane grid, int[] position) {
        grid.getChildren().remove(vehicleImageView);
        grid.add(vehicleImageView, position[1], position[0]); // Move the vehicle image
    }
}