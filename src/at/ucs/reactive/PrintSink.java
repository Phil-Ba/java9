package at.ucs.reactive;

import org.pmw.tinylog.Logger;

import java.util.concurrent.Flow;

/**
 *
 */
public class PrintSink implements Flow.Subscriber<Integer> {

    private Flow.Subscription subscription;
    boolean finished = false;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        Logger.info("PrintSink Subscribed");
        this.subscription = subscription;
        subscription.request(Main.BATCH_SIZE);
    }

    @Override
    public void onNext(Integer item) {
        Logger.trace(item);
        subscription.request(Main.BATCH_SIZE);
    }

    @Override
    public void onError(Throwable throwable) {
        Logger.error(throwable, "Awkward. This should not happen:");
    }

    @Override
    public void onComplete() {
        finished = true;
        Logger.info("PrintSink is finished");
    }
}