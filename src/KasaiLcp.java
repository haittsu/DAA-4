final class KasaiLcp {
    private KasaiLcp() {}

    // suffixArray lists all starting offsets in lexicographic suffix order.
    // Entry zero is 0. Every subsequent entry compares the suffix to its predecessor.
    static int[] build(String text, int[] suffixArray) {
        int n = text.length();
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[suffixArray[i]] = i;
        }
        int[] lcp = new int[n];
        for (int start = 0, common = 0; start < n; start++) {
            int position = rank[start];
            if (position == 0) {
                common = 0;
                continue;
            }
            int previous = suffixArray[position - 1];
            while (start + common < n && previous + common < n
                    && text.charAt(start + common) == text.charAt(previous + common)) {
                common++;
            }
            lcp[position] = common;
            if (common > 0) {
                common--;
            }
        }
        return lcp;
    }
}