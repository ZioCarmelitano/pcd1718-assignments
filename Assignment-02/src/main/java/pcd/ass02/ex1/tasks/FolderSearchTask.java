package pcd.ass02.ex1.tasks;

import pcd.ass02.domain.Document;
import pcd.ass02.domain.Folder;
import pcd.ass02.ex1.OccurrencesCounter;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiConsumer;

public class FolderSearchTask extends RecursiveTask<Long> {

    private final Folder folder;
    private final String regex;
    private final OccurrencesCounter oc;
    private final BiConsumer<Document, Long> callback;

    public FolderSearchTask(OccurrencesCounter oc, Folder folder, String regex, BiConsumer<Document, Long> callback) {
        super();
        this.oc = oc;
        this.folder = folder;
        this.regex = regex;
        this.callback = callback;
    }

    @Override
    protected Long compute() {
        long count = 0L;
        List<RecursiveTask<Long>> forks = new LinkedList<RecursiveTask<Long>>();
        for (Folder subFolder : folder.getSubFolders()) {
            FolderSearchTask task = new FolderSearchTask(oc, subFolder, regex, callback);
            forks.add(task);
            task.fork();
        }
        for (Document document : folder.getDocuments()) {
            DocumentSearchTask task = new DocumentSearchTask(oc, document, regex, callback);
            forks.add(task);
            task.fork();
        }
        for (RecursiveTask<Long> task : forks) {
            count += task.join();
        }
        return count;
    }

}
    