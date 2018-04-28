package pcd.ass02.ex2.verticles.codec;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import pcd.ass02.domain.Document;

import java.util.List;

public class DocumentCodec extends AbstractMessageCodec<Document, Document> {

    @Override
    protected void encodeToWire(JsonObject json, Document document) {
        json.put("lines", new JsonArray(document.getLines()));
        json.put("name", document.getName());
    }

    @Override
    protected Document decodeFromWire(JsonObject json) {
        final String linesStr = json.getJsonArray("lines").encode();
        final TypeReference<List<String>> ref = new TypeReference<List<String>>() {
        };

        final List<String> lines = Json.decodeValue(linesStr, ref);
        final String name = json.getString("name");

        return new Document(name, lines);
    }

    @Override
    public Document transform(Document document) {
        return document;
    }

    public static DocumentCodec getInstance() {
        return Holder.INSTANCE;
    }

    private DocumentCodec() {
    }

    private static final class Holder {
        static final DocumentCodec INSTANCE = new DocumentCodec();
    }

}
