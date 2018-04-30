package pcd.ass02.ex1.tasks;

import pcd.ass02.domain.Document;
import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchResult;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Consumer;

public class FolderSearchTask extends RecursiveTask<Long> {

    private final Folder folder;
    private final String regex;
    private final Consumer<? super SearchResult> callback;

    public FolderSearchTask(Folder folder, String regex, Consumer<? super SearchResult> callback) {
        this.folder = folder;
        this.regex = regex;
        this.callback = callback;
    }

    @Override
    protected Long compute() {
        final List<ForkJoinTask<Long>> forks = new LinkedList<>();
        for (final Folder subFolder : folder.getSubFolders()) {
            final FolderSearchTask task = new FolderSearchTask(subFolder, regex, callback);
            forks.add(task);
            task.fork();
        }
        for (final Document document : folder.getDocuments()) {
            final DocumentSearchTask task = new DocumentSearchTask(document, regex, callback);
            forks.add(task);
            task.fork();
        }

        long count = 0L;
        for (final ForkJoinTask<Long> fork : forks) {
            count += fork.join();
        }
        return count;
    }

}
    