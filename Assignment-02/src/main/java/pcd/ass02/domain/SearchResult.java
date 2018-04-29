package pcd.ass02.domain;

public class SearchResult {

    private final String documentName;
    private final long occurrences;

    public SearchResult(String documentName, long occurrences) {
        this.documentName = documentName;
        this.occurrences = occurrences;
    }

    public String getDocumentName() {
        return documentName;
    }

    public long getOccurrences() {
        return occurrences;
    }

}
