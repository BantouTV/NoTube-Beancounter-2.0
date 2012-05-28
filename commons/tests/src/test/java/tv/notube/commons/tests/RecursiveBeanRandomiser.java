package tv.notube.commons.tests;

import tv.notube.commons.tests.model.RecursiveBean;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class RecursiveBeanRandomiser extends DefaultRandomiser<RecursiveBean> {

    public RecursiveBeanRandomiser(String name) {
        super(name);
    }

    public Class<RecursiveBean> type() {
        return RecursiveBean.class;
    }

    public RecursiveBean getRandom() {
        try {
            return new RecursiveBean(new URL("http://fake.org"), null);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
