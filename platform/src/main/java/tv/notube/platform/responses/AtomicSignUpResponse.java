package tv.notube.platform.responses;

import tv.notube.usermanager.AtomicSignUp;
import tv.notube.platform.PlatformResponse;

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

    public void setAtomicSignUp(AtomicSignUp atomicSignUp) {
        this.atomicSignUp = atomicSignUp;
    }
}
