package pcd.ass02.domain;

public class SearchResult {

    private final String documentName;
    private final long count;

    public SearchResult(String documentName, long count) {
        this.documentName = documentName;
        this.count = count;
    }

    public String getDocumentName() {
        return documentName;
    }

    public long getCount() {
        return count;
    }

}
