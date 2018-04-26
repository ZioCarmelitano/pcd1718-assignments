package pcd.ass02.domain;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Folder {

    private final List<Folder> subFolders;
    private final List<Document> documents;

    private Folder(List<Folder> subFolders, List<Document> documents) {
        this.subFolders = subFolders;
        this.documents = documents;
    }

    public List<Folder> getSubFolders() {
        return subFolders;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public static Folder fromDirectory(File dir, int maxDepth) {
        final List<Document> documents = new LinkedList<>();
        final List<Folder> subFolders = new LinkedList<>();

        final File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (maxDepth != 0) {
                        subFolders.add(Folder.fromDirectory(file, maxDepth - 1));
                    }
                } else {
                    documents.add(Document.fromFile(file));
                }
            }
        }
        return new Folder(subFolders, documents);
    }

}

