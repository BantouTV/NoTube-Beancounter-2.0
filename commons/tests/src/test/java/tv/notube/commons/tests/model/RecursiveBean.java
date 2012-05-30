package tv.notube.commons.tests.model;

import tv.notube.commons.tests.annotations.Random;

import java.net.URL;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class RecursiveBean {

    private URL url;

    private RecursiveBean bean;

    @Random(names = { "url", "bean" })
    public RecursiveBean(URL url, RecursiveBean bean) {
        this.url = url;
        this.bean = bean;
    }

    public RecursiveBean getBean() {
        return bean;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecursiveBean that = (RecursiveBean) o;

        if (bean != null ? !bean.equals(that.bean) : that.bean != null)
            return false;
        if (url != null ? !url.equals(that.url) : that.url != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (bean != null ? bean.hashCode() : 0);
        return result;
    }
}
