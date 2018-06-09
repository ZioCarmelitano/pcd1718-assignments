package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import pcd.ass02.domain.Document;
import pcd.ass02.domain.Folder;

import static pcd.ass02.ex2.util.MessageHelper.wrap;

class FolderSearchVerticle extends AbstractVerticle {

    private EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        eventBus.consumer(C.folderSearch, wrap(this::onFolder));
    }

    private void onFolder(Folder folder) {
        folder.getSubFolders().forEach(this::onSubFolder);
        folder.getDocuments().forEach(this::onDocument);
    }

    private void onSubFolder(Folder subFolder) {
        eventBus.send(C.folderSearch, subFolder);
    }

    private void onDocument(Document document) {
        eventBus.send(C.documentSearch.analyze, document);
    }

}
