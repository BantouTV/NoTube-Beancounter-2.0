package io.beancounter.platform.rai;

public class MyRaiTVAuthResponse {

    private String token;

    private String username;

    public MyRaiTVAuthResponse(String token, String username) {
        this.token = token;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }
}
