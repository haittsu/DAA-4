import java.util.ArrayList;
import java.util.List;

final class RabinKarpMatcher {
    private RabinKarpMatcher() {}

    private static final long BASE = 257;
    private static final long MODULUS = 1_000_000_007;

    static MatchResult search(String text, String pattern) {
        int length = pattern.length();
        int lastStart = text.length() - length;
        if (length == 0 || lastStart < 0) {
            return NaiveMatcher.search(text, pattern);
        }

        long patternHash = 0;
        long windowHash = 0;
        long leadingPower = 1;
        for (int j = 0; j < length; j++) {
            patternHash = (patternHash * BASE + pattern.charAt(j)) % MODULUS;
            windowHash = (windowHash * BASE + text.charAt(j)) % MODULUS;
            if (j < length - 1) {
                leadingPower = leadingPower * BASE % MODULUS;
            }
        }

        List<Integer> indices = new ArrayList<>();
        long comparisons = 0;
        for (int start = 0; start <= lastStart; start++) {
            if (windowHash == patternHash) {
                int j = 0;
                while (j < length) {
                    comparisons++;
                    if (text.charAt(start + j) != pattern.charAt(j)) {
                        break;
                    }
                    j++;
                }
                if (j == length) {
                    indices.add(start);
                }
            }
            if (start < lastStart) {
                long outgoing = text.charAt(start) * leadingPower % MODULUS;
                long remaining = (windowHash - outgoing + MODULUS) % MODULUS;
                windowHash = (remaining * BASE + text.charAt(start + length)) % MODULUS;
            }
        }
        return new MatchResult(indices, 0, comparisons);
    }
}