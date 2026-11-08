import java.util.List;

record MatchResult(List<Integer> indices, long preprocessingComparisons, long searchComparisons) {}