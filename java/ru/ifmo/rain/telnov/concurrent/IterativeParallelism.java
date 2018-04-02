package ru.ifmo.rain.telnov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Telnov Sergey on 25.03.2018.
 */
public class IterativeParallelism implements ListIP {

    private <T, R> R customMethod(int threads, final List<? extends T> list,
                                  final Function<Stream<? extends T>, ? extends R> func,
                                  final Function<Stream<? extends R>, ? extends R> rFunc)
            throws InterruptedException {
        final int threadsCount = Math.min(threads, list.size());

        final List<R> values = new ArrayList<>(Collections.nCopies(threadsCount, null));
        final List<Thread> workers = new ArrayList<>(Collections.nCopies(threadsCount, null));

        final int count = list.size() / threadsCount;
        int rest = list.size() % threadsCount;
        int prevRight = 0;
        for (int i = 0; i != threadsCount; i++) {
            final int index = i;
            final int left = prevRight;
            final int right = prevRight + count + (rest-- > 0 ? 1 : 0);
            prevRight = right;

            workers.set(i, new Thread(() -> values.set(index, func.apply(list.subList(left, right).stream()))));
            workers.get(i).start();
        }
        for (Thread worker: workers) {
            worker.join();
        }
        return rFunc.apply(values.stream());
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        final Function<Stream<? extends T>, T> func = stream -> stream.max(comparator).orElse(null);
        return customMethod(threads, values, func, func);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return customMethod(threads,
                values,
                stream -> stream.allMatch(predicate),
                stream -> stream.allMatch(Boolean::booleanValue));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, it -> !predicate.test(it));
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return customMethod(threads, values,
                stream -> stream.map(Objects::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining())
        );
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return customMethod(threads, values,
                stream -> stream.filter(predicate).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList())
        );
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return customMethod(threads, values,
                stream -> stream.map(f).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }
}
