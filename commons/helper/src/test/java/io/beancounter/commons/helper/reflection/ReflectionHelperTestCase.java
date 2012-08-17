package io.beancounter.commons.helper.reflection;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URL;

/**
 * Reference test case for {@link ReflectionHelper}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ReflectionHelperTestCase {

    public class TestBean {
        private String s;
        private int i;
        private TestBean inner;

        public String getS() {
            return s;
        }

        public int getI() {
            return i;
        }

        public TestBean getInner() {
            return inner;
        }
    }

    public final class ExtendedTestBean extends TestBean {
        private URL url;

        public URL getUrl() {
            return url;
        }
    }

    @Test
    public void testGetGetters() throws ReflectionHelperException {
        ReflectionHelper.Access[] access = ReflectionHelper.getGetters(ExtendedTestBean.class, false);
        Assert.assertEquals(access.length, 5);
    }

     @Test
    public void getGettersFieldNames() throws ReflectionHelperException {
        ReflectionHelper.Access[] access = ReflectionHelper.getGetters(ExtendedTestBean.class, true, String.class);
        Assert.assertEquals(access.length, 2);
    }

}
