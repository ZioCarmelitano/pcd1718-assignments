package pcd.ass02.ex2.verticles.codecs;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import pcd.ass02.domain.SearchStatistics;

import java.util.Map;

public class SearchStatisticsMessageCodec extends AbstractMessageCodec<SearchStatistics, SearchStatistics> {

    private static final TypeReference<Map<String, Long>> REFERENCE = new TypeReference<Map<String, Long>>() {};

    @Override
    protected void encodeToWire(JsonObject jsonObject, SearchStatistics statistics) {
        jsonObject.put("documentResults", new JsonObject((Buffer) statistics.getDocumentResults()));
        jsonObject.put("matchingRate", statistics.getMatchingRate());
        jsonObject.put("averageMatches", statistics.getAverageMatches());
    }

    @Override
    protected SearchStatistics decodeFromWire(JsonObject jsonObject) {
        final String documentNamesStr = jsonObject.getJsonArray("documentResults").encode();

        final Map<String, Long> documentNames = Json.decodeValue(documentNamesStr, REFERENCE);
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
