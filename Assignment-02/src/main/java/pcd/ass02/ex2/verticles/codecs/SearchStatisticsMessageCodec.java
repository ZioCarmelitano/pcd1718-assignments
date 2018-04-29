package pcd.ass02.ex2.verticles.codecs;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import pcd.ass02.domain.SearchStatistics;

import java.util.List;

public class SearchStatisticsMessageCodec extends AbstractMessageCodec<SearchStatistics, SearchStatistics> {

    @Override
    protected void encodeToWire(JsonObject jsonObject, SearchStatistics statistics) {
        jsonObject.put("documentNames", new JsonArray(statistics.getDocumentNames()));
        jsonObject.put("matchingRate", statistics.getMatchingRate());
        jsonObject.put("averageMatches", statistics.getAverageMatches());
    }

    @Override
    protected SearchStatistics decodeFromWire(JsonObject jsonObject) {
        final String documentNamesStr = jsonObject.getJsonArray("documentNames").encode();
        final TypeReference<List<String>> ref = new TypeReference<List<String>>() {
        };

        final List<String> documentNames = Json.decodeValue(documentNamesStr, ref);
        final double matchingRate = jsonObject.getDouble("matchingRate");
        final double averageMatches = jsonObject.getDouble("averageMatches");

        return new SearchStatistics(documentNames, matchingRate, averageMatches);
    }

    @Override
    public SearchStatistics transform(SearchStatistics statistics) {
        return statistics;
    }

    @Override
    public String name() {
        return "searchstatistics";
    }

    public static SearchStatisticsMessageCodec getInstance() {
        return Holder.INSTANCE;
    }

    private SearchStatisticsMessageCodec() {
    }

    private static final class Holder {
        static final SearchStatisticsMessageCodec INSTANCE = new SearchStatisticsMessageCodec();
    }

}
