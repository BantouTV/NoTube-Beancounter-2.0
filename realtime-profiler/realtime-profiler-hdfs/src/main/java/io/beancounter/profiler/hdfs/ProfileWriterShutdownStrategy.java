package io.beancounter.profiler.hdfs;

import org.apache.camel.impl.DefaultShutdownStrategy;

public class ProfileWriterShutdownStrategy extends DefaultShutdownStrategy {

    private ProfileWriter profileWriter;

    @Override
    protected void doShutdown() throws Exception {
        if (profileWriter != null) {
            profileWriter.close();
        }
        super.doShutdown();
    }

    public void setProfileWriter(ProfileWriter profileWriter) {
        this.profileWriter = profileWriter;
    }
}
