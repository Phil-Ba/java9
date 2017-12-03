package at.ucs.reactive;

import org.pmw.tinylog.Logger;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

/**
 *
 */
public class SumIntsFlow
        extends SubmissionPublisher<Integer>
        implements Flow.Processor<String, Integer> {

    private Flow.Subscription subscription;
    boolean finished = false;

    public SumIntsFlow(ExecutorService executor) {
        super(executor, Flow.defaultBufferSize());
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        Logger.info("SumIntsFlow Subscribed");
        this.subscription = subscription;
        subscription.request(Main.BATCH_SIZE);
    }

    @Override
    public void onNext(String item) {
        int sum = new Scanner(item)
                .useDelimiter(";")
                .tokens()
                .parallel()
                .mapToInt(Integer::valueOf)
                .sum();

        submit(sum);
        subscription.request(Main.BATCH_SIZE);
    }


    @Override
    public void onError(Throwable throwable) {
        Logger.error(throwable, "Awkward. This should not happen:");
    }

    @Override
    public void onComplete() {
        finished = true;
        close();
        Logger.info("SumIntsFlow is finished");
    }
}