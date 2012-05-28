package tv.notube.platform.responses;

import tv.notube.crawler.Report;
import tv.notube.platform.PlatformResponse;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@XmlRootElement
public class ReportPlatformResponse extends PlatformResponse<Report> {

    private Report report;

    public ReportPlatformResponse() {}

    public ReportPlatformResponse(Status status, String message) {
        super(status, message);
    }

    public ReportPlatformResponse(Status status, String message, Report rep) {
        super(status, message);
        this.report = rep;
    }

    @XmlElement
    public Report getObject() {
        return report;
    }
}