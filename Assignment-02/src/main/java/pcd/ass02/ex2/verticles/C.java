package pcd.ass02.ex2.verticles;

final class C {

    static final String accumulator = "accumulator";

    static final String folderSearch = "folderSearch";

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

    private C() {
    }

}
