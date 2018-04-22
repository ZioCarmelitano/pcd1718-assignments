package pcd.ass02.domain;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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
        List<Document> documents = new LinkedList<>();
        List<Folder> subFolders = new LinkedList<>();
        for (File entry : Objects.requireNonNull(dir.listFiles())) {
            if (entry.isDirectory()) {
                if(maxDepth != 0){
                    subFolders.add(Folder.fromDirectory(entry, maxDepth - 1));
                }
            } else {
                documents.add(Document.fromFile(entry));
            }
        }
        return new Folder(subFolders, documents);
    }

}

