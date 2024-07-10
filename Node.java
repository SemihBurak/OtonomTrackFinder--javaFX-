import java.util.*;

public  class Node implements Comparable<Node> {
    int x, y;
    double g, h;
    Node parent;

    public Node(int x, int y, double g, double h, Node parent) {
        this.x = x;
        this.y = y;
        this.g = g;
        this.h = h;
        this.parent = parent;
    }

    @Override
    public int compareTo(Node other) {
        return Double.compare(this.g + this.h, other.g + other.h);
    }
}