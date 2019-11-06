package xyz.acrylicstyle.doubletimecommandsbungee.utils;

import util.CollectionList;

import java.util.function.Consumer;

public class Scheduler<T> {
    private CollectionList<Consumer<T>> consumers = new CollectionList<>();
    private CollectionList<T> values = new CollectionList<>();
    private CollectionList<Integer> delays = new CollectionList<>();

    public Scheduler<T> schedule(Consumer<T> consumer, T arg, Integer minimumDelay) {
        Validate.notNull(consumer, minimumDelay);
        delays.add(minimumDelay);
        values.add(arg);
        consumers.add(consumer);
        if (consumers.size() == 1) new Poller().start();
        return this;
    }

    private class Poller extends Thread {
        public void run() {
            try {
                sleep(delays.first());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            consumers.first().accept(values.first());
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            values.remove(0);
            delays.remove(0);
            consumers.remove(0);
            if (consumers.size() > 0) new Poller().start();
        }
    }
}
