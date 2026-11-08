import java.io.IOException;

public final class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 1 && args[0].equals("--test")) {
            new AlgorithmTests().run();
        } else if (args.length == 0 || (args.length == 1 && args[0].equals("--benchmark"))) {
            new AlgorithmTests().run();
            Benchmark.run();
        } else {
            throw new IllegalArgumentException("Usage: Main [--test | --benchmark]");
        }
    }
}