package pcd.ass02.domain;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Folder {
    private final List<Folder> subFolders;
    private final List<Document> documents;

    public Folder(List<Folder> subFolders, List<Document> documents) {
        this.subFolders = subFolders;
        this.documents = documents;
    }

    public List<Folder> getSubFolders() {
        return subFolders;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public static Folder fromDirectory(File dir, int maxDepth) throws IOException {
        List<Document> documents = new LinkedList<Document>();
        List<Folder> subFolders = new LinkedList<Folder>();
        for (File entry : dir.listFiles()) {
            if (entry.isDirectory() && maxDepth != 0) {
                subFolders.add(Folder.fromDirectory(entry, maxDepth - 1));
            } else {
                documents.add(Document.fromFile(entry));
            }
        }
        return new Folder(subFolders, documents);
    }

}

