package pcd.ass02.ex2.verticles;

final class Channels {

    static final String accumulator = "accumulator";

    static final class coordinator {
        static final String documentAnalyzed = "coordinator.documentAnalyzed";

        static final String documentCount = "coordinator.documentCount";

        static final String done = "coordinator.done";

        private coordinator() {
        }
    }

    static final class documentSearch {
        static final String analyze = "documentSearch.analyze";

        static final String regex = "documentSearch.regex";

        private documentSearch() {
        }
    }

    static final String folderSearch = "folderSearch";

    private Channels() {
    }

}
