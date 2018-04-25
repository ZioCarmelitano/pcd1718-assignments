package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import pcd.ass02.domain.Document;
import pcd.ass02.domain.Folder;

public class FolderSearchVerticle extends AbstractVerticle {

    private final Folder folder;
    private final String regex;

    public FolderSearchVerticle(Folder folder, String regex) {
        this.folder = folder;
        this.regex = regex;
    }

    @Override
    public void start() throws Exception {
        super.start();

        for (Folder subFolder : folder.getSubFolders()) {
            vertx.deployVerticle(new FolderSearchVerticle(subFolder, regex),
                    new DeploymentOptions().setWorker(true));
        }

        for (Document document : folder.getDocuments()) {
            vertx.deployVerticle(new DocumentSearchVerticle(document, regex),
                    new DeploymentOptions().setWorker(true));
        }
    }
}
