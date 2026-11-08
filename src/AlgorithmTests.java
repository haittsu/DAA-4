import java.util.Arrays;
import java.util.List;

final class AlgorithmTests {
    private int checks;

    void run() {
        expect("LCS identical", 4, Lcs.length("same", "same"));
        expect("LCS disjoint", 0, Lcs.length("abc", "xyz"));
        expect("LCS empty", 0, Lcs.length("", "abc"));
        expect("LCS noncontiguous", 4, Lcs.length("ABCBDAB", "BDCABA"));

        expect("Edit identical", 0, EditDistance.distance("same", "same"));
        expect("Edit empty source", 3, EditDistance.distance("", "abc"));
        expect("Edit empty target", 3, EditDistance.distance("abc", ""));
        expect("Edit substitution", 1, EditDistance.distance("cat", "cut"));
        expect("Edit insertion", 1, EditDistance.distance("cat", "cart"));
        expect("Edit deletion", 1, EditDistance.distance("cart", "cat"));
        expect("Edit all three", 3, EditDistance.distance("abcdef", "bcdexg"));

        match("beginning", "abczzz", "abc", List.of(0));
        match("middle", "zzabczz", "abc", List.of(2));
        match("end", "zzzabc", "abc", List.of(3));
        match("multiple", "abcxabcabc", "abc", List.of(0, 4, 7));
        match("overlapping", "aaaaa", "aaa", List.of(0, 1, 2));
        match("absent", "ababa", "abb", List.of());
        match("longer pattern", "ab", "abc", List.of());
        match("empty pattern", "abc", "", List.of(0, 1, 2, 3));
        match("empty text and pattern", "", "", List.of(0));
        // With base 257, 'A' * 257 + 257 equals 'B' * 257 + 0.
        match("hash collision", "B\0A\u0101", "A\u0101", List.of(2));

        int[] bananaSuffixes = {5, 3, 1, 0, 4, 2};
        array("banana suffix order", bananaSuffixes, Benchmark.suffixArray("banana"));
        array("banana LCP", new int[]{0, 1, 3, 0, 0, 2},
                KasaiLcp.build("banana", bananaSuffixes));
        array("repeated characters", new int[]{0, 1, 2, 3},
                KasaiLcp.build("aaaa", new int[]{3, 2, 1, 0}));
        array("empty LCP", new int[]{}, KasaiLcp.build("", new int[]{}));
        array("single-character LCP", new int[]{0}, KasaiLcp.build("a", new int[]{0}));

        System.out.println("PASS: " + checks + " assertions");
    }

    private void match(String label, String text, String pattern, List<Integer> expected) {
        List<Integer> naive = NaiveMatcher.search(text, pattern).indices();
        expect(label + " / Naive", expected, naive);
        expect(label + " / KMP", naive, KmpMatcher.search(text, pattern).indices());
        expect(label + " / Rabin-Karp", naive, RabinKarpMatcher.search(text, pattern).indices());
    }

    private void array(String label, int[] expected, int[] actual) {
        expect(label, Arrays.toString(expected), Arrays.toString(actual));
    }

    private void expect(String label, Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
        checks++;
    }
}