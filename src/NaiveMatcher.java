import java.util.ArrayList;
import java.util.List;

final class NaiveMatcher {
    private NaiveMatcher() {}

    static MatchResult search(String text, String pattern) {
        List<Integer> indices = new ArrayList<>();
        long comparisons = 0;
        int length = pattern.length();
        for (int start = 0; start <= text.length() - length; start++) {
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
        return new MatchResult(indices, 0, comparisons);
    }
}