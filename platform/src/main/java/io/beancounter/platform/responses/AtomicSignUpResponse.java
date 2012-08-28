package io.beancounter.platform.responses;

import io.beancounter.usermanager.AtomicSignUp;
import io.beancounter.platform.PlatformResponse;

/**
 * {@link PlatformResponse} specialized to wrap {@link AtomicSignUp}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class AtomicSignUpResponse extends PlatformResponse<AtomicSignUp> {

    private AtomicSignUp atomicSignUp;

    public AtomicSignUpResponse(){}

    public AtomicSignUpResponse(Status s, String m) {
        super(s, m);
    }

    public AtomicSignUpResponse(Status s, String m, AtomicSignUp atomicSignUp) {
        super(s, m);
        this.atomicSignUp = atomicSignUp;
    }

    @Override
    public AtomicSignUp getObject() {
        return atomicSignUp;
    }

    public void setObject(AtomicSignUp atomicSignUp) {
        this.atomicSignUp = atomicSignUp;
    }
}
