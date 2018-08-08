package pcd.ass04.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public final class JsonHelper {

    public static void ifPresentString(JsonObject object, String key, Consumer<? super String> action) {
        if (object != null && object.containsKey(key)) {
            action.accept(object.getString(key));
        }
    }

    public static void ifPresentBoolean(JsonObject object, String key, Consumer<? super Boolean> action) {
        if (object != null && object.containsKey(key)) {
            action.accept(object.getBoolean(key));
        }
    }

    public static void ifPresentJsonArray(JsonObject object, String key, Consumer<? super JsonArray> action) {
        if (object != null && object.containsKey(key)) {
            action.accept(object.getJsonArray(key));
        }
    }

    public static void ifPresentJsonObject(JsonObject object, String key, Consumer<? super JsonObject> action) {
        if (object != null && object.containsKey(key)) {
            action.accept(object.getJsonObject(key));
        }
    }

    public static void ifPresentDouble(JsonObject object, String key, DoubleConsumer action) {
        if (object != null && object.containsKey(key)) {
            action.accept(object.getDouble(key));
        }
    }

    public static void ifPresentInteger(JsonObject object, String key, IntConsumer action) {
        if (object != null && object.containsKey(key)) {
            action.accept(object.getInteger(key));
        }
    }

    public static void ifPresentLong(JsonObject object, String key, LongConsumer action) {
        if (object != null && object.containsKey(key)) {
            action.accept(object.getLong(key));
        }
    }

    private JsonHelper() {
    }

}
