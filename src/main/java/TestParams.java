import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * TestParams fully describes the inputs and expected outputs for test cases.
 * After being generated (test-case autogeneration code not shown in this file),
 * this information is serialized for testing.
 * @author Alan Yao
 */
public class TestParams implements Serializable {
    static final long serialVersionUID = 79469696963812654L;

    Map<String, Double> rasterParams;
    Map<String, Double> routeParams;
    String prefixSearchParam;
    String actualSearchParam;
    Map<String, Object> rasterResult;
    byte[] rasterOutput;
    List<Long> routeResult;
    byte[] routeRaster;
    List<Map<String, Object>> actualSearchResult;
    List<String> autocompleteResults;
}
