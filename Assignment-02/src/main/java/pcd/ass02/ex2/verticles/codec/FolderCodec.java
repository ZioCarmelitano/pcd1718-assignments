package pcd.ass02.ex2.verticles.codec;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import pcd.ass02.domain.Document;
import pcd.ass02.domain.Folder;

import java.util.LinkedList;
import java.util.List;

public class FolderCodec extends AbstractMessageCodec<Folder, Folder> {

    private static DocumentCodec documentCodec = DocumentCodec.getInstance();

    @Override
    protected void encodeToWire(JsonObject json, Folder folder) {
        json.put("subFolders", new JsonArray(folder.getSubFolders()));
        json.put("folders", new JsonArray(folder.getDocuments()));
    }

    @Override
    protected Folder decodeFromWire(JsonObject json) {
        final List<Folder> subFolders = new LinkedList<>();
        final List<Document> documents = new LinkedList<>();

        final JsonArray subFoldersJson = json.getJsonArray("subFolders");
        for (int i = 0; i < subFoldersJson.size(); i++) {
            final JsonObject subFolder = subFoldersJson.getJsonObject(i);
            subFolders.add(decodeFromWire(subFolder));
        }

        final JsonArray documentsJson = json.getJsonArray("documents");
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

    public static FolderCodec getInstance() {
        return Holder.INSTANCE;
    }

    private FolderCodec() {
    }

    private static final class Holder {
        static final FolderCodec INSTANCE = new FolderCodec();
    }

}
