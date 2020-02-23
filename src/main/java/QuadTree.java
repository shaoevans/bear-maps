import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.HashMap;


public class QuadTree {

    private static HashMap<String, BufferedImage> imageMap = new HashMap<>(16);
    private QuadTreeNode root;
    private ArrayList<QuadTreeNode> rasterList = new ArrayList<QuadTreeNode>();
    private double resolutionRequirement;

    public QuadTree() {
        root = new QuadTreeNode(-122.2998046875, 37.892195547244356,
                -122.2119140625, 37.82280243352756);
    }

    public void setResolution(double resolution) {
        this.resolutionRequirement = resolution;
    }

    public void clearList() {
        rasterList.clear();
    }

    public void raster(QuadTreeNode r, double ullon, double ullat, double lrlon, double lrlat) {
        if (r.intersects(ullon, ullat, lrlon, lrlat)) {
            if (r.getresolution() <= resolutionRequirement) {
                rasterList.add(r);
            } else if (r.nameLength() < 7) {
                if (r.getHasDaughters()) {
                    raster(r.getUpperLeft(), ullon, ullat, lrlon, lrlat);
                    raster(r.getUpperRight(), ullon, ullat, lrlon, lrlat);
                    raster(r.getLowerLeft(), ullon, ullat, lrlon, lrlat);
                    raster(r.getLowerRight(), ullon, ullat, lrlon, lrlat);
                } else {
                    r.split();
                    raster(r.getUpperLeft(), ullon, ullat, lrlon, lrlat);
                    raster(r.getUpperRight(), ullon, ullat, lrlon, lrlat);
                    raster(r.getLowerLeft(), ullon, ullat, lrlon, lrlat);
                    raster(r.getLowerRight(), ullon, ullat, lrlon, lrlat);
                }
            } else {
                rasterList.add(r);
            }
        }
    }

    public int getDepth() {
        QuadTreeNode q = rasterList.get(0);
        if (q.getName().equals("root")) {
            return 0;
        } else {
            return q.nameLength();
        }
    }

    public double getUpLeftLong() {
        return rasterList.get(0).getUpLeftLong();
    }

    public double getUpLeftLat() {
        return rasterList.get(0).getUpLeftLat();
    }

    public double getLowRightLong() {
        return rasterList.get(rasterList.size() - 1).getLowRightLong();
    }

    public double getLowRightLat() {
        return rasterList.get(rasterList.size() - 1).getLowRightLat();
    }

    public QuadTreeNode getRoot() {
        return this.root;
    }

    public ArrayList<QuadTreeNode> getRasterList() {
        return rasterList;
    }


    public class NodeComparator implements Comparator<QuadTreeNode> {
        @Override
        public int compare(QuadTreeNode a, QuadTreeNode b) {
            if (a.getUpLeftLat() == b.getUpLeftLat()) {
                if (a.getUpLeftLong() < b.getUpLeftLong()) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (a.getUpLeftLat() > b.getUpLeftLat()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public void sorter() {
        Comparator<QuadTreeNode> rasterCompare = new NodeComparator();
        Collections.sort(rasterList, rasterCompare);
    }

    public int findWidth(int counter, int widthCounter) {
        widthCounter += 256;
        if (rasterList.get(counter).getUpLeftLat() != rasterList.get(counter + 1).getUpLeftLat()
                || rasterList.get(counter + 1) == null) {
            return widthCounter;
        }
        return findWidth(counter + 1, widthCounter);
    }

    public int findHeight(double width) {
        double tileNumber = width / 256;
        return (int) (rasterList.size() / tileNumber) * 256;

    }


    public BufferedImage imageCombiner() {
        int width = this.findWidth(0, 0);
        BufferedImage result = new BufferedImage(width,
                findHeight(width), BufferedImage.TYPE_INT_RGB);
        Graphics g = result.getGraphics();
        int x = 0;
        int y = 0;
        BufferedImage bi;
        for (QuadTreeNode image : rasterList) {
            try {
                if (!image.hasImage()) {
                    bi = ImageIO.read(new File("img/" + image.getName() + ".png"));
                    image.setBuffer(bi);
                } else {
                    bi = image.getBuffer();
                }
            } catch (IOException e) {
                bi = null;
            }
            g.drawImage(bi, x, y, null);
            x += 256;
            if (x >= result.getWidth()) {
                x = 0;
                y += 256;
            }
        }
        /* try {
            ImageIO.write(result, "png", new File("negro.png"));
        } catch (IOException e) {
            return null;
        } */
        return result;
    }
    /*
    public static void main(String[] args) {
        QuadTree newTree = new QuadTree();
        newTree.setResolution((122.241632 - 122.24053) / 892);
        // QuadTreeNode q = new QuadTreeNode(-122.241632, 37.87655, -122.24053, 37.87548);
        QuadTreeNode q = new QuadTreeNode(-122.241632, 37.87655, -122.24053, 37.87548);
        newTree.raster(newTree.root, q);
        ArrayList<QuadTreeNode> list = newTree.getRasterList();
        newTree.sorter();
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).getName());
        }
        newTree.imageCombiner();
    }
    */
}
