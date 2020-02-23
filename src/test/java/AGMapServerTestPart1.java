import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AGMapServerTestPart1 {
    static List<TestParams> params;
    static final double doubleThreshhold = 0.0000000000001;
    static final double SSD_THRESHHOLD = 2.5;
    static int passedRaster = 0;
    static int passedRoute = 0;
    static boolean initialized = false;

    /**
     * Initializes the student MapServer statically.
     * Reads in the serialized <code>List</code> of TestParams.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        if (initialized) return;
        MapServer.initialize();
        FileInputStream fis = new FileInputStream("test_data");
        ObjectInputStream ois = new ObjectInputStream(fis);
        params = (List<TestParams>) ois.readObject();
        ois.close();
        initialized = true;
    }

    private double computePixelSSD(int rgb1, int rgb2) {
        double ssd = 0;
        for (int i = 0; i < 4; i++) {
            int shifted1 = rgb1 >> i*8 & 255;
            int shifted2 = rgb2 >> i*8 & 255;
            ssd += Math.pow(shifted1 - shifted2, 2);
        }
        return ssd;
    }

    private double computeSSD(BufferedImage im1, BufferedImage im2) {
        assertEquals("Incorrect image dimensions.", im1.getWidth(), im2.getWidth());
        assertEquals("Incorrect image dimensions.", im1.getHeight(), im2.getHeight());
        double ssd = 0;
        for (int i = 0; i < im1.getWidth(); i++) {
            for (int j = 0; j < im2.getHeight(); j++) {
                ssd += computePixelSSD(im1.getRGB(i, j), im2.getRGB(i, j));
            }
        }
        return ssd;
    }

    private void assertImageArrayEquals(String msg, byte[] imarr1, byte[] imarr2)
            throws IOException {
        assertImageArrayEquals(msg, imarr1, imarr2, SSD_THRESHHOLD);
    }

    private void assertImageArrayEquals(String msg, byte[] imarr1, byte[] imarr2, double threshhold)
            throws IOException {
        BufferedImage im1 = ImageIO.read(new ByteArrayInputStream(imarr1));
        BufferedImage im2 = ImageIO.read(new ByteArrayInputStream(imarr2));
        double ssd = computeSSD(im1, im2);
        assertTrue(msg, ssd < threshhold);
    }

    private void checkParamsMap(String err, Map<String, Object> m1, Map<String, Object> m2) {
        for (String key : m1.keySet()) {
            assertTrue(m2.containsKey(key));
            Object o1 = m1.get(key);
            Object o2 = m2.get(key);
            if (o1 instanceof Double && o2 instanceof Double) {
                assertTrue(err, Math.abs((Double)o1 - (Double)o2) < doubleThreshhold);
            } else {
                assertEquals(err, o1, o2);
            }
        }
    }

    /**
     * Test the rastering functionality of the student code, by writing the ByteArrayOutputStream
     * to a byte[] array and comparing it with the staff result byte-wise.
     * @throws Exception
     */
    @Test
    public void testAGetMapRaster() throws Exception {
        for (int i = 0; i < params.size(); i++) {
            TestParams p = params.get(i);
            Map<String, Object> raster_result = new HashMap<>();
            BufferedImage student_output = MapServer.getMapRaster(p.rasterParams, raster_result);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            MapServer.writeJpgToStream(student_output, os);
            assertImageArrayEquals("Raw image output differed for input: " + p.rasterParams + ".\n See " +
                    "example image " + i + ".\n", p.rasterOutput, os.toByteArray());
        }
        passedRaster++;
    }

    /**
     * Check the student raster output parameters against the staff output parameters.
     * @throws Exception
     */
    @Test
    public void testBGetMapRasterParams() throws Exception {
        for (int i = 0; i < params.size(); i++) {
            TestParams p = params.get(i);
            Map<String, Object> student_raster_result = new HashMap<>();
            MapServer.getMapRaster(p.rasterParams, student_raster_result);
            checkParamsMap("Returned parameters differed for input: " + p.rasterParams +
                            ".\n Expected: " + p.rasterResult + " but got: " + student_raster_result + "."
                    , p.rasterResult, student_raster_result);
        }
        passedRaster++;
    }
}
