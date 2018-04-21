/*
 * Fork-Join example, adapted from
 * http://www.oracle.com/technetwork/articles/java/fork-join-422606.html
 * 
 */
package fj;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public class OccurrencesCounter {

    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    
    public Long occurrencesCount(Document document, String regex) {
        long count = 0;
        for (String line : document.getLines()) {
            count += MatcherHelper.countMatches(regex, line);
        }
        return count;
    }
        
    public Long countOccurrencesOnSingleThread(Folder folder, String regex) {
        long count = 0;
        for (Folder subFolder : folder.getSubFolders()) {
            count = count + countOccurrencesOnSingleThread(subFolder, regex);
        }
        for (Document document : folder.getDocuments()) {
            count = count + occurrencesCount(document, regex);
        }
        return count;
    }

    public Long countOccurrencesInParallel(Folder folder, String searchedWord, BiConsumer<Document, Long> callback) {
        return forkJoinPool.invoke(new FolderSearchTask(this, folder, searchedWord, callback));
    }

}
