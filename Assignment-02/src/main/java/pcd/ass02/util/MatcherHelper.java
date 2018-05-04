package pcd.ass02.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class MatcherHelper {

    public static int countMatches(final String regex, final String input, final boolean disjoint) {
        final Pattern pattern = Pattern.compile(regex);
        return countMatches(pattern, input, disjoint);
    }

    private static int countMatches(final Pattern pattern, final String input, final boolean disjoint) {
        final Matcher matcher = pattern.matcher(input);
        return countMatches(matcher, disjoint);
    }

    private static int countMatches(final Matcher matcher, final boolean disjoint) {
        return disjoint ? countMatchesDisjoint(matcher) : countMatches(matcher);
    }

    public static int countMatches(final String regex, final String input) {
        final Pattern pattern = Pattern.compile(regex);
        return countMatches(pattern, input);
    }

    private static int countMatches(final Pattern pattern, final String input) {
        final Matcher matcher = pattern.matcher(input);
        return countMatches(matcher);
    }

    private static int countMatches(final Matcher matcher) {
        int count = 0;
        while (matcher.find())
            count++;
        return count;
    }

    public static int countMatchesDisjoint(final String regex, final String input) {
        final Pattern pattern = Pattern.compile(regex);
        return countMatchesDisjoint(pattern, input);
    }

    private static int countMatchesDisjoint(final Pattern pattern, final String input) {
        final Matcher matcher = pattern.matcher(input);
        return countMatchesDisjoint(matcher);
    }

    private static int countMatchesDisjoint(final Matcher matcher) {
        int count = 0;
        for (int from = 0; matcher.find(from); from = matcher.start() + 1)
            count++;
        return count;
    }

    private MatcherHelper() {
    }

}