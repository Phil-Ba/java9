package at.ucs.reactive;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Warmup;

import java.io.IOException;


/**
 *
 */
public class Main {

    public static final int LINES_IN_FILE = 1_000;
    public static final int INTS_PER_LINE = 100_000;
    public static final int BATCH_SIZE = 1;


    @org.openjdk.jmh.annotations.Benchmark()
    @Warmup(iterations = 5)
    @org.openjdk.jmh.annotations.Measurement(iterations = 10)
    @org.openjdk.jmh.annotations.Fork(1)
    @org.openjdk.jmh.annotations.BenchmarkMode(Mode.AverageTime)
    public void measureName() throws IOException {
        ReactiveRunner.run();
    }

    public static void main(String[] args) throws IOException {
        ReactiveRunner.run();
    }

}