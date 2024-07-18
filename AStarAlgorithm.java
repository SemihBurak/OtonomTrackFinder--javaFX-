import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class AStarAlgorithm {

    public static List<int[]> aStar(int[][] map, int startX, int startY, int destX, int destY, boolean[][] occupiedCells, boolean canFly) {
        PriorityQueue<Node> openList = new PriorityQueue<>();
        boolean[][] closedList = new boolean[10][10];

        Node startNode = new Node(startX, startY, 0, Math.abs(destX - startX) + Math.abs(destY - startY), null);
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node current = openList.poll();

            if (current.x == destX && current.y == destY) {
                List<int[]> path = new ArrayList<>();
                while (current != null) {
                    path.add(new int[]{current.x, current.y});
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
                if (newX >= 0 && newX < 10 && newY >= 0 && newY < 10 &&
                    (!occupiedCells[newX][newY] || canFly) &&
                    (canFly || map[newX][newY] != 1) && !closedList[newX][newY]) {
                    double g = current.g;
                    if (direction[0] != 0 && direction[1] != 0) {
                        g += Math.sqrt(2) + 0.001; // Diagonal movement cost with penalty
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
}