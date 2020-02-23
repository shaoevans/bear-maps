import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.*;
import java.awt.image.BufferedImage;
import java.awt.*;



/**
 * Wraps the parsing functionality of the MapDBHandler as an example.
 * You may choose to add to the functionality of this class if you wish.
 * @author Alan Yao
 */
public class GraphDB {

    private HashMap<String, GraphNode> graphNodes;
    private Trie locations;
    private ArrayList<ArrayList<GraphNode>> myWays = new ArrayList<>();
    private HashMap<String, GraphNode> finalNodes;
    private ArrayList<GraphNode> currentPredecessors = new ArrayList<>();

    /**
     * Example constructor shows how to create and start an XML parser.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        graphNodes = new HashMap<>();
        locations = new Trie();
        finalNodes = new HashMap<>();
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            MapDBHandler maphandler = new MapDBHandler(this);
            saxParser.parse(inputFile, maphandler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        addConnections();
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        for (GraphNode value : graphNodes.values()) {
            if (!value.getAdjacents().isEmpty()) {
                finalNodes.put(value.getId(), value);
            }
        }
    }

    public void addWay(ArrayList<GraphNode> g) {
        myWays.add(g);
    }

    public GraphNode addGraphNode(double longitude, double latitude, String id) {
        graphNodes.put(id, new GraphNode(longitude, latitude, id));
        return graphNodes.get(id);
    }

    public HashMap<String, GraphNode>  getGraphNodes() {
        return this.graphNodes;
    }

    public Trie getTrie() {
       return this.locations;
    }

    public GraphNode findClosestVertex(double longitude, double latitude) {
        GraphNode min = null;
        GraphNode temp = new GraphNode(longitude, latitude);
        double minDistance = 1000000000;
        for (GraphNode value : finalNodes.values()) {
            double distance = value.findDistance(temp);
            if (distance < minDistance) {
                min = value;
                minDistance = distance;
            }
        }
        return min;
    }


    private void addConnections() {
        for (int i = 0; i < myWays.size(); i++) {
            for (int j = 0; j < myWays.get(i).size(); j++) {
                if (j + 1 != myWays.get(i).size()) {
                    myWays.get(i).get(j).addAdjacent(myWays.get(i).get(j + 1));
                }
            }
        }
    }

    private void removePredecessors() {
        for (GraphNode value : currentPredecessors) {
            value.removePredecessor();
        }
    }

    public void drawLine(Map<String, Object> rasterImageParams,
                         BufferedImage im, ArrayList<Long> nodeIds) {
        Graphics2D myGraphic = (Graphics2D) im.getGraphics();
        myGraphic.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        myGraphic.setColor(new Color(108, 181, 230, 200));
        int width = (int) rasterImageParams.get("raster_width");
        double lrlon = (double) rasterImageParams.get("raster_lr_lon");
        double ullon = (double) rasterImageParams.get("raster_ul_lon");
        double lrlat = (double) rasterImageParams.get("raster_lr_lat");
        double ullat = (double) rasterImageParams.get("raster_ul_lat");
        int height = (int) rasterImageParams.get("raster_height");
        double dpplong = (lrlon - ullon) / width;
        double dpplat = (ullat - lrlat) / height;
        for (int i = 0; i < nodeIds.size() - 1; i++) {
            double node1long = finalNodes.get(Long.toString(nodeIds.get(i))).getLongitude();
            double node1lat = finalNodes.get(Long.toString(nodeIds.get(i))).getLatitude();
            double node2long = finalNodes.get(Long.toString(nodeIds.get(i + 1))).getLongitude();
            double node2lat = finalNodes.get(Long.toString(nodeIds.get(i + 1))).getLatitude();
            int x1 = (int) ((node1long - ullon) / dpplong);
            int x2 = (int) ((node2long - ullon) / dpplong);
            int y1 = (int) (-(node1lat - ullat) / dpplat);
            int y2 = (int) (-(node2lat - ullat) / dpplat);
            myGraphic.drawLine(x1, y1, x2, y2);
        }
    }


    public HashMap<String, GraphNode> getFinalNodes() {
        return this.finalNodes;
    }



    public ArrayList<Long> shortestPath(GraphNode startVertex, GraphNode endVertex) {
        GraphNode current;
        this.removePredecessors();
        PriorityQueue<GraphNode> fringe = new PriorityQueue<GraphNode>
                (finalNodes.size(), new Comparator<GraphNode>() {
                public int compare(GraphNode o1, GraphNode o2) {
                    double heuristic1 = o1.getDistanceFromStart() + o1.findDistance(endVertex);
                    double heuristic2 = o2.getDistanceFromStart() + o2.findDistance(endVertex);
                    if (heuristic1 < heuristic2) {
                        return -1;
                    } else if (heuristic1 > heuristic2) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
        HashSet<GraphNode> visited = new HashSet<>();
        fringe.add(startVertex);
        while (fringe.size() != 0) {
            GraphNode temp = fringe.poll();
            if (temp.nodeEquals(endVertex)) {
                break;
            } else if (visited.contains(temp)) {
                continue;
            } else {
                visited.add(temp);
            }
            for (int i = 0; i < temp.getAdjacents().size(); i++) {
                current = temp.getAdjacents().get(i);
                if (visited.contains(current)) {
                    continue;
                }
                if (!fringe.contains(current)) {
                    current.setDistanceFromStart(temp.getDistanceFromStart()
                            + temp.findDistance(current), temp);
                    fringe.add(current);
                    currentPredecessors.add(current);
                } else if (temp.getDistanceFromStart() + temp.findDistance(current)
                        < current.getDistanceFromStart()) {
                    current.setDistanceFromStart(temp.getDistanceFromStart()
                            + temp.findDistance(current), temp);
                    currentPredecessors.add(current);
                }
            }
        }
        ArrayList<Long> path = new ArrayList<>();
        GraphNode counter = endVertex;
        while (!counter.nodeEquals(startVertex) && counter.getPredecessor() != null) {
            path.add(Long.parseLong(counter.getId()));
            counter = counter.getPredecessor();
        }
        path.add(Long.parseLong(counter.getId()));
        Collections.reverse(path);
        return path;
    }
}
