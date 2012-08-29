package io.beancounter.platform.validation;

import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class RequestValidatorTest {

    @Test
    public void mapBuiltByCreateParamsMethodShouldBeALinkedHashMap() throws Exception {
        assertEquals(RequestValidator.createParams().getClass(), LinkedHashMap.class);
    }

    @Test
    public void creatingParamsWithOneParamPairShouldReturnValidMap() throws Exception {
        Map<String, Object> params = RequestValidator.createParams("one", 1);

        assertEquals(params.size(), 1);
        assertEquals(params.get("one"), 1);
    }

    @Test
    public void creatingParamsWithTwoParamPairsShouldReturnValidMap() throws Exception {
        Map<String, Object> params = RequestValidator.createParams(
                "one", 1,
                "two", 2
        );

        assertEquals(params.size(), 2);
        assertEquals(params.get("one"), 1);
        assertEquals(params.get("two"), 2);
    }

    @Test
    public void creatingParamsWithOddNumberOfArgumentsShouldReturnValidMapWithLastKeyIgnored() throws Exception {
        Map<String, Object> params = RequestValidator.createParams(
                "one", 1,
                "ignore-me"
        );

        assertEquals(params.size(), 1);
        assertEquals(params.get("one"), 1);
        assertNull(params.get("ignore-me"));
    }
}
