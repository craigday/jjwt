package io.jsonwebtoken.impl.json;

import io.jsonwebtoken.ObjectMapperType;

/**
 * @author Craig Day (craig@livewirelabs.com.au)
 */
public class ObjectMapperFactory {

    public static final ObjectMapperFactory INSTANCE = new ObjectMapperFactory();

    public ObjectMapper defaultObjectMapper() {
        return newObjectMapper(ObjectMapperType.JACKSON);
    }

    public ObjectMapper newObjectMapper(ObjectMapperType objectMapperType) {
        switch (objectMapperType) {
            case JACKSON:
                return new JacksonObjectMapper();
            case GSON:
                return new GsonObjectMapper();
            default:
                throw new IllegalArgumentException(String.valueOf(objectMapperType));
        }
    }
}
