import java.awt.image.BufferedImage;

public class QuadTreeNode {
    private QuadTreeNode upperLeft;
    private QuadTreeNode upperRight;
    private QuadTreeNode lowerLeft;
    private QuadTreeNode lowerRight;
    private double upleftlong;
    private double upleftlat;
    private double lowrightlong;
    private double lowrightlat;
    private String name;
    private double resolution;
    private boolean hasdaughters = false;
    private BufferedImage buffer = null;


    public QuadTreeNode(double upleftlong, double upleftlat,
                        double lowrightlong, double lowrightlat) { // constructor
        this.upleftlong = upleftlong;
        this.upleftlat = upleftlat;
        this.lowrightlong = lowrightlong;
        this.lowrightlat = lowrightlat;
        this.resolution = 0.00034332275390625;
        this.name = "root";
    }

    public QuadTreeNode(double upleftlong, double upleftlat,
                        double lowrightlong,
                        double lowrightlat,
                        double resolution, String name) { // constructor
        this.upleftlong = upleftlong;
        this.upleftlat = upleftlat;
        this.lowrightlong = lowrightlong;
        this.lowrightlat = lowrightlat;
        this.resolution = resolution;
        this.name = name;
    }

    public void split() { // splits a quadtreenode into 4 smaller nodes
        // check if string name is certain length
        this.upperLeftMaker();
        this.upperRightMaker();
        this.lowerLeftMaker();
        this.lowerRightMaker();
        this.hasdaughters = true;
    }

    public void upperLeftMaker() { // creates a new upper left node
        this.upperLeft = new QuadTreeNode(this.upleftlong, this.upleftlat,
                (this.lowrightlong + this.upleftlong) / 2, (this.lowrightlat + this.upleftlat) / 2,
                this.resolution / 2, this.name);
        upperLeft.changeName('1');
    }

    public void upperRightMaker() { // creates a new upper right node
        this.upperRight = new QuadTreeNode((this.lowrightlong + this.upleftlong) / 2,
                this.upleftlat, this.lowrightlong, (this.lowrightlat + this.upleftlat) / 2,
                this.resolution / 2, this.name);
        upperRight.changeName('2');
    }

    public void lowerLeftMaker() { // creates a new lower left node
        this.lowerLeft = new QuadTreeNode(this.upleftlong, (this.upleftlat + this.lowrightlat) / 2,
                (this.lowrightlong + this.upleftlong) / 2, this.lowrightlat,
                this.resolution / 2, this.name);
        lowerLeft.changeName('3');
    }

    public void lowerRightMaker() { // creates a new lower right node
        this.lowerRight = new QuadTreeNode((this.upleftlong + this.lowrightlong) / 2,
                (this.upleftlat + this.lowrightlat) / 2, this.lowrightlong, this.lowrightlat,
                this.resolution / 2, this.name);
        lowerRight.changeName('4');
    }

    public void changeName(char x) { // not sure if this works, needs editingd
        if (this.name.equals("root")) {
            this.name = Character.toString(x);
        } else {
            this.name += x;
        }
    }

    public boolean intersects(double ullon, double ullat, double lrlon, double lrlat) {
        if (this.lowrightlong < ullon) {
            return false;
        }
        if (this.lowrightlat > ullat) {
            return false;
        }
        if (this.upleftlong > lrlon) {
            return false;
        }
        if (this.upleftlat < lrlat) {
            return false;
        }
        return true;
    }

    public double getresolution() {
        return this.resolution;
    }


    public int nameLength() {
        return this.name.length();
    }

    public boolean hasImage() {
        return this.buffer != null;
    }

    public QuadTreeNode getUpperLeft() {
        return this.upperLeft;
    }

    public QuadTreeNode getUpperRight() {
        return this.upperRight;
    }

    public QuadTreeNode getLowerLeft() {
        return this.lowerLeft;
    }

    public QuadTreeNode getLowerRight() {
        return this.lowerRight;
    }

    public double getUpLeftLong() {
        return this.upleftlong;
    }

    public double getUpLeftLat() {
        return this.upleftlat;
    }

    public double getLowRightLong() {
        return this.lowrightlong;
    }

    public double getLowRightLat() {
        return this.lowrightlat;
    }

    public String getName() {
        return this.name;
    }

    public boolean getHasDaughters() {
        return this.hasdaughters;
    }

    public BufferedImage getBuffer() {
        return this.buffer;
    }

    public void setBuffer(BufferedImage e) {
        this.buffer = e;
    }

}
