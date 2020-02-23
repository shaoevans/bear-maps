
import java.util.*;



public class GraphNode {

    double longitude;
    double latitude;
    String id;
    private GraphNode predecessor;
    private double distanceFromStart;
    private ArrayList<GraphNode> adjacents;
    String locationName;


    public GraphNode(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        adjacents = new ArrayList<>();
    }

    public GraphNode(double longitude, double latitude, String id) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.id = id;
        adjacents = new ArrayList<>();
    }

    public void addAdjacent(GraphNode g) {
        adjacents.add(g);
        g.getAdjacents().add(this);

    }

    public double findDistance(GraphNode g) {
        return Math.sqrt(Math.pow(g.getLatitude() - this.latitude, 2)
                + Math.pow(g.getLongitude() - this.longitude, 2));
    }

    public boolean nodeEquals(GraphNode g) {
        return ((this.id.equals(g.id))
                && (this.latitude == g.latitude)
                && (this.longitude == g.longitude));
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setDistanceFromStart(double distance, GraphNode predecessor) {
        this.predecessor = predecessor;
        this.distanceFromStart = distance;
    }

    public double getDistanceFromStart() {
        return this.distanceFromStart;
    }

    public ArrayList<GraphNode> getAdjacents() {
        return this.adjacents;
    }

    public GraphNode getPredecessor() {
        return this.predecessor;
    }

    public String getId() {
        return this.id;
    }

    public void removePredecessor() {
        this.predecessor = null;
        this.distanceFromStart = 1000000000;
    }

}


