package at.ucs.reactive;

import org.awaitility.Awaitility;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static at.ucs.reactive.Main.INTS_PER_LINE;
import static at.ucs.reactive.Main.LINES_IN_FILE;
import static org.awaitility.Duration.TEN_SECONDS;

/**
 *
 */
public class ReactiveRunner {

    private static Random random = new Random();

    public static void run() throws IOException {
        Configurator.defaultConfig()
                .formatPattern("{date:yyyy-MM-dd HH:mm:ss} [{thread}] {class_name} {level}: {message}")
                .level(ReactiveRunner.class, Level.INFO)
                .level(PrintSink.class, Level.DEBUG)
                .writingThread(true)
                .activate();
        Path pathToFile = Paths.get("ints.txt");

        createFileIfMissing(pathToFile);

//        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()/2);
//        ExecutorService executor2 = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()/2);
//
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ExecutorService executor2 = executor;

//        ExecutorService executor = Executors.newWorkStealingPool();
//        ExecutorService executor2 = executor;

//        ExecutorService executor = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors()/2);
//        ExecutorService executor2 = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors()/2);

//        SubmissionPublisher<String> source = new SubmissionPublisher<>(executor, 10);
        SubmissionPublisher<String> source = new SubmissionPublisher<>(executor, Flow.defaultBufferSize());
        SumIntsFlow sumIntsFlow = new SumIntsFlow(executor2);
        PrintSink sink = new PrintSink();
        try (source; sumIntsFlow) {
            source.subscribe(sumIntsFlow);
            sumIntsFlow.subscribe(sink);


            List<String> lines = new Scanner(pathToFile)
                    .useDelimiter(System.lineSeparator())
                    .tokens()
                    .collect(Collectors.toList());

            Instant starts = Instant.now();
            lines.stream()
//                    .parallel()
                    .forEach(line -> {
                        Logger.trace("Submitting line");
                        source.submit(line);
                    });

            Logger.info("Submission is finished! Time: {}", Duration.between(starts, Instant.now()));
            source.close();

            Awaitility.waitAtMost(TEN_SECONDS.plus(10))
                    .until(() -> sink.finished);
//                    .until(() -> sink.ai.get() == LINES_IN_FILE);
            Instant ends = Instant.now();
            Logger.info("Total time: {}", Duration.between(starts, ends));
        } catch (Exception e) {
            Logger.error(e, "Error in main: ");
        }
        executor.shutdown();
        executor2.shutdown();
    }

    private static void createFileIfMissing(Path pathToFile) throws IOException {
        if (Files.exists(pathToFile)) {
            Logger.warn("Input file exists -> skipping creation!");
            return;
        }

        BufferedWriter writer = Files.newBufferedWriter(pathToFile, StandardOpenOption.CREATE);
        try (writer) {
            IntStream.range(0, LINES_IN_FILE)
                    .mapToObj(lineNumber -> createLineNumberIntStream(INTS_PER_LINE, lineNumber)
                            .map(Object::toString)
                            .collect(Collectors.joining(";")))
                    .forEach(line -> {
                        try {
                            writer.write(line);
                            writer.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    private static Stream<Integer> createRandomIntStream(long size) {
        return Stream.generate(() -> random.nextInt(1000)).limit(size);
    }

    private static Stream<Integer> createLineNumberIntStream(long size, int lineNumber) {
        return Stream.iterate(lineNumber, t -> 0).limit(size);
    }

}
