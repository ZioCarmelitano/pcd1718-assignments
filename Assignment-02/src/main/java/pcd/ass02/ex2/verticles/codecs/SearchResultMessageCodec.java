package pcd.ass02.ex2.verticles.codecs;

import io.vertx.core.json.JsonObject;
import pcd.ass02.domain.SearchResult;

public class SearchResultMessageCodec extends AbstractMessageCodec<SearchResult, SearchResult> {

    @Override
    protected void encodeToWire(JsonObject jsonObject, SearchResult result) {
        jsonObject.put("documentName", result.getDocumentName());
        jsonObject.put("count", result.getCount());
    }

    @Override
    protected SearchResult decodeFromWire(JsonObject jsonObject) {
        final String documentName = jsonObject.getString("documentName");
        final long count = jsonObject.getLong("count");
        return new SearchResult(documentName, count);
    }

    @Override
    public SearchResult transform(SearchResult result) {
        return result;
    }

    @Override
    public String name() {
        return "searchresult";
    }

    public static SearchResultMessageCodec getInstance() {
        return Holder.INSTANCE;
    }

    private SearchResultMessageCodec() {
    }

    private static final class Holder {
        static final SearchResultMessageCodec INSTANCE = new SearchResultMessageCodec();
    }

}
