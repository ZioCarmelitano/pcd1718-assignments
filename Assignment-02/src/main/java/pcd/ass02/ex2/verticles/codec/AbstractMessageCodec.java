package pcd.ass02.ex2.verticles.codec;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

public abstract class AbstractMessageCodec<S, R> implements MessageCodec<S, R> {

    @Override
    public void encodeToWire(Buffer buffer, S s) {
        final JsonObject json = new JsonObject();

        encodeToWire(json, s);

        final String encoding = json.encode();
        final int length = encoding.length();

        buffer.appendInt(length);
        buffer.appendString(encoding);
    }

    @Override
    public R decodeFromWire(int pos, Buffer buffer) {
        // Length of JSON
        final int length = buffer.getInt(pos);
        pos += 4;

        // Get JSON string by it's length
        // Jump 4 because getInt() == 4 bytes
        String jsonStr = buffer.getString(pos, pos + length);
        JsonObject json = new JsonObject(jsonStr);
        return decodeFromWire(json);
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }

    protected abstract void encodeToWire(JsonObject json, S s);

    protected abstract R decodeFromWire(JsonObject json);


}
