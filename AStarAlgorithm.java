import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class AStarAlgorithm {

    public static List<int[]> aStar(int[][] map, int startX, int startY, int destX, int destY,
            boolean[][] occupiedCells, boolean canFly, String vehicleType) {
        PriorityQueue<Node> openList = new PriorityQueue<>();
        boolean[][] closedList = new boolean[12][23];

        Node startNode = new Node(startX, startY, 0, Math.abs(destX - startX) + Math.abs(destY - startY), null);
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node current = openList.poll();

            if (current.x == destX && current.y == destY) {
                List<int[]> path = new ArrayList<>();
                while (current != null) {
                    path.add(new int[] { current.x, current.y });
                    current = current.parent;
                }
                Collections.reverse(path);
                return path;
            }

            closedList[current.x][current.y] = true;

            int[][] directions = {
                    { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 },
                    { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }
            };

            for (int[] direction : directions) {
                int newX = current.x + direction[0];
                int newY = current.y + direction[1];
                if (newX >= 0 && newX < 12 && newY >= 0 && newY < 23 && (!occupiedCells[newX][newY])
                        && (map[newX][newY] != 3) && (canFly || map[newX][newY] != 1)
                        && (canFly || map[newX][newY] != 5) && (canFly || map[newX][newY] != 6)
                        && (canFly || map[newX][newY] != 4) && !closedList[newX][newY]
                        && !isNearTower(newX, newY, vehicleType, map)) {
                    double g = current.g;
                    if (direction[0] != 0 && direction[1] != 0) {
                        g += Math.sqrt(2); // Diagonal movement cost
                    } else {
                        g += 1; // Horizontal/vertical movement cost
                    }
                    int dx = Math.abs(destX - newX);
                    int dy = Math.abs(destY - newY);
                    int h = (int) (Math.sqrt(2) * Math.min(dx, dy) + Math.max(dx, dy) - Math.min(dx, dy));
                    Node neighbor = new Node(newX, newY, g, h, current);
                    openList.add(neighbor);
                }
            }
        }

        return new ArrayList<>(); // No path found
    }

    private static boolean isNearTower(int x, int y, String vehicleType, int[][] map) {
        int rows = map.length;
        int cols = map[0].length;

        for (int i = Math.max(0, x - 1); i <= Math.min(rows - 1, x + 1); i++) {
            for (int j = Math.max(0, y - 1); j <= Math.min(cols - 1, y + 1); j++) {
                if (map[i][j] == 6 && (vehicleType == "Tank" || vehicleType == "Helicopter")) { // Enemy obstacle
                    return true;
                } else if (map[i][j] == 5 && (vehicleType == "EnemyTank" || vehicleType == "EnemyHelicopter")) {
                    return true;
                }
            }
        }

        return false;
    }
}