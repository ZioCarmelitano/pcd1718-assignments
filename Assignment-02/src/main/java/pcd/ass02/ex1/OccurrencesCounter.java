/*
 * Fork-Join example, adapted from
 * http://www.oracle.com/technetwork/articles/java/fork-join-422606.html
 *
 */
package pcd.ass02.ex1;

import pcd.ass02.domain.Document;
import pcd.ass02.domain.Folder;
import pcd.ass02.ex1.tasks.FolderSearchTask;
import pcd.ass02.util.MatcherHelper;

import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;

public class OccurrencesCounter {

    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    public static long occurrencesCount(Document document, String regex) {
        long count = 0;
        for (String line : document.getLines()) {
            count += MatcherHelper.countMatches(regex, line);
        }
        return count;
    }

    private Long countOccurrencesOnSingleThread(Folder folder, String regex) {
        long count = 0;
        for (Folder subFolder : folder.getSubFolders()) {
            count = count + countOccurrencesOnSingleThread(subFolder, regex);
        }
        for (Document document : folder.getDocuments()) {
            count = count + occurrencesCount(document, regex);
        }
        return count;
    }

    public Long countOccurrencesInParallel(Folder folder, String regex, BiConsumer<Document, Long> callback) {
        return forkJoinPool.invoke(new FolderSearchTask(folder, regex, callback));
    }

}
