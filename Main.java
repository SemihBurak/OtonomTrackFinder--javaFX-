import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
//import same javafx 
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class Main extends Application{

    public static void dfs(int[][] map, boolean[][] visited, int i, int j, int destX, int destY, List<int[]> path, List<List<int[]>> allPaths) {
        if (i < 0 || i >= 10 || j < 0 || j >= 10 || map[i][j] == 1 || visited[i][j]) {
            return;
        }

        int[] coordinates = new int[]{i,j};
        path.add(coordinates);

        if (i == destX && j == destY) {
            allPaths.add(new ArrayList<>(path));
            path.remove(path.size() - 1); // Remove destination after adding path to avoid duplicates
            return;
        } 

        visited[i][j] = true;
        dfs(map, visited, i - 1, j, destX, destY, path, allPaths); // up
        dfs(map, visited, i + 1, j, destX, destY, path, allPaths); // down
        dfs(map, visited, i, j - 1, destX, destY, path, allPaths); // left
        dfs(map, visited, i, j + 1, destX, destY, path, allPaths); // right
        visited[i][j] = false;

        path.remove(path.size() - 1); // Remove the current cell before backtracking
    }

    public static List<List<int[]>> findPaths(int[][] map, int startX, int startY, int destX, int destY) {
        List<List<int[]>> allPaths = new ArrayList<>();
        List<int[]> path = new ArrayList<>();
        boolean[][] visited = new boolean[10][10];
        dfs(map, visited, startX, startY, destX, destY, path, allPaths);
        return allPaths;
    }

    public static void printPaths(List<List<int[]>> allPaths) {

    System.out.println("\nAll paths: ");
    for (List<int[]> path : allPaths) {
        // Calculate the time for the path
        int time = path.size();
        for (int[] cell : path) {
            System.out.print(Arrays.toString(cell) + " ");
            try {
                Thread.sleep(0); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Time: " + time + " seconds");
    }
}
public static void printShortestPath(List<List<int[]>> allPaths) {
    int shortestTime = Integer.MAX_VALUE;
    List<int[]> shortestPath = null;

    for (List<int[]> path : allPaths) {
        int time = path.size();
        if (time < shortestTime) {
            shortestTime = time;
            shortestPath = path;
        }
    }

    if (shortestPath != null) {
        System.out.println("\nShortest path: ");
        for (int[] cell : shortestPath) {
            System.out.print(Arrays.toString(cell) + " ");
        }
        System.out.println("Time: " + shortestTime + " seconds");
    } else {
        System.out.println("No path found");
    }
}

    @Override
    public void start(Stage stage) throws Exception {
        stage.show();
    }

    public static void main(String[] args){
        launch(args);
        Scanner sc = new Scanner(System.in);
        TrackGenerator generator = new TrackGenerator(10, 10);
        generator.printTrack();
        int[][] map = generator.getTrack();

       
        System.out.println("Enter the car coordinates on map (type row first then column): ");
        int carY = sc.nextInt();
        int carX = sc.nextInt();
        
        while (carY < 0 || carY >= 10 || carX < 0 || carX >= 10 || map[carY][carX] == 1) {
            System.out.println("The entered coordinates are either an obstacle or out of bounds. Please enter different coordinates: ");
            carY = sc.nextInt();
            carX = sc.nextInt();
        }

        map[carY][carX] = -1;

        System.out.println("Enter the destination coordinates on map (type row first then column): ");
        int desY = sc.nextInt();
        int desX = sc.nextInt();

        while (desY<0 || desY >=10 || desX<0 || desX>=10 || map[desY][desX] == 1){
            System.out.println("The entered coordinates are either an obstacle or out of bounds. Please enter different coordinates: ");
            desY = sc.nextInt();
            desX = sc.nextInt();
        }

        map[desY][desX] = 2;
        generator.printTrack();


        List<List<int[]>> allPaths = findPaths(map, carY, carX, desY, desX);
        //printPaths(allPaths);
        printShortestPath(allPaths);
        sc.close();
    }
}
