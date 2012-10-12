package io.beancounter.commons.pig;

import io.beancounter.commons.model.Interest;
import io.beancounter.commons.model.UserProfile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.pig.LoadFunc;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UserProfileLoader extends LoadFunc {

    protected RecordReader in;

    private TupleFactory mTupleFactory = TupleFactory.getInstance();

    private static final int BUFFER_SIZE = 1024;

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void setLocation(String location, Job job) throws IOException {
        FileInputFormat.setInputPaths(job, location);
    }

    @Override
    public InputFormat getInputFormat() throws IOException {
        return new TextInputFormat();
    }

    @Override
    public void prepareToRead(RecordReader recordReader, PigSplit pigSplit) throws IOException {
        in = recordReader;
    }

    @Override
    public Tuple getNext() throws IOException {
        System.out.println("YO!!");
        boolean done;
        try {
            done = in.nextKeyValue();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (done) {
            Text value;
            try {
                value = (Text) in.getCurrentValue();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            byte[] buf = value.getBytes();
            String rowStr = new String(buf, "UTF-8");
            UserProfile snapshot;
            try {
                snapshot = mapper.readValue(rowStr, UserProfile.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println("profile: " + snapshot);
            return toTuple(snapshot);
        }
        return null;
    }

    private Tuple toTuple(UserProfile snapshot) throws ExecException {
        Tuple tuple = mTupleFactory.newTuple(snapshot.getInterests().size());
        int i = 0;
        for (Interest interest : snapshot.getInterests()) {
            Tuple iTuple = mTupleFactory.newTuple(snapshot.getInterests().size());
            iTuple.set(0, snapshot.getLastUpdated().getMillis());
            iTuple.set(1, interest.getLabel());
            iTuple.set(2, interest.getResource());
            tuple.set(i, iTuple);
            i++;
        }
        return tuple;
    }

}
