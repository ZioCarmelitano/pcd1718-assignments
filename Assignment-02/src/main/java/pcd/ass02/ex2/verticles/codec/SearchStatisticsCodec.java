package pcd.ass02.ex2.verticles.codec;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import pcd.ass02.domain.SearchStatistics;

import java.util.List;

public class SearchStatisticsCodec extends AbstractMessageCodec<SearchStatistics, SearchStatistics> {

    @Override
    protected void encodeToWire(JsonObject json, SearchStatistics statistics) {
        json.put("documentNames", new JsonArray(statistics.getDocumentNames()));
        json.put("matchingRate", statistics.getMatchingRate());
        json.put("averageMatches", statistics.getAverageMatches());
    }

    @Override
    protected SearchStatistics decodeFromWire(JsonObject json) {
        final String documentNamesStr = json.getJsonArray("documentNames").encode();
        final TypeReference<List<String>> ref = new TypeReference<List<String>>() {
        };

        final List<String> documentNames = Json.decodeValue(documentNamesStr, ref);
        final double matchingRate = json.getDouble("matchingRate");
        final double averageMatches = json.getDouble("averageMatches");

        return new SearchStatistics(documentNames, matchingRate, averageMatches);
    }

    @Override
    public SearchStatistics transform(SearchStatistics statistics) {
        return statistics;
    }

    public static SearchStatisticsCodec getInstance() {
        return Holder.INSTANCE;
    }

    private SearchStatisticsCodec() {
    }

    private static final class Holder {
        static final SearchStatisticsCodec INSTANCE = new SearchStatisticsCodec();
    }

}
