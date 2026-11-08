import java.util.ArrayList;
import java.util.List;

final class KmpMatcher {
    private KmpMatcher() {}

    static MatchResult search(String text, String pattern) {
        int length = pattern.length();
        if (length == 0 || length > text.length()) {
            return NaiveMatcher.search(text, pattern);
        }

        int[] lps = new int[length];
        long preprocessing = 0;
        for (int i = 1, prefix = 0; i < length; ) {
            preprocessing++;
            if (pattern.charAt(i) == pattern.charAt(prefix)) {
                lps[i++] = ++prefix;
            } else if (prefix > 0) {
                prefix = lps[prefix - 1];
            } else {
                i++;
            }
        }

        List<Integer> indices = new ArrayList<>();
        long comparisons = 0;
        for (int i = 0, prefix = 0; i < text.length(); ) {
            comparisons++;
            if (text.charAt(i) == pattern.charAt(prefix)) {
                i++;
                prefix++;
                if (prefix == length) {
                    indices.add(i - length);
                    prefix = lps[prefix - 1];
                }
            } else if (prefix > 0) {
                prefix = lps[prefix - 1];
            } else {
                i++;
            }
        }
        return new MatchResult(indices, preprocessing, comparisons);
    }
}