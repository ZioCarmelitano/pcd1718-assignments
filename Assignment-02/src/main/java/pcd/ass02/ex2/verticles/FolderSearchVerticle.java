package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import pcd.ass02.domain.Document;
import pcd.ass02.domain.Folder;

public class FolderSearchVerticle extends AbstractVerticle {

    private EventBus eventBus;

    public FolderSearchVerticle() {
    }

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        eventBus.<Folder>consumer("folderSearch", m -> onFolder(m.body()));
    }

    private void onFolder(Folder folder) {
        folder.getSubFolders().forEach(this::onSubFolder);
        folder.getDocuments().forEach(this::onDocument);
    }

    private void onSubFolder(Folder subFolder) {
        eventBus.send("folderSearch", subFolder);
    }

    private void onDocument(Document document) {
        eventBus.send("documentSearch", document);
    }

}
