import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

public abstract class Vehicle {
    protected ImageView vehicleImageView;

    public Vehicle(String imagePath) {
        Image vehicleImage = new Image(imagePath);
        vehicleImageView = new ImageView(vehicleImage);
        vehicleImageView.setFitWidth(50); // Adjust the size as needed
        vehicleImageView.setFitHeight(50); // Adjust the size as needed
    }

    public ImageView getVehicleImageView() {
        return vehicleImageView;
    }

    public abstract void move(GridPane grid, int[] position);

    public boolean isAtPosition(int x, int y) {
        Integer col = GridPane.getColumnIndex(vehicleImageView);
        Integer row = GridPane.getRowIndex(vehicleImageView);
        return col != null && row != null && col == y && row == x; }
    
    }