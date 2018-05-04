package pcd.ass02.interactors;

import pcd.ass02.domain.Folder;

public interface OccurrencesCounter {

    void start();

    void reset();

    void stop();

    long countOccurrences(Folder rootFolder, String regex);

}
