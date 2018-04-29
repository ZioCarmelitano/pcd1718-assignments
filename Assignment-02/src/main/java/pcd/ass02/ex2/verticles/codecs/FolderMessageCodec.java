package pcd.ass02.ex2.verticles.codecs;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import pcd.ass02.domain.Document;
import pcd.ass02.domain.Folder;

import java.util.LinkedList;
import java.util.List;

public class FolderMessageCodec extends AbstractMessageCodec<Folder, Folder> {

    private static final DocumentMessageCodec documentCodec = DocumentMessageCodec.getInstance();

    @Override
    protected void encodeToWire(JsonObject jsonObject, Folder folder) {
        jsonObject.put("subFolders", new JsonArray(folder.getSubFolders()));
        jsonObject.put("folders", new JsonArray(folder.getDocuments()));
    }

    @Override
    protected Folder decodeFromWire(JsonObject jsonObject) {
        final List<Folder> subFolders = new LinkedList<>();
        final List<Document> documents = new LinkedList<>();

        final JsonArray subFoldersJson = jsonObject.getJsonArray("subFolders");
        for (int i = 0; i < subFoldersJson.size(); i++) {
            final JsonObject subFolder = subFoldersJson.getJsonObject(i);
            subFolders.add(decodeFromWire(subFolder));
        }

        final JsonArray documentsJson = jsonObject.getJsonArray("documents");
        for (int i = 0; i < documentsJson.size(); i++) {
            final JsonObject document = subFoldersJson.getJsonObject(i);
            documents.add(documentCodec.decodeFromWire(document));
        }

        return new Folder(subFolders, documents);
    }

    @Override
    public Folder transform(Folder folder) {
        return folder;
    }

    @Override
    public String name() {
        return "folder";
    }

    public static FolderMessageCodec getInstance() {
        return Holder.INSTANCE;
    }

    private FolderMessageCodec() {
    }

    private static final class Holder {
        static final FolderMessageCodec INSTANCE = new FolderMessageCodec();
    }

}
