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

    protected double calculateRotationAngle(int[] from, int[] to) {
        int dx = to[1] - from[1];
        int dy = to[0] - from[0];

        if (dx == 0 && dy < 0)
            return 0;
        if (dx > 0 && dy < 0)
            return 45;
        if (dx > 0 && dy == 0)
            return 90;
        if (dx > 0 && dy > 0)
            return 135;
        if (dx == 0 && dy > 0)
            return 180;
        if (dx < 0 && dy > 0)
            return 225;
        if (dx < 0 && dy == 0)
            return 270;
        if (dx < 0 && dy < 0)
            return 315;

        return 0;
    }

    public void rotateTowards(int[] targetPosition) {
        double angle = calculateRotationAngle(currentPosition, targetPosition);
        vehicleImageView.setRotate(angle);
    }

    public abstract String getType();

    public abstract boolean isEnemy();

    public abstract void move(GridPane grid, int[] position);

    public abstract boolean canFly();
}