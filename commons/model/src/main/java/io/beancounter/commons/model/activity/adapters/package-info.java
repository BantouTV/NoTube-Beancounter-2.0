@XmlJavaTypeAdapters({
        @XmlJavaTypeAdapter(type=DateTime.class, value=DateTimeAdapterJAXB.class)
})
package io.beancounter.commons.model.activity.adapters;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import org.joda.time.DateTime;