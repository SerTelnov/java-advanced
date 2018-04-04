package ru.ifmo.rain.telnov.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Telnov Sergey on 03.04.2018.
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final Queue<Runnable> runnables = new ArrayDeque<>();
    private final Thread[] workers;

    public ParallelMapperImpl(final int nThread) {
        if (nThread <= 0) {
            throw new RuntimeException("number of threads must be move then zero!");
        }

        workers = new Thread[nThread];
        for (int i = 0; i != nThread; i++) {
            workers[i] = new Thread(new Worker());
            workers[i].start();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        final List<Stream<? extends T>> blocks = MapperIterativeParallelism
                .getBlocks(args, Math.min(workers.length, args.size()));
        final Future<R> future = new Future<>(blocks.size());

        for (int i = 0; i != blocks.size(); i++) {
            final int index = i;
            Stream<? extends T> currBlock = blocks.get(index);
            addRunnable(() -> {
                future.set(index, currBlock.map(f).collect(Collectors.toList()));
            });
        }
        return future.get();
    }

    private void addRunnable(Runnable runnable) {
        synchronized (runnables) {
            runnables.add(runnable);
            runnables.notify();
        }
    }

    @Override
    public void close() {
        for (Thread worker : workers) {
            worker.interrupt();
        }
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private class Future<R> {
        private final List<List<R>> values;
        private int counter = 0;

        public Future(final int size) {
            values = new ArrayList<>(Collections.nCopies(size, null));
        }

        public void set(final int index, List<R> value) {
            values.set(index, value);

            synchronized (this) {
                counter++;
                if (counter == values.size()) {
                    notify();
                }
            }
        }

        public synchronized List<R> get() throws InterruptedException {
            while (counter != values.size()) {
                wait();
            }

            return values
                    .stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }
    }

    private class Worker implements Runnable {

        private Runnable getRunnable() throws InterruptedException {
            Runnable runnable;
            synchronized (runnables) {
                while (runnables.isEmpty()) {
                    runnables.wait();
                }
                runnable = runnables.poll();
                runnables.notify();
            }
            return runnable;
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    getRunnable().run();
                }
            } catch (InterruptedException ignored) {
            } finally {
                Thread.currentThread().interrupt();
            }
        }
    }
}
