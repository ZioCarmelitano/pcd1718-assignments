package pcd.ass02.ex2.verticles.codecs;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import pcd.ass02.domain.Document;

import java.util.List;

public class DocumentMessageCodec extends AbstractMessageCodec<Document, Document> {

    private static final TypeReference<List<String>> REFERENCE = new TypeReference<List<String>>() {
    };

    @Override
    protected void encodeToWire(JsonObject jsonObject, Document document) {
        jsonObject.put("lines", new JsonArray(document.getLines()));
        jsonObject.put("name", document.getName());
    }

    @Override
    protected Document decodeFromWire(JsonObject jsonObject) {
        final String linesStr = jsonObject.getJsonArray("lines").encode();
        final List<String> lines = Json.decodeValue(linesStr, REFERENCE);
        final String name = jsonObject.getString("name");

        return new Document(name, lines);
    }

    @Override
    public Document transform(Document document) {
        return document;
    }

    @Override
    public String name() {
        return "document";
    }

    public static DocumentMessageCodec getInstance() {
        return Holder.INSTANCE;
    }

    private DocumentMessageCodec() {
    }

    private static final class Holder {
        static final DocumentMessageCodec INSTANCE = new DocumentMessageCodec();
    }

}
