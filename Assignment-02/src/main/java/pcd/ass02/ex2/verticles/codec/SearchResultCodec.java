package pcd.ass02.ex2.verticles.codec;

import io.vertx.core.json.JsonObject;
import pcd.ass02.domain.SearchResult;

public class SearchResultCodec extends AbstractMessageCodec<SearchResult, SearchResult> {

    @Override
    protected void encodeToWire(JsonObject json, SearchResult result) {
        json.put("documentName", result.getDocumentName());
        json.put("count", result.getCount());
    }

    @Override
    protected SearchResult decodeFromWire(JsonObject json) {
        final String documentName = json.getString("documentName");
        final long count = json.getLong("count");
        return new SearchResult(documentName, count);
    }

    @Override
    public SearchResult transform(SearchResult result) {
        return result;
    }

    public static SearchResultCodec getInstance() {
        return Holder.INSTANCE;
    }

    private SearchResultCodec() {
    }

    private static final class Holder {
        static final SearchResultCodec INSTANCE = new SearchResultCodec();
    }

}
