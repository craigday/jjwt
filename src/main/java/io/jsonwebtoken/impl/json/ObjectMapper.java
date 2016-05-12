package io.jsonwebtoken.impl.json;

import java.io.IOException;

/**
 * @author Craig Day (craig@livewirelabs.com.au)
 */
public interface ObjectMapper {

    String toJson(Object object);

    byte[] toJsonBytes(Object object);

    <T> T fromJson(String json, Class<T> clazz) throws IOException;

}
