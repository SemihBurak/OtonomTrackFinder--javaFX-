import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

public abstract class Vehicle {
    protected ImageView vehicleImageView;
    protected int[] currentPosition;

    public Vehicle(String imagePath) {
        Image vehicleImage = new Image(imagePath);
        vehicleImageView = new ImageView(vehicleImage);
        vehicleImageView.setFitWidth(50); 
        vehicleImageView.setFitHeight(50); 
        currentPosition = new int[2];
    }

    public ImageView getVehicleImageView() {
        return vehicleImageView;
    }

    public int[] getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int[] position) {
        this.currentPosition = position;
    }

    public abstract String getType();
    
    public abstract void move(GridPane grid, int[] position);

    public abstract boolean canFly();
}