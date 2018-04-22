package pcd.ass02.ex1.tasks;

import pcd.ass02.domain.Document;
import pcd.ass02.domain.Folder;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiConsumer;

public class FolderSearchTask extends RecursiveTask<Long> {

    private final Folder folder;
    private final String regex;
    private final BiConsumer<Document, Long> callback;

    public FolderSearchTask(Folder folder, String regex, BiConsumer<Document, Long> callback) {
        super();
        this.folder = folder;
        this.regex = regex;
        this.callback = callback;
    }

    @Override
    protected Long compute() {
        long count = 0L;
        List<RecursiveTask<Long>> forks = new LinkedList<>();
        for (Folder subFolder : folder.getSubFolders()) {
            FolderSearchTask task = new FolderSearchTask(subFolder, regex, callback);
            forks.add(task);
            task.fork();
        }
        for (Document document : folder.getDocuments()) {
            DocumentSearchTask task = new DocumentSearchTask(document, regex, callback);
            forks.add(task);
            task.fork();
        }
        for (RecursiveTask<Long> task : forks) {
            count += task.join();
        }
        return count;
    }

}
    