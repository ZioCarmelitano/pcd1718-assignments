package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import pcd.ass02.domain.Document;
import pcd.ass02.domain.Folder;

import static pcd.ass02.ex2.util.MessageHelper.handler;
import static pcd.ass02.ex2.verticles.Channels.*;
import static pcd.ass02.ex2.verticles.Channels.folderSearch;

class FolderSearchVerticle extends AbstractVerticle {

    private EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        eventBus.consumer(folderSearch, handler(this::onFolder));
    }

    private void onFolder(Folder folder) {
        folder.getSubFolders().forEach(this::onSubFolder);
        folder.getDocuments().forEach(this::onDocument);
    }

    private void onSubFolder(Folder subFolder) {
        eventBus.send(folderSearch, subFolder);
    }

    private void onDocument(Document document) {
        eventBus.send(documentSearch.analyze, document);
    }

}
