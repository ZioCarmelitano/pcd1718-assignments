package fj;

public class SearchResult {
    private final String documentName;
    private final Long count;

    public SearchResult(String documentName, Long count) {
        this.documentName = documentName;
        this.count = count;
    }

    public String getDocumentName() {
        return this.documentName;
    }

    public Long getCount() {
        return this.count;
    }
}
