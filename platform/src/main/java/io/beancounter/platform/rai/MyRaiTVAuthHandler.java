package io.beancounter.platform.rai;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class MyRaiTVAuthHandler {

    private static String SERVICE_PATTERN = "http://www.rai.tv/MyRaiTv/login.do?username=%s&password=%s";

    public String authOnRai(String username, String password) throws IOException {
        URL url = new URL(
                String.format(SERVICE_PATTERN, username, password)
        );
        URLConnection urlConnection = url.openConnection();
        InputStream is = urlConnection.getInputStream();
        String response;
        try {
            response = new java.util.Scanner(is).useDelimiter("\\A").next();
        } finally {
            is.close();
        }
        String splitResponse[] = response.split("-");
        if (splitResponse.length != 2) {
            return "ko";
        }
        return splitResponse[0];
    }
}
