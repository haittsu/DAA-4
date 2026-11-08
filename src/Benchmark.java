import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;
final class Benchmark {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final long SEED = 42;
    private static final int PATTERN_LENGTH = 32;
    private static final int WARMUP_RUNS = 25;
    private static final int TIMED_RUNS = 5;

    // Consume every returned result outside its timed region.
    private static volatile Object resultSink;

    record Measurement(String workload, String algorithm, int size, double meanMs,
                       int result, Long preprocessingComparisons, Long searchComparisons) {}

    private Benchmark() {}

    static void run() throws IOException {
        Random random = new Random(SEED);
        List<Measurement> rows = new ArrayList<>();

        for (int size : new int[]{100, 500, 1_000}) {
            String first = randomString(random, size);
            String second = randomString(random, size);
            rows.add(measure("DP", "LCS", size, () -> Lcs.length(first, second)));
            rows.add(measure("DP", "Edit distance", size,
                    () -> EditDistance.distance(first, second)));
        }

        for (int size : new int[]{10_000, 50_000, 100_000}) {
            String text = randomString(random, size);
            int start = size / 2;
            String present = text.substring(start, start + PATTERN_LENGTH);
            String candidate;
            do {
                candidate = randomString(random, PATTERN_LENGTH);
            } while (!NaiveMatcher.search(text, candidate).indices().isEmpty());

            matching(rows, "Present", text, present);
            matching(rows, "Absent", text, candidate);
        }

        for (int size : new int[]{1_000, 5_000, 10_000}) {
            String text = randomString(random, size);
            int[] suffixes = suffixArray(text);
            rows.add(measure("LCP", "Kasai", size, () -> KasaiLcp.build(text, suffixes)));
        }

        StringBuilder csv = new StringBuilder(
                "workload,algorithm,size,mean_ms,result,preprocessing_comparisons,search_comparisons\n");
        for (Measurement row : rows) {
            csv.append(String.format(Locale.ROOT, "%s,%s,%d,%.6f,%d,%s,%s%n",
                    row.workload(), row.algorithm(), row.size(), row.meanMs(), row.result(),
                    row.preprocessingComparisons() == null ? "" : row.preprocessingComparisons(),
                    row.searchComparisons() == null ? "" : row.searchComparisons()));
        }

        Path directory = Files.createDirectories(Path.of("out", "benchmark"));
        Files.writeString(directory.resolve("measurements.csv"), csv);
        System.out.print(csv);
        System.out.println("Saved measurements in " + directory.toAbsolutePath());
    }

    private static void matching(List<Measurement> rows, String workload, String text, String pattern) {
        List<Integer> expected = NaiveMatcher.search(text, pattern).indices();
        boolean found = !expected.isEmpty();
        if (found != workload.equals("Present")
                || !expected.equals(KmpMatcher.search(text, pattern).indices())
                || !expected.equals(RabinKarpMatcher.search(text, pattern).indices())) {
            throw new AssertionError("Incorrect benchmark inputs or inconsistent match lists");
        }
        rows.add(measure(workload, "Naive", text.length(), () -> NaiveMatcher.search(text, pattern)));
        rows.add(measure(workload, "KMP", text.length(), () -> KmpMatcher.search(text, pattern)));
        rows.add(measure(workload, "Rabin-Karp", text.length(),
                () -> RabinKarpMatcher.search(text, pattern)));
    }

    private static Measurement measure(String workload, String algorithm, int size, Supplier<?> operation) {
        for (int run = 0; run < WARMUP_RUNS; run++) {
            resultSink = operation.get();
        }

        long elapsed = 0;
        Object result = null;
        for (int run = 0; run < TIMED_RUNS; run++) {
            long start = System.nanoTime();
            result = operation.get();
            elapsed += System.nanoTime() - start;
            resultSink = result;
        }
        double meanMs = elapsed / (TIMED_RUNS * 1_000_000.0);
        if (result instanceof MatchResult matches) {
            return new Measurement(workload, algorithm, size, meanMs, matches.indices().size(),
                    matches.preprocessingComparisons(), matches.searchComparisons());
        }
        int value = result instanceof int[] lcp ? Arrays.stream(lcp).max().orElse(0) : (Integer) result;
        return new Measurement(workload, algorithm, size, meanMs, value, null, null);
    }

    private static String randomString(Random random, int size) {
        StringBuilder result = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            result.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return result.toString();
    }

    // This input helper is outside Kasai's timed region.
    static int[] suffixArray(String text) {
        Integer[] starts = new Integer[text.length()];
        for (int i = 0; i < starts.length; i++) {
            starts[i] = i;
        }
        Arrays.sort(starts, (a, b) -> {
            int i = a;
            int j = b;
            while (i < text.length() && j < text.length()) {
                int comparison = Character.compare(text.charAt(i++), text.charAt(j++));
                if (comparison != 0) {
                    return comparison;
                }
            }
            return Integer.compare(j, i);
        });
        return Arrays.stream(starts).mapToInt(Integer::intValue).toArray();
    }
}