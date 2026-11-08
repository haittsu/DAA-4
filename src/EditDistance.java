final class EditDistance {
    private EditDistance() {}

    static int distance(String first, String second) {
        int[][] dp = new int[first.length() + 1][second.length() + 1];
        for (int i = 0; i <= first.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= second.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= first.length(); i++) {
            for (int j = 1; j <= second.length(); j++) {
                int substitution = first.charAt(i - 1) == second.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(dp[i - 1][j - 1] + substitution,
                        1 + Math.min(dp[i - 1][j], dp[i][j - 1]));
            }
        }
        return dp[first.length()][second.length()];
    }
}