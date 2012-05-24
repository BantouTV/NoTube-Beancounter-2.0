package tv.notube.platform;

/**
 * This is the most generic response the REST platform is able to deliver.
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 * @see {@link javax.xml.bind.annotation.XmlRootElement}
 */
public interface PlatformResponse<T> {

    public T getObject();

}
