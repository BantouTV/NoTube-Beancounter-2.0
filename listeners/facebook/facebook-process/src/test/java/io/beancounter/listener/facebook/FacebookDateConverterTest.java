package io.beancounter.listener.facebook;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

import java.text.ParseException;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookDateConverterTest {

    @Test
    public void testDate() throws ParseException {
        //String gmtFormat = "yyyy-MM-dd'T'HH:mm:ssZ";
        String dateIn = "2012-07-05T13:13:37+0000";
        DateTime date = new DateTime(dateIn);

        //Date date2 = new Date(1341490417000L);
        System.out.println(date);
        //Assert.assertEquals(1341490417000L, date);
    }
}