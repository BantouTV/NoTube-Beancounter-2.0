package io.beancounter.publisher.facebook.adapters;

import com.restfb.types.FacebookType;
import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.model.activity.Verb;

/**
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public interface Publisher <T extends Object> {

    public FacebookType publishActivity(String token, Verb verb, T t);

}
