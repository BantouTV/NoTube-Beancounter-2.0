package io.beancounter.platform.responses;

import io.beancounter.platform.PlatformResponse;
import io.beancounter.platform.rai.MyRaiTVSignUp;

public class MyRaiTVSignUpResponse extends PlatformResponse<MyRaiTVSignUp> {

    private MyRaiTVSignUp signUp;

    public MyRaiTVSignUpResponse() {}

    public MyRaiTVSignUpResponse(Status status, String message) {
        super(status, message);
    }

    public MyRaiTVSignUpResponse(Status status, String message, MyRaiTVSignUp signUp) {
        super(status, message);
        this.signUp = signUp;
    }

    @Override
    public MyRaiTVSignUp getObject() {
        return signUp;
    }

    public void setObject(MyRaiTVSignUp signUp) {
        this.signUp = signUp;
    }
}
