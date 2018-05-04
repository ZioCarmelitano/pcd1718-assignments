package pcd.ass02.ex2.verticles.codecs;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

abstract class AbstractMessageCodec<S, R> implements MessageCodec<S, R> {

    AbstractMessageCodec() {
    }

    @Override
    public void encodeToWire(Buffer buffer, S s) {
        final JsonObject jsonObject = new JsonObject();

        encodeToWire(jsonObject, s);

        final Buffer encoded = jsonObject.toBuffer();
        final int length = encoded.length();

        buffer.appendInt(length);
        buffer.appendBuffer(encoded);
    }

    @Override
    public R decodeFromWire(int pos, Buffer buffer) {
        final int length = buffer.getInt(pos);
        pos += 4;
        JsonObject decoded = new JsonObject(buffer.getString(pos, pos + length));

        return decodeFromWire(decoded);
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }

    protected abstract void encodeToWire(JsonObject jsonObject, S s);

    protected abstract R decodeFromWire(JsonObject jsonObject);

}
