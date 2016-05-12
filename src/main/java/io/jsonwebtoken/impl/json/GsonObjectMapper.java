package io.jsonwebtoken.impl.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.LongSerializationPolicy;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * @author Craig Day (craig@livewirelabs.com.au)
 */
public class GsonObjectMapper implements ObjectMapper {

    private final Gson gson;

    public GsonObjectMapper() {
        GsonBuilder builder = new GsonBuilder();
        builder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json,
                                    Type typeOfT,
                                    JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });
        builder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
            @Override
            public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.getTime());
            }
        });
        gson = builder.create();
    }

    public String toJson(Object object) {
        return gson.toJson(object);
    }

    public byte[] toJsonBytes(Object object) {
        return gson.toJson(object).getBytes(Charset.forName("UTF-8"));
    }

    public <T> T fromJson(String json, Class<T> clazz) throws IOException {
        try {
            return gson.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            throw new IOException(e);
        }
    }


    public static void main(String[] args) {
        Gson gson = new Gson();
    }

}
