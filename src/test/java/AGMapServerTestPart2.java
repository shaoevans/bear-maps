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
public class AGMapServerTestPart2 {
    static List<TestParams> params;
    static final double doubleThreshhold = 0.0000000000001;
    static final double SSD_THRESHHOLD = 500000;
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
        assertTrue(msg + String.format("\nSSD was above threshhold: %f > %f", ssd, threshhold), ssd < threshhold);
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

    /**
     * Test the routefinding functionality by comparing the node id list item by item.
     * @throws Exception
     */
    @Test
    public void testCFindAndSetRoute() throws Exception {
        for (int i = 0; i < params.size() / 2; i++) {
            TestParams p = params.get(i);
            List<Long> student_route_result = MapServer.findAndDrawRoute(
                    p.routeParams, null, null);
            assertEquals("Found route differs for input: " + p.routeParams + ".\n",
                    p.routeResult, student_route_result);
        }
        passedRoute++;
    }

    /**
     * Test the route raster the same way the map raster is tested, except with the route pre-set
     * before making the call to raster.
     * @throws Exception
     */
    @Test
    public void testDRouteRaster() throws Exception {
        for (int i = 0; i < params.size(); i++) {
            TestParams p = params.get(i);
            Map<String, Object> student_raster_result = new HashMap<>();
            BufferedImage im = MapServer.getMapRaster(p.rasterParams,
                    student_raster_result);
            MapServer.findAndDrawRoute(p.routeParams, student_raster_result, im);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            MapServer.writeJpgToStream(im, os);
            assertImageArrayEquals("Raw image output differed for input: " + p.rasterParams + "\nWith" +
                    " route params: " + p.routeParams + ".\n See " +
                    "example image " + i + ".\n", p.routeRaster, os.toByteArray());
        }
        passedRoute++;
    }

    /**
     * Test Autocomplete for each prefix, comparing the sets of outputs against each other.
     * @throws Exception
     */
    @Test(timeout = 3000)
    public void testGGetLocationsByPrefix() throws Exception {
        /* Correctness */
        for (TestParams p : params) {
            List<String> student_autocomplete_result = MapServer.getLocationsByPrefix(p
                    .prefixSearchParam);
            HashSet<String> set_student = new HashSet<>(student_autocomplete_result);
            HashSet<String> set_staff = new HashSet<>(p.autocompleteResults);

            assertEquals("Autocompletion results differ for prefix " + p.prefixSearchParam,
                    set_staff, set_student);
        }
    }

    /**
     * Test location search by full search string, comparing the output lists against each other
     * element by element; note that we assume the most reasonable construction of each of these
     * lists, that is, that they are in order of the locations as they appear in the OSM file.
     * @throws Exception
     */
    @Test(timeout = 500)
    public void testHGetLocations() throws Exception {
        /* Correctness */
        for (TestParams p : params) {
            List<Map<String, Object>> student_search_result = MapServer.getLocations(p.actualSearchParam);
            Collections.sort(student_search_result,
                    (Map<String, Object> o1, Map<String, Object> o2) ->
                            ((Long) o1.get("id")).compareTo((Long) o2.get("id")));
            Collections.sort(p.actualSearchResult,
                    (Map<String, Object> o1, Map<String, Object> o2) ->
                            ((Long) o1.get("id")).compareTo((Long) o2.get("id")));
            assertEquals("Search results differ for search term: " + p.actualSearchParam,
                    p.actualSearchResult, student_search_result);
        }
    }
}
