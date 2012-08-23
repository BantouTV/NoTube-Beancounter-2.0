package io.beancounter.commons.helper;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class UriUtilsTest {

    @Test
    public void shouldCorrectlyEncodeUriWithForwardSlashes() throws Exception {
        String rawUri = "http://api.beancounter.io/oauth/callback/username";
        String expected = "aHR0cDovL2FwaS5iZWFuY291bnRlci5pby9vYXV0aC9jYWxsYmFjay91c2VybmFtZQ";

        assertEquals(UriUtils.encodeBase64(rawUri), expected);
    }

    @Test
    public void longEncodedUrisShouldNotBeChunked() throws Exception {
        String longInput = "Man is distinguished, not only by his reason, but by this singular passion from other animals, which is a lust of the mind, that by a perseverance of delight in the continued and indefatigable generation of knowledge, exceeds the short vehemence of any carnal pleasure.";
        String expected = "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlz"
                + "IHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2Yg"
                + "dGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGlu"
                + "dWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRo"
                + "ZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4";

        assertEquals(UriUtils.encodeBase64(longInput), expected);
    }

    @Test
    public void shouldCorrectlyDecodeUri() throws Exception {
        String encoded = "aHR0cDovL2FwaS5iZWFuY291bnRlci5pby9vYXV0aC9jYWxsYmFjay91c2VybmFtZQ";
        String expected = "http://api.beancounter.io/oauth/callback/username";

        assertEquals(UriUtils.decodeBase64(encoded), expected);
    }
}
